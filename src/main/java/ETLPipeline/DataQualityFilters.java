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
            DataQualityFilter.rule("Missing movie id",
                movie -> movie.getId() != null && !movie.getId().isBlank(),
                "fid",
                MovieRecord::getId),
            DataQualityFilter.rule("Missing movie title",
                movie -> movie.getTitle() != null && !movie.getTitle().isBlank(),
                "t",
                MovieRecord::getTitle),
            DataQualityFilter.rule("Missing or invalid release year",
                movie -> movie.getYear() != null,
                "year",
                MovieRecord::getYear)
        ));
    }

    static DataQualityFilter<StarRecord> starFilter() {
        return new DataQualityFilter<>("star", List.of(
            DataQualityFilter.rule("Missing star id",
                star -> star.getId() != null && !star.getId().isBlank(),
                "id",
                StarRecord::getId),
            DataQualityFilter.rule("Missing star name",
                star -> star.getName() != null && !star.getName().isBlank(),
                "name",
                StarRecord::getName)
        ));
    }

    static DataQualityFilter<StarMovieRelation> starMovieRelationFilter() {
        return new DataQualityFilter<>("star-movie relation", List.of(
            DataQualityFilter.rule("Missing star id",
                relation -> relation.getStarId() != null && !relation.getStarId().isBlank(),
                "starId",
                StarMovieRelation::getStarId),
            DataQualityFilter.rule("Missing movie id",
                relation -> relation.getMovieId() != null && !relation.getMovieId().isBlank(),
                "movieId",
                StarMovieRelation::getMovieId)
        ));
    }

    static DataQualityFilter<GenreMovieRelationRecord> genreMovieRelationFilter() {
        return new DataQualityFilter<>("genre-movie relation", List.of(
            DataQualityFilter.rule("Missing movie id",
                relation -> relation.getMovieId() != null && !relation.getMovieId().isBlank(),
                "movieId",
                GenreMovieRelationRecord::getMovieId),
            DataQualityFilter.rule("Missing genre name",
                relation -> relation.getGenreName() != null && !relation.getGenreName().isBlank(),
                "genre",
                GenreMovieRelationRecord::getGenreName)
        ));
    }
}

