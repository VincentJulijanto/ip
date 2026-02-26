package bit.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an event task with a start and end date/time.
 * An {@code Event} has a non-null start and end time, and the end is not before the start.
 */
public class Event extends Task {

    private static final DateTimeFormatter OUTPUT_DATE =
            DateTimeFormatter.ofPattern("MMM dd yyyy");

    private static final DateTimeFormatter OUTPUT_DATETIME =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mma");

    private final LocalDateTime from;
    private final LocalDateTime to;

    /**
     * Creates an event task with a start and end time.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code from} is not {@code null}.</li>
     *   <li>{@code to} is not {@code null}.</li>
     *   <li>{@code to} is not before {@code from}.</li>
     * </ul>
     *
     * @param description Description of the event (non-null, non-blank; enforced by {@link Task}).
     * @param from Start date and time of the event (non-null).
     * @param to End date and time of the event (non-null, not before {@code from}).
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);

        assert from != null : "Event start time (from) must not be null";
        assert to != null : "Event end time (to) must not be null";
        assert !to.isBefore(from) : "Event end time (to) must not be before start time (from)";

        this.from = from;
        this.to = to;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    @Override
    public String toString() {
        // If you always store datetime, this will format consistently.
        String fromStr = from.format(OUTPUT_DATETIME);
        String toStr = to.format(OUTPUT_DATETIME);

        return "[E]" + super.toString()
                + " (from: " + fromStr
                + " to: " + toStr + ")";
    }
}