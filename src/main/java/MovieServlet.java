// Naming convention figured out later

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

/**
 * Endpoints for querying movie information for a detailed view
 */

@WebServlet("/movie/*")
public class MovieServlet extends HttpServlet {
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //Install mySQL driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String query = "SELECT * FROM movies WHERE id = '?'";
        String pathInfo = request.getPathInfo();
        String movieId;

        // Parse path and initialize movieId
        if (pathInfo == null || pathInfo.isEmpty()|| pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No movie ID provided");
            return;
        }
        movieId = pathInfo.substring(1); // removes the leading '/'

        // Establish connection and prepare query statement
        try (Connection conn = DriverManager.getConnection(
                "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                Parameters.username,
                Parameters.password);
             PreparedStatement st = conn.prepareStatement(query)) {

            st.setString(1, movieId); // insert movieId into query statement

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) { // query returned an object

                    JSONObject movie = new JSONObject();
                    ResultSetMetaData rsmd = rs.getMetaData(); // get col names

                    // Iterate through results and add to the movie object
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        String col = rsmd.getColumnName(i);
                        Object val = rs.getObject(i);
                        movie.put(col, val);
                    }

                    // Write movie object to response
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(movie.toString());

                } else { // the query result is empty
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found");
                    return;
                }
            }
        } catch (Exception e) {
            //TODO: Handle specific errors and provide detailed logging
            throw new IOException(e);
        }
    }
}