package ETLPipeline;

import ETLPipeline.types.GenreMovieRelationRecord;
import ETLPipeline.types.MovieRecord;
import ETLPipeline.types.StarMovieRelation;
import ETLPipeline.types.StarRecord;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

final class DataQualityFilters {
    private DataQualityFilters() { }

    private static final Set<String> CANONICAL_GENRES = Set.of(
        "Action",
        "Adult",
        "Adventure",
        "Animation",
        "Biography",
        "Comedy",
        "Crime",
        "Documentary",
        "Drama",
        "Family",
        "Fantasy",
        "History",
        "Horror",
        "Music",
        "Musical",
        "Mystery",
        "Reality-TV",
        "Romance",
        "Sci-Fi",
        "Sport",
        "Thriller",
        "War",
        "Western"
    );

    private static final Map<String, String> ALIAS_TO_CANONICAL = Map.ofEntries(
        Map.entry("ACTN", "Action"),
        Map.entry("ADVT", "Adventure"),
        Map.entry("ANIM", "Animation"),
        Map.entry("BIOP", "Biography"),
        Map.entry("COMD", "Comedy"),
        Map.entry("CRIM", "Crime"),
        Map.entry("DOCU", "Documentary"),
        Map.entry("DRAM", "Drama"),
        Map.entry("FAMI", "Family"),
        Map.entry("FANT", "Fantasy"),
        Map.entry("HIST", "History"),
        Map.entry("HORR", "Horror"),
        Map.entry("MUSI", "Music"),
        Map.entry("MUSC", "Musical"),
        Map.entry("MYST", "Mystery"),
        Map.entry("ROMT", "Romance"),
        Map.entry("SCFI", "Sci-Fi"),
        Map.entry("SF", "Sci-Fi"),
        Map.entry("SUSP", "Thriller"),
        Map.entry("THRL", "Thriller"),
        Map.entry("WEST", "Western"),
        Map.entry("WAR", "War")
    );

