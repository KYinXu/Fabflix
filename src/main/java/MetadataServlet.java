import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.MongoDBConnectionConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "MetadataServlet", urlPatterns = {"/metadata"})
public class MetadataServlet extends HttpServlet {
    private MongoDBConnectionConfig mongoConfig;

    @Override
    public void init(){
        mongoConfig = new MongoDBConnectionConfig();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject result = new JSONObject();
        JSONArray collectionsArray = new JSONArray();

        try {
            MongoDatabase database = establishDatabaseConnection();

            for (String collectionName : database.listCollectionNames()) {

                JSONObject collectionObj = new JSONObject();
                collectionObj.put("name", collectionName);

                JSONArray fieldsArray = new JSONArray();

                MongoCollection<Document> collection = database.getCollection(collectionName);
                Document sample = collection.find().first();

                if (sample != null) {
                    for (String key : sample.keySet()) {
                        JSONObject fieldObj = new JSONObject();
                        fieldObj.put("name", key);
                        fieldObj.put("type", sample.get(key) != null ? sample.get(key).getClass().getSimpleName() : "unknown");
                        fieldsArray.put(fieldObj);
                    }
                }

                collectionObj.put("fields", fieldsArray);
                collectionsArray.put(collectionObj);
            }

            result.put("collections", collectionsArray);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getMessage());
        }

        PrintWriter out = response.getWriter();
        out.write(result.toString());
        out.close();
    }

    protected MongoDatabase establishDatabaseConnection(){
        try {
            MongoDatabase database = mongoConfig.getDatabase();
            return database;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        if (mongoConfig != null) {
            mongoConfig.closeConnection();
        }
    }
}
