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
 * Migrates movies from MySQL to MongoDB
 * Handles denormalization of genres, stars, and ratings
 */
public class MovieMigrator extends BaseMigrator {
    
    public MovieMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        super(mysqlConfig, mongoConfig);
    }
    
    @Override
    public void migrate() throws Exception {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  MIGRATING MOVIES");
        System.out.println("=".repeat(60) + "\n");
        
        Connection conn = null;
        
        try {
            // Connect to MySQL
            conn = mysqlConfig.getConnection();
            System.out.println("✓ Connected to MySQL");
            
            // Get MongoDB collection
            MongoDatabase database = mongoConfig.getDatabase();
            MongoCollection<Document> collection = database.getCollection(getCollectionName());
            
            // Clear existing data (optional - comment out to append)
            collection.drop();
            System.out.println("✓ Cleared existing MongoDB collection");
            
            // Get total count
            long totalMovies = getSourceCount();
            long effectiveLimit = getEffectiveLimit(totalMovies);
            
            if (migrationLimit != null && migrationLimit < totalMovies) {
                System.out.println("✓ Total movies available: " + totalMovies);
                System.out.println("✓ Migration limit set to: " + effectiveLimit);
                System.out.println("✓ Migrating first " + effectiveLimit + " movies\n");
            } else {
                System.out.println("✓ Total movies to migrate: " + totalMovies + "\n");
            }
            
            // Migrate in batches
            int offset = 0;
            int processed = 0;
            
            while (offset < effectiveLimit) {
                // Calculate how many records to fetch in this batch
                int batchLimit = (int) Math.min(batchSize, effectiveLimit - offset);
                
                List<Document> batch = migrateMovieBatch(conn, offset, batchLimit);
                
                if (!batch.isEmpty()) {
                    collection.insertMany(batch);
                    processed += batch.size();
                    logProgress(processed, effectiveLimit);
                }
                
                offset += batchSize;
            }
            
            System.out.println("\n✓ Migration complete!");
            System.out.println("  Migrated: " + processed + " movies");
            
        } finally {
            if (conn != null) {
                mysqlConfig.closeConnection(conn);
            }
        }
    }
    
    @Override
    public boolean validate() throws Exception {
        System.out.println("\nValidating movie migration...");
        
        long sourceCount = getSourceCount();
        long destCount = getDestinationCount();
        
        System.out.println("  MySQL movies:   " + sourceCount);
        System.out.println("  MongoDB movies: " + destCount);
        
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
        return "movies";
    }
    
    @Override
    public long getSourceCount() throws Exception {
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM movies")) {
            
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
        return "movies";
    }
    
    /**
     * Migrate a batch of movies with embedded stars, genres, and ratings
     * Uses default batch size
     */
    @SuppressWarnings("unused")
    private List<Document> migrateMovieBatch(Connection conn, int offset) throws Exception {
        return migrateMovieBatch(conn, offset, batchSize);
    }
    
    /**
     * Migrate a batch of movies with a specific batch size
     */
    private List<Document> migrateMovieBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> movies = new ArrayList<>();
        
        // Query to get movies
        String movieQuery = "SELECT id, title, year, director FROM movies LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(movieQuery)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String movieId = rs.getString("id");
                String title = rs.getString("title");
                int year = rs.getInt("year");
                String director = rs.getString("director");
                
                // Create movie document
                Document movieDoc = new Document()
                    .append("_id", movieId)
                    .append("title", title)
                    .append("year", year)
                    .append("director", director);
                
                // Embed rating
                Document rating = getMovieRating(conn, movieId);
                if (rating != null) {
                    movieDoc.append("rating", rating);
                }
                
                // Embed stars
                List<Document> stars = getMovieStars(conn, movieId);
                movieDoc.append("stars", stars);
                
                // Embed genres
                List<Document> genres = getMovieGenres(conn, movieId);
                movieDoc.append("genres", genres);
                
                movies.add(movieDoc);
            }
        }
        
        return movies;
    }
    
    /**
     * Get rating for a movie
     */
    private Document getMovieRating(Connection conn, String movieId) throws Exception {
        String query = "SELECT ratings, vote_count FROM ratings WHERE movie_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                float score = rs.getFloat("ratings");
                int voteCount = rs.getInt("vote_count");
                
                return new Document()
                    .append("score", score)
                    .append("voteCount", voteCount);
            }
        }
        
        return null;
    }
    
    /**
     * Get all stars for a movie
     */
    private List<Document> getMovieStars(Connection conn, String movieId) throws Exception {
        List<Document> stars = new ArrayList<>();
        
        String query = "SELECT s.id, s.name, s.birth_year " +
                      "FROM stars s " +
                      "INNER JOIN stars_in_movies sim ON s.id = sim.star_id " +
                      "WHERE sim.movie_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Document star = new Document()
                    .append("id", rs.getString("id"))
                    .append("name", rs.getString("name"));
                
                int birthYear = rs.getInt("birth_year");
                if (!rs.wasNull()) {
                    star.append("birthYear", birthYear);
                }
                
                stars.add(star);
            }
        }
        
        return stars;
    }
    
    /**
     * Get all genres for a movie
     */
    private List<Document> getMovieGenres(Connection conn, String movieId) throws Exception {
        List<Document> genres = new ArrayList<>();
        
        String query = "SELECT g.id, g.name " +
                      "FROM genres g " +
                      "INNER JOIN genres_in_movies gim ON g.id = gim.genre_id " +
                      "WHERE gim.movie_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, movieId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Document genre = new Document()
                    .append("id", rs.getInt("id"))
                    .append("name", rs.getString("name"));
                
                genres.add(genre);
            }
        }
        
        return genres;
    }
    
    /**
     * Main method to run movie migration
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  FABFLIX MOVIE MIGRATION");
        System.out.println("=".repeat(60));
        
        try {
            // Initialize configs
            MySQLConnectionConfig mysqlConfig = new MySQLConnectionConfig();
            MongoDBConnectionConfig mongoConfig = new MongoDBConnectionConfig();
            
            // Create migrator
            MovieMigrator migrator = new MovieMigrator(mysqlConfig, mongoConfig);
            
            // Run migration
            migrator.migrate();
            
            // Validate
            migrator.validate();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Movie migration completed successfully!");
            System.out.println("=".repeat(60) + "\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ Movie migration failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
