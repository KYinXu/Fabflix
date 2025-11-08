package ETLPipeline;

import ETLPipeline.types.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainPipeline {
    // Create threads (Main pipeline)
    // Parse data from XML (XML parser)
    // Transform data into formatted models (Data transformer)
    // Push data to database (Dataloader --> !!!NOT NECESSARY!!! Database writer)

    private static final String DEFAULT_DATA_DIRECTORY = "./src/main/java/ETLPipeline/data";

    private int numThreads;
    private ExecutorService executorService;
    private ETLConfig config;

    private XMLDataParser xmlParser;  // XML parser (SAX or DOM-based)
    private DataTransformer transformer;
    private DatabaseWriter writer;
    private final String dataDirectory;
    
    // Tracking
    private AtomicInteger processedFiles = new AtomicInteger(0);
    private List<Future<ParseResult>> parseFutures;

    /**
     * Static factory method - creates MainPipeline using Parameters from default package
     * Call this from code that has access to Parameters.java
     * Example: MainPipeline pipeline = MainPipeline.createFromParameters(
     *              Parameters.dbtype, Parameters.dbname, Parameters.username, Parameters.password);
     */
    public static MainPipeline createFromParameters(String dbType, String dbName, 
                                                     String username, String password) {
        ETLConfig config = new ETLConfig();
        config.initializeFromParameters(dbType, dbName, username, password);
        return new MainPipeline(config);
    }
    
    /**
     * Constructor - uses default ETLConfig (uninitialized)
     * Note: For default Parameters, use createFromParameters() instead
     */
    public MainPipeline() {
        this(new ETLConfig(), DEFAULT_DATA_DIRECTORY);
    }
    
    /**
     * Constructor with custom ETLConfig
     * @param config ETL configuration
     */
    public MainPipeline(ETLConfig config) {
        this(config, DEFAULT_DATA_DIRECTORY);
    }

    /**
     * Constructor with custom ETLConfig and data directory override.
     * @param config ETL configuration
     * @param dataDirectory Directory containing XML files
     */
    public MainPipeline(ETLConfig config, String dataDirectory) {
        this.config = config;
        this.dataDirectory = dataDirectory != null ? dataDirectory : DEFAULT_DATA_DIRECTORY;
        this.numThreads = config.numThreads != 0 ? config.numThreads : 
                         Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(numThreads);
        this.xmlParser = new XMLDataParser();
        this.transformer = new DataTransformer();
        this.writer = new DatabaseWriter(config);
        this.parseFutures = new ArrayList<>();
    }
    
    /**
     * Main entry point: Process XML files concurrently
     * @param xmlFilePaths List of XML file paths to process
     * @return PipelineResult with statistics
     */
    public PipelineResult processConcurrent(List<String> xmlFilePaths) {
        if (xmlFilePaths == null || xmlFilePaths.isEmpty()) {
            throw new ETLException("No XML files provided for processing");
        }
        parseFutures.clear();
        long startTime = System.currentTimeMillis();
        System.out.println("Starting concurrent parsing with " + numThreads + " threads");
        
        try {
            // Phase 1: Submit all parsing tasks to thread pool
            System.out.println("Submitting " + xmlFilePaths.size() + " parsing tasks...");
            for (String xmlFilePath : xmlFilePaths) {
                Future<ParseResult> future = executorService.submit(
                    () -> parseXmlFile(xmlFilePath)
                );
                parseFutures.add(future);
            }
            
            // Phase 2: Wait for all parsing tasks to complete and collect results
            System.out.println("Waiting for parsing tasks to complete...");
            List<ParseResult> parseResults = new ArrayList<>();
            for (Future<ParseResult> future : parseFutures) {
                try {
                    ParseResult result = future.get(); // Block until task completes
                    parseResults.add(result);
                    System.out.println("Completed parsing: " + result.getFilePath() + 
                                     " - " + result.getRecordsProcessed() + " records");
                } catch (ExecutionException e) {
                    System.err.println("Error in parsing task: " + e.getCause().getMessage());
                    e.printStackTrace();
                }
            }
            
            // Phase 3: Transform all parsed data concurrently
            System.out.println("Transforming parsed data...");
            List<Future<TransformedData>> transformFutures = new ArrayList<>();
            for (ParseResult parseResult : parseResults) {
                final ParseResult result = parseResult;  // Capture for lambda
                Future<TransformedData> transformFuture = executorService.submit(
                    () -> transformer.transform(result)
                );
                transformFutures.add(transformFuture);
            }
            
            // Phase 4: Collect transformed data
            List<TransformedData> transformedDataList = new ArrayList<>();
            for (Future<TransformedData> future : transformFutures) {
                try {
                    transformedDataList.add(future.get());
                } catch (ExecutionException e) {
                    System.err.println("Error in transformation: " + e.getCause().getMessage());
                    e.printStackTrace();
                }
            }
            
            // Phase 5: Write to database (can be done concurrently for different tables)
            System.out.println("Writing data to database...");
            writeDataConcurrent(transformedDataList);
            
            // Phase 6: Calculate statistics
            long endTime = System.currentTimeMillis();
            PipelineResult result = calculateStatistics(parseResults, transformedDataList, 
                                                       endTime - startTime);
            
            System.out.println("ETL completed successfully!");
            System.out.println("Total time: " + result.getElapsedTimeMs() + "ms");
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ETLException("ETL processing failed", e);
        } finally {
            shutdown();
        }
    }
    
    /**
     * Convenience method: discover and process every XML file in a directory.
     * @param xmlDirectory Path to directory containing XML files
     * @return PipelineResult with statistics
     */
    public PipelineResult processDirectory(String xmlDirectory) {
        try {
            List<String> xmlFiles = collectXmlFiles(xmlDirectory);
            System.out.println("Discovered " + xmlFiles.size() + " XML files in " + xmlDirectory);
            return processConcurrent(xmlFiles);
        } catch (IOException e) {
            throw new ETLException("Unable to read XML directory: " + xmlDirectory, e);
        }
    }
    
    /**
     * Parse a single XML file (runs in worker thread)
     * @param xmlFilePath Path to XML file
     * @return ParseResult containing raw parsed data
     */
    private ParseResult parseXmlFile(String xmlFilePath) {
        System.out.println("Thread " + Thread.currentThread().getName() + 
                         " parsing: " + xmlFilePath);
        
        try {
            // Parse XML file using SAX or DOM parser
            RawData rawData = xmlParser.parse(xmlFilePath, config);
            
            int recordsProcessed = rawData.getRecordCount();
            processedFiles.incrementAndGet();
            
            return new ParseResult(xmlFilePath, rawData, recordsProcessed);
        } catch (Exception e) {
            System.err.println("Error parsing " + xmlFilePath + ": " + e.getMessage());
            throw new RuntimeException("Failed to parse " + xmlFilePath, e);
        }
    }
    
    /**
     * Write transformed data to database concurrently
     * Different tables can be written in parallel
     */
    private void writeDataConcurrent(List<TransformedData> transformedDataList) {
        // Aggregate data from all parsing tasks
        TransformedData aggregated = aggregateTransformedData(transformedDataList);
        
        writer.writeMovies(aggregated.getMovies());
        writer.writeStars(aggregated.getStars());
        writer.writeGenres(aggregated.getGenres());
        writer.writeStarMovieRelations(aggregated.getStarMovieRelations());
        writer.writeGenreMovieRelations(aggregated.getGenreMovieRelations());
    }
    
    /**
     * Aggregate transformed data from multiple parsing tasks
     */
    private TransformedData aggregateTransformedData(List<TransformedData> dataList) {
        // Combine all transformed data from different parsing tasks
        // This could involve merging lists, deduplicating, etc.
        // Implementation depends on data structure
        return transformer.aggregate(dataList);
    }
    
    /**
     * Calculate final statistics
     */
    private PipelineResult calculateStatistics(List<ParseResult> parseResults,
                                              List<TransformedData> transformedData,
                                              long elapsedTime) {
        PipelineResult result = new PipelineResult();
        result.setElapsedTimeMs(elapsedTime);
        result.setFilesProcessed(parseResults.size());
        
        // Aggregate counts from all parse results
        int totalRecords = parseResults.stream()
            .mapToInt(ParseResult::getRecordsProcessed)
            .sum();
        result.setTotalRecordsProcessed(totalRecords);
        
        // Aggregate entity counts from transformed data
        // (Implementation depends on TransformedData structure)
        
        return result;
    }
    
    /**
     * Shutdown thread pool gracefully
     */
    private void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (xmlParser != null) {
            try {
                xmlParser.close();
            } catch (Exception e) {
                System.err.println("Failed to close XML parser: " + e.getMessage());
            }
        }
    }
    
    /**
     * Alternative: Process single large XML file by splitting into chunks
     * Each chunk is parsed concurrently
     */
    public PipelineResult processChunked(String xmlFilePath, int chunkSize) {
        System.out.println("Processing large XML file in chunks...");
        
        // Split XML file into chunks (byte offsets or line-based)
        List<XMLChunk> chunks = xmlParser.splitIntoChunks(xmlFilePath, chunkSize, numThreads);
        
        // Parse each chunk concurrently
        List<Future<ParseResult>> chunkFutures = new ArrayList<>();
        for (XMLChunk chunk : chunks) {
            Future<ParseResult> future = executorService.submit(
                () -> parseXmlChunk(chunk)
            );
            chunkFutures.add(future);
        }
        
        // Collect results and continue with transformation/writing
        // (Similar to processConcurrent method)
        
        return null; // Placeholder
    }
    
    /**
     * Parse a single chunk of XML (for chunked processing)
     */
    private ParseResult parseXmlChunk(XMLChunk chunk) {
        // Parse the specific chunk of XML
        RawData rawData = xmlParser.parseChunk(chunk, config);
        return new ParseResult(chunk.getSourceFilePath(), rawData, rawData.getRecordCount());
    }

    /**
     * Locate every XML file within the given directory (recursively).
     * @param directory Directory to scan
     * @return List of XML file paths
     */
    private List<String> collectXmlFiles(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new IOException("Directory does not exist: " + directory);
        }
        try (Stream<Path> stream = Files.walk(dirPath)) {
            List<String> files = stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".xml"))
                .map(Path::toString)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toCollection(ArrayList::new));
            if (files.isEmpty()) {
                throw new IOException("No XML files found in directory: " + directory);
            }
            return files;
        }
    }

    public static void main(String[] args) {
        MainPipeline pipeline = new MainPipeline();
        pipeline.processDirectory(pipeline.dataDirectory);
    }
}

