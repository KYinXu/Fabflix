import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import utils.ResponseUtils;
import java.io.IOException;
import java.sql.*;

/**
 * Endpoints for querying movie information for a detailed view
 */
@WebServlet("/movie/*")
public class MovieServlet extends HttpServlet {

    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        String GET_MOVIE_BY_ID = """
                SELECT *
                FROM movies
                WHERE id = ?
                """;
        String GET_RATINGS_INFORMATION = """
                SELECT ratings, vote_count
                FROM ratings
                WHERE movie_id = ?
                """;
        String GET_STARS_INFORMATION = """
                SELECT s.*
                FROM stars s
                INNER JOIN stars_in_movies sm ON s.id = sm.star_id
                WHERE sm.movie_id = ?
                """;
        String GET_GENRES_INFORMATION = """
                SELECT g.*
                FROM genres g
                    INNER JOIN genres_in_movies gm ON g.id = gm.genre_id
                WHERE gm.movie_id = ?
                """;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //Install mySQL driver
            //ResponseUtils.setCommonHeaders(response);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String pathInfo = request.getPathInfo();
        if (!isValidPath(pathInfo)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No movie ID provided");
            return;
        }
        String movieId = pathInfo.substring(1); // removes the leading '/'
        try (Connection conn = DriverManager.getConnection(
                "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                Parameters.username,
                Parameters.password)) {
            JSONObject movie = new JSONObject();
            try (PreparedStatement statement1 = conn.prepareStatement(GET_MOVIE_BY_ID)) {
                statement1.setString(1, movieId);
                try (ResultSet rs = statement1.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData(); // get col names
                    if (rs.next()) {
                        insertResult(rs, rsmd, movie);
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found");
                    }
                }
            }
            try (PreparedStatement statement2 = conn.prepareStatement(GET_RATINGS_INFORMATION)) {
                statement2.setString(1, movieId);
                try (ResultSet rs = statement2.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rs.next()) {
                        JSONObject ratings = new JSONObject();
                        insertResult(rs, rsmd, ratings);
                        movie.put("ratings", ratings);
                    }
                }
            }
            try (PreparedStatement statement3 = conn.prepareStatement(GET_STARS_INFORMATION)) {
                statement3.setString(1, movieId);
                try (ResultSet rs = statement3.executeQuery()) {
                    JSONArray stars = new JSONArray();
                    ResultSetMetaData rsmd = rs.getMetaData();
                    while (rs.next()) {
                        JSONObject thisStar = new JSONObject();
                        insertResult(rs, rsmd, thisStar);
                        stars.put(thisStar);
                    }
                    movie.put("stars", stars);
                }
            }
            try (PreparedStatement statement4 = conn.prepareStatement(GET_GENRES_INFORMATION)) {
                statement4.setString(1, movieId);
                try (ResultSet rs = statement4.executeQuery()) {
                    JSONArray genres = new JSONArray();
                    ResultSetMetaData rsmd = rs.getMetaData();
                    while (rs.next()) {
                        JSONObject thisGenre = new JSONObject();
                        insertResult(rs, rsmd, thisGenre);
                        genres.put(thisGenre);
                    }
                    movie.put("genres", genres);
                }
            }
            // Write movie object to response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(movie.toString());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid movie ID");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error: " + e.getMessage());
        }
    }

    private static boolean isValidPath(String pathInfo) {
        return !(pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/"));
    }

    protected void insertResult(ResultSet rs, ResultSetMetaData rsmd, JSONObject obj) throws SQLException {
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String col = rsmd.getColumnName(i);
            Object val = rs.getObject(i);
            obj.put(col, val);
        }
    }

}