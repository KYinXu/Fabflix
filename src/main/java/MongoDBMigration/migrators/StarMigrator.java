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
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATING STARS");
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
            long totalStars = getSourceCount();
            long effectiveLimit = getEffectiveLimit(totalStars);
            
            if (migrationLimit != null && migrationLimit < totalStars) {
                System.out.println("✓ Total stars available: " + totalStars);
                System.out.println("✓ Migration limit set to: " + effectiveLimit);
                System.out.println("✓ Migrating first " + effectiveLimit + " stars\n");
            } else {
                System.out.println("✓ Total stars to migrate: " + totalStars + "\n");
            }
            
            // Migrate in batches
            int offset = 0;
            int processed = 0;
            
            while (offset < effectiveLimit) {
                // Calculate how many records to fetch in this batch
                int batchLimit = (int) Math.min(batchSize, effectiveLimit - offset);
                
                List<Document> batch = migrateStarBatch(conn, offset, batchLimit);
                
                if (!batch.isEmpty()) {
                    collection.insertMany(batch);
                    processed += batch.size();
                    logProgress(processed, effectiveLimit);
                }
                
                offset += batchSize;
            }
            
            System.out.println("\n✓ Migration complete!");
            System.out.println("  Migrated: " + processed + " stars");
            
        } finally {
            if (conn != null) {
                mysqlConfig.closeConnection(conn);
            }
        }
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
    public long getDestinationCount() throws Exception {
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
    
    /**
     * Main method to run star migration
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FABFLIX STAR MIGRATION");
        System.out.println("=".repeat(60));
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Create migrator
            StarMigrator migrator = new StarMigrator(mysqlConfig, mongoConfig);
            
            // Run migration
            migrator.migrate();
            
            // Validate
            migrator.validate();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Star migration completed successfully!");
            System.out.println("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Star migration failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
