import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import config.MongoDBConnectionConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import utils.CartItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"}) // Allows Tomcat to Interpret URL
public class PaymentServlet extends HttpServlet {
    private MongoDBConnectionConfig mongoConfig;

    @Override
    public void init() {
        mongoConfig = new MongoDBConnectionConfig();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String id = jsonObject.getString("id");
        String first_name = jsonObject.getString("first_name");
        String last_name = jsonObject.getString("last_name");
        String expirationString = jsonObject.getString("expiration");
        java.util.Date expiration = java.sql.Date.valueOf(expirationString);

        setMimeType(response);

        MongoDatabase databaseConnection = establishDatabaseConnection();
        boolean validPaymentFlag = false;
        Integer customerId = null;

        try {
            MongoCollection<Document> customersCollection = databaseConnection.getCollection("customers");
            
            Bson filter = Filters.and(
                Filters.eq("creditCard.id", id),
                Filters.eq("creditCard.firstName", first_name),
                Filters.eq("creditCard.lastName", last_name),
                Filters.eq("creditCard.expiration", expiration)
            );
            
            Document customerDocument = customersCollection.find(filter).first();

            if (customerDocument != null) {
                validPaymentFlag = true;
                customerId = customerDocument.getInteger("mysqlId");
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Map<String, CartItem> shoppingCart = (Map<String, CartItem>) session.getAttribute("cart");
            if (validPaymentFlag && customerId != null){
                try {
                    updateDatabaseSale(databaseConnection, customerId, shoppingCart);
                } catch (MongoException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        JSONObject jsonSuccessStatus = buildJSONSuccess(validPaymentFlag);
        PrintWriter reactOutput = response.getWriter();
        reactOutput.write(jsonSuccessStatus.toString());
        reactOutput.flush();
        reactOutput.close();
    }

    protected void updateDatabaseSale(MongoDatabase databaseConnection, Integer customerId,
                                      Map<String, CartItem> shoppingCart) throws MongoException {
        try {
            MongoCollection<Document> salesCollection = databaseConnection.getCollection("sales");
            List<Document> saleDocs = new ArrayList<>();
            for (CartItem item : shoppingCart.values()) {
                Document sale = new Document()
                        .append("customerId", customerId)
                        .append("movieId", item.getMovieId())
                        .append("saleDate", new java.util.Date());

                saleDocs.add(sale);
            }

            if (!saleDocs.isEmpty()) {
                salesCollection.insertMany(saleDocs);
            }
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    protected JSONObject buildJSONSuccess(boolean success){
        JSONObject jsonSuccessStatus = new JSONObject();
        if (success){
            jsonSuccessStatus.put("status", "success");
        }
        else{
            jsonSuccessStatus.put("status", "failure");
        }
        return jsonSuccessStatus;
    }

    protected StringBuilder buildJSONString(HttpServletRequest request) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return jsonString;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
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

    protected void setMimeType(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    @Override
    public void destroy() {
        if (mongoConfig != null) {
            mongoConfig.closeConnection();
        }
    }
}



