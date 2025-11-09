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

@WebServlet(name = "AddMovieServlet", urlPatterns = {"/add-movie"})
public class AddMovieServlet extends HttpServlet {
    public static final String ADD_MOVIE_QUERY = """
            INSERT INTO movies (id, title, year, director)
            VALUES (?, ?, ?, ?);
            """;

    public static final String GET_MAX_ID = """
            SELECT MAX(id) AS max_id FROM movies;
            """;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String title = jsonObject.getString("title");
        String year = jsonObject.getString("year");
        String newId = getNewId();
        String director = jsonObject.getString("director");
        setMimeType(response);

        Connection databaseConnection = establishDatabaseConnection();
        boolean existenceFlag = false;
        int rowsAffected = 0;

        try (PreparedStatement queryStatement = databaseConnection.prepareStatement(ADD_MOVIE_QUERY)) {
            queryStatement.setString(1, newId);
            queryStatement.setString(2, title);
            queryStatement.setInt(3, Integer.parseInt(year));
            queryStatement.setString(4, director);
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
        String newId = "tt0000001";
        try (Connection databaseConnection = establishDatabaseConnection();
             PreparedStatement queryStatement = databaseConnection.prepareStatement(GET_MAX_ID);
             ResultSet resultSet = queryStatement.executeQuery()) {

            if (resultSet.next() && resultSet.getString("max_id") != null) {
                String maxId = resultSet.getString("max_id");
                int nextNum = Integer.parseInt(maxId.substring(2)) + 1;
                newId = String.format("tt%07d", nextNum);
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
