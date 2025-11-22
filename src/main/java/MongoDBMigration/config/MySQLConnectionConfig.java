package MongoDBMigration.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuration and connection management for MySQL database
 */
public class MySQLConnectionConfig {
    
    private String url;
    private String username;
    private String password;
    
    public MySQLConnectionConfig() {
        // Load parameters from Parameters interface (default package)
        try {
            Class<?> params = Class.forName("Parameters");
            String dbname = (String) params.getField("dbname").get(null);
            this.username = (String) params.getField("username").get(null);
            this.password = (String) params.getField("password").get(null);
            this.url = "jdbc:mysql://localhost:3306/" + dbname + 
                       "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
        } catch (Exception e) {
            throw new RuntimeException("Cannot load MySQL parameters", e);
        }
        
        // Load MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * Get MySQL connection
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Close MySQL connection properly
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing MySQL connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test MySQL connectivity
     */
    public void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("MySQL connection successful!");
            }
        } catch (SQLException e) {
            System.err.println("MySQL connection failed: " + e.getMessage());
            throw new RuntimeException("Cannot connect to MySQL", e);
    }
}
}
