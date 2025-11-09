import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "MetadataServlet", urlPatterns = {"/metadata"})
public class MetadataServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject result = new JSONObject();
        JSONArray tablesArray = new JSONArray();

        try (Connection conn = establishDatabaseConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                JSONObject tableObj = new JSONObject();
                tableObj.put("name", tableName);

                JSONArray columnsArray = new JSONArray();
                ResultSet columns = metaData.getColumns(null, null, tableName, "%");
                while (columns.next()) {
                    JSONObject columnObj = new JSONObject();
                    columnObj.put("name", columns.getString("COLUMN_NAME"));
                    columnObj.put("type", columns.getString("TYPE_NAME"));
                    columnsArray.put(columnObj);
                }
                columns.close();

                tableObj.put("columns", columnsArray);
                tablesArray.put(tableObj);
            }
            tables.close();
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getMessage());
        }

        result.put("tables", tablesArray);
        PrintWriter out = response.getWriter();
        out.write(result.toString());
        out.close();
    }

    private Connection establishDatabaseConnection() throws Exception {
        String loginUser = Parameters.username;
        String loginPassword = Parameters.password;
        String loginUrl = "jdbc:" + Parameters.dbtype + ":///" + Parameters.dbname +
                "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(loginUrl, loginUser, loginPassword);
    }
}
