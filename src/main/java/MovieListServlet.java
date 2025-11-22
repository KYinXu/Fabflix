import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import config.MongoDBConnectionConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "MovieListServlet", urlPatterns = {"/", "/movies"})
public class MovieListServlet extends HttpServlet {

    private MongoDBConnectionConfig mongoConfig;
    private static final int DEFAULT_MOVIES_PER_PAGE = 25;
    private static final int[] ALLOWED_PAGE_SIZES = {10, 25, 50, 100};

    @Override
    public void init() {
        mongoConfig = new MongoDBConnectionConfig();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter frontendOutput = response.getWriter();

        try {
            if (mongoConfig == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB configuration not initialized");
                return;
            }

            String action = request.getParameter("action");
            if ("listGenres".equals(action)) {
                handleGenreList(frontendOutput);
                frontendOutput.close();
                return;
            }

            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> moviesCollection = database.getCollection("movies");

            JSONArray movies = new JSONArray();

            String genreIdParam = request.getParameter("genreId");
            if (genreIdParam != null && !genreIdParam.trim().isEmpty()) {
                handleGenreFilter(movies, moviesCollection, genreIdParam, request);
            } else {
                handleMovieList(movies, moviesCollection, request);
            }

            frontendOutput.write(movies.toString());
            frontendOutput.flush();
        } catch (com.mongodb.MongoTimeoutException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB connection timeout. Is MongoDB running?");
        } catch (com.mongodb.MongoException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB error: " + e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        } finally {
            frontendOutput.close();
        }
    }

    private void handleGenreList(PrintWriter frontendOutput) {
        try {
            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> genresCollection = database.getCollection("genres");

            JSONArray genres = new JSONArray();
            for (Document genreDoc : genresCollection.find().sort(Sorts.ascending("name"))) {
                JSONObject genre = new JSONObject();
                genre.put("id", genreDoc.getInteger("_id"));
                genre.put("name", genreDoc.getString("name"));
                genres.put(genre);
            }

            frontendOutput.write(genres.toString());
            frontendOutput.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error getting genre list", e);
        }
    }

    private void handleGenreFilter(JSONArray movies, MongoCollection<Document> collection, String genreIdParam, HttpServletRequest request) {
        try {
            int genreId = Integer.parseInt(genreIdParam);
            String pageParam = request.getParameter("page");
            int page = (pageParam != null && !pageParam.isEmpty()) ? Integer.parseInt(pageParam) : 0;
            int pageSize = parsePageSize(request.getParameter("pageSize"));
            int offset = page * pageSize;

            Bson filter = Filters.eq("genres.id", genreId);
            Bson sort = Sorts.descending("rating.score");
            Bson projection = Projections.fields(
                Projections.include("_id", "title", "year", "director", "rating", "stars", "genres")
            );

            List<Document> movieDocs = collection.find(filter)
                .projection(projection)
                .sort(sort)
                .skip(offset)
                .limit(pageSize)
                .into(new ArrayList<>());

            populateMoviesFromDocs(movies, movieDocs);
        } catch (NumberFormatException e) {
            // Invalid genre ID, return empty result
        }
    }

