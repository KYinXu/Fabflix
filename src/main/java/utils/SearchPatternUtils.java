package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SearchPatternUtils {
    
    public enum SearchMode {
        SIMPLE,
        TOKEN_BASED
    }
    
    public static Pattern createSearchPattern(String searchInput, SearchMode mode) {
        if (searchInput == null || searchInput.trim().isEmpty()) {
            return Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
        }
        
        return switch (mode) {
            case SIMPLE -> createSimplePattern(searchInput);
            case TOKEN_BASED -> createTokenBasedPattern(searchInput);
        };
    }
    
    private static Pattern createSimplePattern(String searchInput) {
        String trimmed = searchInput.trim();
        String escaped = escapeRegex(trimmed);
        String pattern = ".*" + escaped + ".*";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    
    private static Pattern createTokenBasedPattern(String searchInput) {
        String trimmed = searchInput.trim();
        String[] tokens = trimmed.split("\\s+");
        
        if (tokens.length == 0) {
            return Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
        }
        
        List<String> lookaheads = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                String escaped = Pattern.quote(token);
                lookaheads.add("(?=.*\\b" + escaped + ")");
            }
        }
        
        String pattern = String.join("", lookaheads) + ".*";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    
    private static String escapeRegex(String pattern) {
        return pattern.replace("\\", "\\\\")
                     .replace("^", "\\^")
                     .replace("$", "\\$")
                     .replace(".", "\\.")
                     .replace("|", "\\|")
                     .replace("?", "\\?")
                     .replace("*", "\\*")
                     .replace("+", "\\+")
                     .replace("(", "\\(")
                     .replace(")", "\\)")
                     .replace("[", "\\[")
                     .replace("]", "\\]")
                     .replace("{", "\\{")
                     .replace("}", "\\}");
    }
}

