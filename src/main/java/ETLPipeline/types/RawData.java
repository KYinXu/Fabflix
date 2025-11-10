package ETLPipeline.types;

import java.util.List;

/**
 * Container for raw parsed data from XML
 */
public class RawData {
    private final String sourceFilePath;
    private final List<Object> records;  // Raw movie records
    private final int recordCount;
    
    public RawData(String sourceFilePath, List<Object> records) {
        this.sourceFilePath = sourceFilePath;
        this.records = records;
        this.recordCount = records != null ? records.size() : 0;
    }
    
    public String getSourceFilePath() {
        return sourceFilePath;
    }
    
    public List<Object> getRecords() {
        return records;
    }
    
    public int getRecordCount() {
        return recordCount;
    }
}



