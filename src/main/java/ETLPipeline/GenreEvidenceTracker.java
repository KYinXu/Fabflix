package ETLPipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class GenreEvidenceTracker {
    private static final Map<String, AtomicInteger> EVIDENCE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> FIRST_MOVIE_MAP = new ConcurrentHashMap<>();

    private GenreEvidenceTracker() { }

    static void recordUnknown(String genreName, String movieId) {
        String sanitized = DataQualityFilters.sanitizeGenreValue(genreName);
        if (sanitized == null) {
            return;
        }
        String key = DataQualityFilters.comparisonKey(sanitized);
        if (key == null) {
            return;
        }
        FIRST_MOVIE_MAP.putIfAbsent(key, movieId);
        AtomicInteger counter = EVIDENCE_MAP.computeIfAbsent(key, _ -> new AtomicInteger(0));
        int newCount = counter.incrementAndGet();
        String body = "genre=" + sanitized
            + " normalizedKey=" + key
            + " evidenceCount=" + newCount
            + " firstMovieId=" + FIRST_MOVIE_MAP.get(key);
        DataQualityLogger.log("[genre-unknown]", body, false);
    }

    static int getEvidenceCount(String genreName) {
        String key = DataQualityFilters.comparisonKey(genreName);
        if (key == null) {
            return 0;
        }
        AtomicInteger counter = EVIDENCE_MAP.get(key);
        return counter != null ? counter.get() : 0;
    }
}

