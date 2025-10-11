import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.MovieList;

@WebServlet(name = "MovieListServlet", urlPatterns = {"/", "/movies"}) // Allows Tomcat to Interpret URL
public class MovieListServlet extends HttpServlet{

    public static final String QUERY = """
            SELECT m.title, m.year, m.director
            FROM movies m
            LIMIT 20;
            """;

    private void addCORSHeader(HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        addCORSHeader(response);

        // MySQL Connection Information
        String loginUser = Parameters.username;
        String loginPassword = Parameters.password;
        String loginUrl = "jdbc:mysql://localhost:3306/movie_db";

        // Response Mime Type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write(movie.toString());

        // Print Writer
        PrintWriter frontendOutput = response.getWriter();

        // Movie List, containing the Top 20 Result
        ArrayList<MovieList> topTwentyMovies = new ArrayList<>();

        // Register and Load driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Connect to database via URL
        try {
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword);

            // Create Query for Movie Table Attributes and Rating Attributes
            Statement movieQuery = connection.createStatement();
            ResultSet queryResult = movieQuery.executeQuery(QUERY);

            // Create JSON Movie List
            JSONArray movies = new JSONArray();
            ResultSetMetaData queriedMetaData = queryResult.getMetaData();

            // Retrieve Query Output
            while (queryResult.next()){
                JSONObject movie = new JSONObject();

                // Iterate and Add to Movie Object
                for (int i = 1; i <= queriedMetaData.getColumnCount(); i++) {
                    String col = queriedMetaData.getColumnName(i);
                    Object val = queryResult.getObject(i);
                    movie.put(col, val);
                }

                // Add Movie Object to Movies Array
                movies.put(movie);
            }

            // Write movie object to response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            frontendOutput.write(movies.toString());
            frontendOutput.flush();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        frontendOutput.close();

    }
}