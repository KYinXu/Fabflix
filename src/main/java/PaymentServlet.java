import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import utils.CartItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"}) // Allows Tomcat to Interpret URL
public class PaymentServlet extends HttpServlet {
    public static final String PAYMENT_VERIFICATION_QUERY = """
            SELECT EXISTS(
                    SELECT 1
                    FROM credit_cards
                    WHERE id = ? AND first_name = ? AND last_name = ? AND expiration = ?
            )
            """;

    public static final String SALE_UPDATE_QUERY = """
            INSERT INTO sales (customer_id, movie_id, sale_date)
            VALUES (
                (SELECT id FROM customers WHERE credit_card_id = ?),
                 ?, 
                 NOW()
             )
            """;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String id = jsonObject.getString("id");
        String first_name = jsonObject.getString("first_name");
        String last_name = jsonObject.getString("last_name");
        String expirationString = jsonObject.getString("expiration");
        Date expiration = Date.valueOf(expirationString);


        setMimeType(response);

        Connection databaseConnection = establishDatabaseConnection();
        boolean validPaymentFlag = false;


        try (PreparedStatement queryStatement = databaseConnection.prepareStatement(PAYMENT_VERIFICATION_QUERY)) {
            queryStatement.setString(1, id);
            queryStatement.setString(2, first_name);
            queryStatement.setString(3, last_name);
            queryStatement.setDate(4, expiration);
            ResultSet queryResult = queryStatement.executeQuery();
            if (queryResult.next()){
                validPaymentFlag = queryResult.getBoolean(1);
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Map<String, CartItem> shoppingCart = (Map<String, CartItem>) session.getAttribute("cart");
            if (validPaymentFlag){
                try {
                    updateDatabaseSale(databaseConnection, id, shoppingCart);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        JSONObject jsonSuccessStatus = buildJSONSuccess(validPaymentFlag);
        PrintWriter reactOutput = response.getWriter();
        reactOutput.write(jsonSuccessStatus.toString());
        reactOutput.flush();
        reactOutput.close();

        try {
            databaseConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void updateDatabaseSale(Connection databaseConnection, String id,
                                      Map<String, CartItem> shoppingCart) throws SQLException {
        try (PreparedStatement queryStatement = databaseConnection
                .prepareStatement(SALE_UPDATE_QUERY)) {
            for (CartItem item : shoppingCart.values()) {
                queryStatement.setString(1, id);
                queryStatement.setString(2, item.getMovieId());
                queryStatement.addBatch();
            }
            queryStatement.executeBatch();
        } catch (SQLException e) {
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

    protected Connection establishDatabaseConnection(){
        String loginUser = Parameters.username;
        String loginPassword = Parameters.password;
        String loginUrl = "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(loginUrl, loginUser, loginPassword);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    protected void setMimeType(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

}



