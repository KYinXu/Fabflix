package models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import java.util.Date;

/**
 * MongoDB document model for customers
 * Embeds credit card information for denormalized access
 */
public class CustomerDocument {
    
    @BsonId
    private ObjectId id; // MongoDB ObjectId
    
    @BsonProperty("mysqlId")
    private Integer mysqlId; // Original MySQL ID for reference during migration
    
    @BsonProperty("firstName")
    private String firstName;
    
    @BsonProperty("lastName")
    private String lastName;
    
    @BsonProperty("email")
    private String email;
    
    @BsonProperty("password")
    private String password; // Should be hashed
    
    @BsonProperty("address")
    private String address;
    
    @BsonProperty("creditCard")
    private CreditCard creditCard;
    
    // Constructors
    public CustomerDocument() {
    }
    
    public CustomerDocument(String firstName, String lastName, String email, String password, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
    }
    
    // Getters and Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public Integer getMysqlId() {
        return mysqlId;
    }
    
    public void setMysqlId(Integer mysqlId) {
        this.mysqlId = mysqlId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public CreditCard getCreditCard() {
        return creditCard;
    }
    
    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }
    
    /**
     * Nested class for embedded credit card information
     */
    public static class CreditCard {
        @BsonProperty("id")
        private String id;
        
        @BsonProperty("firstName")
        private String firstName;
        
        @BsonProperty("lastName")
        private String lastName;
        
        @BsonProperty("expiration")
        private Date expiration;
        
        public CreditCard() {
        }
        
        public CreditCard(String id, String firstName, String lastName, Date expiration) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.expiration = expiration;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public Date getExpiration() {
            return expiration;
        }
        
        public void setExpiration(Date expiration) {
            this.expiration = expiration;
        }
    }
}

