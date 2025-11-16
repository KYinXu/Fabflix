package MongoDBMigration.models;

import java.util.Date;

/**
 * MongoDB document model for sales transactions
 */
public class MongoSale {
    
    private String id; // MongoDB ObjectId
    private String customerId; // Reference to customer ObjectId
    private String movieId; // Movie ID
    private Date saleDate;
    private double price;
    
    public MongoSale() {
    }
    
    // TODO: Add getters and setters
}

