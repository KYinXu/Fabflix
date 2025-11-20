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
 * Migrates employees from MySQL to MongoDB
 * Simple authentication data migration
 */
public class EmployeeMigrator extends BaseMigrator {
    
    public EmployeeMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        super(mysqlConfig, mongoConfig);
    }
    
    @Override
    public void migrate() throws Exception {
        MigrationContext context = null;
        
        try {
            // Setup: establish connections, prepare collection
            context = setupMigration();
            logMigrationStart(context);
            
            // Execute migration: fetch and transform employees
            List<Document> employees = fetchAndTransformEmployees(context);
            
            // Insert: perform batch insert
            if (!employees.isEmpty()) {
                performBatchInsert(context.mongoCollection, employees);
                context.processedCount = employees.size();
                logProgress(context.processedCount, context.effectiveLimit);
            }
            
            // Complete: log results
            logMigrationComplete(context);
            
        } finally {
            closeMigrationContext(context);
        }
    }
    
    /**
     * Fetch employees from MySQL and transform to MongoDB documents
     * Separated for reusability and testing
     */
    private List<Document> fetchAndTransformEmployees(MigrationContext context) throws Exception {
        return migrateEmployeeBatch(context.sqlConnection, 0, (int) context.effectiveLimit);
    }
    
    @Override
    public boolean validate() throws Exception {
        System.out.println("\nValidating employee migration...");
        
        long sourceCount = getSourceCount();
        long destCount = getDestinationCount();
        
        System.out.println("  MySQL employees:   " + sourceCount);
        System.out.println("  MongoDB employees: " + destCount);
        
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
        return "employees";
    }
    
    @Override
    public long getSourceCount() throws Exception {
        try (Connection conn = mysqlConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employees")) {
            
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
        return "employees";
    }
    
    /**
     * Migrate employees from MySQL
     */
    private List<Document> migrateEmployeeBatch(Connection conn, int offset, int limit) throws Exception {
        List<Document> employees = new ArrayList<>();
        
        String query = "SELECT email, password, fullname FROM employees LIMIT ? OFFSET ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String email = rs.getString("email");
                String password = rs.getString("password");
                String fullname = rs.getString("fullname");
                
                // Create employee document (email as _id)
                Document employeeDoc = new Document()
                    .append("_id", email)
                    .append("password", password)
                    .append("fullname", fullname)
                    .append("role", "staff"); // Default role
                
                employees.add(employeeDoc);
            }
        }
        
        return employees;
    }
}
