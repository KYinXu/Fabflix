import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebFilter(filterName = "SessionFilter", urlPatterns = "/*")
public class SessionFilter implements Filter {
    private ServletContext servletContext;
    private final List<String> allowedURIs = new ArrayList<>();

    public void init(FilterConfig fConfig) {
        this.servletContext = fConfig.getServletContext();
        this.servletContext.log("Session Filter Initialized");
        allowedURIs.add("/login");
        allowedURIs.add("/api/login");
        allowedURIs.add("/fabflix_war_exploded/login");
        allowedURIs.add("/logout");
        allowedURIs.add("/api/logout");
        allowedURIs.add("/fabflix_war_exploded/logout");
        allowedURIs.add(".css");
        allowedURIs.add(".js");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        addCORSHeader(servletResponse);

        if ("OPTIONS".equals(servletRequest.getMethod())) {
            servletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        if (isUrlAllowedWithoutSession(servletRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = servletRequest.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            this.servletContext.log("Unauthorized Access Denied - URI: " + servletRequest.getRequestURI() + 
                ", Session: " + (session != null ? "exists" : "null") + 
                ", Email: " + (session != null ? session.getAttribute("email") : "no session"));
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isUrlAllowedWithoutSession(String requestURI) {
        return true;
    }

    private void addCORSHeader(HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Vary", "Origin");
    }
}
