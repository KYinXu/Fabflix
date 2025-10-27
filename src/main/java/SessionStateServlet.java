import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "SessionStateServlet", urlPatterns = {"/api/session-state"})
public class SessionStateServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        setMimeType(response);
        addCORSHeader(response);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
        }

        JSONObject jsonObject = new JSONObject(jsonString.toString());

        // Save movie list state to session
        session.setAttribute("browseType", jsonObject.getString("browseType"));
        session.setAttribute("selectedLetter", jsonObject.getString("selectedLetter"));
        session.setAttribute("selectedGenreId", jsonObject.optInt("selectedGenreId", -1));
        session.setAttribute("searchState", jsonObject.getJSONObject("searchState").toString());
        session.setAttribute("sortCriteria", jsonObject.optString("sortCriteria", "r.ratings"));
        session.setAttribute("sortOrder", jsonObject.optString("sortOrder", "DESC"));
        session.setAttribute("pageSize", jsonObject.optInt("pageSize", 25));
        session.setAttribute("currentPage", jsonObject.optInt("currentPage", 0));

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "success");
        jsonResponse.put("message", "State saved successfully");

        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse.toString());
        writer.flush();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws  IOException {
        setMimeType(response);
        addCORSHeader(response);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }

        JSONObject state = new JSONObject();
        state.put("browseType", session.getAttribute("browseType"));
        state.put("selectedLetter", session.getAttribute("selectedLetter"));
        
        Object genreIdObj = session.getAttribute("selectedGenreId");
        if (genreIdObj instanceof Integer) {
            state.put("selectedGenreId", genreIdObj);
        } else {
            state.put("selectedGenreId", JSONObject.NULL);
        }

        String searchStateJson = (String) session.getAttribute("searchState");
        if (searchStateJson != null) {
            state.put("searchState", new JSONObject(searchStateJson));
        } else {
            state.put("searchState", new JSONObject());
        }

        state.put("sortCriteria", session.getAttribute("sortCriteria"));
        state.put("sortOrder", session.getAttribute("sortOrder"));
        state.put("pageSize", session.getAttribute("pageSize"));
        state.put("currentPage", session.getAttribute("currentPage"));

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "success");
        jsonResponse.put("state", state);

        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse.toString());
        writer.flush();
    }

    protected void setMimeType(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    protected void addCORSHeader(HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Vary", "Origin");
    }
}
