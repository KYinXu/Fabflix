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
 * Migrates stars from MySQL to MongoDB
 * Includes denormalized movie list for each star
 */
public class StarMigrator extends BaseMigrator {
    
    public StarMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
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
            List<Document> batch = fetchAndTransformStarBatch(context, offset, batchLimit);
            if (!batch.isEmpty()) {
                performBatchInsert(context.mongoCollection, batch);
                context.processedCount += batch.size();
                logProgress(context.processedCount, context.effectiveLimit);
            }
            offset += batchSize;
        }
    }
    
    private List<Document> fetchAndTransformStarBatch(MigrationContext context, int offset, int limit) 
            throws Exception {
        return migrateStarBatch(context.sqlConnection, offset, limit);
    }
    
    @Override
    public boolean validate() throws Exception {
        System.out.println("\nValidating star migration...");
        long sourceCount = getSourceCount();
        long destCount = getDestinationCount();
        System.out.println("  MySQL stars:   " + sourceCount);
        System.out.println("  MongoDB stars: " + destCount);
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
        return "stars";
    }
    
    @Override
    public long getSourceCount() throws Exception {
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM stars")) {
            
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
        return "stars";
    }
    
    /**
     * Migrate a batch of stars with their movie references
     */
    private List<Document> migrateStarBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> stars = new ArrayList<>();
        
        String query = "SELECT id, name, birth_year FROM stars LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String starId = rs.getString("id");
                String name = rs.getString("name");
                int birthYear = rs.getInt("birth_year");
                boolean hasBirthYear = !rs.wasNull();
                
                // Create star document
                Document starDoc = new Document()
                    .append("_id", starId)
                    .append("name", name);
                
                if (hasBirthYear) {
                    starDoc.append("birthYear", birthYear);
                }
                
                // Get movies this star appeared in
                List<String> movies = getStarMovies(conn, starId);
                starDoc.append("movies", movies);
                starDoc.append("movieCount", movies.size());
                
                stars.add(starDoc);
            }
        }
        
        return stars;
    }
    
    /**
     * Get all movie IDs for a star
     */
    private List<String> getStarMovies(Connection conn, String starId) throws Exception {
        List<String> movies = new ArrayList<>();
        
        String query = "SELECT movie_id FROM stars_in_movies WHERE star_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, starId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                movies.add(rs.getString("movie_id"));
            }
        }
        
        return movies;
    }
}
