import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.CartItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = {"/cart"}) // Allows Tomcat to Interpret URL
public class ShoppingCartServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try{
            HttpSession currentSession = request.getSession(true);
            Map<String, CartItem> shoppingCart = (Map<String, CartItem>) currentSession.getAttribute("cart");

            if (shoppingCart == null) {
                shoppingCart = new HashMap<>();
                currentSession.setAttribute("cart", shoppingCart);
            }

            String jsonString = buildJSONString(request).toString();

            JSONArray moviesArray = new JSONArray(jsonString);

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject currentMovie = moviesArray.getJSONObject(i);
                String movieId = currentMovie.getString("movieId");
                String title = currentMovie.getString("title");
                int quantity = currentMovie.getInt("quantity");

                if (shoppingCart.containsKey(movieId)) {
                    CartItem existingItem = shoppingCart.get(movieId);
                    existingItem.increaseQuantity();
                }
                else{
                    double price = Math.floor((5 + (new Random().nextDouble() * 25)) * 100) / 100;
                    CartItem movie = new CartItem(movieId, title, price, quantity);
                    shoppingCart.put(movieId, movie);
                }
            }

            double totalPrice = 0;
            for (CartItem item : shoppingCart.values()) {
                totalPrice += item.getPrice() * item.getQuantity();
            }

            JSONObject cartResponse = buildJSONCartResponse(shoppingCart, totalPrice);

            setMimeType(response);
            PrintWriter reactOutput = response.getWriter();
            reactOutput.write(cartResponse.toString());
            reactOutput.flush();
        } catch(Exception e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession currentSession = request.getSession(false);
        if (currentSession == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No session found");
            return;
        }

        Map<String, CartItem> shoppingCart = (Map<String, CartItem>) currentSession.getAttribute("cart");
        if (shoppingCart == null) {
            shoppingCart = new HashMap<>();
        }

        double totalPrice = 0;
        for (CartItem item : shoppingCart.values()) {
            totalPrice += item.getPrice() * item.getQuantity();
        }

        JSONObject cartResponse = new JSONObject();
        JSONObject currentCart = new JSONObject();

        for (Map.Entry<String, CartItem> entry : shoppingCart.entrySet()) {
            CartItem item = entry.getValue();
            JSONObject itemJson = new JSONObject();
            itemJson.put("movieId", item.getMovieId());
            itemJson.put("title", item.getTitle());
            itemJson.put("price", item.getPrice());
            itemJson.put("quantity", item.getQuantity());
            currentCart.put(entry.getKey(), itemJson);
        }

        cartResponse.put("Current Cart", currentCart);
        cartResponse.put("Total Price", totalPrice);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write(cartResponse.toString());
        out.flush();
    }

    protected JSONObject buildJSONCartResponse(Map<String, CartItem> shoppingCart, double totalPrice){
        JSONObject cartResponse = new JSONObject();
        JSONObject currentCart = new JSONObject();

        for (Map.Entry<String, CartItem> entry : shoppingCart.entrySet()) {
            CartItem item = entry.getValue();

            JSONObject itemJson = new JSONObject();
            itemJson.put("movieId", item.getMovieId());
            itemJson.put("title", item.getTitle());
            itemJson.put("price", item.getPrice());
            itemJson.put("quantity", item.getQuantity());

            currentCart.put(entry.getKey(), itemJson);
        }

        cartResponse.put("Current Cart", currentCart);
        cartResponse.put("Total Price", totalPrice);
        return cartResponse;
    }

    protected void setMimeType(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
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
}
