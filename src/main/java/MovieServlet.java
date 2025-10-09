import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;

/**
 * Endpoints for querying movie information for a detailed view
 */
@WebServlet("/movie/*")
public class MovieServlet extends HttpServlet {
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        // Add CORS headers
        doOptions(request, response);
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //Install mySQL driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


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
                Parameters.password)) {
            // movie object to store query response information
            JSONObject movie = new JSONObject();

            // Execute first query to get movie information
            String query1 = "SELECT * FROM movies m WHERE id = ? ";
            try (PreparedStatement st1 = conn.prepareStatement(query1)) { // create prepared statement
                st1.setString(1, movieId); // insert movieId into query statement

                try (ResultSet rs = st1.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData(); // get col names
                    if (rs.next()) { // query returned an object
                        insertResult(rs, rsmd, movie);
                    } else { // the query result is empty
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found");
                    }
                }
            }

            // Execute second query to get rating information
            String query2 = "SELECT ratings, vote_count FROM ratings WHERE movie_id = ?";
            try (PreparedStatement st2 = conn.prepareStatement(query2)) {
                st2.setString(1, movieId);

                try (ResultSet rs = st2.executeQuery()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rs.next()) {
                        JSONObject ratings = new JSONObject();
                        insertResult(rs, rsmd, ratings);
                        movie.put("ratings", ratings);
                    }
                }
            }

            // Execute third query to get cast information
            String query3 = "SELECT s.* FROM stars s INNER JOIN stars_in_movies sm ON s.id = sm.star_id WHERE sm.movie_id = ?";
            try (PreparedStatement st3 = conn.prepareStatement(query3)) {
                st3.setString(1, movieId);
                try (ResultSet rs = st3.executeQuery()) {
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

            // Execute fourth query to get genre information
            String query4 = "SELECT g.* FROM genres g INNER JOIN genres_in_movies gm ON g.id = gm.genre_id WHERE gm.movie_id = ?";
            try (PreparedStatement st4 = conn.prepareStatement(query4)) {
                st4.setString(1, movieId);
                try (ResultSet rs = st4.executeQuery()) {
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
        } catch (Exception e) {
            //TODO: Handle specific errors and provide detailed logging
            throw new IOException(e);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Handle preflight requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }


    protected void insertResult(ResultSet rs, ResultSetMetaData rsmd, JSONObject obj) throws SQLException {
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String col = rsmd.getColumnName(i);
            Object val = rs.getObject(i);
            obj.put(col, val);
        }
    }

}