    private void handleMovieList(JSONArray movies, MongoCollection<Document> collection, HttpServletRequest request) {
        String titlePattern = createSearchPattern(request.getParameter("title"));
        String starPattern = createSearchPattern(request.getParameter("star"));
        String directorPattern = createSearchPattern(request.getParameter("director"));
        int year = createYearFilter(request.getParameter("year"));

        String letterParam = request.getParameter("letter");
        if (letterParam != null && !letterParam.equals("All") && letterParam.matches("^[A-Z0-9]$")) {
            titlePattern = letterParam + "%";
        }

        String pageParam = request.getParameter("page");
        int page = (pageParam != null && !pageParam.isEmpty()) ? Integer.parseInt(pageParam) : 0;
        int pageSize = parsePageSize(request.getParameter("pageSize"));
        int offset = page * pageSize;

        String sortCriteriaParam = request.getParameter("sortCriteria");
        String sortOrderParam = request.getParameter("sortOrder");
        String tieBreakerParam = request.getParameter("tieBreaker");

        String sortCriteria = (sortCriteriaParam != null && !sortCriteriaParam.isEmpty()) ? sortCriteriaParam : "rating.score";
        String sortOrder = (sortOrderParam != null && !sortOrderParam.isEmpty()) ? sortOrderParam : "DESC";

        Bson filter = buildFilter(titlePattern, starPattern, directorPattern, year);
        Bson sort = buildSort(sortCriteria, sortOrder, tieBreakerParam);
        Bson projection = Projections.fields(
            Projections.include("_id", "title", "year", "director", "rating", "stars", "genres")
        );

        List<Document> movieDocs = collection.find(filter)
            .projection(projection)
            .sort(sort)
            .skip(offset)
            .limit(pageSize)
            .into(new ArrayList<>());

        populateMoviesFromDocs(movies, movieDocs);
    }

    private Bson buildFilter(String titlePattern, String starPattern, String directorPattern, int year) {
        List<Bson> filters = new ArrayList<>();

        if (titlePattern != null && !titlePattern.equals("%")) {
            Pattern titleRegex = Pattern.compile(escapeRegex(titlePattern).replace("%", ".*"), Pattern.CASE_INSENSITIVE);
            filters.add(Filters.regex("title", titleRegex));
        }

        if (starPattern != null && !starPattern.equals("%")) {
            Pattern starRegex = Pattern.compile(escapeRegex(starPattern).replace("%", ".*"), Pattern.CASE_INSENSITIVE);
            filters.add(Filters.elemMatch("stars", Filters.regex("name", starRegex)));
        }

        if (directorPattern != null && !directorPattern.equals("%")) {
            Pattern directorRegex = Pattern.compile(escapeRegex(directorPattern).replace("%", ".*"), Pattern.CASE_INSENSITIVE);
            filters.add(Filters.regex("director", directorRegex));
        }

        if (year != -1) {
            filters.add(Filters.eq("year", year));
        }

        return filters.isEmpty() ? new Document() : Filters.and(filters);
    }

    private Bson buildSort(String sortCriteria, String sortOrder, String tieBreaker) {
        String validatedOrder = sortOrder.toUpperCase().equals("ASC") ? "ASC" : "DESC";

        String mongoField = switch (sortCriteria) {
            case "r.ratings", "rating.score" -> "rating.score";
            case "m.title", "title" -> "title";
            case "m.year", "year" -> "year";
            case "m.director", "director" -> "director";
            default -> "rating.score";
        };

        List<Bson> sortList = new ArrayList<>();
        if (validatedOrder.equals("ASC")) {
            sortList.add(Sorts.ascending(mongoField));
        } else {
            sortList.add(Sorts.descending(mongoField));
        }

        if (tieBreaker != null && !tieBreaker.isEmpty()) {
            String tieBreakerField = switch (tieBreaker) {
                case "title" -> "title";
                case "ratings" -> "rating.score";
                default -> "";
            };
            if (!tieBreakerField.isEmpty() && !tieBreakerField.equals(mongoField)) {
                if (validatedOrder.equals("ASC")) {
                    sortList.add(Sorts.ascending(tieBreakerField));
                } else {
                    sortList.add(Sorts.descending(tieBreakerField));
                }
            }
        }

        return sortList.size() == 1 ? sortList.get(0) : Sorts.orderBy(sortList);
    }

