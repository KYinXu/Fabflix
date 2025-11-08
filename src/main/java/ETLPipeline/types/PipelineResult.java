package ETLPipeline.types;

public class PipelineResult {
    private long elapsedTimeMs;
    private int filesProcessed;
    private int totalRecordsProcessed;
    private int moviesProcessed;
    private int starsProcessed;
    private int genresProcessed;
    
    public long getElapsedTimeMs() {
        return elapsedTimeMs;
    }
    
    public void setElapsedTimeMs(long elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }
    
    public int getFilesProcessed() {
        return filesProcessed;
    }
    
    public void setFilesProcessed(int filesProcessed) {
        this.filesProcessed = filesProcessed;
    }
    
    public int getTotalRecordsProcessed() {
        return totalRecordsProcessed;
    }
    
    public void setTotalRecordsProcessed(int totalRecordsProcessed) {
        this.totalRecordsProcessed = totalRecordsProcessed;
    }
    
    public int getMoviesProcessed() {
        return moviesProcessed;
    }
    
    public void setMoviesProcessed(int moviesProcessed) {
        this.moviesProcessed = moviesProcessed;
    }
    
    public int getStarsProcessed() {
        return starsProcessed;
    }
    
    public void setStarsProcessed(int starsProcessed) {
        this.starsProcessed = starsProcessed;
    }
    
    public int getGenresProcessed() {
        return genresProcessed;
    }
    
    public void setGenresProcessed(int genresProcessed) {
        this.genresProcessed = genresProcessed;
    }
}



