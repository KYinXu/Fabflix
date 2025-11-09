package ETLPipeline.types;

import java.util.List;

/**
 * Container for transformed data ready for database insertion
 */
public class TransformedData {
    private final List<MovieRecord> movies = new java.util.ArrayList<>();
    private final List<StarRecord> stars = new java.util.ArrayList<>();
    private final List<StarMovieRelation> starMovieRelations = new java.util.ArrayList<>();
    private final java.util.Set<String> genres = new java.util.LinkedHashSet<>();
    private final List<GenreMovieRelationRecord> genreMovieRelations = new java.util.ArrayList<>();
    
    public List<MovieRecord> getMovies() {
        return movies;
    }
    
    public List<StarRecord> getStars() {
        return stars;
    }
    
    public List<StarMovieRelation> getStarMovieRelations() {
        return starMovieRelations;
    }
    
    public java.util.Set<String> getGenres() {
        return genres;
    }
    
    public List<GenreMovieRelationRecord> getGenreMovieRelations() {
        return genreMovieRelations;
    }
    
    public void addMovie(MovieRecord movie) {
        if (movie != null) {
            movies.add(movie);
        }
    }
    
    public void addMovies(List<MovieRecord> collection) {
        if (collection != null) {
            collection.stream()
                .filter(java.util.Objects::nonNull)
                .forEach(movies::add);
        }
    }
    
    public void addStar(StarRecord star) {
        if (star != null) {
            stars.add(star);
        }
    }
    
    public void addStars(List<StarRecord> collection) {
        if (collection != null) {
            collection.stream()
                .filter(java.util.Objects::nonNull)
                .forEach(stars::add);
        }
    }
    
    public void addStarMovieRelation(StarMovieRelation relation) {
        if (relation != null) {
            starMovieRelations.add(relation);
        }
    }
    
    public void addStarMovieRelations(List<StarMovieRelation> collection) {
        if (collection != null) {
            collection.stream()
                .filter(java.util.Objects::nonNull)
                .forEach(starMovieRelations::add);
        }
    }
    
    public void addGenre(String genre) {
        if (genre != null) {
            String trimmed = genre.trim();
            if (!trimmed.isEmpty()) {
                genres.add(trimmed);
            }
        }
    }
    
    public void addGenres(java.util.Collection<String> collection) {
        if (collection != null) {
            collection.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(genres::add);
        }
    }
    
    public void addGenreMovieRelation(String movieId, String genreName) {
        if (movieId == null || movieId.isBlank() || genreName == null || genreName.isBlank()) {
            return;
        }
        genreMovieRelations.add(new GenreMovieRelationRecord(movieId.trim(), genreName.trim()));
    }
    
    public void addGenreMovieRelation(GenreMovieRelationRecord relation) {
        if (relation != null) {
            genreMovieRelations.add(relation);
        }
    }
    
    public void addGenreMovieRelations(List<GenreMovieRelationRecord> collection) {
        if (collection != null) {
            collection.stream()
                .filter(java.util.Objects::nonNull)
                .forEach(genreMovieRelations::add);
        }
    }
}



