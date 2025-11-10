package ETLPipeline;

import ETLPipeline.types.GenreMovieRelationRecord;
import ETLPipeline.types.MovieRecord;
import ETLPipeline.types.ParseResult;
import ETLPipeline.types.RawData;
import ETLPipeline.types.StarMovieRelation;
import ETLPipeline.types.StarRecord;
import ETLPipeline.types.TransformedData;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class DataTransformer {
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");
    private static final DataQualityFilter<MovieRecord> MOVIE_FILTER = DataQualityFilters.movieFilter();
    private static final DataQualityFilter<StarRecord> STAR_FILTER = DataQualityFilters.starFilter();
    private static final DataQualityFilter<StarMovieRelation> STAR_RELATION_FILTER = DataQualityFilters.starMovieRelationFilter();
    private static final DataQualityFilter<GenreMovieRelationRecord> GENRE_RELATION_FILTER = DataQualityFilters.genreMovieRelationFilter();
    
    private enum SourceType {
        MOVIES,
        STARS,
        CASTS,
        UNKNOWN
    }
    
    /**
     * Transform raw parsed data into structured format
     * @param rawData Raw parsed data from XML
     * @return TransformedData ready for database insertion
     */
    public TransformedData transform(ParseResult parseResult) {
        if (parseResult == null) {
            return new TransformedData();
        }
        
        RawData rawData = parseResult.getRawData();
        if (rawData == null) {
            return new TransformedData();
        }
        
        SourceType sourceType = detectSource(rawData.getSourceFilePath());
        switch (sourceType) {
            case MOVIES:
                return transformMovies(rawData);
            case STARS:
                return transformActors(rawData);
            case CASTS:
                return transformCasts(rawData);
            default:
                return new TransformedData();
        }
    }
    
    /**
     * Aggregate transformed data from multiple parsing tasks
     * @param dataList List of TransformedData from different parsing tasks
     * @return Aggregated TransformedData
     */
    public TransformedData aggregate(List<TransformedData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return new TransformedData();
        }
        
        Map<String, MovieRecord> movieMap = new LinkedHashMap<>();
        Map<String, StarRecord> starMap = new LinkedHashMap<>();
        Set<StarMovieRelation> starRelations = new LinkedHashSet<>();
        Set<String> genres = new LinkedHashSet<>();
        Set<GenreMovieRelationRecord> genreRelations = new LinkedHashSet<>();
        
        for (TransformedData data : dataList) {
            if (data == null) {
                continue;
            }
            
            data.getMovies().forEach(movie -> {
                if (movie == null || movie.getId() == null) {
                    return;
                }
                movieMap.putIfAbsent(movie.getId(), movie);
            });
            
            data.getStars().forEach(star -> {
                if (star == null || star.getId() == null) {
                    return;
                }
                starMap.merge(star.getId(), star, this::mergeStarRecords);
            });
            
            data.getStarMovieRelations().forEach(relation -> {
                if (relation == null || relation.getMovieId() == null || relation.getStarId() == null) {
                    return;
                }
                starRelations.add(relation);
            });
            
            data.getGenres().forEach(genre -> {
                if (genre != null && !genre.isBlank()) {
                    genres.add(genre);
                }
            });
            
            data.getGenreMovieRelations().forEach(relation -> {
                if (relation == null || relation.getMovieId() == null || relation.getGenreName() == null) {
                    return;
                }
                genreRelations.add(relation);
            });
        }
        
        TransformedData aggregated = new TransformedData();
        Map<String, Long> starCounts = starRelations.stream()
            .collect(java.util.stream.Collectors.groupingBy(StarMovieRelation::getMovieId, java.util.stream.Collectors.counting()));

        int droppedMovies = 0;
        java.util.Set<String> moviesWithStars = new java.util.LinkedHashSet<>();
        for (Map.Entry<String, MovieRecord> entry : movieMap.entrySet()) {
            long count = starCounts.getOrDefault(entry.getKey(), 0L);
            if (count > 0) {
                aggregated.addMovie(entry.getValue());
                moviesWithStars.add(entry.getKey());
            } else {
                droppedMovies++;
            }
        }
        if (droppedMovies > 0) {
            System.err.println("[QUALITY][movie] Dropped " + droppedMovies
                + " movies due to having zero associated star references.");
        }

        aggregated.addStars(new ArrayList<>(starMap.values()));
        aggregated.addStarMovieRelations(starRelations.stream()
            .filter(relation -> moviesWithStars.contains(relation.getMovieId()))
            .collect(java.util.stream.Collectors.toList()));
        aggregated.addGenres(genres);
        aggregated.addGenreMovieRelations(genreRelations.stream()
            .filter(relation -> moviesWithStars.contains(relation.getMovieId()))
            .collect(java.util.stream.Collectors.toList()));
        return aggregated;
    }
    
    private SourceType detectSource(String sourceFilePath) {
        if (sourceFilePath == null) {
            return SourceType.UNKNOWN;
        }
        String normalized = sourceFilePath.toLowerCase(Locale.ROOT);
        if (normalized.contains("mains")) {
            return SourceType.MOVIES;
        }
        if (normalized.contains("actors")) {
            return SourceType.STARS;
        }
        if (normalized.contains("casts")) {
            return SourceType.CASTS;
        }
        return SourceType.UNKNOWN;
    }
    
    private TransformedData transformMovies(RawData rawData) {
        TransformedData transformed = new TransformedData();
        String sourcePath = rawData.getSourceFilePath();
        for (Object recordObj : safeRecords(rawData)) {
            Map<String, Object> recordMap = asMap(recordObj);
            if (recordMap == null) {
                continue;
            }
            
            String defaultDirector = extractString(getNestedValue(recordMap, "director", "dirname"));
            Map<String, Object> filmsWrapper = asMap(recordMap.get("films"));
            Object filmNode = filmsWrapper != null ? filmsWrapper.get("film") : recordMap.get("film");
            for (Map<String, Object> filmMap : mapList(filmNode)) {
                String movieId = normalizeId(extractString(filmMap.get("fid")));
                String title = extractString(filmMap.get("t"));
                Integer year = parseYear(extractString(filmMap.get("year")));
                String director = determineDirector(filmMap, defaultDirector);

                MovieRecord movie = new MovieRecord(movieId, title, year, director);
                if (!MOVIE_FILTER.accept(movie, sourcePath, "film element", "film", filmMap)) {
                    continue;
                }
                transformed.addMovie(movie);

                Map<String, Object> catsWrapper = asMap(filmMap.get("cats"));
                Object catNode = catsWrapper != null ? catsWrapper.get("cat") : filmMap.get("cat");
                for (String rawGenre : collectStringValues(catNode)) {
                    GenreMovieRelationRecord rawRelation = new GenreMovieRelationRecord(movie.getId(), rawGenre);
                    if (!GENRE_RELATION_FILTER.accept(rawRelation, sourcePath, "film genre", "cat", rawGenre)) {
                        continue;
                    }
                    String normalizedGenre = DataQualityFilters.canonicalizeGenre(rawGenre);
                    if (normalizedGenre == null) {
                        normalizedGenre = DataQualityFilters.normalizeEmergingGenre(rawGenre);
                    }
                    if (normalizedGenre == null) {
                        continue;
                    }
                    if (rawGenre != null && !normalizedGenre.equalsIgnoreCase(rawGenre.trim())) {
                        DataQualityFilters.logGenreNormalization(sourcePath, movie.getId(), rawGenre, normalizedGenre);
                    }
                    transformed.addGenre(normalizedGenre);
                    transformed.addGenreMovieRelation(new GenreMovieRelationRecord(movie.getId(), normalizedGenre));
                }
            }
        }
        return transformed;
    }
    
    private TransformedData transformActors(RawData rawData) {
        TransformedData transformed = new TransformedData();
        String sourcePath = rawData.getSourceFilePath();
        for (Object recordObj : safeRecords(rawData)) {
            Map<String, Object> recordMap = asMap(recordObj);
            if (recordMap == null) {
                continue;
            }
            String stageName = normalizeName(extractString(recordMap.get("stagename")));
            String starId = generateStarId(stageName);
            Integer birthYear = parseYear(extractString(recordMap.get("dob")));
            StarRecord star = new StarRecord(starId, stageName, birthYear);
            if (!STAR_FILTER.accept(star, sourcePath, "actor entry", "actor", recordMap)) {
                continue;
            }
            transformed.addStar(star);
        }
        return transformed;
    }
    
    private TransformedData transformCasts(RawData rawData) {
        TransformedData transformed = new TransformedData();
        String sourcePath = rawData.getSourceFilePath();
        for (Object recordObj : safeRecords(rawData)) {
            Map<String, Object> recordMap = asMap(recordObj);
            if (recordMap == null) {
                continue;
            }
            Object filmcNode = recordMap.get("filmc");
            for (Map<String, Object> filmcMap : mapList(filmcNode)) {
                Object castEntries = filmcMap.get("m");
                for (Map<String, Object> castMap : mapList(castEntries)) {
                    String movieId = normalizeId(extractString(castMap.get("f")));
                    String actorName = normalizeName(extractString(castMap.get("a")));
                    String starId = generateStarId(actorName);

                    StarRecord star = new StarRecord(starId, actorName, null);
                    if (!STAR_FILTER.accept(star, sourcePath, "cast actor reference", "cast", castMap)) {
                        continue;
                    }
                    transformed.addStar(star);

                    StarMovieRelation relation = new StarMovieRelation(starId, movieId);
                    if (STAR_RELATION_FILTER.accept(relation, sourcePath, "cast relation", "cast", castMap)) {
                        transformed.addStarMovieRelation(relation);
                    }
                }
            }
        }
        return transformed;
    }
    
    private List<Object> safeRecords(RawData rawData) {
        List<Object> records = rawData.getRecords();
        return records != null ? records : Collections.emptyList();
    }

    private List<String> collectStringValues(Object node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        if (node instanceof List<?>) {
            for (Object item : (List<?>) node) {
                values.addAll(collectStringValues(item));
            }
        } else if (node instanceof Map<?, ?> || node instanceof String || node instanceof Number) {
            String value = extractString(node);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
    
    private List<Map<String, Object>> mapList(Object node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        if (node instanceof List<?>) {
            for (Object item : (List<?>) node) {
                Map<String, Object> map = asMap(item);
                if (map != null) {
                    result.add(map);
                }
            }
        } else {
            Map<String, Object> map = asMap(node);
            if (map != null) {
                result.add(map);
            }
        }
        return result;
    }
    
    private Object getNestedValue(Map<String, Object> map, String... keys) {
        Map<String, Object> current = map;
        for (int i = 0; i < keys.length; i++) {
            if (current == null) {
                return null;
            }
            Object value = current.get(keys[i]);
            if (i == keys.length - 1) {
                return value;
            }
            current = asMap(value);
        }
        return null;
    }
    
    private String extractString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            String trimmed = ((String) value).trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
        if (value instanceof Number) {
            return value.toString();
        }
        Map<String, Object> map = asMap(value);
        if (map != null) {
            Object text = map.get("$text");
            if (text != null) {
                return extractString(text);
            }
            if (map.size() == 1) {
                return extractString(map.values().iterator().next());
            }
        }
        return null;
    }
    
    private Integer parseYear(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(value);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
    
    private String determineDirector(Map<String, Object> filmMap, String defaultDirector) {
        Map<String, Object> dirsWrapper = asMap(filmMap.get("dirs"));
        if (dirsWrapper != null) {
            Object dirNode = dirsWrapper.get("dir");
            for (Map<String, Object> dirMap : mapList(dirNode)) {
                String name = extractString(dirMap.get("dirn"));
                if (name != null) {
                    return name;
                }
            }
        }
        return defaultDirector;
    }
    
    private String normalizeId(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    private String normalizeName(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    private String generateStarId(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        CRC32 crc = new CRC32();
        crc.update(normalized.getBytes(StandardCharsets.UTF_8));
        long value = crc.getValue() % 100000000L;
        return String.format("nm%08d", value);
    }
    
    private StarRecord mergeStarRecords(StarRecord existing, StarRecord incoming) {
        if (existing == null) {
            return incoming;
        }
        if (incoming == null) {
            return existing;
        }
        String name = existing.getName();
        if (name == null || name.isBlank()) {
            name = incoming.getName();
        }
        Integer birthYear = existing.getBirthYear() != null ? existing.getBirthYear() : incoming.getBirthYear();
        return new StarRecord(existing.getId(), name, birthYear);
    }
    
}
