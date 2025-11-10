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
    public static final String ADD_MOVIE_PROCEDURE = "{ CALL add_movie(?, ?, ?, ?, ?) }";

    public static final String GET_MAX_ID = """
            SELECT MAX(id) AS max_id FROM movies;
            """;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String title = jsonObject.getString("title");
        String year = jsonObject.getString("year");
        String director = jsonObject.getString("director");
        setMimeType(response);

        Connection databaseConnection = establishDatabaseConnection();
        boolean existenceFlag = false;

        try (CallableStatement stmt = databaseConnection.prepareCall(ADD_MOVIE_PROCEDURE)) {
            stmt.setString(1, title);
            stmt.setInt(2, Integer.parseInt(year));
            stmt.setString(3, director);
            stmt.setString(4, jsonObject.getString("star_name"));
            stmt.setString(5, jsonObject.getString("genre_name"));
            stmt.execute();
            existenceFlag = true;
        } catch (Exception e) {
            e.printStackTrace();
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
