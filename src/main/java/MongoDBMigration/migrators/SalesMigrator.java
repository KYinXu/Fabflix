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
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATING SALES");
        System.out.println("=".repeat(60) + "\n");
        
        Connection conn = null;
        
        try {
            // Connect to MySQL
            conn = mysqlConfig.getConnection();
            System.out.println("✓ Connected to MySQL");
            
            // Get MongoDB collection
            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> collection = database.getCollection(getCollectionName());
            
            // Clear existing data
            collection.drop();
            System.out.println("✓ Cleared existing MongoDB collection");
            
            // Get total count
            long totalSales = getSourceCount();
            long effectiveLimit = getEffectiveLimit(totalSales);
            
            if (migrationLimit != null && migrationLimit < totalSales) {
                System.out.println("✓ Total sales available: " + totalSales);
                System.out.println("✓ Migration limit set to: " + effectiveLimit);
                System.out.println("✓ Migrating first " + effectiveLimit + " sales\n");
            } else {
                System.out.println("✓ Total sales to migrate: " + totalSales + "\n");
            }
            
            // Migrate in batches
            int offset = 0;
            int processed = 0;
            
            while (offset < effectiveLimit) {
                // Calculate how many records to fetch in this batch
                int batchLimit = (int) Math.min(batchSize, effectiveLimit - offset);
                
                List<Document> batch = migrateSaleBatch(conn, offset, batchLimit);
                
                if (!batch.isEmpty()) {
                    collection.insertMany(batch);
                    processed += batch.size();
                    logProgress(processed, effectiveLimit);
                }
                
                offset += batchSize;
            }
            
            System.out.println("\n✓ Migration complete!");
            System.out.println("  Migrated: " + processed + " sales");
            
        } finally {
            if (conn != null) {
                mysqlConfig.closeConnection(conn);
            }
        }
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
    public long getDestinationCount() throws Exception {
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
    
    /**
     * Main method to run sales migration
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FABFLIX SALES MIGRATION");
        System.out.println("=".repeat(60));
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Create migrator
            SalesMigrator migrator = new SalesMigrator(mysqlConfig, mongoConfig);
            
            // Run migration
            migrator.migrate();
            
            // Validate
            migrator.validate();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Sales migration completed successfully!");
            System.out.println("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Sales migration failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
