package MongoDBMigration.migrators;

import MongoDBMigration.config.MySQLConnectionConfig;
import MongoDBMigration.config.MongoDBConnectionConfig;

/**
 * Abstract base class for all migrators
 * Provides common functionality and migration lifecycle methods
 */
public abstract class BaseMigrator {
    
    protected MySQLConnectionConfig mysqlConfig;
    protected MongoDBConnectionConfig mongoConfig;
    protected int batchSize;
    
    public BaseMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        this.mysqlConfig = mysqlConfig;
        this.mongoConfig = mongoConfig;
        this.batchSize = 1000; // Default batch size
    }
    
    /**
     * Execute the migration for this specific entity
     */
    public abstract void migrate() throws Exception;
    
    /**
     * Validate the migrated data
     */
    public abstract boolean validate() throws Exception;
    
    /**
     * Get the name of the collection being migrated
     */
    public abstract String getCollectionName();
    
    /**
     * Get total count from MySQL source
     */
    protected abstract long getSourceCount() throws Exception;
    
    /**
     * Get total count from MongoDB destination
     */
    protected abstract long getDestinationCount() throws Exception;
    
    /**
     * Clear the MongoDB collection (for re-migration)
     */
    protected void clearCollection() {
        // TODO: Drop collection or delete all documents
    }
    
    /**
     * Log migration progress
     */
    protected void logProgress(int processed, long total) {
        // TODO: Log migration progress
    }
}

