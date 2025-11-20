package models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import java.util.List;

/**
 * MongoDB document model for movies
 * Represents denormalized movie structure with embedded stars, genres, and rating
 */
public class MovieDocument {
    
    @BsonId
    private String id;
    
    @BsonProperty("title")
    private String title;
    
    @BsonProperty("year")
    private int year;
    
    @BsonProperty("director")
    private String director;
    
    @BsonProperty("rating")
    private Rating rating;
    
    @BsonProperty("stars")
    private List<Star> stars;
    
    @BsonProperty("genres")
    private List<Genre> genres;
    
    // Constructors
    public MovieDocument() {
    }
    
    public MovieDocument(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public String getDirector() {
        return director;
    }
    
    public void setDirector(String director) {
        this.director = director;
    }
    
    public Rating getRating() {
        return rating;
    }
    
    public void setRating(Rating rating) {
        this.rating = rating;
    }
    
    public List<Star> getStars() {
        return stars;
    }
    
    public void setStars(List<Star> stars) {
        this.stars = stars;
    }
    
    public List<Genre> getGenres() {
        return genres;
    }
    
    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }
    
    /**
     * Nested class for embedded rating information
     */
    public static class Rating {
        @BsonProperty("score")
        private float score;
        
        @BsonProperty("voteCount")
        private int voteCount;
        
        public Rating() {
        }
        
        public Rating(float score, int voteCount) {
            this.score = score;
            this.voteCount = voteCount;
        }
        
        public float getScore() {
            return score;
        }
        
        public void setScore(float score) {
            this.score = score;
        }
        
        public int getVoteCount() {
            return voteCount;
        }
        
        public void setVoteCount(int voteCount) {
            this.voteCount = voteCount;
        }
    }
    
    /**
     * Nested class for embedded star information
     */
    public static class Star {
        @BsonProperty("id")
        private String id;
        
        @BsonProperty("name")
        private String name;
        
        @BsonProperty("birthYear")
        private Integer birthYear;
        
        public Star() {
        }
        
        public Star(String id, String name, Integer birthYear) {
            this.id = id;
            this.name = name;
            this.birthYear = birthYear;
        }
        
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
    }
    
    /**
     * Nested class for embedded genre information
     */
    public static class Genre {
        @BsonProperty("id")
        private int id;
        
        @BsonProperty("name")
        private String name;
        
        public Genre() {
        }
        
        public Genre(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}

