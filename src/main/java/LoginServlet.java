import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

//This is a code freeze for project 2

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"}) // Allows Tomcat to Interpret URL
public class LoginServlet extends HttpServlet {
    public static final String LOGIN_VERIFICATION_QUERY = """
            SELECT EXISTS(
                    SELECT 1
                    FROM customers
                    WHERE email = ? AND password = ?
            )
            """;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String email = jsonObject.getString("email");
        String password = jsonObject.getString("password");
        setMimeType(response);

        Connection databaseConnection = establishDatabaseConnection();
        boolean existenceFlag = false;

        try (PreparedStatement queryStatement = databaseConnection.prepareStatement(LOGIN_VERIFICATION_QUERY)){
            queryStatement.setString(1, email);
            queryStatement.setString(2, password);
            ResultSet queryResult = queryStatement.executeQuery();
            if (queryResult.next()){
                existenceFlag = queryResult.getBoolean(1);
            }
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

        if (existenceFlag){
            HttpSession session = request.getSession(true);
            session.setAttribute("email", email);
            session.setMaxInactiveInterval(30 * 60);
        }

        JSONObject jsonSuccessStatus = buildJSONSuccess(existenceFlag);
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
}