    private void populateMoviesFromDocs(JSONArray movies, List<Document> movieDocs) {
        for (Document movieDoc : movieDocs) {
            JSONObject movie = new JSONObject();
            
            // Handle _id field - it could be String or ObjectId
            Object idObj = movieDoc.get("_id");
            String movieId = null;
            if (idObj != null) {
                movieId = idObj.toString();
            }
            
            if (movieId == null) {
                // Skip movies without ID (shouldn't happen, but safety check)
                continue;
            }
            
            movie.put("id", movieId);
            movie.put("title", movieDoc.getString("title"));
            movie.put("year", movieDoc.getInteger("year"));
            movie.put("director", movieDoc.getString("director"));

            Document ratingDoc = movieDoc.get("rating", Document.class);
            if (ratingDoc != null) {
                JSONObject ratings = new JSONObject();
                ratings.put("ratings", ratingDoc.getDouble("score"));
                ratings.put("vote_count", ratingDoc.getInteger("voteCount"));
                movie.put("ratings", ratings);
            } else {
                movie.put("ratings", JSONObject.NULL);
            }

            List<Document> starsList = movieDoc.getList("stars", Document.class);
            if (starsList != null && !starsList.isEmpty()) {
                JSONArray stars = new JSONArray();
                int starCount = 0;
                for (Document starDoc : starsList) {
                    if (starCount >= 3) break;
                    JSONObject star = new JSONObject();
                    star.put("id", starDoc.getString("id"));
                    star.put("name", starDoc.getString("name"));
                    if (starDoc.containsKey("birthYear")) {
                        star.put("birth_year", starDoc.getInteger("birthYear"));
                    }
                    if (starDoc.containsKey("movieCount")) {
                        star.put("movie_count", starDoc.getInteger("movieCount"));
                    }
                    stars.put(star);
                    starCount++;
                }
                movie.put("stars", stars);
            } else {
                movie.put("stars", new JSONArray());
            }

            List<Document> genresList = movieDoc.getList("genres", Document.class);
            if (genresList != null && !genresList.isEmpty()) {
                JSONArray genres = new JSONArray();
                int genreCount = 0;
                for (Document genreDoc : genresList) {
                    if (genreCount >= 3) break;
                    JSONObject genre = new JSONObject();
                    genre.put("id", genreDoc.getInteger("id"));
                    genre.put("name", genreDoc.getString("name"));
                    genres.put(genre);
                    genreCount++;
                }
                movie.put("genres", genres);
            } else {
                movie.put("genres", new JSONArray());
            }

            movies.put(movie);
        }
    }

    private String escapeRegex(String pattern) {
        return pattern.replace("\\", "\\\\")
                     .replace("^", "\\^")
                     .replace("$", "\\$")
                     .replace(".", "\\.")
                     .replace("|", "\\|")
                     .replace("?", "\\?")
                     .replace("*", "\\*")
                     .replace("+", "\\+")
                     .replace("(", "\\(")
                     .replace(")", "\\)")
                     .replace("[", "\\[")
                     .replace("]", "\\]")
                     .replace("{", "\\{")
                     .replace("}", "\\}");
    }

    private int parsePageSize(String pageSizeParam) {
        if (pageSizeParam == null || pageSizeParam.trim().isEmpty()) {
            return DEFAULT_MOVIES_PER_PAGE;
        }
        try {
            int pageSize = Integer.parseInt(pageSizeParam);
            for (int allowed : ALLOWED_PAGE_SIZES) {
                if (pageSize == allowed) {
                    return pageSize;
                }
            }
        } catch (NumberFormatException e) {
            // Invalid format, return default
        }
        return DEFAULT_MOVIES_PER_PAGE;
    }

    private String createSearchPattern(String searchInput) {
        return (searchInput != null && !searchInput.trim().isEmpty())
                ? "%" + searchInput.trim() + "%"
                : "%";
    }

    private int createYearFilter(String yearInput) {
        if (yearInput == null || yearInput.trim().isEmpty()) {
            return -1;
        }
        return Integer.parseInt(yearInput.trim());
    }

    @Override
    public void destroy() {
        if (mongoConfig != null) {
            mongoConfig.closeConnection();
        }
    }
}
