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
    private static final String SELECT_MOVIE_BY_ID = "SELECT * FROM movies WHERE id = ?";

    private static void addCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void writeMovieToResponse(HttpServletResponse response, JSONObject movie) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(movie.toString());
    }

    private static JSONObject resultSetToMovieJson(ResultSet resultSet) throws SQLException {
        JSONObject movie = new JSONObject();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            String column = resultSetMetaData.getColumnName(i);
            Object value = resultSet.getObject(i);
            movie.put(column, value);
        }
        return movie;
    }

    private static boolean pathInfoIsInvalid(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, RuntimeException {
        addCORSHeaders(response);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //Install mySQL driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (pathInfoIsInvalid(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No movie ID provided");
            return;
        }
        String movieId = request.getPathInfo().substring(1); // removes the leading '/'
        try (Connection connection = DriverManager.getConnection(
                "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname + "?autoReconnect=true&useSSL=false",
                Parameters.username,
                Parameters.password);
             PreparedStatement statement = connection.prepareStatement(SELECT_MOVIE_BY_ID)) {
            statement.setString(1, movieId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    JSONObject movie = resultSetToMovieJson(resultSet);
                    writeMovieToResponse(response, movie);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found");
                }
            }
        } catch (Exception e) {
            //TODO: Handle specific errors and provide detailed logging
            throw new IOException(e);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Handle preflight requests
        addCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}