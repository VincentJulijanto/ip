package bit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import bit.task.Task;
import bit.task.Todo;
import bit.task.Deadline;
import bit.task.Event;

/**
 * Handles loading tasks from disk and saving tasks back to disk.
 * File format (one task per line):
 * TYPE | DONE | DESCRIPTION | EXTRA
 *
 * TYPE: T / D / E
 * DONE: 0 (not done) / 1 (done)
 * EXTRA:
 *  - Todo: empty
 *  - Deadline: yyyy-MM-dd OR yyyy-MM-dd HHmm (legacy may be wrapped like "(by: yyyy-MM-dd)")
 *  - Event: "yyyy-MM-dd HHmm | yyyy-MM-dd HHmm"
 */
public class Storage {
    private final Path filePath;

    /**
     * Creates a Storage instance that reads from and writes to the given file path.
     *
     * @param filePath Path to the task data file
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    private void ensureDataFileExists() throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
    }

    /**
     * Handles legacy formats like "(by: 2019-10-15)" or "by: 2019-10-15".
     *
     * @param extra the raw extra column from file
     * @return unwrapped extra content
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
     * Loads tasks from disk into Task[] tasks.
     *
     * @param tasks the array to populate
     * @return number of tasks loaded
     */
    public int loadTasks(Task[] tasks) {
        try {
            ensureDataFileExists();

            List<String> lines = Files.readAllLines(filePath);
            int count = 0;

            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                // Expected: TYPE | DONE | DESC | EXTRA
                String[] parts = line.split("\\s*\\|\\s*", -1);
                if (parts.length != 4) {
                    continue;
                }

                String type = parts[0].trim();        // T / D / E
                String done = parts[1].trim();        // 0 / 1
                String description = parts[2].trim();
                String extra = parts[3].trim();

                if (!(type.equals("T") || type.equals("D") || type.equals("E"))) {
                    continue;
                }
                if (!(done.equals("0") || done.equals("1"))) {
                    continue;
                }
                if (description.isEmpty()) {
                    continue;
                }
                if (count >= tasks.length) {
                    break;
                }

                Task task = parseTask(type, description, extra);
                if (task == null) {
                    continue;
                }

                if (done.equals("1")) {
                    task.markDone();
                } else {
                    task.markUndone();
                }

                tasks[count] = task;
                count++;
            }

            return count;

        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Parses a single task from file data.
     *
     * @param type T / D / E
     * @param description task description
     * @param extra extra column data
     * @return Task instance, or null if invalid
     */
    private Task parseTask(String type, String description, String extra) {
        if (type.equals("T")) {
            return new Todo(description);
        }

        if (type.equals("D")) {
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

        if (type.equals("E")) {
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

        return null;
    }

    /**
     * Saves Task[] tasks to disk using the same file format as before.
     *
     * @param tasks the array of tasks
     * @param count number of tasks to save
     * @throws IOException if writing fails
     */
    public void saveTasks(Task[] tasks, int count) throws IOException {
        ensureDataFileExists();

        try (var writer = Files.newBufferedWriter(filePath)) {
            for (int i = 0; i < count; i++) {
                Task t = tasks[i];
                if (t == null) {
                    continue;
                }

                String typeCode = getTypeCode(t);
                String done = t.isDone() ? "1" : "0";
                String desc = t.getDescription();
                String extra = getExtraForFile(t);

                writer.write(typeCode + " | " + done + " | " + desc + " | " + extra);
                writer.newLine();
            }
        }
    }

    /**
     * Converts a task object into its file type code.
     *
     * @param t Task instance
     * @return File type code ("T", "D", or "E")
     */
    private String getTypeCode(Task t) {
        if (t instanceof Todo) {
            return "T";
        }
        if (t instanceof Deadline) {
            return "D";
        }
        if (t instanceof Event) {
            return "E";
        }
        return "T"; // safe default
    }

    /**
     * Converts a task object into its "extra" column representation for file storage.
     *
     * @param t Task instance
     * @return Extra column string for storage (empty for Todo)
     */
    private String getExtraForFile(Task t) {
        if (t instanceof Deadline) {
            Deadline d = (Deadline) t;

            // If the deadline was created with date+time, preserve time in file.
            if (d.getByDateTime() != null) {
                return d.getByDateTime().format(Bit.INPUT_DATETIME);
            }

            // Otherwise save date-only.
            return d.getBy().format(Bit.INPUT_DATE);
        }

        if (t instanceof Event) {
            Event e = (Event) t;
            return e.getFrom().format(Bit.INPUT_DATETIME) + " | " + e.getTo().format(Bit.INPUT_DATETIME);
        }

        // Todo has no extra
        return "";
    }
}