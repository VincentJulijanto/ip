package bit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import bit.task.Deadline;
import bit.task.Event;
import bit.task.Task;
import bit.task.Todo;

/**
 * Handles loading tasks from disk and saving tasks back to disk.
 *
 * <p>File format (one task per line):
 * <pre>
 * TYPE | DONE | DESCRIPTION | EXTRA
 * </pre>
 *
 * <p>TYPE: {@code T} / {@code D} / {@code E}<br>
 * DONE: {@code 0} (not done) / {@code 1} (done)<br>
 * EXTRA:
 * <ul>
 *   <li>Todo: empty</li>
 *   <li>Deadline: {@code yyyy-MM-dd} OR {@code yyyy-MM-dd HHmm}
 *       (legacy may be wrapped like {@code "(by: yyyy-MM-dd)"})</li>
 *   <li>Event: {@code "yyyy-MM-dd HHmm | yyyy-MM-dd HHmm"}</li>
 * </ul>
 */
public class Storage {

    private static final String TYPE_TODO = "T";
    private static final String TYPE_DEADLINE = "D";
    private static final String TYPE_EVENT = "E";

    private static final String DONE = "1";
    private static final String NOT_DONE = "0";

    private static final int EXPECTED_PARTS = 4;

    private final Path filePath;

    /**
     * Creates a Storage instance that reads from and writes to the given file path.
     *
     * <p><b>Assumption:</b> {@code filePath} is not {@code null}.
     *
     * @param filePath Path to the task data file (non-null).
     */
    public Storage(Path filePath) {
        assert filePath != null : "Storage filePath must not be null";
        this.filePath = filePath;
    }

    /**
     * Ensures the data file exists by creating its parent directories and the file if needed.
     *
     * @throws IOException If directory or file creation fails.
     */
    private void ensureDataFileExists() throws IOException {
        assert filePath != null : "Storage filePath must not be null";

        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
    }

    /**
     * Unwraps legacy deadline formats like {@code "(by: 2019-10-15)"} or {@code "by: 2019-10-15"}.
     *
     * <p><b>Assumption:</b> {@code extra} may be {@code null}.
     *
     * @param extra The raw extra column from file.
     * @return Unwrapped extra content, or empty string if {@code extra} is {@code null}.
     */
    private String unwrapBy(String extra) {
        if (extra == null) {
            return "";
        }

        String s = extra.trim();

        if (s.startsWith("(by:") && s.endsWith(")")) {
            s = s.substring(4, s.length() - 1).trim(); // remove "(by:" and ")"
            if (s.startsWith(":")) {
                s = s.substring(1).trim();
            }
        }

        if (s.startsWith("by:")) {
            s = s.substring(3).trim();
        }

        return s;
    }

