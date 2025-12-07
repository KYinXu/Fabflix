import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "MovieListServlet", urlPatterns = {"/", "/movies"}) // Allows Tomcat to Interpret URL
public class MovieListServlet extends HttpServlet{

    public static final String GET_MOVIE_LIST = """
            SELECT DISTINCT m.id, m.title, m.year, m.director, r.ratings
            FROM movies m
            INNER JOIN ratings r ON m.id = r.movie_id
            INNER JOIN stars_in_movies sm ON m.id = sm.movie_id
            INNER JOIN stars s ON sm.star_id = s.id
            WHERE m.title LIKE ?
            AND (s.name LIKE ? OR ? = '%')
            AND m.director LIKE ?
            AND (m.year = ? OR ? = -1)
            """;
    
    public static final String GET_MOVIE_LIST_BY_GENRE = """
            SELECT DISTINCT m.id, m.title, m.year, m.director, r.ratings
            FROM movies m
            INNER JOIN ratings r ON m.id = r.movie_id
            INNER JOIN genres_in_movies gm ON m.id = gm.movie_id
            WHERE gm.genre_id = ?
            """;
    public static final int TITLE_SEARCH_IDX = 1;
    public static final int STAR_SEARCH_IDX = 2;
    public static final int STAR_SEARCH_CHECK_IDX = 3;
    public static final int DIRECTOR_SEARCH_IDX = 4;
    public static final int YEAR_SEARCH_IDX = 5;
    public static final int YEAR_SEARCH_CHECK_IDX = 6;
    public static final int DISPLAY_LIMIT_IDX = 7;
    public static final int DISPLAY_OFFSET_IDX = 8;
    private static final int DEFAULT_MOVIES_PER_PAGE = 25;
    private static final int[] ALLOWED_PAGE_SIZES = {10, 25, 50, 100};
    
    public static final String GET_ALL_GENRES_QUERY = """
            SELECT DISTINCT id, name
            FROM genres
            ORDER BY name ASC
            """;
    
    // Batched query templates for movie list population
    public static final String BATCH_RATINGS_QUERY_TEMPLATE = """
            SELECT movie_id, ratings, vote_count
            FROM ratings
            WHERE movie_id IN
            """;
    
    public static final String BATCH_STARS_QUERY_TEMPLATE = """
            SELECT sm.movie_id, s.id, s.name, s.birth_year,
                   (SELECT COUNT(*) FROM stars_in_movies sm2 WHERE sm2.star_id = s.id) as movie_count
            FROM stars s
            INNER JOIN stars_in_movies sm ON s.id = sm.star_id
            WHERE sm.movie_id IN
            """;
    
    public static final String BATCH_GENRES_QUERY_TEMPLATE = """
            SELECT gm.movie_id, g.id, g.name
            FROM genres g
            INNER JOIN genres_in_movies gm ON g.id = gm.genre_id
            WHERE gm.movie_id IN
            """;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTs = System.nanoTime();
        long elapsedTj = 0;