    private static final Pattern PLAUSIBLE_GENRE_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 ,'&/+\\-]*$");
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z0-9]+");
    private static final ConcurrentMap<String, String> KNOWN_GENRES = new ConcurrentHashMap<>();
    private static final List<GenreNormalizationStage> CANONICAL_PIPELINE = List.of(
        DataQualityFilters::matchKnownDirect,
        DataQualityFilters::matchNearKnown,
        DataQualityFilters::matchAlias,
        DataQualityFilters::matchCompoundAlias
    );

    static {
        CANONICAL_GENRES.forEach(DataQualityFilters::registerKnownGenre);
    }

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
                GenreMovieRelationRecord::getGenreName),
            DataQualityFilter.rule("Suspicious characters in genre name (possible data entry error)",
                relation -> {
                    String value = relation.getGenreName();
                    if (containsInvalidCharacters(value)) {
                        logGenreAnomaly("Suspicious characters detected in genre entry", relation.getMovieId(), value);
                        return false;
                    }
                    return true;
                },
                "genre",
                GenreMovieRelationRecord::getGenreName),
            DataQualityFilter.rule("Genre name not recognized (possible misspelling)",
                relation -> {
                    String genreName = relation.getGenreName();
                    if (isKnownGenre(genreName)) {
                        return true;
                    }
                    GenreEvidenceTracker.recordUnknown(genreName, relation.getMovieId());
                    return true;
                },
                "genre",
                GenreMovieRelationRecord::getGenreName)
        ));
    }

    private static boolean containsInvalidCharacters(String value) {
        if (value == null) {
            return false;
        }
        return value.contains("*") || value.contains(";") || value.contains(":") || value.contains("|");
    }

    private static boolean isKnownGenre(String value) {
        return canonicalizeGenre(value) != null;
    }

    static String canonicalizeGenre(String value) {
        String sanitized = sanitizeGenreValue(value);
        if (sanitized == null) {
            return null;
        }
        return resolveKnownGenre(sanitized);
    }

    static String normalizeEmergingGenre(String value) {
        String sanitized = sanitizeGenreValue(value);
        if (sanitized == null) {
            return null;
        }
        if (containsInvalidCharacters(sanitized)) {
            return null;
        }
        if (sanitized.length() > 32) {
            return null;
        }
        if (!PLAUSIBLE_GENRE_PATTERN.matcher(sanitized).matches()) {
            return null;
        }
        String known = resolveKnownGenre(sanitized);
        if (known != null) {
            return known;
        }
        if (GenreEvidenceTracker.getEvidenceCount(sanitized) < 2) {
            logGenreAnomaly("Insufficient evidence to accept emerging genre", null, sanitized);
            return null;
        }
        String displayCase = toDisplayCase(sanitized);
        registerKnownGenre(displayCase);
        return displayCase;
    }

    private static String toDisplayCase(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    builder.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    builder.append(c);
                }
            } else {
                builder.append(c);
                capitalizeNext = c == ' ' || c == '-' || c == '/' || c == '&';
            }
        }
        return builder.toString().trim();
    }

    private static String resolveKnownGenre(String sanitized) {
        GenreCandidate candidate = buildCandidate(sanitized);
        if (candidate == null) {
            return null;
        }
        for (GenreNormalizationStage stage : CANONICAL_PIPELINE) {
            String resolved = stage.apply(candidate);
            if (resolved != null) {
                registerKnownGenre(resolved);
                return resolved;
            }
        }
        return null;
    }

    private static String matchKnownDirect(GenreCandidate candidate) {
        if (candidate.key == null) {
            return null;
        }
        return KNOWN_GENRES.get(candidate.key);
    }

    private static String matchNearKnown(GenreCandidate candidate) {
        String key = candidate.key;
        if (key == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : KNOWN_GENRES.entrySet()) {
            String knownKey = entry.getKey();
            if (knownKey == null) {
                continue;
            }
            if (isSingleInsertionOrDeletion(knownKey, key)
                || isSingleReplacement(knownKey, key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String matchAlias(GenreCandidate candidate) {
        return resolveAlias(candidate.sanitized);
    }

    private static String matchCompoundAlias(GenreCandidate candidate) {
        return resolveCompoundAlias(candidate.sanitized);
    }

    private static GenreCandidate buildCandidate(String sanitized) {
        if (sanitized == null) {
            return null;
        }
        return new GenreCandidate(sanitized, collapseKey(sanitized));
    }

    private static String collapseKey(String sanitized) {
        String collapsed = sanitized.replaceAll("[\\s\\-]", "");
        collapsed = collapsed.replaceAll("[^A-Za-z0-9]", "");
        if (collapsed.isEmpty()) {
            return null;
        }
        return collapsed.toLowerCase(Locale.ROOT);
    }

    static String comparisonKey(String value) {
        String sanitized = sanitizeGenreValue(value);
        if (sanitized == null) {
            return null;
        }
        return collapseKey(sanitized);
    }

    private static void logGenreAnomaly(String message, String movieId, String genreValue) {
        String body = "source=" + compactSource(null)
            + " movieId=" + (movieId != null ? movieId : "unknown")
            + " value=" + (genreValue != null ? genreValue : "null") + " -> " + message;
        DataQualityLogger.log("[genre]", body, true);
    }

    static void logGenreNormalization(String source, String movieId, String original, String normalized) {
        StringBuilder builder = new StringBuilder();
        builder.append("source=").append(compactSource(source))
            .append(" movieId=").append(movieId != null ? movieId : "unknown")
            .append(" original=").append(original != null ? original : "null")
            .append(" normalized=").append(normalized != null ? normalized : "null");
        DataQualityLogger.log("[genre-fixed]", builder.toString(), false);
    }

    private static String compactSource(String source) {
        if (source == null || source.isBlank()) {
            return "unknown-source";
        }
        String normalized = source.replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        if (index >= 0 && index + 1 < normalized.length()) {
            return normalized.substring(index + 1);
        }
        return normalized;
    }

    private static String resolveAlias(String value) {
        String collapsed = value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return null;
        }
        return ALIAS_TO_CANONICAL.get(collapsed);
    }

    private static String resolveCompoundAlias(String value) {
        java.util.regex.Matcher matcher = WORD_PATTERN.matcher(value);
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;
        boolean replaced = false;
        while (matcher.find()) {
            builder.append(value, lastIndex, matcher.start());
            String token = matcher.group();
            String resolved = resolveAlias(token);
            if (resolved != null) {
                builder.append(resolved);
                replaced = true;
            } else {
                builder.append(token);
            }
            lastIndex = matcher.end();
        }
        if (!replaced) {
            return null;
        }
        builder.append(value.substring(lastIndex));
        return toDisplayCase(builder.toString());
    }

    static void registerKnownGenres(Collection<String> genres) {
        if (genres == null) {
            return;
        }
        for (String genre : genres) {
            registerKnownGenre(genre);
        }
    }

    static void registerKnownGenre(String genre) {
        if (genre == null) {
            return;
        }
        String originalTrimmed = genre.trim();
        String sanitized = sanitizeGenreValue(genre);
        if (sanitized == null) {
            return;
        }
        String key = collapseKey(sanitized);
        if (key == null) {
            return;
        }
        String display = sanitized.equals(originalTrimmed)
            ? originalTrimmed
            : toDisplayCase(sanitized);
        KNOWN_GENRES.putIfAbsent(key, display);
    }

    static String sanitizeGenreValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String cleaned = trimmed.replace('<', ' ')
            .replace('>', ' ')
            .replace('.', ' ');
        cleaned = cleaned.replaceAll("[\\s\\u00A0]+", " ").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static boolean isSingleInsertionOrDeletion(String base, String other) {
        int diff = base.length() - other.length();
        if (Math.abs(diff) != 1) {
            return false;
        }
        String longer = diff > 0 ? base : other;
        String shorter = diff > 0 ? other : base;
        int i = 0;
        int j = 0;
        boolean foundDifference = false;
        while (i < longer.length() && j < shorter.length()) {
            if (longer.charAt(i) == shorter.charAt(j)) {
                i++;
                j++;
                continue;
            }
            if (foundDifference) {
                return false;
            }
            foundDifference = true;
            i++;
        }
        return true;
    }

    private static boolean isSingleReplacement(String base, String other) {
        if (base.length() != other.length()) {
            return false;
        }
        int differences = 0;
        for (int i = 0; i < base.length(); i++) {
            if (base.charAt(i) != other.charAt(i)) {
                differences++;
                if (differences > 1) {
                    return false;
                }
            }
        }
        return differences == 1;
    }

    private static final class GenreCandidate {
        private final String sanitized;
        private final String key;

        private GenreCandidate(String sanitized, String key) {
            this.sanitized = sanitized;
            this.key = key;
        }
    }

    @FunctionalInterface
    private interface GenreNormalizationStage {
        String apply(GenreCandidate candidate);
    }
}

