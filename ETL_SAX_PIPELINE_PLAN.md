# Apache Spark ETL Pipeline
## Efficiently Parse Large XML Files → MySQL Database with Parallel Processing

### Overview
A modular Apache Spark-based ETL pipeline that leverages Spark's distributed processing capabilities to efficiently parse large XML files and insert data into MySQL. Spark automatically handles parallelization and threading across CPU cores.

---

## Architecture Overview

```
XML File → Spark Session → DataFrame Reader (XML) → Transformations → JDBC Writer → MySQL
                ↓                    ↓                    ↓                ↓
           Extract Phase        Transform Phase       Load Phase          Parallel Processing
         (XMLDataExtractor)    (DataTransformer)   (DataLoader +        (All Phases)
                                                   DatabaseWriter)
```

### Core Components:
1. **SparkETLPipeline** - Main orchestrator using Spark Session
2. **XMLDataExtractor** - Extracts data from XML files into Spark DataFrames (Extract phase)
3. **DataTransformer** - Transforms and cleans data using Spark operations (Transform phase)
4. **DataLoader** - Loads reference data from MySQL database (for lookups, deduplication)
5. **DatabaseWriter** - Writes DataFrames to MySQL using Spark JDBC (Load phase)
6. **ETLConfig** - Configuration for Spark and processing settings

---

## Package Structure

```
src/main/java/
├── ETLPipeline/
│   ├── SparkETLPipeline.java        # Main Spark pipeline
│   ├── XMLDataExtractor.java        # Extract: XML file to Spark DataFrames
│   ├── DataTransformer.java         # Transform: Data cleaning & transformation
│   ├── DataLoader.java              # Load: Reads reference data from MySQL
│   ├── DatabaseWriter.java          # Load: Writes DataFrames to MySQL
│   └── ETLConfig.java               # Configuration
└── models/
    ├── Movie.java                   # Movie domain model
    ├── Star.java                    # Star domain model
    └── Genre.java                   # Genre domain model
```

---

## Implementation Plan

### Phase 1: Spark Configuration & Setup

**File**: `ETLPipeline/ETLConfig.java`

**Purpose**: Configuration for Spark session and processing parameters

**Key Features**:
- Spark master URL (local[*] for local multi-threading)
- Number of partitions for parallel processing
- Batch sizes for database writes
- Database connection properties

**Configuration**:
```java
public class ETLConfig {
    // Spark Configuration
    public String sparkMaster = "local[*]";  // Use all CPU cores
    public String appName = "FabflixETL";
    public int numPartitions = 4;  // Parallel partitions
    public boolean enableHiveSupport = false;
    
    // Database Configuration
    public String dbUrl;
    public String dbUser;
    public String dbPassword;
    public String dbDriver = "com.mysql.cj.jdbc.Driver";
    
    // Batch Configuration
    public int batchSize = 5000;  // Records per batch write
    public int writePartitions = 4;  // Parallel writes
    
    // XML Configuration
    public String rowTag = "movie";  // XML row element name
    public String rootTag = "movies";  // XML root element name
}
```

---

### Phase 2: XML Data Extractor

**File**: `ETLPipeline/XMLDataExtractor.java`

**Purpose**: Extracts data from XML files into Spark DataFrames using spark-xml library (Extract phase of ETL)

**Key Features**:
- Reads XML files in parallel
- Handles nested XML structures
- Configurable row tags
- Returns Spark DataFrame

**Methods**:
```java
public class XMLDataExtractor {
    private SparkSession spark;
    
    /**
     * Extracts movie data from XML file into Spark DataFrame
     * @param xmlFilePath Path to XML file
     * @param config ETL configuration
     * @return DataFrame containing raw movie data
     */
    public DataFrame extractMovies(String xmlFilePath, ETLConfig config) {
        return spark.read()
            .format("xml")
            .option("rowTag", config.rowTag)
            .option("rootTag", config.rootTag)
            .option("inferSchema", "true")
            .load(xmlFilePath);
    }
    
    /**
     * Extracts data with explicit schema definition
     * @param xmlFilePath Path to XML file
     * @param schema Explicit schema for the data
     * @return DataFrame with defined schema
     */
    public DataFrame extractWithSchema(String xmlFilePath, StructType schema) {
        return spark.read()
            .format("xml")
            .schema(schema)
            .option("rowTag", "movie")
            .load(xmlFilePath);
    }
}
```

