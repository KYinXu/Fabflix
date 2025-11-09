package ETLPipeline;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Generic data quality filter that applies a list of rules to a value and logs any failures.
 * Designed to make it easy to add and manage validation criteria in the transformer phase.
 */
public final class DataQualityFilter<T> {
    public static final class Rule<T> {
        private final String description;
        private final Predicate<T> predicate;

        public Rule(String description, Predicate<T> predicate) {
            this.description = Objects.requireNonNull(description, "description");
            this.predicate = Objects.requireNonNull(predicate, "predicate");
        }

        public String description() {
            return description;
        }

        public boolean test(T value) {
            return predicate.test(value);
        }
    }

    private final String entityName;
    private final List<Rule<T>> rules;

    public DataQualityFilter(String entityName, List<Rule<T>> rules) {
        this.entityName = entityName != null ? entityName : "entity";
        this.rules = List.copyOf(rules);
    }

    public boolean accept(T value, String source, String context) {
        if (value == null) {
            log(source, context, "Value is null");
            return false;
        }
        for (Rule<T> rule : rules) {
            if (!rule.test(value)) {
                log(source, context, rule.description());
                return false;
            }
        }
        return true;
    }

    private void log(String source, String context, String message) {
        String sourceLabel = (source != null && !source.isBlank()) ? source : "unknown-source";
        String contextLabel = (context != null && !context.isBlank()) ? (" [" + context + "]") : "";
        System.err.println("[QUALITY][" + entityName + "] " + sourceLabel + contextLabel + " -> " + message);
    }

    public static <T> Rule<T> rule(String description, Predicate<T> predicate) {
        return new Rule<>(description, predicate);
    }
}

