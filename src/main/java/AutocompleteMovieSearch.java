import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import config.MongoDBConnectionConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import utils.SearchPatternUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

@WebServlet(name = "AutocompleteMovieSearch", urlPatterns = {"/autocomplete-movie-search"})
public class AutocompleteMovieSearch extends HttpServlet {
    
    private MongoDBConnectionConfig mongoConfig;
    private static final int MAX_SUGGESTIONS = 10;
    
    @Override
    public void init() {
        mongoConfig = new MongoDBConnectionConfig();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String titleParam = request.getParameter("title");
        try (PrintWriter writer = response.getWriter()) {
            if (mongoConfig == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB configuration not initialized");
                return;
            }
            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> moviesCollection = database.getCollection("movies");
            JSONArray suggestions = new JSONArray();
            
            Pattern titlePattern = SearchPatternUtils.createSearchPattern(titleParam, SearchPatternUtils.SearchMode.TOKEN_BASED);
            
            moviesCollection.find(Filters.regex("title", titlePattern))
                    .sort(Sorts.ascending("title"))
                    .limit(MAX_SUGGESTIONS)
                    .forEach(document -> {
                        JSONObject movie = new JSONObject();
                        
                        // Handle _id field - it could be String or ObjectId
                        Object idObj = document.get("_id");
                        String movieId = null;
                        if (idObj != null) {
                            movieId = idObj.toString();
                        }
                        
                        if (movieId != null) {
                            movie.put("id", movieId);
                            movie.put("title", document.getString("title"));
                            Object year = document.get("year");
                            if (year != null) {
                                movie.put("year", year instanceof Integer ? year : Integer.parseInt(year.toString()));
                            }
                            suggestions.put(movie);
                        }
                    });
            
            writer.write(suggestions.toString());
            writer.flush();
            
        } catch (com.mongodb.MongoTimeoutException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB connection timeout. Is MongoDB running?");
        } catch (com.mongodb.MongoException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MongoDB error: " + e.getMessage());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
    
}