**XML Structure Expected**:
```xml
<movies>
  <movie>
    <id>tt1234567</id>
    <title>Movie Title</title>
    <year>2020</year>
    <director>Director Name</director>
    <genres>
      <genre>Action</genre>
      <genre>Drama</genre>
    </genres>
    <stars>
      <star>
        <id>nm0000001</id>
        <name>Star Name</name>
        <birthYear>1980</birthYear>
      </star>
    </stars>
    <ratings>
      <rating>8.5</rating>
      <votes>12345</votes>
    </ratings>
  </movie>
</movies>
```

---

### Phase 3: Data Transformer

**File**: `ETLPipeline/DataTransformer.java`

**Purpose**: Transforms and cleans DataFrames using Spark operations (Transform phase of ETL)

**Key Features**:
- Explodes nested arrays (genres, stars)
- Handles missing/null values
- Data type conversions
- Deduplication
- Creates separate DataFrames for each entity type

**Methods**:
```java
public class DataTransformer {
    
    // Extract movies DataFrame
    public DataFrame transformMovies(DataFrame rawDF) {
        return rawDF.select(
            col("id"),
            col("title"),
            col("year").cast("int"),
            col("director")
        )
        .filter(col("id").isNotNull())
        .filter(col("title").isNotNull());
    }
    
    // Extract stars DataFrame (from nested structure)
    public DataFrame transformStars(DataFrame rawDF) {
        return rawDF.select(
            explode(col("stars.star")).alias("star")
        )
        .select(
            col("star.id").alias("id"),
            col("star.name").alias("name"),
            col("star.birthYear").cast("int").alias("birth_year")
        )
        .filter(col("id").isNotNull())
        .dropDuplicates("id");  // Deduplicate stars
    }
    
    // Extract genres DataFrame
    public DataFrame transformGenres(DataFrame rawDF) {
        return rawDF.select(
            explode(col("genres.genre")).alias("genre")
        )
        .select(col("genre").alias("name"))
        .filter(col("name").isNotNull())
        .dropDuplicates("name");  // Deduplicate genres
    }
    
    // Extract star-movie relationships
    public DataFrame transformStarMovieRelations(DataFrame rawDF) {
        return rawDF.select(
            col("id").alias("movie_id"),
            explode(col("stars.star.id")).alias("star_id")
        )
        .filter(col("movie_id").isNotNull())
        .filter(col("star_id").isNotNull())
        .dropDuplicates("star_id", "movie_id");
    }
    
    // Extract genre-movie relationships
    public DataFrame transformGenreMovieRelations(DataFrame rawDF, DataFrame genresDF) {
        DataFrame movieGenres = rawDF.select(
            col("id").alias("movie_id"),
            explode(col("genres.genre")).alias("genre_name")
        );
        
        // Join with genres to get genre IDs
        return movieGenres.join(
            genresDF,
            movieGenres.col("genre_name") == genresDF.col("name"),
            "inner"
        )
        .select(
            col("id").alias("genre_id"),
            col("movie_id")
        )
        .dropDuplicates("genre_id", "movie_id");
    }
    
    // Extract ratings DataFrame
    public DataFrame transformRatings(DataFrame rawDF) {
        return rawDF.select(
            col("id").alias("movie_id"),
            col("ratings.rating").cast("float").alias("ratings"),
            col("ratings.votes").cast("int").alias("vote_count")
        )
        .filter(col("movie_id").isNotNull())
        .filter(col("ratings").isNotNull());
    }
}
```

---

### Phase 4: Data Loader

**File**: `ETLPipeline/DataLoader.java`

**Purpose**: Loads reference data from MySQL database using Spark JDBC (for lookups, deduplication, foreign key resolution)

**Key Features**:
- Loads existing data from database (genres, stars, movies)
- Returns Spark DataFrames for joining with new data
- Handles reference data lookups
- Used for foreign key resolution and duplicate checking

