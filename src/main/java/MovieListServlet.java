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
            SELECT DISTINCT m.id, m.title, m.year, m.director, r.ratings
            FROM movies m
            LEFT JOIN ratings r ON m.id = r.movie_id
            LEFT JOIN stars_in_movies sm ON m.id = sm.movie_id
            LEFT JOIN stars s ON sm.star_id = s.id
            WHERE m.title LIKE ?
            AND (s.name LIKE ? OR ? = '%')
            AND m.director LIKE ?
            AND (m.year = ? OR ? = -1)
            """;
    public static final int TITLE_SEARCH_IDX = 1;
    public static final int STAR_SEARCH_IDX = 2;
    public static final int STAR_SEARCH_CHECK_IDX = 3;
    public static final int DIRECTOR_SEARCH_IDX = 4;
    public static final int YEAR_SEARCH_IDX = 5;
    public static final int YEAR_SEARCH_CHECK_IDX = 6;
    public static final int DISPLAY_LIMIT_IDX = 7;
    
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
            String titlePattern = createSearchPattern(request.getParameter("title"));
            String starPattern = createSearchPattern(request.getParameter("star"));
            String directorPattern = createSearchPattern(request.getParameter("director"));
            int year = createYearFilter(request.getParameter("year"));
            String sortCriteria = "r.ratings";
            String sortOrder = "DESC";
            String completeQuery = buildMovieListQuery(sortCriteria, sortOrder);
            //noinspection SqlSourceToSinkFlow
            try (PreparedStatement movieQuery = connection.prepareStatement(completeQuery)) {
                setQueryParameters(movieQuery, titlePattern, starPattern, directorPattern, year);
                try(ResultSet queryResult = movieQuery.executeQuery()) {
                    //ResultSetMetaData queriedMetaData = queryResult.getMetaData();
                    while (queryResult.next()) {
                        JSONObject movie = new JSONObject();
                        populateMovie(movie, connection, queryResult);
                        movies.put(movie);
                    }
                }
            }
            frontendOutput.write(movies.toString());
            frontendOutput.flush();
        }
        catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
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
     * Builds the complete movie list query by adding ORDER BY clause dynamically
     * @param sortCriteria - column to sort by (e.g., "r.ratings", "m.title")
     * @param sortOrder - sort order (e.g., "ASC", "DESC")
     * @return Complete SQL query string with ORDER BY clause
     */
    protected String buildMovieListQuery(String sortCriteria, String sortOrder) {
        String validatedOrder = switch (sortOrder.toUpperCase()) {
            case "ASC", "DESC" -> sortOrder.toUpperCase();
            default -> "DESC";
        };
        String validatedCriteria = switch (sortCriteria) {
            case "r.ratings", "m.title", "m.year", "m.director" -> sortCriteria;
            default -> "r.ratings";
        };
        return GET_MOVIE_LIST + "ORDER BY " + validatedCriteria + " " + validatedOrder + " " + "LIMIT ?";
    }
    /**
     * Sets query parameters for movie list query
     * @param movieQuery - prepared statement for movie list query
     * @param titlePattern - pattern used by SQL query to find movies with similar titles
     * @param starPattern - pattern used by SQL query to find movies with stars of similar names
     * @param directorPattern - pattern used by SQL query to find movies by director name
     * @param year - year to filter movies by (-1 for no filter)
     */
    protected void setQueryParameters(PreparedStatement movieQuery, String titlePattern, String starPattern, String directorPattern, int year) throws SQLException {
        movieQuery.setString(TITLE_SEARCH_IDX, titlePattern);
        movieQuery.setString(STAR_SEARCH_IDX, starPattern);
        movieQuery.setString(STAR_SEARCH_CHECK_IDX, starPattern);
        movieQuery.setString(DIRECTOR_SEARCH_IDX, directorPattern);
        movieQuery.setInt(YEAR_SEARCH_IDX, year);
        movieQuery.setInt(YEAR_SEARCH_CHECK_IDX, year);
        movieQuery.setInt(DISPLAY_LIMIT_IDX, 20);
    }
    
    /**
     * Converts raw text input from search bar into query-able SQL sequence
     * @param searchInput - search bar input string
     * @return - Pattern used by SQL query with LIKE operator (e.g., "%search%")
     */
    protected String createSearchPattern(String searchInput) {
        return (searchInput != null && !searchInput.trim().isEmpty())
                ? "%" + searchInput.trim() + "%"
                : "%";
    }
    
    /**
     * Converts year from dropdown into integer for exact matching
     * @param yearInput - year from dropdown (always valid or empty)
     * @return - Year as integer, or -1 if empty (no filter)
     */
    protected int createYearFilter(String yearInput) {
        if (yearInput == null || yearInput.trim().isEmpty()) {
            return -1;
        }
        return Integer.parseInt(yearInput.trim());
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
                    stars.put(star);
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
                    genres.put(genre);
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