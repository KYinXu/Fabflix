import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setMimeType(response);
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Invalidate the session to clear all session data
            session.invalidate();
        }
        
        // Create success response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "success");
        jsonResponse.put("message", "Logged out successfully");
        
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse.toString());
        writer.flush();
        writer.close();
    }

    protected void setMimeType(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }
}