**Methods**:
```java
public class DataLoader {
    private SparkSession spark;
    private ETLConfig config;
    
    /**
     * Loads all genres from database with their IDs
     * @return DataFrame with genre id and name
     */
    public DataFrame loadGenres() {
        return spark.read()
            .format("jdbc")
            .option("url", config.dbUrl)
            .option("user", config.dbUser)
            .option("password", config.dbPassword)
            .option("dbtable", "genres")
            .load();
    }
    
    /**
     * Loads all stars from database
     * @return DataFrame with star id, name, birth_year
     */
    public DataFrame loadStars() {
        return spark.read()
            .format("jdbc")
            .option("url", config.dbUrl)
            .option("user", config.dbUser)
            .option("password", config.dbPassword)
            .option("dbtable", "stars")
            .load();
    }
    
    /**
     * Loads all movies from database
     * @return DataFrame with movie data
     */
    public DataFrame loadMovies() {
        return spark.read()
            .format("jdbc")
            .option("url", config.dbUrl)
            .option("user", config.dbUser)
            .option("password", config.dbPassword)
            .option("dbtable", "movies")
            .load();
    }
    
    /**
     * Checks if genres exist in database and returns mapping
     * @param genreNames DataFrame with genre names
     * @return DataFrame with genre names and their database IDs
     */
    public DataFrame loadGenreMapping(DataFrame genreNames) {
        DataFrame existingGenres = loadGenres();
        return genreNames.join(
            existingGenres,
            genreNames.col("name") == existingGenres.col("name"),
            "left"
        );
    }
}
```

---

### Phase 5: Database Writer

**File**: `ETLPipeline/DatabaseWriter.java`

**Purpose**: Writes Spark DataFrames to MySQL using JDBC with batch operations (Load phase of ETL)

**Key Features**:
- Parallel writes across partitions
- Batch inserts for efficiency
- Handles duplicates (ON DUPLICATE KEY UPDATE)
- Transaction management
- Progress tracking

**Methods**:
```java
public class DatabaseWriter {
    private ETLConfig config;
    
    public void writeMovies(DataFrame moviesDF) {
        moviesDF.write()
            .mode("overwrite")  // or "append"
            .option("driver", config.dbDriver)
            .option("url", config.dbUrl)
            .option("user", config.dbUser)
            .option("password", config.dbPassword)
            .option("dbtable", "movies")
            .option("batchsize", config.batchSize)
            .option("numPartitions", config.writePartitions)
            .jdbc(config.dbUrl, "movies", getConnectionProperties());
    }
    
    public void writeStars(DataFrame starsDF) {
        // Use custom SQL for ON DUPLICATE KEY UPDATE
        starsDF.foreachPartition(partition -> {
            // Batch insert with ON DUPLICATE KEY UPDATE
            // Handle in batches within each partition
        });
    }
    
    // Alternative: Use custom JDBC writer for complex SQL
    public void writeWithCustomSQL(DataFrame df, String table, String insertSQL) {
        df.foreachPartition(partition -> {
            Connection conn = DriverManager.getConnection(
                config.dbUrl, config.dbUser, config.dbPassword
            );
            PreparedStatement stmt = conn.prepareStatement(insertSQL);
            
            int batchCount = 0;
            while (partition.hasNext()) {
                Row row = partition.next();
                // Set parameters
                stmt.addBatch();
                batchCount++;
                
                if (batchCount % config.batchSize == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            conn.close();
        });
    }
    
    private Properties getConnectionProperties() {
        Properties props = new Properties();
        props.setProperty("user", config.dbUser);
        props.setProperty("password", config.dbPassword);
        props.setProperty("rewriteBatchedStatements", "true");
        return props;
    }
}
```

**SQL for ON DUPLICATE KEY UPDATE**:
Since Spark's JDBC writer doesn't support ON DUPLICATE KEY UPDATE directly, we'll use a custom approach:

1. **Option A**: Use `foreachPartition` with PreparedStatement (as shown above)
2. **Option B**: Write to temporary table, then use SQL MERGE
3. **Option C**: Use Spark's `saveMode("append")` and handle duplicates in MySQL triggers

---

### Phase 6: Main Spark Pipeline

**File**: `ETLPipeline/SparkETLPipeline.java`

**Purpose**: Orchestrates the entire ETL process using Spark

**Key Features**:
- Creates SparkSession
- Coordinates data loading, transformation, and writing
- Handles errors and provides progress updates
- Returns processing statistics

