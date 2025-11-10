package ETLPipeline.types;

import java.util.Objects;

public class GenreMovieRelationRecord {
    private final String movieId;
    private final String genreName;

    public GenreMovieRelationRecord(String movieId, String genreName) {
        this.movieId = movieId;
        this.genreName = genreName;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getGenreName() {
        return genreName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GenreMovieRelationRecord)) {
            return false;
        }
        GenreMovieRelationRecord that = (GenreMovieRelationRecord) o;
        return Objects.equals(movieId, that.movieId)
            && Objects.equals(genreName, that.genreName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, genreName);
    }
}

