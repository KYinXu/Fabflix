import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "AddStarServlet", urlPatterns = {"/add-star"})
public class AddStarServlet extends HttpServlet {
    public static final String ADD_STAR_QUERY = """
            INSERT INTO stars (id, name, birth_year)
            VALUES (?, ?, ?);
            """;

    public static final String GET_MAX_ID = """
            SELECT MAX(id) AS max_id FROM stars;
            """;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String name = jsonObject.getString("name");
        String birth_year = jsonObject.getString("birth_year");
        String newId = getNewId();
        setMimeType(response);

        Connection databaseConnection = establishDatabaseConnection();
        boolean existenceFlag = false;
        int rowsAffected = 0;

        try (PreparedStatement queryStatement = databaseConnection.prepareStatement(ADD_STAR_QUERY)) {
            queryStatement.setString(1, newId);
            queryStatement.setString(2, name);
            if (birth_year == null || birth_year.isEmpty()) {
                queryStatement.setNull(3, Types.INTEGER);
            }
            else{
                queryStatement.setInt(3, Integer.parseInt(birth_year));
            }
            rowsAffected = queryStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (rowsAffected > 0){
            existenceFlag = true;
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

    protected String getNewId() {
        String newId = "nm0000001";
        try (Connection databaseConnection = establishDatabaseConnection();
            PreparedStatement queryStatement = databaseConnection.prepareStatement(GET_MAX_ID);
            ResultSet resultSet = queryStatement.executeQuery()) {

                if (resultSet.next() && resultSet.getString("max_id") != null) {
                    String maxId = resultSet.getString("max_id");
                    int nextNum = Integer.parseInt(maxId.substring(2)) + 1;
                    newId = String.format("nm%07d", nextNum);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return newId;
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
