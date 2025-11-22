package MongoDBMigration.migrators;

import MongoDBMigration.config.MySQLConnectionConfig;
import config.MongoDBConnectionConfig;
import MongoDBMigration.utils.MigrationOptimizer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Abstract base class for all migrators
 * Provides common functionality and migration lifecycle methods
 */
public abstract class BaseMigrator {
    
    protected MySQLConnectionConfig mysqlConfig;
    protected MongoDBConnectionConfig mongoConfig;
    protected int batchSize;
    protected Long migrationLimit; // Optional limit for testing/partial migrations
    protected boolean enableBulkWrite; // Enable MongoDB bulk write optimization
    
    public BaseMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        this.mysqlConfig = mysqlConfig;
        this.mongoConfig = mongoConfig;
        this.batchSize = 1000; // Default batch size - optimal for most cases
        this.migrationLimit = null; // No limit by default
        this.enableBulkWrite = true; // Enable bulk writes by default
    }
    
    /**
     * Set a limit on the number of records to migrate
     * Useful for testing or partial migrations
     * @param limit Maximum number of records to migrate, or null for no limit
     */
    public void setMigrationLimit(Long limit) {
        this.migrationLimit = limit;
    }
    
    /**
     * Enable or disable bulk write optimization
     * @param enable true to enable bulk writes
     */
    public void setEnableBulkWrite(boolean enable) {
        this.enableBulkWrite = enable;
    }
    
    /**
     * Get the effective migration limit considering both total count and set limit
     * @param totalCount Total available records
     * @return The number of records to actually migrate
     */
    protected long getEffectiveLimit(long totalCount) {
        if (migrationLimit != null && migrationLimit < totalCount) {
            return migrationLimit;
        }
        return totalCount;
    }
    
    /**
     * Execute the migration for this specific entity
     */
    public abstract void migrate() throws Exception;
    
    /**
     * Validate the migrated data
     */
    public abstract boolean validate() throws Exception;
    
    /**
     * Get the name of the collection being migrated
     */
    public abstract String getCollectionName();
    
    /**
     * Get total count from MySQL source
     */
    public abstract long getSourceCount() throws Exception;
    
    /**
     * Get total count from MongoDB destination
     */
    public long getDestinationCount() {
        MongoDatabase database = mongoConfig.getDatabase();
        MongoCollection<Document> collection = database.getCollection(getCollectionName());
        return collection.countDocuments();
    }
    
    /**
     * Log migration progress
     */
    protected void logProgress(int processed, long total) {
        if (processed % 100 == 0 || processed == total) {
            double percentage = (total > 0) ? (processed * 100.0 / total) : 0;
            System.out.printf("[%s] Progress: %d/%d (%.2f%%)\n", 
                getCollectionName(), processed, total, percentage);
        }
    }
    
    /**
     * Test MongoDB connection and insert a test document
     */
    public void testMongoConnection() {
        System.out.println("\n=== Testing MongoDB Connection ===");
        try {
            // Get database
            MongoDatabase database = mongoConfig.getDatabase();
            System.out.println("✓ Connected to MongoDB database: " + database.getName());
            
            // Get or create test collection
            String testCollection = "test_" + getCollectionName();
            MongoCollection<Document> collection = database.getCollection(testCollection);
            System.out.println("✓ Accessing collection: " + testCollection);
            
            // Insert test document
            Document testDoc = new Document()
                .append("test", true)
                .append("message", "Test document from " + getClass().getSimpleName())
                .append("timestamp", System.currentTimeMillis())
                .append("collection", getCollectionName());
            
            collection.insertOne(testDoc);
            System.out.println("✓ Successfully inserted test document");
            System.out.println("  Document ID: " + testDoc.get("_id"));
            
            // Count documents
            long count = collection.countDocuments();
            System.out.println("✓ Total documents in test collection: " + count);
            
            // Read back the document
            Document retrieved = collection.find(new Document("_id", testDoc.get("_id"))).first();
            if (retrieved != null) {
                System.out.println("✓ Successfully retrieved test document");
                System.out.println("  Message: " + retrieved.getString("message"));
            }
            
            // Clean up test document
            collection.deleteOne(new Document("_id", testDoc.get("_id")));
            System.out.println("✓ Cleaned up test document");
            
            System.out.println("\n=== MongoDB Connection Test: SUCCESS ===\n");
            
        } catch (Exception e) {
            System.err.println("\n✗ MongoDB Connection Test: FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test MySQL connection and query
     */
    public void testMySQLConnection(String testQuery) {
        System.out.println("\n=== Testing MySQL Connection ===");
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(testQuery)) {
            
            System.out.println("✓ Connected to MySQL database");
            System.out.println("✓ Query executed successfully: " + testQuery);
            
            if (rs.next()) {
                System.out.println("✓ Retrieved data from MySQL");
            }
            
            System.out.println("\n=== MySQL Connection Test: SUCCESS ===\n");
            
        } catch (Exception e) {
            System.err.println("\n✗ MySQL Connection Test: FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run full connection test (both MySQL and MongoDB)
     */
    public void runConnectionTest() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  CONNECTION TEST: " + getClass().getSimpleName());
        System.out.println("=".repeat(50));
        
        // Test MongoDB
        testMongoConnection();
        
        // Test MySQL with a simple query
        String query = "SELECT COUNT(*) as count FROM " + getSourceTableName();
        testMySQLConnection(query);
        
        System.out.println("=".repeat(50) + "\n");
    }
    
    /**
     * Get the source table name from MySQL
     * Override this in subclasses
     */
    protected String getSourceTableName() {
        // Default implementation - override in subclasses
        return getCollectionName();
    }
    
    // ========== REUSABLE MIGRATION COMPONENTS ==========
    
    /**
     * Inner class to hold migration context and statistics
     */
    protected static class MigrationContext {
        public Connection sqlConnection;
        public MongoCollection<Document> mongoCollection;
        public long totalCount;
        public long effectiveLimit;
        public long startTime;
        public int processedCount;
        
        public MigrationContext() {
            this.startTime = System.currentTimeMillis();
            this.processedCount = 0;
        }
    }
    
    /**
     * Setup migration - establishes connections, prepares collection
     * @return MigrationContext with all necessary connections and info
     */
    protected MigrationContext setupMigration() throws Exception {
        MigrationContext context = new MigrationContext();
        
        // Connect to MySQL
        context.sqlConnection = mysqlConfig.getConnection();
        System.out.println("✓ Connected to MySQL");
        
        // Optimize JDBC settings
        MigrationOptimizer.optimizeJdbcFetch(context.sqlConnection);
        
        // Get MongoDB collection
        MongoDatabase database = mongoConfig.getDatabase();
        context.mongoCollection = database.getCollection(getCollectionName());
        
        // Clear existing data
        context.mongoCollection.drop();
        System.out.println("✓ Cleared existing MongoDB collection");
        
        // Get counts
        context.totalCount = getSourceCount();
        context.effectiveLimit = getEffectiveLimit(context.totalCount);
        
        return context;
    }
    
    /**
     * Log migration start information
     */
    protected void logMigrationStart(MigrationContext context) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATING: " + getCollectionName().toUpperCase());
        System.out.println("=".repeat(60) + "\n");
        
        if (migrationLimit != null && migrationLimit < context.totalCount) {
            System.out.println("✓ Total records available: " + context.totalCount);
            System.out.println("✓ Migration limit set to: " + context.effectiveLimit);
            System.out.println("✓ Migrating first " + context.effectiveLimit + " records");
        } else {
            System.out.println("✓ Total records to migrate: " + context.totalCount);
        }
        
        System.out.println("✓ Batch size: " + batchSize);
        System.out.println("✓ Bulk write: " + (enableBulkWrite ? "enabled" : "disabled") + "\n");
    }
    
    /**
     * Perform batch insert with appropriate write strategy
     */
    protected void performBatchInsert(MongoCollection<Document> collection, List<Document> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        
        if (enableBulkWrite) {
            MigrationOptimizer.bulkInsertUnordered(collection, batch);
        } else {
            collection.insertMany(batch);
        }
    }
    
    /**
     * Log migration completion and performance statistics
     */
    protected void logMigrationComplete(MigrationContext context) {
        System.out.println("\n✓ Migration complete!");
        System.out.println("  Migrated: " + context.processedCount + " " + getCollectionName());
        
        // Log performance statistics
        MigrationOptimizer.logPerformanceStats(
            getCollectionName(), 
            context.processedCount, 
            context.startTime
        );
    }
    
    /**
     * Calculate batch limit for current iteration
     */
    protected int calculateBatchLimit(int offset, long effectiveLimit) {
        return (int) Math.min(batchSize, effectiveLimit - offset);
    }
    
    /**
     * Close migration context resources
     */
    protected void closeMigrationContext(MigrationContext context) {
        if (context != null && context.sqlConnection != null) {
            try {
                context.sqlConnection.close();
            } catch (Exception e) {
                System.err.println("Warning: Error closing SQL connection: " + e.getMessage());
            }
        }
    }
}

