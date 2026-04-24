package logs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class logWriter {

    public static synchronized void appendJsonObject(String fileName, String json) throws IOException {
        Path path = Paths.get(fileName);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String entry = json + System.lineSeparator();
        Files.write(path, entry.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
