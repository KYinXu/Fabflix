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
 * Migrates genres from MySQL to MongoDB
 * Simple reference data migration
 */
public class GenreMigrator extends BaseMigrator {
    
    public GenreMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        super(mysqlConfig, mongoConfig);
    }
    
    @Override
    public void migrate() throws Exception {
        MigrationContext context = null;
        
        try {
            // Setup: establish connections, prepare collection
            context = setupMigration();
            logMigrationStart(context);
            
            // Execute migration: fetch and transform genres
            List<Document> genres = fetchAndTransformGenres(context);
            
            // Insert: perform batch insert
            if (!genres.isEmpty()) {
                performBatchInsert(context.mongoCollection, genres);
                context.processedCount = genres.size();
                logProgress(context.processedCount, context.effectiveLimit);
            }
            
            // Complete: log results
            logMigrationComplete(context);
            
        } finally {
            closeMigrationContext(context);
        }
    }
    
    /**
     * Fetch genres from MySQL and transform to MongoDB documents
     * Separated for reusability and testing
     */
    private List<Document> fetchAndTransformGenres(MigrationContext context) throws Exception {
        return migrateGenreBatch(context.sqlConnection, 0, (int) context.effectiveLimit);
    }
    
    @Override
    public boolean validate() throws Exception {
        System.out.println("\nValidating genre migration...");
        
        long sourceCount = getSourceCount();
        long destCount = getDestinationCount();
        
        System.out.println("  MySQL genres:   " + sourceCount);
        System.out.println("  MongoDB genres: " + destCount);
        
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
        return "genres";
    }
    
    @Override
    public long getSourceCount() throws Exception {
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM genres")) {
            
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
        return "genres";
    }
    
    /**
     * Migrate genres from MySQL
     */
    private List<Document> migrateGenreBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> genres = new ArrayList<>();
        
        String query = "SELECT id, name FROM genres LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                
                // Create genre document
                Document genreDoc = new Document()
                    .append("_id", id)
                    .append("name", name)
                    .append("movieCount", 0); // Will be updated later if needed
                
                genres.add(genreDoc);
            }
        }
        
        return genres;
    }
}
