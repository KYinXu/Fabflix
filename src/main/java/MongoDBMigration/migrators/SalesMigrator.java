package MongoDBMigration.migrators;

import MongoDBMigration.config.MySQLConnectionConfig;
import MongoDBMigration.config.MongoDBConnectionConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrates sales from MySQL to MongoDB
 * Includes denormalized customer and movie information for reporting
 */
public class SalesMigrator extends BaseMigrator {
    
    public SalesMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        super(mysqlConfig, mongoConfig);
    }
    
    @Override
    public void migrate() throws Exception {
        MigrationContext context = null;
        
        try {
            // Setup: establish connections, prepare collection
            context = setupMigration();
            logMigrationStart(context);
            
            // Execute migration: process in batches
            processBatchedMigration(context);
            
            // Complete: log results
            logMigrationComplete(context);
            
        } finally {
            closeMigrationContext(context);
        }
    }
    
    /**
     * Process sales in batches
     * Separated for reusability and clarity
     */
    private void processBatchedMigration(MigrationContext context) throws Exception {
        int offset = 0;
        
        while (offset < context.effectiveLimit) {
            // Calculate batch limit
            int batchLimit = calculateBatchLimit(offset, context.effectiveLimit);
            
            // Fetch and transform batch
            List<Document> batch = fetchAndTransformSaleBatch(context, offset, batchLimit);
            
            // Insert batch
            if (!batch.isEmpty()) {
                performBatchInsert(context.mongoCollection, batch);
                context.processedCount += batch.size();
                logProgress(context.processedCount, context.effectiveLimit);
            }
            
            offset += batchSize;
        }
    }
    
    /**
     * Fetch and transform a single batch of sales
     * Separated for reusability and testing
     */
    private List<Document> fetchAndTransformSaleBatch(MigrationContext context, int offset, int limit) 
            throws Exception {
        return migrateSaleBatch(context.sqlConnection, offset, limit);
    }
    
    @Override
    public boolean validate() throws Exception {
        System.out.println("\nValidating sales migration...");
        
        long sourceCount = getSourceCount();
        long destCount = getDestinationCount();
        
        System.out.println("  MySQL sales:   " + sourceCount);
        System.out.println("  MongoDB sales: " + destCount);
        
        boolean valid = sourceCount == destCount;
        
        if (valid) {
            System.out.println("✓ Validation passed: counts match");
        } else {
            System.out.println("✗ Validation failed: counts don't match");
        }
        
        return valid;
    }
    
    @Override
    public String getCollectionName() {
        return "sales";
    }
    
    @Override
    public long getSourceCount() throws Exception {
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM sales")) {
            
            if (rs.next()) {
                return rs.getLong("count");
            }
        }
        return 0;
    }
    
    @Override
    public long getDestinationCount() {
        MongoDatabase database = mongoConfig.getDatabase();
        MongoCollection<Document> collection = database.getCollection(getCollectionName());
        return collection.countDocuments();
    }
    
    @Override
    protected String getSourceTableName() {
        return "sales";
    }
    
    /**
     * Migrate a batch of sales with denormalized customer and movie info
     */
    private List<Document> migrateSaleBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> sales = new ArrayList<>();
        
        // Join with customers and movies for denormalized data
        String query = "SELECT s.id, s.customer_id, s.movie_id, s.sale_date, " +
                      "c.first_name, c.last_name, c.email, " +
                      "m.title " +
                      "FROM sales s " +
                      "LEFT JOIN customers c ON s.customer_id = c.id " +
                      "LEFT JOIN movies m ON s.movie_id = m.id " +
                      "LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int saleId = rs.getInt("id");
                int customerId = rs.getInt("customer_id");
                String movieId = rs.getString("movie_id");
                java.sql.Date saleDate = rs.getDate("sale_date");
                
                // Denormalized customer info
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                
                // Denormalized movie info
                String movieTitle = rs.getString("title");
                
                // Create sale document
                Document saleDoc = new Document()
                    .append("mysqlId", saleId)  // Keep original MySQL ID
                    .append("customerId", customerId)
                    .append("movieId", movieId)
                    .append("saleDate", saleDate)
                    .append("price", 9.99);  // Default price - adjust as needed
                
                // Add denormalized fields for reporting
                if (firstName != null && lastName != null) {
                    saleDoc.append("customerName", firstName + " " + lastName);
                }
                if (email != null) {
                    saleDoc.append("customerEmail", email);
                }
                if (movieTitle != null) {
                    saleDoc.append("movieTitle", movieTitle);
                }
                
                sales.add(saleDoc);
            }
        }
        
        return sales;
    }
}