    /**
     * Loads tasks from disk into the given {@code tasks} array.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code tasks} is not {@code null}.</li>
     *   <li>{@code tasks.length} represents the maximum capacity to load.</li>
     * </ul>
     *
     * <p>If the file does not exist or cannot be read, this method returns {@code 0}.
     *
     * @param tasks The array to populate.
     * @return Number of tasks loaded (0 if file missing/unreadable).
     */
    public int loadTasks(Task[] tasks) {
        assert tasks != null : "tasks array must not be null";

        try {
            ensureDataFileExists();

            List<String> lines = Files.readAllLines(filePath);
            int count = 0;

            for (String line : lines) {
                if (isBlank(line)) {
                    continue;
                }
                if (count >= tasks.length) {
                    break;
                }

                String[] parts = splitLine(line);
                if (parts == null) {
                    continue;
                }

                String type = parts[0];
                String done = parts[1];
                String description = parts[2];
                String extra = parts[3];

                if (!isValidType(type) || !isValidDoneFlag(done) || description.isEmpty()) {
                    continue;
                }

                Task task = parseTask(type, description, extra);
                if (task == null) {
                    continue;
                }

                if (DONE.equals(done)) {
                    task.markDone();
                } else {
                    task.markUndone();
                }

                tasks[count++] = task;
            }

            return count;

        } catch (IOException e) {
            return 0;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Splits a line into its 4 columns and trims each column.
     *
     * @param line input line (non-null, non-blank)
     * @return trimmed parts array of length 4, or null if invalid
     */
    private String[] splitLine(String line) {
        String[] raw = line.split("\\s*\\|\\s*", -1);
        if (raw.length != EXPECTED_PARTS) {
            return null;
        }

        for (int i = 0; i < raw.length; i++) {
            raw[i] = raw[i].trim();
        }
        return raw;
    }

    private boolean isValidType(String type) {
        return TYPE_TODO.equals(type) || TYPE_DEADLINE.equals(type) || TYPE_EVENT.equals(type);
    }

    private boolean isValidDoneFlag(String done) {
        return DONE.equals(done) || NOT_DONE.equals(done);
    }

    /**
     * Parses a single task from file data.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code type} is one of {@code "T"}, {@code "D"}, or {@code "E"}.</li>
     *   <li>{@code description} is non-null and non-blank.</li>
     * </ul>
     *
     * @param type Task type code ("T", "D", "E").
     * @param description Task description.
     * @param extra Extra column data (may be empty).
     * @return Task instance, or {@code null} if invalid/unparseable.
     */
    private Task parseTask(String type, String description, String extra) {
        assert type != null : "type must not be null";
        assert description != null && !description.isBlank()
                : "description must be non-null and non-blank";

        if (type.equals("T")) {
            return new Todo(description);
        case TYPE_DEADLINE:
            return parseDeadline(description, extra);
        case TYPE_EVENT:
            return parseEvent(description, extra);
        default:
            return null;
        }
    }

    private Task parseDeadline(String description, String extra) {
        String cleaned = unwrapBy(extra);

        // Try datetime first (yyyy-MM-dd HHmm), then date (yyyy-MM-dd)
        try {
            LocalDateTime byDateTime = LocalDateTime.parse(cleaned, Bit.INPUT_DATETIME);
            return new Deadline(description, byDateTime);
        } catch (Exception ignoredDt) {
            try {
                LocalDate byDate = LocalDate.parse(cleaned, Bit.INPUT_DATE);
                return new Deadline(description, byDate);
            } catch (Exception ignoredDate) {
                return null;
            }
        }
    }

    private Task parseEvent(String description, String extra) {
        // Event extra: "yyyy-MM-dd HHmm | yyyy-MM-dd HHmm"
        String[] dt = extra.split("\\s*\\|\\s*");
        if (dt.length != 2) {
            return null;
        }

        try {
            LocalDateTime from = LocalDateTime.parse(dt[0].trim(), Bit.INPUT_DATETIME);
            LocalDateTime to = LocalDateTime.parse(dt[1].trim(), Bit.INPUT_DATETIME);
            return new Event(description, from, to);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Saves the first {@code count} tasks from {@code tasks} to disk using the same file format.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code tasks} is not {@code null}.</li>
     *   <li>{@code count} is within {@code [0, tasks.length]}.</li>
     * </ul>
     *
     * @param tasks The array of tasks.
     * @param count Number of tasks to save.
     * @throws IOException If writing fails.
     */
    public void saveTasks(Task[] tasks, int count) throws IOException {
        assert tasks != null : "tasks array must not be null";
        assert count >= 0 && count <= tasks.length : "count must be within [0, tasks.length]";

        ensureDataFileExists();

        try (var writer = Files.newBufferedWriter(filePath)) {
            for (int i = 0; i < count; i++) {
                Task t = tasks[i];
                if (t == null) {
                    continue;
                }

                writer.write(toFileLine(t));
                writer.newLine();
            }
        }
    }

    /**
     * Converts a task object into its file type code.
     *
     * <p><b>Assumption:</b> {@code t} is not {@code null}.
     *
     * @param t Task instance.
     * @return File type code ("T", "D", or "E").
     */
    private String getTypeCode(Task t) {
        assert t != null : "Task must not be null";

        if (t instanceof Todo) {
            return TYPE_TODO;
        }
        if (t instanceof Deadline) {
            return TYPE_DEADLINE;
        }
        if (t instanceof Event) {
            return TYPE_EVENT;
        }
        return TYPE_TODO; // safe default
    }

    /**
     * Converts a task object into its "extra" column representation for file storage.
     *
     * <p><b>Assumption:</b> {@code t} is not {@code null}.
     *
     * @param t Task instance.
     * @return Extra column string for storage (empty for Todo).
     */
    private String getExtraForFile(Task t) {
        assert t != null : "Task must not be null";

        if (t instanceof Deadline) {
            Deadline d = (Deadline) t;

            if (d.getByDateTime() != null) {
                return d.getByDateTime().format(Bit.INPUT_DATETIME);
            }
            return d.getBy().format(Bit.INPUT_DATE);
        }

        if (t instanceof Event) {
            Event e = (Event) t;
            return e.getFrom().format(Bit.INPUT_DATETIME) + " | " + e.getTo().format(Bit.INPUT_DATETIME);
        }

        return "";
    }
}
