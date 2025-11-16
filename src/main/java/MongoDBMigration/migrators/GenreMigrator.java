package MongoDBMigration.migrators;

import MongoDBMigration.config.MySQLConnectionConfig;
import MongoDBMigration.config.MongoDBConnectionConfig;

/**
 * Migrates genres from MySQL to MongoDB
 * Simple lookup table with no dependencies
 */
public class GenreMigrator extends BaseMigrator {
    
    public GenreMigrator(MySQLConnectionConfig mysqlConfig, MongoDBConnectionConfig mongoConfig) {
        super(mysqlConfig, mongoConfig);
    }
    
    @Override
    public void migrate() throws Exception {
        // TODO: Read genres from MySQL and insert into MongoDB
    }
    
    @Override
    public boolean validate() throws Exception {
        // TODO: Validate genre migration
        return false;
    }
    
    @Override
    public String getCollectionName() {
        return "genres";
    }
    
    @Override
    protected long getSourceCount() throws Exception {
        // TODO: Count genres in MySQL
        return 0;
    }
    
    @Override
    protected long getDestinationCount() throws Exception {
        // TODO: Count genres in MongoDB
        return 0;
    }
}

