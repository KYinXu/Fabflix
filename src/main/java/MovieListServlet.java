import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "MovieListServlet", urlPatterns = {"/", "/movies"}) // Allows Tomcat to Interpret URL
public class MovieListServlet extends HttpServlet{

    public static final String QUERY = """
            SELECT m.id, m.title, m.year, m.director
            FROM movies m
            LEFT JOIN ratings r ON m.id = r.movie_id
            ORDER BY r.ratings DESC
            LIMIT 20;
            """;
    
    public static final String GET_RATINGS_QUERY = """
            SELECT ratings, vote_count
            FROM ratings
            WHERE movie_id = ?
            """;
    
    public static final String GET_STARS_QUERY = """
            SELECT s.id, s.name, s.birth_year
            FROM stars s
            INNER JOIN stars_in_movies sm ON s.id = sm.star_id
            WHERE sm.movie_id = ?
            LIMIT 3
            """;
    
    public static final String GET_GENRES_QUERY = """
            SELECT g.id, g.name
            FROM genres g
            INNER JOIN genres_in_movies gm ON g.id = gm.genre_id
            WHERE gm.movie_id = ?
            LIMIT 3
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
        String loginUrl = "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";

        // Response Mime Type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Print Writer
        PrintWriter frontendOutput = response.getWriter();

        // Register and Load driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Connect to database via URL
        try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword)) {

            // Create JSON Movie List
            JSONArray movies = new JSONArray();

            // Get movies
            try (Statement movieQuery = connection.createStatement();
                 ResultSet queryResult = movieQuery.executeQuery(QUERY)) {

                ResultSetMetaData queriedMetaData = queryResult.getMetaData();

                // Retrieve Query Output
                while (queryResult.next()) {
                    JSONObject movie = new JSONObject();

                    // Iterate and Add to Movie Object
                    for (int i = 1; i <= queriedMetaData.getColumnCount(); i++) {
                        String col = queriedMetaData.getColumnName(i);
                        Object val = queryResult.getObject(i);
                        movie.put(col, val);
                    }

                    String movieId = queryResult.getString("id");

                    // Get ratings for this movie
                    try (PreparedStatement ratingsStmt = connection.prepareStatement(GET_RATINGS_QUERY)) {
                        ratingsStmt.setString(1, movieId);
                        try (ResultSet ratingsRs = ratingsStmt.executeQuery()) {
                            if (ratingsRs.next()) {
                                JSONObject ratings = new JSONObject();
                                ResultSetMetaData ratingsMetaData = ratingsRs.getMetaData();
                                for (int i = 1; i <= ratingsMetaData.getColumnCount(); i++) {
                                    String col = ratingsMetaData.getColumnName(i);
                                    Object val = ratingsRs.getObject(i);
                                    ratings.put(col, val);
                                }
                                movie.put("ratings", ratings);
                            }
                        }
                    }

                    // Get stars for this movie (limit 3)
                    try (PreparedStatement starsStmt = connection.prepareStatement(GET_STARS_QUERY)) {
                        starsStmt.setString(1, movieId);
                        try (ResultSet starsRs = starsStmt.executeQuery()) {
                            JSONArray stars = new JSONArray();
                            ResultSetMetaData starsMetaData = starsRs.getMetaData();
                            while (starsRs.next()) {
                                JSONObject star = new JSONObject();
                                for (int i = 1; i <= starsMetaData.getColumnCount(); i++) {
                                    String col = starsMetaData.getColumnName(i);
                                    Object val = starsRs.getObject(i);
                                    star.put(col, val);
                                }
                                stars.put(star);
                            }
                            movie.put("stars", stars);
                        }
                    }

                    // Get genres for this movie (limit 3)
                    try (PreparedStatement genresStmt = connection.prepareStatement(GET_GENRES_QUERY)) {
                        genresStmt.setString(1, movieId);
                        try (ResultSet genresRs = genresStmt.executeQuery()) {
                            JSONArray genres = new JSONArray();
                            ResultSetMetaData genresMetaData = genresRs.getMetaData();
                            while (genresRs.next()) {
                                JSONObject genre = new JSONObject();
                                for (int i = 1; i <= genresMetaData.getColumnCount(); i++) {
                                    String col = genresMetaData.getColumnName(i);
                                    Object val = genresRs.getObject(i);
                                    genre.put(col, val);
                                }
                                genres.put(genre);
                            }
                            movie.put("genres", genres);
                        }
                    }

                    // Add Movie Object to Movies Array
                    movies.put(movie);
                }
            }

            // Write movie object to response
            frontendOutput.write(movies.toString());
            frontendOutput.flush();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        frontendOutput.close();

    }
}