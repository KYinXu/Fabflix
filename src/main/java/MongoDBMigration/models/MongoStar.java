package MongoDBMigration.models;

import java.util.List;

/**
 * MongoDB document model for stars
 */
public class MongoStar {
    
    private String id;
    private String name;
    private Integer birthYear;
    private List<String> movies; // Movie IDs
    
    public MongoStar() {
    }
    
    // TODO: Add getters and setters
}

