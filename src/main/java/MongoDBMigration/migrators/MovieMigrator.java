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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        MigrationContext context = null;
        
        try {
            // Setup: establish connections, prepare collection
            context = setupMigration();
            logMigrationStart(context);
            
            // Execute migration: process in batches
            processBatchedMigration(context);
            
            // Complete: log results
            logMigrationComplete(context);
            
        } finally {
            closeMigrationContext(context);
        }
    }
    
    /**
     * Process movies in batches
     * Separated for reusability and clarity
     */
    private void processBatchedMigration(MigrationContext context) throws Exception {
        int offset = 0;
        
        while (offset < context.effectiveLimit) {
            // Calculate batch limit
            int batchLimit = calculateBatchLimit(offset, context.effectiveLimit);
            
            // Fetch and transform batch
            List<Document> batch = fetchAndTransformMovieBatch(context, offset, batchLimit);
            
            // Insert batch
            if (!batch.isEmpty()) {
                performBatchInsert(context.mongoCollection, batch);
                context.processedCount += batch.size();
                logProgress(context.processedCount, context.effectiveLimit);
            }
            
            offset += batchSize;
        }
    }
    
    /**
     * Fetch and transform a single batch of movies
     * Separated for reusability and testing
     */
    private List<Document> fetchAndTransformMovieBatch(MigrationContext context, int offset, int limit) 
            throws Exception {
        return migrateMovieBatch(context.sqlConnection, offset, limit);
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
     * Migrate a batch of movies with a specific batch size
     * OPTIMIZED: Fetches all related data in batch to avoid N+1 queries
     */
    private List<Document> migrateMovieBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> movies = new ArrayList<>();
        List<String> movieIds = new ArrayList<>();
        Map<String, Document> movieMap = new LinkedHashMap<>();
        
        // Step 1: Query to get movies
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
                    .append("director", director)
                    .append("stars", new ArrayList<Document>())
                    .append("genres", new ArrayList<Document>());
                
                movieIds.add(movieId);
                movieMap.put(movieId, movieDoc);
            }
        }
        
        // If no movies found, return empty list
        if (movieIds.isEmpty()) {
            return movies;
        }
        
        // Step 2: Batch fetch ALL ratings for these movies (1 query instead of N)
        fetchRatingsInBatch(conn, movieIds, movieMap);
        
        // Step 3: Batch fetch ALL stars for these movies (1 query instead of N)
        fetchStarsInBatch(conn, movieIds, movieMap);
        
        // Step 4: Batch fetch ALL genres for these movies (1 query instead of N)
        fetchGenresInBatch(conn, movieIds, movieMap);
        
        // Step 5: Convert map to list maintaining order
        movies.addAll(movieMap.values());
        
        return movies;
    }
    
    /**
     * Batch fetch ratings for multiple movies (1 query instead of N)
     * OPTIMIZED: Eliminates N+1 query problem
     */
    private void fetchRatingsInBatch(Connection conn, List<String> movieIds, Map<String, Document> movieMap) throws Exception {
        if (movieIds.isEmpty()) return;
        
        // Build IN clause with placeholders
        String placeholders = String.join(",", movieIds.stream().map(id -> "?").toArray(String[]::new));
        String query = "SELECT movie_id, ratings, vote_count FROM ratings WHERE movie_id IN (" + placeholders + ")";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set parameters
            for (int i = 0; i < movieIds.size(); i++) {
                stmt.setString(i + 1, movieIds.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                float score = rs.getFloat("ratings");
                int voteCount = rs.getInt("vote_count");
                
                Document rating = new Document()
                    .append("score", score)
                    .append("voteCount", voteCount);
                
                Document movieDoc = movieMap.get(movieId);
                if (movieDoc != null) {
                    movieDoc.append("rating", rating);
                }
            }
        }
    }
    
    /**
     * Batch fetch stars for multiple movies (1 query instead of N)
     * OPTIMIZED: Eliminates N+1 query problem
     */
    @SuppressWarnings("unchecked")
    private void fetchStarsInBatch(Connection conn, List<String> movieIds, Map<String, Document> movieMap) throws Exception {
        if (movieIds.isEmpty()) return;
        
        // Build IN clause with placeholders
        String placeholders = String.join(",", movieIds.stream().map(id -> "?").toArray(String[]::new));
        String query = "SELECT sim.movie_id, s.id, s.name, s.birth_year " +
                      "FROM stars s " +
                      "INNER JOIN stars_in_movies sim ON s.id = sim.star_id " +
                      "WHERE sim.movie_id IN (" + placeholders + ") " +
                      "ORDER BY sim.movie_id, s.name";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set parameters
            for (int i = 0; i < movieIds.size(); i++) {
                stmt.setString(i + 1, movieIds.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                String starId = rs.getString("id");
                String name = rs.getString("name");
                int birthYear = rs.getInt("birth_year");
                
                Document star = new Document()
                    .append("id", starId)
                    .append("name", name);
                
                if (!rs.wasNull()) {
                    star.append("birthYear", birthYear);
                }
                
                Document movieDoc = movieMap.get(movieId);
                if (movieDoc != null) {
                    List<Document> stars = (List<Document>) movieDoc.get("stars");
                    stars.add(star);
                }
            }
        }
    }
    
    /**
     * Batch fetch genres for multiple movies (1 query instead of N)
     * OPTIMIZED: Eliminates N+1 query problem
     */
    @SuppressWarnings("unchecked")
    private void fetchGenresInBatch(Connection conn, List<String> movieIds, Map<String, Document> movieMap) throws Exception {
        if (movieIds.isEmpty()) return;
        
        // Build IN clause with placeholders
        String placeholders = String.join(",", movieIds.stream().map(id -> "?").toArray(String[]::new));
        String query = "SELECT gim.movie_id, g.id, g.name " +
                      "FROM genres g " +
                      "INNER JOIN genres_in_movies gim ON g.id = gim.genre_id " +
                      "WHERE gim.movie_id IN (" + placeholders + ") " +
                      "ORDER BY gim.movie_id, g.name";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Set parameters
            for (int i = 0; i < movieIds.size(); i++) {
                stmt.setString(i + 1, movieIds.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                int genreId = rs.getInt("id");
                String name = rs.getString("name");
                
                Document genre = new Document()
                    .append("id", genreId)
                    .append("name", name);
                
                Document movieDoc = movieMap.get(movieId);
                if (movieDoc != null) {
                    List<Document> genres = (List<Document>) movieDoc.get("genres");
                    genres.add(genre);
                }
            }
        }
    }
}
