package models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * MongoDB document model for genres
 * Maintains original MySQL ID for compatibility and embedded genre references
 */
public class GenreDocument {
    
    @BsonId
    private Integer id; // Keep original MySQL ID as primary key
    
    @BsonProperty("name")
    private String name;
    
    @BsonProperty("movieCount")
    private int movieCount; // Denormalized count for performance
    
    // Constructors
    public GenreDocument() {
    }
    
    public GenreDocument(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.movieCount = 0;
    }
    
    public GenreDocument(Integer id, String name, int movieCount) {
        this.id = id;
        this.name = name;
        this.movieCount = movieCount;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getMovieCount() {
        return movieCount;
    }
    
    public void setMovieCount(int movieCount) {
        this.movieCount = movieCount;
    }
}

