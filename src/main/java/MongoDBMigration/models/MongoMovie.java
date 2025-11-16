package MongoDBMigration.models;

import java.util.List;

/**
 * MongoDB document model for movies
 * Represents denormalized movie structure
 */
public class MongoMovie {
    
    private String id;
    private String title;
    private int year;
    private String director;
    private Rating rating;
    private List<Star> stars;
    private List<Genre> genres;
    
    public MongoMovie() {
    }
    
    // TODO: Add getters and setters
    
    /**
     * Nested class for embedded rating information
     */
    public static class Rating {
        private float score;
        private int voteCount;
        
        // TODO: Add getters and setters
    }
    
    /**
     * Nested class for embedded star information
     */
    public static class Star {
        private String id;
        private String name;
        private Integer birthYear;
        
        // TODO: Add getters and setters
    }
    
    /**
     * Nested class for embedded genre information
     */
    public static class Genre {
        private int id;
        private String name;
        
        // TODO: Add getters and setters
    }
}