        // MySQL Connection Information
        String loginUser = Parameters.username;
        String loginPassword = Parameters.password;
        String loginUrl = "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
        // Set response information
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter frontendOutput = response.getWriter(); // Print Writer


        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Register and Load driver
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database connection error");
        }

        // Check if client wants genre list (early return pattern)
        String action = request.getParameter("action");
        if ("listGenres".equals(action)) {
            long startTj = System.nanoTime();
            try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword)) {
                handleGenreList(frontendOutput, connection);
                long endTj = System.nanoTime();
                elapsedTj = endTj - startTj;
            } catch (SQLException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error getting genre list");
            }
            // frontendOutput will be closed below with the rest of the method
            long endTs = System.nanoTime();
            long elapsedTs = endTs - startTs;
            writeJMeterTimingToFile(elapsedTs, elapsedTj);

            frontendOutput.close();
            return;
        }

        // Connect to database via URL
        long startTj = System.nanoTime();
        try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword)) {
            // Return movie list (existing behavior)
            JSONArray movies = new JSONArray();
            
            // Check if browsing by genre
            String genreIdParam = request.getParameter("genreId");
            if (genreIdParam != null && !genreIdParam.trim().isEmpty()) {
                // Handle genre-based filtering
                try {
                    int genreId = Integer.parseInt(genreIdParam);
                    String pageParam = request.getParameter("page");
                    int page = (pageParam != null && !pageParam.isEmpty()) ? Integer.parseInt(pageParam) : 0;
                    int pageSize = parsePageSize(request.getParameter("pageSize"));
                    int offset = page * pageSize;
                    String completeQuery = GET_MOVIE_LIST_BY_GENRE + "ORDER BY r.ratings DESC LIMIT " + pageSize + " OFFSET " + offset;
                    try (PreparedStatement movieQuery = connection.prepareStatement(completeQuery)) {
                        movieQuery.setInt(1, genreId);
                        try(ResultSet queryResult = movieQuery.executeQuery()) {
                            populateMovies(movies, connection, queryResult);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid genre ID, return empty result
                }
            } else {
                // Existing movie list query with filters
                String titlePattern = createSearchPattern(request.getParameter("title"));
                String starPattern = createSearchPattern(request.getParameter("star"));
                String directorPattern = createSearchPattern(request.getParameter("director"));
                int year = createYearFilter(request.getParameter("year"));
                
                // Handle letter-based filtering for browse by title
                String letterParam = request.getParameter("letter");
                if (letterParam != null && !letterParam.equals("All") && letterParam.matches("^[A-Z0-9]$")) {
                    titlePattern = letterParam + "%";
                }
                
                // Parse page parameter
                String pageParam = request.getParameter("page");
                int page = (pageParam != null && !pageParam.isEmpty()) ? Integer.parseInt(pageParam) : 0;
                int pageSize = parsePageSize(request.getParameter("pageSize"));
                
                // Parse sort parameters
                String sortCriteriaParam = request.getParameter("sortCriteria");
                String sortOrderParam = request.getParameter("sortOrder");
                String tieBreakerParam = request.getParameter("tieBreaker");
                
                String sortCriteria = (sortCriteriaParam != null && !sortCriteriaParam.isEmpty()) ? sortCriteriaParam : "r.ratings";
                String sortOrder = (sortOrderParam != null && !sortOrderParam.isEmpty()) ? sortOrderParam : "DESC";
                String completeQuery = buildMovieListQuery(sortCriteria, sortOrder, tieBreakerParam);
                //noinspection SqlSourceToSinkFlow
                try (PreparedStatement movieQuery = connection.prepareStatement(completeQuery)) {
                    setQueryParameters(movieQuery, titlePattern, starPattern, directorPattern, year, page, pageSize);
                    try(ResultSet queryResult = movieQuery.executeQuery()) {
                        populateMovies(movies, connection, queryResult);
                    }
                }
            }
            long endTj = System.nanoTime();
            elapsedTj = endTj - startTj;

            frontendOutput.write(movies.toString());
            frontendOutput.flush();
        }
        catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
        catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
        finally {
            long endTs = System.nanoTime();
            long elapsedTs = endTs - startTs;
            writeJMeterTimingToFile(elapsedTs, elapsedTj);
        }

        frontendOutput.close();
    }

    private void writeJMeterTimingToFile(long elapsedTs, long elapsedTj) {
        String tmpDirPath = getServletContext().getRealPath("/tmp");
        File tmpDir;
        tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs(); // create tmp folder if it doesn't exist
        }

        // 2. Create the log file inside tmp
        File logFile = new File(tmpDir, "timing_movielist_mysql.txt");

        // 3. Write timing data
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(elapsedTs + "," + elapsedTj);
        } catch (IOException e) {
            System.err.println("Error writing timing data: " + e.getMessage());
        }
    }
    
    private void handleGenreList(PrintWriter frontendOutput, Connection connection) throws SQLException {
        JSONArray genres = new JSONArray();
        try (PreparedStatement genreQuery = connection.prepareStatement(GET_ALL_GENRES_QUERY)) {
            try (ResultSet queryResult = genreQuery.executeQuery()) {
                while (queryResult.next()) {
                    JSONObject genre = new JSONObject();
                    insertResult(queryResult, genre);
                    genres.put(genre);
                }
            }
        }
        frontendOutput.write(genres.toString());
        frontendOutput.flush();
    }

    /**
     * Builds the complete movie list query by adding ORDER BY clause dynamically
     * @param sortCriteria - column to sort by (e.g., "r.ratings", "m.title")
     * @param sortOrder - sort order (e.g., "ASC", "DESC")
     * @param tieBreaker - column to use as tie-breaker (e.g., "title", null)
     * @return Complete SQL query string with ORDER BY clause
     */
    protected String buildMovieListQuery(String sortCriteria, String sortOrder, String tieBreaker) {
        String validatedOrder = switch (sortOrder.toUpperCase()) {
            case "ASC", "DESC" -> sortOrder.toUpperCase();
            default -> "DESC";
        };
        String validatedCriteria = switch (sortCriteria) {
            case "r.ratings" -> "r.ratings";
            case "m.title" -> "m.title";
            case "m.year" -> "m.year";
            case "m.director" -> "m.director";
            default -> "r.ratings";
        };
        
        String orderByClause = "ORDER BY " + validatedCriteria + " " + validatedOrder;
        if (tieBreaker != null && !tieBreaker.isEmpty()) {
            String tieBreakerField = switch (tieBreaker) {
                case "title" -> "m.title";
                case "ratings" -> "r.ratings";
                default -> "";
            };
            if (!tieBreakerField.isEmpty()) {
                orderByClause += ", " + tieBreakerField + " " + validatedOrder;
            }
        }
        
        return GET_MOVIE_LIST + orderByClause + " " + "LIMIT ? OFFSET ?";
    }
    /**
     * Sets query parameters for movie list query
     * @param movieQuery - prepared statement for movie list query
     * @param titlePattern - pattern used by SQL query to find movies with similar titles
     * @param starPattern - pattern used by SQL query to find movies with stars of similar names
     * @param directorPattern - pattern used by SQL query to find movies by director name
     * @param year - year to filter movies by (-1 for no filter)
     * @param page - page number (0-indexed)
     */
    protected void setQueryParameters(PreparedStatement movieQuery, String titlePattern, String starPattern, String directorPattern, int year, int page, int pageSize) throws SQLException {
        movieQuery.setString(TITLE_SEARCH_IDX, titlePattern);
        movieQuery.setString(STAR_SEARCH_IDX, starPattern);
        movieQuery.setString(STAR_SEARCH_CHECK_IDX, starPattern);
        movieQuery.setString(DIRECTOR_SEARCH_IDX, directorPattern);
        movieQuery.setInt(YEAR_SEARCH_IDX, year);
        movieQuery.setInt(YEAR_SEARCH_CHECK_IDX, year);
        movieQuery.setInt(DISPLAY_LIMIT_IDX, pageSize);
        movieQuery.setInt(DISPLAY_OFFSET_IDX, page * pageSize);
    }
    
    /**
     * Parses and validates the page size parameter
     * @param pageSizeParam - page size parameter from request
     * @return valid page size or default if invalid
     */
    protected int parsePageSize(String pageSizeParam) {
        if (pageSizeParam == null || pageSizeParam.trim().isEmpty()) {
            return DEFAULT_MOVIES_PER_PAGE;
        }
        try {
            int pageSize = Integer.parseInt(pageSizeParam);
            // Check if page size is in allowed list
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
    
    /**
     * Converts raw text input from search bar into query-able SQL sequence
     * @param searchInput - search bar input string
     * @return - Pattern used by SQL query with LIKE operator (e.g., "%search%")
     */
    protected String createSearchPattern(String searchInput) {
        return (searchInput != null && !searchInput.trim().isEmpty())
                ? "%" + searchInput.trim() + "%"
                : "%";
    }
    
    /**
     * Converts year from dropdown into integer for exact matching
     * @param yearInput - year from dropdown (always valid or empty)
     * @return - Year as integer, or -1 if empty (no filter)
     */
    protected int createYearFilter(String yearInput) {
        if (yearInput == null || yearInput.trim().isEmpty()) {
            return -1;
        }
        return Integer.parseInt(yearInput.trim());
    }
    /**
     * Helper function to populate movies with all relevant fields for display on main page
     * This optimized version batches all queries to minimize database round trips
     * @param movies - JSON array to populate
     * @param connection - current database connection
     * @param movieResults - result set from movie list query
     * @throws SQLException - SQL exception if database communication fails
     */
    protected void populateMovies(JSONArray movies, Connection connection, ResultSet movieResults) throws SQLException {
        // First pass: collect all movie data
        while (movieResults.next()) {
            JSONObject movie = new JSONObject();
            insertResult(movieResults, movie);
            movies.put(movie);
        }
        
        if (movies.isEmpty()) {
            return;
        }
        
        // Build IN clause for all movie IDs
        StringBuilder inClause = new StringBuilder("(");
        for (int i = 0; i < movies.length(); i++) {
            if (i > 0) inClause.append(",");
            inClause.append("?");
        }
        inClause.append(")");
        
        // Fetch all ratings in one query
        String ratingsQuery = BATCH_RATINGS_QUERY_TEMPLATE + inClause;
        try (PreparedStatement ratingsStmt = connection.prepareStatement(ratingsQuery)) {
            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                ratingsStmt.setString(i + 1, movie.getString("id"));
            }
            try (ResultSet ratingsRs = ratingsStmt.executeQuery()) {
                while (ratingsRs.next()) {
                    String movieId = ratingsRs.getString("movie_id");
                    JSONObject ratings = new JSONObject();
                    insertResult(ratingsRs, ratings);
                    // Find and update the corresponding movie
                    for (int i = 0; i < movies.length(); i++) {
                        JSONObject movie = movies.getJSONObject(i);
                        if (movie.getString("id").equals(movieId)) {
                            movie.put("ratings", ratings);
                            break;
                        }
                    }
                }
            }
        }
        
        // Fetch all stars in one query
        String starsQuery = BATCH_STARS_QUERY_TEMPLATE + inClause + " ORDER BY sm.movie_id, movie_count DESC, s.name ASC";
        try (PreparedStatement starsStmt = connection.prepareStatement(starsQuery)) {
            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                starsStmt.setString(i + 1, movie.getString("id"));
            }
            try (ResultSet starsRs = starsStmt.executeQuery()) {
                String currentMovieId = null;
                JSONArray currentStars = null;
                int starCount = 0;
                
                while (starsRs.next()) {
                    String movieId = starsRs.getString("movie_id");
                    
                    if (!movieId.equals(currentMovieId)) {
                        currentMovieId = movieId;
                        currentStars = null;
                        starCount = 0;
                        
                        // Find the movie and initialize its stars array
                        for (int i = 0; i < movies.length(); i++) {
                            JSONObject movie = movies.getJSONObject(i);
                            if (movie.getString("id").equals(movieId)) {
                                currentStars = new JSONArray();
                                movie.put("stars", currentStars);
                                break;
                            }
                        }
                    }
                    
                    if (currentStars != null && starCount < 3) {
                        JSONObject star = new JSONObject();
                        insertResult(starsRs, star);
                        currentStars.put(star);
                        starCount++;
                    }
                }
            }
        }
        
        // Fetch all genres in one query
        String genresQuery = BATCH_GENRES_QUERY_TEMPLATE + inClause + " ORDER BY gm.movie_id, g.name ASC";
        try (PreparedStatement genresStmt = connection.prepareStatement(genresQuery)) {
            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                genresStmt.setString(i + 1, movie.getString("id"));
            }
            try (ResultSet genresRs = genresStmt.executeQuery()) {
                String currentMovieId = null;
                JSONArray currentGenres = null;
                int genreCount = 0;
                
                while (genresRs.next()) {
                    String movieId = genresRs.getString("movie_id");
                    
                    if (!movieId.equals(currentMovieId)) {
                        currentMovieId = movieId;
                        currentGenres = null;
                        genreCount = 0;
                        
                        // Find the movie and initialize its genres array
                        for (int i = 0; i < movies.length(); i++) {
                            JSONObject movie = movies.getJSONObject(i);
                            if (movie.getString("id").equals(movieId)) {
                                currentGenres = new JSONArray();
                                movie.put("genres", currentGenres);
                                break;
                            }
                        }
                    }
                    
                    if (currentGenres != null && genreCount < 3) {
                        JSONObject genre = new JSONObject();
                        insertResult(genresRs, genre);
                        currentGenres.put(genre);
                        genreCount++;
                    }
                }
            }
        }
    }

    protected void insertResult(ResultSet rs, JSONObject obj) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String col = rsmd.getColumnName(i);
            Object val = rs.getObject(i);
            obj.put(col, val);
        }
    }
}