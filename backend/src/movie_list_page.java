import java.sql.*;

@WebServlet("/movies") // Allows Tomcat to Interpret URL
public class MovieListServlet extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // MySQL Connection Information
        String loginUser = "arminm";
        String loginPassword = "Armin138342";
        String loginUrl = "jdbc:mysql://localhost:3306/movie_db";

        // Response Mime Type?
        response.setContentType("text/html"); // edit for React???

        // Print Writer
        PrintWriter frontendOutput = response.getWriter();

        // Register and Load driver
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        // Connect to database via URL
        try {
            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword);

            // Create Query for Movie Table Attributes and Rating Attributes
            Statement movieQuery = connection.createStatement();
            ResultSet queryResult = movieQuery.
                    executeQuery("SELECT m.title, m.year, m.director, g.name, s.stars, r.rating " +
                            "FROM movies m " +
                            "WHERE g.name IN (SELECT g.name FROM movies m" +
                                    "JOIN genres_in_movies gm ON m.movie_id = gm.movie_id" +
                                    "JOIN genres g ON g.id = gm.genre_id" +
                                    "LIMIT 3)"
                            "AND s.name IN (SELECT s.name FROM movies m" +
                                    "JOIN stars_in_movies sm ON m.movie_id = sm.movie_id" +
                                    "JOIN stars s ON s.id = sm.star_id" +
                                    "LIMIT 3)" +
                            "JOIN ratings r ON m.id = r.movie_id " +
                            "ORDER BY r.rating DESC " +
                            "LIMIT 20;");

            // Retrieve Query Output
            while (queryResult.next()){
                int id = queryResult.getInt("m.id");
                String title = queryResult.getString("m.title");
                int year = queryResult.getInt("m.year");
                String director = queryResult.getString("m.director");
                String genre = queryResult.getString("g.name");
                String stars = queryResult.getString("s.stars");
                double rating = queryResult.getDouble("r.rating");
            }
        }
        catch (Exception e) {
            // do something
        }

        frontendOutput.close();

    }
}