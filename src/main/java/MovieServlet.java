import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import config.MongoDBConnectionConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/movie/*")
public class MovieServlet extends HttpServlet {

    private MongoDBConnectionConfig mongoConfig;

    @Override
    public void init() {
        mongoConfig = new MongoDBConnectionConfig();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTs = System.nanoTime(); // start times for JMeter
        long elapsedTj = 0;

        String pathInfo = request.getPathInfo();
        if (!isValidPath(pathInfo)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No movie ID provided");
            return;
        }

        String movieId = pathInfo.substring(1);

        try {
            if (mongoConfig == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB configuration not initialized");
                return;
            }

            long startTj = System.nanoTime(); // JMeter Timing

            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> collection = database.getCollection("movies");
            
            Document movieDoc = collection.find(new Document("_id", movieId)).first();

            long endTj = System.nanoTime(); // JMeter Timing
            elapsedTj = endTj - startTj; // JMeter Timing
            
            if (movieDoc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found");
                return;
            }

            JSONObject movie = convertMovieToJSON(movieDoc);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(movie.toString());
            }
        } catch (com.mongodb.MongoTimeoutException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB connection timeout. Is MongoDB running?");
        } catch (com.mongodb.MongoException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB error: " + e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        finally {
            long endTs = System.nanoTime();
            long elapsedTs = endTs - startTs;
            writeJMeterTimingToFile(elapsedTs, elapsedTj);
        }
    }

    private void writeJMeterTimingToFile(long elapsedTs, long elapsedTj){
        try (FileWriter fw = new FileWriter("/tmp/timing_singlemovie_mongodb.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(elapsedTs + "," + elapsedTj);
        } catch (IOException e) {
            System.err.println("Error writing timing data: " + e.getMessage());
        }
    }

    private JSONObject convertMovieToJSON(Document movieDoc) {
        JSONObject movie = new JSONObject();
        
        movie.put("id", movieDoc.getString("_id"));
        movie.put("title", movieDoc.getString("title"));
        movie.put("year", movieDoc.getInteger("year"));
        movie.put("director", movieDoc.getString("director"));

        Document ratingDoc = movieDoc.get("rating", Document.class);
        if (ratingDoc != null) {
            JSONObject ratings = new JSONObject();
            ratings.put("ratings", ratingDoc.getDouble("score"));
            ratings.put("vote_count", ratingDoc.getInteger("voteCount"));
            movie.put("ratings", ratings);
        }

        java.util.List<Document> starsList = movieDoc.getList("stars", Document.class);
        if (starsList != null) {
            JSONArray stars = new JSONArray();
            for (Document starDoc : starsList) {
                JSONObject star = new JSONObject();
                star.put("id", starDoc.getString("id"));
                star.put("name", starDoc.getString("name"));
                if (starDoc.containsKey("birthYear")) {
                    star.put("birthYear", starDoc.getInteger("birthYear"));
                }
                if (starDoc.containsKey("movieCount")) {
                    star.put("movie_count", starDoc.getInteger("movieCount"));
                }
                stars.put(star);
            }
            movie.put("stars", stars);
        }

        java.util.List<Document> genresList = movieDoc.getList("genres", Document.class);
        if (genresList != null) {
            JSONArray genres = new JSONArray();
            for (Document genreDoc : genresList) {
                JSONObject genre = new JSONObject();
                genre.put("id", genreDoc.getInteger("id"));
                genre.put("name", genreDoc.getString("name"));
                genres.put(genre);
            }
            movie.put("genres", genres);
        }

        return movie;
    }

    private static boolean isValidPath(String pathInfo) {
        return !(pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/"));
    }

    @Override
    public void destroy() {
        if (mongoConfig != null) {
            mongoConfig.closeConnection();
        }
    }
}