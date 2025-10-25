import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/star/*")
public class StarServlet extends HttpServlet {
    /**
     * Endpoints for querying star information for a detailed view
     */
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        String GET_STAR_BY_ID = """
                SELECT *
                FROM stars
                WHERE id= ?
                """;
        String GET_MOVIES_INFORMATION = """
                SELECT m.*
                FROM movies m
                INNER JOIN stars_in_movies sm ON m.id = sm.movie_id
                WHERE sm.star_id = ?
                """;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //Install mySQL driver
            //ResponseUtils.setCommonHeaders(response);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String pathInfo = request.getPathInfo();
        if (!isValidPath(pathInfo)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No star ID provided");
            return;
        }
        String starId = pathInfo.substring(1);
        try (Connection conn = DriverManager.getConnection(
                "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                Parameters.username,
                Parameters.password)) {
            JSONObject star = new JSONObject();
            try (PreparedStatement statement1 = conn.prepareStatement(GET_STAR_BY_ID)) {
                statement1.setString(1, starId);
                try (ResultSet rs = statement1.executeQuery()) {
                    if (rs.next()) {
                        insertResult(rs, rs.getMetaData(), star);
                    }
                }
            }
            try (PreparedStatement statement2 = conn.prepareStatement(GET_MOVIES_INFORMATION)) {
                statement2.setString(1, starId);
                JSONArray movies = new JSONArray();
                try (ResultSet rs = statement2.executeQuery()) {
                    while (rs.next()) {
                        JSONObject thisMovie = new JSONObject();
                        insertResult(rs, rs.getMetaData(), thisMovie);
                        movies.put(thisMovie);
                    }
                    star.put("movies", movies);
                }
            }
            // Write movie object to response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write(star.toString());
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid star ID");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error");
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
