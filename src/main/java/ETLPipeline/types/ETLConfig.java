package ETLPipeline.types;

public class ETLConfig {
    // Thread Configuration
    public int numThreads = 0;  // 0 means use available processors
    
    // Database Configuration
    public String dbUrl;
    public String dbUser;
    public String dbPassword;
    public String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    // Batch Configuration
    public int batchSize = 5000;  // Records per batch write
    public int writePartitions = 4;  // Parallel writes
    
    // XML Configuration (null values allow the parser to auto-detect)
    public String rowTag = null;    // XML row element name
    public String rootTag = null;   // XML root element name
    
    // Chunk Configuration (for large file processing)
    public int chunkSize = 10000;  // Records per chunk
    
    /**
     * Default constructor - initializes from Parameters.java
     * Note: Parameters must be passed from MainPipeline since it's in default package
     */
    public ETLConfig() {
        // Will be initialized by MainPipeline using Parameters
    }
    
    /**
     * Initialize from Parameters values (called from MainPipeline)
     * @param dbType Database type from Parameters
     * @param dbName Database name from Parameters
     * @param username Username from Parameters
     * @param password Password from Parameters
     */
    public void initializeFromParameters(String dbType, String dbName, String username, String password) {
        this.dbUrl = "jdbc:" + dbType + ":///" + dbName + 
                    "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true";
        this.dbUser = username;
        this.dbPassword = password;
    }
    
    /**
     * Constructor that allows overriding database configuration
     * @param dbUrl Database URL (if null, uses Parameters)
     * @param dbUser Database user (if null, uses Parameters)
     * @param dbPassword Database password (if null, uses Parameters)
     */
    public ETLConfig(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }
}


