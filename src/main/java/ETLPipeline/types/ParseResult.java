package ETLPipeline.types;

public class ParseResult {
    private String filePath;
    private RawData rawData;
    private int recordsProcessed;
    
    public ParseResult(String filePath, RawData rawData, int recordsProcessed) {
        this.filePath = filePath;
        this.rawData = rawData;
        this.recordsProcessed = recordsProcessed;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public RawData getRawData() {
        return rawData;
    }
    
    public int getRecordsProcessed() {
        return recordsProcessed;
    }
}