**Main Method**:
```java
public class SparkETLPipeline {
    private SparkSession spark;
    private ETLConfig config;
    private XMLDataExtractor extractor;
    private DataTransformer transformer;
    private DataLoader loader;
    private DatabaseWriter writer;
    
    public SparkETLPipeline(ETLConfig config) {
        this.config = config;
        this.spark = createSparkSession();
        this.extractor = new XMLDataExtractor(spark);
        this.transformer = new DataTransformer();
        this.loader = new DataLoader(spark, config);
        this.writer = new DatabaseWriter(config);
    }
    
    public PipelineResult process(String xmlFilePath) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Extract: Load XML into DataFrame
            System.out.println("Extracting data from XML file...");
            DataFrame rawDF = extractor.extractMovies(xmlFilePath, config);
            long totalRecords = rawDF.count();
            System.out.println("Extracted " + totalRecords + " records");
            
            // 2. Transform: Clean and transform data
            System.out.println("Transforming data...");
            DataFrame moviesDF = transformer.transformMovies(rawDF);
            DataFrame starsDF = transformer.transformStars(rawDF);
            DataFrame genresDF = transformer.transformGenres(rawDF);
            DataFrame ratingsDF = transformer.transformRatings(rawDF);
            
            // 3. Load existing reference data from database (if needed for deduplication)
            // Load existing genres to map names to IDs
            DataFrame existingGenres = loader.loadGenres();
            
            // 4. Write new genres first, then reload to get IDs for relationships
            writer.writeGenres(genresDF);
            DataFrame genresWithIds = loader.loadGenres();  // Reload to get all genre IDs
            
            // 5. Create relationship DataFrames using loaded genre IDs
            DataFrame starMovieDF = transformer.transformStarMovieRelations(rawDF);
            DataFrame genreMovieDF = transformer.transformGenreMovieRelations(
                rawDF, genresWithIds
            );
            
            // 6. Load: Write to database (order matters for foreign keys)
            System.out.println("Loading data into database...");
            writer.writeGenres(genresDF);  // Write genres first
            writer.writeMovies(moviesDF);
            writer.writeStars(starsDF);
            writer.writeRatings(ratingsDF);
            writer.writeStarMovieRelations(starMovieDF);
            writer.writeGenreMovieRelations(genreMovieDF);
            
            // 7. Calculate statistics
            long endTime = System.currentTimeMillis();
            PipelineResult result = new PipelineResult();
            result.moviesProcessed = moviesDF.count();
            result.starsProcessed = starsDF.count();
            result.genresProcessed = genresDF.count();
            result.elapsedTimeMs = endTime - startTime;
            
            System.out.println("ETL completed successfully!");
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ETLException("ETL processing failed", e);
        } finally {
            spark.stop();
        }
    }
    
    private SparkSession createSparkSession() {
        return SparkSession.builder()
            .master(config.sparkMaster)
            .appName(config.appName)
            .config("spark.sql.adaptive.enabled", "true")
            .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
            .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
            .getOrCreate();
    }
    
}
```

---

### Phase 6: Handling ON DUPLICATE KEY UPDATE

Since Spark JDBC doesn't support MySQL's `ON DUPLICATE KEY UPDATE` directly, we'll create a custom writer:

**File**: `ETLPipeline/CustomJDBCWriter.java`

```java
public class CustomJDBCWriter {
    
    public static void writeWithUpsert(DataFrame df, String table, 
                                       String[] keyColumns, ETLConfig config) {
        String sql = buildUpsertSQL(table, df.schema(), keyColumns);
        
        df.foreachPartition(partition -> {
            Connection conn = DriverManager.getConnection(
                config.dbUrl, config.dbUser, config.dbPassword
            );
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            int batchCount = 0;
            while (partition.hasNext()) {
                Row row = partition.next();
                setParameters(stmt, row, df.schema());
                stmt.addBatch();
                batchCount++;
                
                if (batchCount % config.batchSize == 0) {
                    stmt.executeBatch();
                    conn.commit();
                }
            }
            stmt.executeBatch();
            conn.commit();
            conn.close();
        });
    }
    
    private static String buildUpsertSQL(String table, StructType schema, 
                                         String[] keyColumns) {
        // Build INSERT ... ON DUPLICATE KEY UPDATE SQL
        // Implementation details...
    }
}
```

---

## Usage Example

