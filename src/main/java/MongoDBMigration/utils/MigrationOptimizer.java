package MongoDBMigration.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;

import java.sql.Connection;
import java.util.List;

/**
 * Utility class for optimizing migration operations
 * Provides performance tuning and bulk operation helpers
 */
public class MigrationOptimizer {
    
    /**
     * Perform optimized bulk insert with unordered writes
     * Unordered writes allow MongoDB to continue inserting even if one document fails
     * 
     * @param collection MongoDB collection
     * @param documents List of documents to insert
     */
    public static void bulkInsertUnordered(MongoCollection<Document> collection, List<Document> documents) {
        if (documents.isEmpty()) {
            return;
        }
        try {
            InsertManyOptions options = new InsertManyOptions()
                .ordered(false);  // Unordered for better performance
            
            collection.insertMany(documents, options);
        } catch (Exception e) {
            // Even with errors, some documents may have been inserted
            System.err.println("Bulk insert warning: " + e.getMessage());
        }
    }
    
    /**
     * Optimize JDBC fetch size for large result sets
     * 
     * @param connection JDBC connection
     * @param fetchSize Fetch size to set
     */
    public static void optimizeJdbcFetch(Connection connection, int fetchSize) {
        try {
            connection.setAutoCommit(false); // Better performance for read-only operations
            // Note: fetchSize is set per statement, not connection
        } catch (Exception e) {
            System.err.println("Could not optimize JDBC settings: " + e.getMessage());
        }
    }
    
    /**
     * Log performance statistics
     * 
     * @param collectionName Collection name
     * @param recordsProcessed Number of records processed
     * @param startTime Start time in milliseconds
     */
    public static void logPerformanceStats(String collectionName, int recordsProcessed, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        double seconds = duration / 1000.0;
        double recordsPerSecond = recordsProcessed / seconds;
        
        System.out.println("\nPerformance Statistics:");
        System.out.println("  Collection:        " + collectionName);
        System.out.println("  Records processed: " + String.format("%,d", recordsProcessed));
        System.out.println("  Duration:          " + String.format("%.2f", seconds) + " seconds");
        System.out.println("  Throughput:        " + String.format("%.0f", recordsPerSecond) + " records/second");
    }
}

