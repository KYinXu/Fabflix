import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.MongoDBConnectionConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.sql.*;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.bson.Document;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"}) // Allows Tomcat to Interpret URL
public class LoginServlet extends HttpServlet {

    private MongoDBConnectionConfig mongoConfig;

    @Override
    public void init() {
        mongoConfig = new MongoDBConnectionConfig();
    }

    public static final String SECRET_KEY ="6Le3eAIsAAAAAKigdJPFrRk4teMKT1k9bBntTiZR";
    public static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    protected static boolean recaptchaVerification(String gRecaptchaResponse) throws IOException {
        URL verifyURL = new URL(SITE_VERIFY_URL);
        HttpsURLConnection verificationConnection = (HttpsURLConnection) verifyURL.openConnection();
        verificationConnection.setRequestMethod("POST");
        verificationConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
        verificationConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String postParams = "secret=" + SECRET_KEY + "&response=" + gRecaptchaResponse;
        // Send Request
        verificationConnection.setDoOutput(true);

        // Send data to the ReCaptcha Server
        try (OutputStream outStream = verificationConnection.getOutputStream()) {
            outStream.write(postParams.getBytes());
            outStream.flush();
        }

        // Read data sent from the server.
        try (InputStream inputStream = verificationConnection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
            JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);
            return jsonObject.get("success").getAsBoolean();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String jsonString = buildJSONString(request).toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        String email = jsonObject.getString("email");
        String password = jsonObject.getString("password");
        String gRecaptchaResponse = jsonObject.getString("g-recaptcha-response");
        setMimeType(response);

        boolean recaptchaToken = recaptchaVerification(gRecaptchaResponse);

        MongoDatabase databaseConnection = establishDatabaseConnection();
        boolean existenceFlag = false;

        if (!recaptchaToken) {
            JSONObject failedRecaptchaJSON = new JSONObject();
            failedRecaptchaJSON.put("status", "recaptcha-failure");
            PrintWriter reactOutput = response.getWriter();
            reactOutput.write(failedRecaptchaJSON.toString());
            reactOutput.flush();
            reactOutput.close();
        }
        else {
            try {
                MongoCollection<Document> collection = databaseConnection.getCollection("customers");
                Document customerDocument = collection.find(new Document("email", email)).first();

                if (customerDocument != null) {
                    String encryptedPassword = customerDocument.getString("password");
                    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
                    existenceFlag = passwordEncryptor.checkPassword(password, encryptedPassword);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (existenceFlag) {
                HttpSession session = request.getSession(true);
                session.setAttribute("email", email);
                session.setMaxInactiveInterval(30 * 60);
            }

            JSONObject jsonSuccessStatus = buildJSONSuccess(existenceFlag);
            PrintWriter reactOutput = response.getWriter();
            reactOutput.write(jsonSuccessStatus.toString());
            reactOutput.flush();
            reactOutput.close();
        }
    }

    protected MongoDatabase establishDatabaseConnection(){
        try {
            MongoDatabase database = mongoConfig.getDatabase();
            return database;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    protected void setMimeType(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    protected JSONObject buildJSONSuccess(boolean success){
        JSONObject jsonSuccessStatus = new JSONObject();
        if (success){
            jsonSuccessStatus.put("status", "success");
        }
        else{
            jsonSuccessStatus.put("status", "failure");
        }
        return jsonSuccessStatus;
    }

    protected StringBuilder buildJSONString(HttpServletRequest request) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return jsonString;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        if (mongoConfig != null) {
            mongoConfig.closeConnection();
        }
    }
}
