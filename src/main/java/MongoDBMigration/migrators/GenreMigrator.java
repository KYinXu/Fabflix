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
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATING GENRES");
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
            long totalGenres = getSourceCount();
            long effectiveLimit = getEffectiveLimit(totalGenres);
            
            if (migrationLimit != null && migrationLimit < totalGenres) {
                System.out.println("✓ Total genres available: " + totalGenres);
                System.out.println("✓ Migration limit set to: " + effectiveLimit);
                System.out.println("✓ Migrating first " + effectiveLimit + " genres\n");
            } else {
                System.out.println("✓ Total genres to migrate: " + totalGenres + "\n");
            }
            
            // Migrate genres (typically small dataset, can do in one batch)
            List<Document> genres = migrateGenreBatch(conn, 0, (int) effectiveLimit);
            
            if (!genres.isEmpty()) {
                collection.insertMany(genres);
                logProgress(genres.size(), effectiveLimit);
            }
            
            System.out.println("\n✓ Migration complete!");
            System.out.println("  Migrated: " + genres.size() + " genres");
            
        } finally {
            if (conn != null) {
                mysqlConfig.closeConnection(conn);
            }
        }
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
    
    /**
     * Main method to run genre migration
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FABFLIX GENRE MIGRATION");
        System.out.println("=".repeat(60));
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Create migrator
            GenreMigrator migrator = new GenreMigrator(mysqlConfig, mongoConfig);
            
            // Run migration
            migrator.migrate();
            
            // Validate
            migrator.validate();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Genre migration completed successfully!");
            System.out.println("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Genre migration failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
