package bit.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an event task with a start and end date/time.
 * An {@code Event} has a non-null start and end time, and the end is not before the start.
 */
public class Event extends Task {

    private final LocalDateTime from;
    private final LocalDateTime to;

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

    /**
     * Returns the start date and time of the event.
     *
     * @return Start datetime (non-null).
     */
    public LocalDateTime getFrom() {
        return from;
    }

    /**
     * Returns the end date and time of the event.
     *
     * @return End datetime (non-null).
     */
    public LocalDateTime getTo() {
        return to;
    }

    @Override
    public String toString() {
        // Defensive fallback in case assertions are disabled and a bug slips through.
        if (from == null || to == null) {
            return "[E]" + super.toString() + " (from: ? to: ?)";
        }

        boolean startHasTime = !(from.getHour() == 0 && from.getMinute() == 0);
        boolean endHasTime = !(to.getHour() == 0 && to.getMinute() == 0);

        String fromStr = startHasTime
                ? from.format(OUTPUT_DATETIME)
                : from.toLocalDate().format(OUTPUT_DATE);

        String toStr = endHasTime
                ? to.format(OUTPUT_DATETIME)
                : to.toLocalDate().format(OUTPUT_DATE);

        return "[E]" + super.toString()
                + " (from: " + fromStr
                + " to: " + toStr + ")";
    }
}
