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
        private final String fieldName;
        private final java.util.function.Function<T, Object> extractor;

        public Rule(String description, Predicate<T> predicate) {
            this(description, predicate, null, null);
        }

        public Rule(String description,
                    Predicate<T> predicate,
                    String fieldName,
                    java.util.function.Function<T, Object> extractor) {
            this.description = Objects.requireNonNull(description, "description");
            this.predicate = Objects.requireNonNull(predicate, "predicate");
            this.fieldName = fieldName;
            this.extractor = extractor;
        }

        public String description() {
            return description;
        }

        public boolean test(T value) {
            return predicate.test(value);
        }

        public String fieldName() {
            return fieldName;
        }

        public Object extractValue(T value) {
            if (extractor != null && value != null) {
                try {
                    return extractor.apply(value);
                } catch (Exception ignored) {
                    return null;
                }
            }
            return value;
        }
    }

    private final String entityName;
    private final List<Rule<T>> rules;
    public DataQualityFilter(String entityName, List<Rule<T>> rules) {
        this.entityName = entityName != null ? entityName : "entity";
        this.rules = List.copyOf(rules);
    }

    public boolean accept(T value, String source, String context, String elementName) {
        if (value == null) {
            log(source, context, elementName, null, null, "Value is null");
            return false;
        }
        for (Rule<T> rule : rules) {
            if (!rule.test(value)) {
                String field = rule.fieldName() != null ? rule.fieldName() : elementName;
                Object extracted = rule.extractValue(value);
                log(source, context, elementName, field, extracted, rule.description());
                return false;
            }
        }
        return true;
    }

    private void log(String source,
                     String context,
                     String elementName,
                     String fieldName,
                     Object fieldValue,
                     String message) {
        String sourceLabel = compactSource(source);
        String contextLabel = (context != null && !context.isBlank()) ? ("[" + context + "]") : "";
        String elementLabel = elementName != null && !elementName.isBlank() ? (" element=" + elementName) : "";

        String effectiveField = null;
        if (fieldName != null && !fieldName.isBlank()) {
            effectiveField = fieldName;
        } else if (elementName != null && !elementName.isBlank()) {
            effectiveField = elementName;
        }
        if (effectiveField == null || effectiveField.isBlank()) {
            effectiveField = "value";
        }

        String fieldLabel = " field=" + effectiveField;
        String valueLabel = fieldValue != null ? (" value=" + describeValue(fieldValue)) : "";

        String prefix = "[" + entityName + "]" + contextLabel;
        String body = sourceLabel + elementLabel + fieldLabel + valueLabel + " -> " + message;
        DataQualityLogger.log(prefix, body, true);
    }

    private String describeValue(Object nodeValue) {
        String text;
        if (nodeValue instanceof String) {
            text = (String) nodeValue;
        } else if (nodeValue instanceof Number || nodeValue instanceof Boolean) {
            text = String.valueOf(nodeValue);
        } else if (nodeValue instanceof java.util.Map<?, ?> map) {
            text = map.toString();
        } else if (nodeValue instanceof java.util.Collection<?> collection) {
            text = collection.toString();
        } else {
            text = String.valueOf(nodeValue);
        }
        int max = 200;
        if (text.length() > max) {
            return text.substring(0, max - 3) + "...";
        }
        return text;
    }

    private String compactSource(String source) {
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

    public static <T> Rule<T> rule(String description, Predicate<T> predicate) {
        return new Rule<>(description, predicate);
    }

    public static <T> Rule<T> rule(String description,
                                    Predicate<T> predicate,
                                    String fieldName,
                                    java.util.function.Function<T, Object> extractor) {
        return new Rule<>(description, predicate, fieldName, extractor);
    }
}

