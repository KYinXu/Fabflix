package config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.concurrent.TimeUnit;

/**
 * Configuration and connection management for MongoDB database
 */
public class MongoDBConnectionConfig {
    
    private String connectionString;
    private String databaseName;
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    public MongoDBConnectionConfig() {
        try {
            Class<?> params = Class.forName("MongoDBParameters");
            this.connectionString = (String) params.getField("mongoConnectionString").get(null);
            this.databaseName = (String) params.getField("mongoDbName").get(null);
        } catch (Exception e) {
            this.connectionString = "mongodb://localhost:27017";
            this.databaseName = "moviedb";
            System.err.println("Warning: Could not load MongoDBParameters, using defaults. Error: " + e.getMessage());
        }
        
        if (this.connectionString == null || this.connectionString.isEmpty()) {
            this.connectionString = "mongodb://localhost:27017";
        }
        if (this.databaseName == null || this.databaseName.isEmpty()) {
            this.databaseName = "moviedb";
        }
    }
    
    /**
     * Get MongoDB client instance, creates if not exists
     */
    public MongoClient getClient() {
        if (mongoClient == null) {
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(5, TimeUnit.SECONDS)
                           .readTimeout(5, TimeUnit.SECONDS))
                .applyToClusterSettings(builder -> 
                    builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build();
            mongoClient = MongoClients.create(settings);
        }
        return mongoClient;
    }
    
    /**
     * Get MongoDB database instance
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            database = getClient().getDatabase(databaseName);
        }
        return database;
    }
    
    /**
     * Close MongoDB connection
     */
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }
    
    /**
     * Test MongoDB connectivity
     */
    public void testConnection() {
        try {
            MongoDatabase db = getDatabase();
            // Try to run a simple command
            db.runCommand(new Document("ping", 1));
            System.out.println("MongoDB connection successful!");
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
            throw new RuntimeException("Cannot connect to MongoDB", e);
        }
    }
}

