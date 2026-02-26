package bit.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task with a deadline.
 * Supports deadlines with either a date only (YYYY-MM-DD)
 * or a date + time (YYYY-MM-DD HHmm).
 */
public class Deadline extends Task {

    private static final DateTimeFormatter OUTPUT_DATE =
            DateTimeFormatter.ofPattern("MMM dd yyyy");
    private static final DateTimeFormatter OUTPUT_DATETIME =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mma");

    private final LocalDate byDate;
    private final LocalDateTime byDateTime;

    /**
     * Creates a deadline task with a date only.
     *
     * @param description Description of the task
     * @param by Deadline date
     */
    public Deadline(String description, LocalDate by) {
        super(description);

        assert by != null : "Deadline date must not be null";

        this.byDate = by;
        this.byDateTime = null;
    }

    /**
     * Creates a deadline task with a specific date and time.
     *
     * @param description Description of the task
     * @param by Deadline date and time
     */
    public Deadline(String description, LocalDateTime by) {
        super(description);

        assert by != null : "Deadline datetime must not be null";

        this.byDate = null;
        this.byDateTime = by;
    }

    /**
     * Returns the deadline date.
     * If the deadline includes a time, only the date portion is returned.
     *
     * @return Deadline date
     */
    public LocalDate getBy() {
        assert byDate != null || byDateTime != null
                : "Deadline must have either byDate or byDateTime";

        if (byDate != null) {
            return byDate;
        }
        return byDateTime.toLocalDate();
    }

    /**
     * Returns the deadline date and time if this task includes a time.
     *
     * @return Deadline date and time, or {@code null} if this deadline is date-only
     */
    public LocalDateTime getByDateTime() {
        return byDateTime;
    }

    private String formatDeadline() {
        if (byDateTime != null) {
            return byDateTime.format(OUTPUT_DATETIME);
        }
        return byDate.format(OUTPUT_DATE);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + formatDeadline() + ")";
    }
}
