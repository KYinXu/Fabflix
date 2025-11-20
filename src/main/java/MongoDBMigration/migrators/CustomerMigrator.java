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
 * Migrates customers from MySQL to MongoDB
 * Embeds credit card information for denormalized access
 */
public class CustomerMigrator extends BaseMigrator {
    
    public CustomerMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        super(mysqlConfig, mongoConfig);
    }
    
    @Override
    public void migrate() throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATING CUSTOMERS");
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
            long totalCustomers = getSourceCount();
            long effectiveLimit = getEffectiveLimit(totalCustomers);
            
            if (migrationLimit != null && migrationLimit < totalCustomers) {
                System.out.println("✓ Total customers available: " + totalCustomers);
                System.out.println("✓ Migration limit set to: " + effectiveLimit);
                System.out.println("✓ Migrating first " + effectiveLimit + " customers\n");
            } else {
                System.out.println("✓ Total customers to migrate: " + totalCustomers + "\n");
            }
            
            // Migrate in batches
            int offset = 0;
            int processed = 0;
            
            while (offset < effectiveLimit) {
                // Calculate how many records to fetch in this batch
                int batchLimit = (int) Math.min(batchSize, effectiveLimit - offset);
                
                List<Document> batch = migrateCustomerBatch(conn, offset, batchLimit);
                
                if (!batch.isEmpty()) {
                    collection.insertMany(batch);
                    processed += batch.size();
                    logProgress(processed, effectiveLimit);
                }
                
                offset += batchSize;
            }
            
            System.out.println("\n✓ Migration complete!");
            System.out.println("  Migrated: " + processed + " customers");
            
        } finally {
            if (conn != null) {
                mysqlConfig.closeConnection(conn);
            }
        }
    }
    
    @Override
    public boolean validate() throws Exception {
        System.out.println("\nValidating customer migration...");
        
        long sourceCount = getSourceCount();
        long destCount = getDestinationCount();
        
        System.out.println("  MySQL customers:   " + sourceCount);
        System.out.println("  MongoDB customers: " + destCount);
        
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
        return "customers";
    }
    
    @Override
    public long getSourceCount() throws Exception {
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM customers")) {
            
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
        return "customers";
    }
    
    /**
     * Migrate a batch of customers with embedded credit card info
     */
    private List<Document> migrateCustomerBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> customers = new ArrayList<>();
        
        // Join with credit_cards table
        String query = "SELECT c.id, c.first_name, c.last_name, c.email, c.password, c.address, " +
                      "cc.id as cc_id, cc.first_name as cc_first_name, cc.last_name as cc_last_name, " +
                      "cc.expiration " +
                      "FROM customers c " +
                      "LEFT JOIN credit_cards cc ON c.credit_card_id = cc.id " +
                      "LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int customerId = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String address = rs.getString("address");
                
                // Create customer document
                Document customerDoc = new Document()
                    .append("mysqlId", customerId)  // Keep original MySQL ID
                    .append("firstName", firstName)
                    .append("lastName", lastName)
                    .append("email", email)
                    .append("password", password)
                    .append("address", address);
                
                // Embed credit card info if exists
                String ccId = rs.getString("cc_id");
                if (ccId != null) {
                    Document creditCard = new Document()
                        .append("id", ccId)
                        .append("firstName", rs.getString("cc_first_name"))
                        .append("lastName", rs.getString("cc_last_name"))
                        .append("expiration", rs.getDate("expiration"));
                    
                    customerDoc.append("creditCard", creditCard);
                }
                
                customers.add(customerDoc);
            }
        }
        
        return customers;
    }
    
    /**
     * Main method to run customer migration
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FABFLIX CUSTOMER MIGRATION");
        System.out.println("=".repeat(60));
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Create migrator
            CustomerMigrator migrator = new CustomerMigrator(mysqlConfig, mongoConfig);
            
            // Run migration
            migrator.migrate();
            
            // Validate
            migrator.validate();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Customer migration completed successfully!");
            System.out.println("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Customer migration failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
