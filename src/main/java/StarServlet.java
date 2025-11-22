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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/star/*")
public class StarServlet extends HttpServlet {

    private MongoDBConnectionConfig mongoConfig;

    @Override
    public void init() {
        mongoConfig = new MongoDBConnectionConfig();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (!isValidPath(pathInfo)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No star ID provided");
            return;
        }

        String starId = pathInfo.substring(1);

        try {
            if (mongoConfig == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB configuration not initialized");
                return;
            }

            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> starsCollection = database.getCollection("stars");
            
            Document starDoc = starsCollection.find(new Document("_id", starId)).first();
            
            if (starDoc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Star not found");
                return;
            }

            JSONObject star = convertStarToJSON(starDoc, database);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(star.toString());
            }
        } catch (com.mongodb.MongoTimeoutException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB connection timeout. Is MongoDB running?");
        } catch (com.mongodb.MongoException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB error: " + e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private JSONObject convertStarToJSON(Document starDoc, MongoDatabase database) {
        JSONObject star = new JSONObject();
        
        star.put("id", starDoc.getString("_id"));
        star.put("name", starDoc.getString("name"));
        if (starDoc.containsKey("birthYear")) {
            star.put("birthYear", starDoc.getInteger("birthYear"));
        }
        if (starDoc.containsKey("movieCount")) {
            star.put("movie_count", starDoc.getInteger("movieCount"));
        }

        List<String> movieIds = starDoc.getList("movies", String.class);
        if (movieIds != null && !movieIds.isEmpty()) {
            JSONArray movies = fetchMoviesByIds(movieIds, database);
            star.put("movies", movies);
        } else {
            star.put("movies", new JSONArray());
        }

        return star;
    }

    private JSONArray fetchMoviesByIds(List<String> movieIds, MongoDatabase database) {
        JSONArray movies = new JSONArray();
        
        if (movieIds == null || movieIds.isEmpty()) {
            return movies;
        }

        MongoCollection<Document> moviesCollection = database.getCollection("movies");
        
        Document query = new Document("_id", new Document("$in", movieIds));
        
        List<Document> movieDocs = moviesCollection.find(query)
            .sort(new Document("year", -1).append("title", 1))
            .into(new ArrayList<>());
        
        for (Document movieDoc : movieDocs) {
            JSONObject movie = new JSONObject();
            movie.put("id", movieDoc.getString("_id"));
            movie.put("title", movieDoc.getString("title"));
            movie.put("year", movieDoc.getInteger("year"));
            movie.put("director", movieDoc.getString("director"));
            movies.put(movie);
        }

        return movies;
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
