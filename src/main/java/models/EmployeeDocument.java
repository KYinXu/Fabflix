package models;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * MongoDB document model for employees
 * Used for authentication and authorization
 */
public class EmployeeDocument {
    
    @BsonId
    private String email; // Use email as primary key
    
    @BsonProperty("password")
    private String password; // Should be hashed
    
    @BsonProperty("fullname")
    private String fullname;
    
    @BsonProperty("role")
    private String role; // e.g., "admin", "staff"
    
    // Constructors
    public EmployeeDocument() {
    }
    
    public EmployeeDocument(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.role = "staff"; // Default role
    }
    
    public EmployeeDocument(String email, String password, String fullname, String role) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
    }
    
    // Getters and Setters
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
    
    public String getFullname() {
        return fullname;
    }
    
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}