### Simple Usage
```java
public static void main(String[] args) {
    ETLConfig config = new ETLConfig();
    config.dbUrl = "jdbc:mysql://localhost/moviedb";
    config.dbUser = Parameters.username;
    config.dbPassword = Parameters.password;
    config.sparkMaster = "local[*]";  // Use all CPU cores
    
    SparkETLPipeline pipeline = new SparkETLPipeline(config);
    PipelineResult result = pipeline.process("movies.xml");
    
    System.out.println("Processed " + result.moviesProcessed + " movies");
    System.out.println("Time: " + result.elapsedTimeMs + "ms");
}
```

### With Custom Configuration
```java
ETLConfig config = new ETLConfig();
config.sparkMaster = "local[8]";  // Use 8 cores
config.numPartitions = 8;
config.batchSize = 10000;
config.writePartitions = 4;

SparkETLPipeline pipeline = new SparkETLPipeline(config);
pipeline.process("large-movies.xml");
```

---

## Performance Benefits of Spark

### 1. Parallel Processing
- **Automatic threading**: Spark uses all available CPU cores
- **Partitioned processing**: Data is split into partitions processed in parallel
- **Configurable**: Set number of partitions based on data size

### 2. In-Memory Processing
- **Caching**: Frequently used DataFrames can be cached
- **Lazy evaluation**: Optimizes query execution
- **Columnar processing**: Efficient for analytical workloads

### 3. Optimized I/O
- **Parallel reads**: XML file is read in parallel chunks
- **Batch writes**: Multiple partitions write to database simultaneously
- **Connection pooling**: Efficient database connection management

### 4. Scalability
- **Local mode**: Uses all cores on single machine
- **Cluster mode**: Can scale to multiple machines if needed
- **Adaptive execution**: Automatically optimizes execution plan

---

## Configuration for Performance

### Local Mode (Single Machine)
```java
config.sparkMaster = "local[*]";  // Use all CPU cores
config.numPartitions = Runtime.getRuntime().availableProcessors();
config.writePartitions = 4;  // Parallel database writes
```

### Tuning Spark Settings
```java
SparkSession.builder()
    .config("spark.sql.adaptive.enabled", "true")
    .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
    .config("spark.sql.shuffle.partitions", "200")
    .config("spark.executor.memory", "4g")
    .config("spark.driver.memory", "2g")
```

---

## Error Handling

### Spark Error Handling
- Spark automatically handles task failures and retries
- Use try-catch around Spark operations
- Log errors and continue processing other partitions

### Database Error Handling
- Wrap database writes in try-catch
- Use transactions for batch operations
- Log failed batches for retry

---

## Implementation Order

1. **Add Spark dependencies** - Update pom.xml (already done)
2. **Create ETLConfig** - Configuration class
3. **Create XMLDataExtractor** - Test XML extraction (Extract phase)
4. **Create DataTransformer** - Test transformations (Transform phase)
5. **Create DataLoader** - Test loading reference data from database
6. **Create DatabaseWriter** - Test database writes (Load phase)
7. **Create SparkETLPipeline** - Wire everything together
8. **Test with small file** - Verify correctness
9. **Test with large file** - Verify performance and parallelism
10. **Optimize** - Tune partitions and batch sizes

---

## File Structure Summary

```
src/main/java/
├── ETLPipeline/
│   ├── SparkETLPipeline.java      # Main pipeline (~300 lines)
│   ├── XMLDataExtractor.java      # Extract: XML to DataFrame (~100 lines)
│   ├── DataTransformer.java       # Transform: Data cleaning (~200 lines)
│   ├── DataLoader.java            # Load: Database reads (~150 lines)
│   ├── DatabaseWriter.java        # Load: JDBC writer (~250 lines)
│   ├── CustomJDBCWriter.java      # Custom upsert writer (~150 lines)
│   └── ETLConfig.java             # Configuration (~80 lines)
```

**Total**: ~1230 lines of code with parallel processing built-in

---

## Key Benefits

1. **Automatic Parallelization**: Spark handles threading automatically
2. **Efficient**: Uses all CPU cores for processing
3. **Scalable**: Can handle very large files efficiently
4. **Modular**: Components can be modified independently
5. **Optimized**: Spark's query optimizer handles execution plans
6. **Resilient**: Automatic retry on failures

This approach leverages Spark's distributed processing capabilities to efficiently process large XML files using all available CPU cores automatically.
