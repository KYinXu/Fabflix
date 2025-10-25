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

    public static final String GET_MOVIE_LIST = """
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // MySQL Connection Information
        String loginUser = Parameters.username;
        String loginPassword = Parameters.password;
        String loginUrl = "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
        // Set response information
        addCORSHeader(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter frontendOutput = response.getWriter(); // Print Writer
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Register and Load driver
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database connection error");
        }
        // Connect to database via URL
        try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPassword)) {
            JSONArray movies = new JSONArray();
            try (PreparedStatement movieQuery = connection.prepareStatement(GET_MOVIE_LIST);
                 ResultSet queryResult = movieQuery.executeQuery(GET_MOVIE_LIST)) {
                ResultSetMetaData queriedMetaData = queryResult.getMetaData();
                while (queryResult.next()) {
                    JSONObject movie = new JSONObject();
                    populateMovie(movie, connection, queryResult);
                    movies.put(movie);
                }
            }
            frontendOutput.write(movies.toString());
            frontendOutput.flush();
        }
        catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
        frontendOutput.close();
    }

    private void addCORSHeader(HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Helper function to populate a movie object with all relevant fields for display on main page
     * @param movie - movie JSON object to populate
     * @param connection - current database connection
     * @param queryResult - next result from movie list query
     * @throws SQLException - SQL exception if database communication fails
     */
    protected void populateMovie(JSONObject movie, Connection connection, ResultSet queryResult) throws SQLException {
        insertResult(queryResult, movie);
        String movieId = queryResult.getString("id");
        insertRatingsInMovie(connection, movie, movieId);
        insertStarsInMovie(connection, movie, movieId);
        insertGenresInMovie(connection, movie, movieId);
    }
    /**
     * Inserts ratings information into the current movie object
     * @param connection - current database connection
     * @param movie - movie JSON object to insert ratings into
     * @param movieId - ID of movie to query ratings table for
     * @throws SQLException - SQL exception if database communication fails
     */
    protected void insertRatingsInMovie(Connection connection, JSONObject movie, String movieId) throws SQLException {
        try (PreparedStatement ratingsStmt = connection.prepareStatement(GET_RATINGS_QUERY)) {
            ratingsStmt.setString(1, movieId);
            try (ResultSet ratingsRs = ratingsStmt.executeQuery()) {
                if (ratingsRs.next()) {
                    JSONObject ratings = new JSONObject();
                    insertResult(ratingsRs, ratings);
                    movie.put("ratings", ratings);

                }
            }
        }
    }
    /**
     * Inserts stars information into the current movie object
     * @param connection - current database connection
     * @param movie - movie JSON object to insert ratings into
     * @param movieId - ID of movie to query ratings table for
     * @throws SQLException - SQL exception if database communication fails
     */
    protected void insertStarsInMovie(Connection connection, JSONObject movie, String movieId) throws SQLException {
        try (PreparedStatement starsStmt = connection.prepareStatement(GET_STARS_QUERY)) {
            starsStmt.setString(1, movieId);
            try (ResultSet starsRs = starsStmt.executeQuery()) {
                JSONArray stars = new JSONArray();
                while (starsRs.next()) {
                    JSONObject star = new JSONObject();
                    insertResult(starsRs, star);
                }
                movie.put("stars", stars);
            }
        }
    }
    /**
     * Inserts genres information into the current movie object
     * @param connection - current database connection
     * @param movie - movie JSON object to insert ratings into
     * @param movieId - ID of movie to query ratings table for
     * @throws SQLException - SQL exception if database communication fails
     */
    protected void insertGenresInMovie(Connection connection, JSONObject movie, String movieId) throws SQLException {
        try (PreparedStatement genresStmt = connection.prepareStatement(GET_GENRES_QUERY)) {
            genresStmt.setString(1, movieId);
            try (ResultSet genresRs = genresStmt.executeQuery()) {
                JSONArray genres = new JSONArray();
                while (genresRs.next()) {
                    JSONObject genre = new JSONObject();
                    insertResult(genresRs, genre);
                }
                movie.put("genres", genres);
            }
        }
    }

    protected void insertResult(ResultSet rs, JSONObject obj) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String col = rsmd.getColumnName(i);
            Object val = rs.getObject(i);
            obj.put(col, val);
        }
    }
}