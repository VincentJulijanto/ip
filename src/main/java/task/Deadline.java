package task;

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
     * Creates a deadline with a date only.
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.byDate = by;
        this.byDateTime = null;
    }

    /**
     * Creates a deadline with a date + time.
     */
    public Deadline(String description, LocalDateTime by) {
        super(description);
        this.byDate = null;
        this.byDateTime = by;
    }

    /**
     * Returns the deadline date if this Deadline was created with date-only.
     * If this Deadline was created with date+time, returns the date portion.
     */
    public LocalDate getBy() {
        if (byDate != null) {
            return byDate;
        }
        return byDateTime.toLocalDate();
    }

    /**
     * Returns the deadline datetime if this Deadline was created with date+time.
     * Returns null if this Deadline is date-only.
     */
    public LocalDateTime getByDateTime() {
        return byDateTime;
    }

    @Override
    public String toString() {
        if (byDateTime != null) {
            return "[D]" + super.toString()
                    + " (by: " + byDateTime.format(OUTPUT_DATETIME) + ")";
        }
        return "[D]" + super.toString()
                + " (by: " + byDate.format(OUTPUT_DATE) + ")";
    }
}