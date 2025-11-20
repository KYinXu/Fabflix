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
        MigrationContext context = null;
        try {
            context = setupMigration();
            logMigrationStart(context);
            processBatchedMigration(context);
            logMigrationComplete(context);
        } finally {
            closeMigrationContext(context);
        }
    }
    
    private void processBatchedMigration(MigrationContext context) throws Exception {
        int offset = 0;
        while (offset < context.effectiveLimit) {
            int batchLimit = calculateBatchLimit(offset, context.effectiveLimit);
            List<Document> batch = fetchAndTransformCustomerBatch(context, offset, batchLimit);
            if (!batch.isEmpty()) {
                performBatchInsert(context.mongoCollection, batch);
                context.processedCount += batch.size();
                logProgress(context.processedCount, context.effectiveLimit);
            }
            offset += batchSize;
        }
    }
    
    private List<Document> fetchAndTransformCustomerBatch(MigrationContext context, int offset, int limit) 
            throws Exception {
        return migrateCustomerBatch(context.sqlConnection, offset, limit);
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
    public long getDestinationCount() {
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
}
