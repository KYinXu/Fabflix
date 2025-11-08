import ETLPipeline.MainPipeline;

/**
 * Factory class for creating MainPipeline with environment parameters
 */
public class ETLPipelineFactory {
    
    /**
     * Creates a MainPipeline instance using Parameters from Parameters.java
     * @return MainPipeline configured with Parameters
     */
    public static MainPipeline createDefault() {
        return MainPipeline.createFromParameters(
            Parameters.dbtype,
            Parameters.dbname,
            Parameters.username,
            Parameters.password
        );
    }
    
    /**
     * Creates a MainPipeline instance with custom database configuration
     * @param dbType Database type
     * @param dbName Database name
     * @param username Database username
     * @param password Database password
     * @return MainPipeline with custom configuration
     */
    public static MainPipeline createCustom(String dbType, String dbName, 
                                            String username, String password) {
        return MainPipeline.createFromParameters(dbType, dbName, username, password);
    }
}


