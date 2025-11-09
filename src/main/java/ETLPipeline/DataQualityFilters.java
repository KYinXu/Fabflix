package ETLPipeline;

import ETLPipeline.types.GenreMovieRelationRecord;
import ETLPipeline.types.MovieRecord;
import ETLPipeline.types.StarMovieRelation;
import ETLPipeline.types.StarRecord;
import java.util.List;

final class DataQualityFilters {
    private DataQualityFilters() { }

    static DataQualityFilter<MovieRecord> movieFilter() {
        return new DataQualityFilter<>("movie", List.of(
            DataQualityFilter.rule("Missing movie id", movie -> movie.getId() != null && !movie.getId().isBlank()),
            DataQualityFilter.rule("Missing movie title", movie -> movie.getTitle() != null && !movie.getTitle().isBlank()),
            DataQualityFilter.rule("Missing or invalid release year", movie -> movie.getYear() != null)
        ));
    }

    static DataQualityFilter<StarRecord> starFilter() {
        return new DataQualityFilter<>("star", List.of(
            DataQualityFilter.rule("Missing star id", star -> star.getId() != null && !star.getId().isBlank()),
            DataQualityFilter.rule("Missing star name", star -> star.getName() != null && !star.getName().isBlank())
        ));
    }

    static DataQualityFilter<StarMovieRelation> starMovieRelationFilter() {
        return new DataQualityFilter<>("star-movie relation", List.of(
            DataQualityFilter.rule("Missing star id", relation -> relation.getStarId() != null && !relation.getStarId().isBlank()),
            DataQualityFilter.rule("Missing movie id", relation -> relation.getMovieId() != null && !relation.getMovieId().isBlank())
        ));
    }

    static DataQualityFilter<GenreMovieRelationRecord> genreMovieRelationFilter() {
        return new DataQualityFilter<>("genre-movie relation", List.of(
            DataQualityFilter.rule("Missing movie id", relation -> relation.getMovieId() != null && !relation.getMovieId().isBlank()),
            DataQualityFilter.rule("Missing genre name", relation -> relation.getGenreName() != null && !relation.getGenreName().isBlank())
        ));
    }
}

