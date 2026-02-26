package bit.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a task with a deadline.
 * A {@code Deadline} can store either:
 * <ul>
 *     <li>a date only</li>
 *     <li>a date and time</li>
 * </ul>
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
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code by} is not {@code null}</li>
     * </ul>
     *
     * @param description Description of the task
     * @param by Deadline date (non-null)
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
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code by} is not {@code null}</li>
     * </ul>
     *
     * @param description Description of the task
     * @param by Deadline date and time (non-null)
     */
    public Deadline(String description, LocalDateTime by) {
        super(description);

        assert by != null : "Deadline date-time must not be null";

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

        // Defensive check in case assertions are disabled
        assert byDate != null || byDateTime != null
                : "Deadline must have either a date or datetime";

        if (byDate != null) {
            return byDate;
        }

        return byDateTime.toLocalDate();
    }

    /**
     * Returns the deadline date and time if this task includes a time.
     *
     * @return Deadline datetime, or {@code null} if this deadline is date-only
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
