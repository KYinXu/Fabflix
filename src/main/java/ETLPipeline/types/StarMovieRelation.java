package ETLPipeline.types;

import java.util.Objects;

public class StarMovieRelation {
    private final String starId;
    private final String movieId;

    public StarMovieRelation(String starId, String movieId) {
        this.starId = starId;
        this.movieId = movieId;
    }

    public String getStarId() {
        return starId;
    }

    public String getMovieId() {
        return movieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StarMovieRelation)) {
            return false;
        }
        StarMovieRelation that = (StarMovieRelation) o;
        return Objects.equals(starId, that.starId)
            && Objects.equals(movieId, that.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(starId, movieId);
    }
}

