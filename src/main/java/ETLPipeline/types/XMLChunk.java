package ETLPipeline.types;

/**
 * Represents a chunk of XML file for parallel processing
 */
public class XMLChunk {
    private String sourceFilePath;
    private long startOffset;  // Byte offset in file
    private long endOffset;    // Byte offset in file
    private int chunkIndex;
    
    public XMLChunk(String sourceFilePath, long startOffset, long endOffset, int chunkIndex) {
        this.sourceFilePath = sourceFilePath;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.chunkIndex = chunkIndex;
    }
    
    public String getSourceFilePath() {
        return sourceFilePath;
    }
    
    public long getStartOffset() {
        return startOffset;
    }
    
    public long getEndOffset() {
        return endOffset;
    }
    
    public int getChunkIndex() {
        return chunkIndex;
    }
}



