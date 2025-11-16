package MongoDBMigration.models;

import java.util.Date;

/**
 * MongoDB document model for customers
 * Embeds credit card information
 */
public class MongoCustomer {
    
    private String id; // MongoDB ObjectId
    private int mysqlId; // Original MySQL ID for reference
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private CreditCard creditCard;
    
    public MongoCustomer() {
    }
    
    // TODO: Add getters and setters
    
    /**
     * Nested class for embedded credit card information
     */
    public static class CreditCard {
        private String id;
        private String firstName;
        private String lastName;
        private Date expiration;
        
        // TODO: Add getters and setters
    }
}

