package ETLPipeline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class DataQualityLogger {
    private static final Path LOG_PATH = Path.of("data-quality.log");
    private static final Object LOG_LOCK = new Object();

    private DataQualityLogger() { }

    static void append(String line) {
        synchronized (LOG_LOCK) {
            try {
                Files.writeString(LOG_PATH, line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("[QUALITY][logger] Failed to append to log file: " + e.getMessage());
            }
        }
    }

    static void log(String prefix, String body, boolean echoToConsole) {
        String line = "[QUALITY]" + prefix + " " + body;
        if (echoToConsole) {
            System.err.println(line);
        }
        append(line);
    }
}

