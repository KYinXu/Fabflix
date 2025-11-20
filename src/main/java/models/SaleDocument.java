package models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import java.util.Date;

/**
 * MongoDB document model for sales transactions
 * Contains both references and denormalized data for efficient querying
 */
public class SaleDocument {
    
    @BsonId
    private ObjectId id; // MongoDB ObjectId
    
    @BsonProperty("customerId")
    private ObjectId customerId; // Reference to customer ObjectId
    
    @BsonProperty("movieId")
    private String movieId; // Movie ID
    
    @BsonProperty("saleDate")
    private Date saleDate;
    
    @BsonProperty("price")
    private double price;
    
    // Denormalized fields for reporting and display (optional but useful)
    @BsonProperty("customerEmail")
    private String customerEmail;
    
    @BsonProperty("customerName")
    private String customerName;
    
    @BsonProperty("movieTitle")
    private String movieTitle;
    
    // Constructors
    public SaleDocument() {
    }
    
    public SaleDocument(ObjectId customerId, String movieId, Date saleDate, double price) {
        this.customerId = customerId;
        this.movieId = movieId;
        this.saleDate = saleDate;
        this.price = price;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public ObjectId getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(ObjectId customerId) {
        this.customerId = customerId;
    }
    
    public String getMovieId() {
        return movieId;
    }
    
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }
    
    public Date getSaleDate() {
        return saleDate;
    }
    
    public void setSaleDate(Date saleDate) {
        this.saleDate = saleDate;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getMovieTitle() {
        return movieTitle;
    }
    
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
}

