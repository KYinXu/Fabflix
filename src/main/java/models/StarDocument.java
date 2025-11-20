package models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import java.util.List;

/**
 * MongoDB document model for stars
 * Contains list of movie IDs the star has appeared in
 */
public class StarDocument {
    
    @BsonId
    private String id;
    
    @BsonProperty("name")
    private String name;
    
    @BsonProperty("birthYear")
    private Integer birthYear;
    
    @BsonProperty("movies")
    private List<String> movies; // Movie IDs
    
    @BsonProperty("movieCount")
    private int movieCount; // Denormalized count for performance
    
    // Constructors
    public StarDocument() {
    }
    
    public StarDocument(String id, String name, Integer birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getBirthYear() {
        return birthYear;
    }
    
    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }
    
    public List<String> getMovies() {
        return movies;
    }
    
    public void setMovies(List<String> movies) {
        this.movies = movies;
        this.movieCount = movies != null ? movies.size() : 0;
    }
    
    public int getMovieCount() {
        return movieCount;
    }
    
    public void setMovieCount(int movieCount) {
        this.movieCount = movieCount;
    }
}

