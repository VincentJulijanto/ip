package bit.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatLog {

    public static final String TYPE_DATE = "DATE";
    public static final String TYPE_ME = "ME";
    public static final String TYPE_BIT = "BIT";
    public static final String TYPE_SESSION_END = "SESSION_END";

    private static final String SEP = "|";

    private final Path path;
    private final int retentionDays;

    public static class Record {
        public final Instant ts;
        public final String type;
        public final String payload;

        public Record(Instant ts, String type, String payload) {
            this.ts = ts;
            this.type = type;
            this.payload = payload;
        }
    }

    public ChatLog(String filePath, int retentionDays) {
        this.path = Paths.get(filePath);
        this.retentionDays = retentionDays;
    }

    public void append(String type, String payload) {
        try {
            Files.createDirectories(path.getParent());

            String safePayload = payload == null ? "" : payload.replace("\n", "\\n");
            String line = Instant.now() + SEP + type + SEP + safePayload + System.lineSeparator();

            Files.writeString(
                    path,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // non-fatal
        }
    }

    public List<Record> loadRecentAndPrune() {
        List<Record> out = new ArrayList<>();

        try {
            if (!Files.exists(path)) {
                return out;
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            StringBuilder kept = new StringBuilder();

            for (String raw : lines) {
                if (raw == null || raw.isBlank()) continue;

                String[] parts = raw.split("\\|", 3);
                if (parts.length < 3) continue;

                Instant ts;
                try {
                    ts = Instant.parse(parts[0]);
                } catch (Exception ex) {
                    continue;
                }

                if (!withinRetention(ts)) continue;

                String type = parts[1];
                String payload = parts[2].replace("\\n", "\n");

                kept.append(raw).append(System.lineSeparator());
                out.add(new Record(ts, type, payload));
            }

            Files.writeString(
                    path,
                    kept.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException ignored) {
            // non-fatal
        }

        return out;
    }

    private boolean withinRetention(Instant ts) {
        Instant cutoff = Instant.now().minus(java.time.Duration.ofDays(retentionDays));
        return !ts.isBefore(cutoff);
    }

    public static LocalDate toLocalDate(Instant ts) {
        return ts.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static String normalizeChipLabel(String label, LocalDate chipDay, DateTimeFormatter chipFmt) {
        if ("Today".equals(label) && !chipDay.equals(LocalDate.now())) {
            return chipDay.format(chipFmt);
        }
        return label;
    }
}
