package bit.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an event task with a start and end time.
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
     * @param description Description of the event
     * @param from Start date and time of the event
     * @param to End date and time of the event
     */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);

        assert from != null : "Event start datetime must not be null";
        assert to != null : "Event end datetime must not be null";
        assert !to.isBefore(from) : "Event end datetime must not be before start datetime";

        this.from = from;
        this.to = to;
    }

    /**
     * Returns the start date and time of the event.
     *
     * @return Start datetime
     */
    public LocalDateTime getFrom() {
        return from;
    }

    /**
     * Returns the end date and time of the event.
     *
     * @return End datetime
     */
    public LocalDateTime getTo() {
        return to;
    }

    @Override
    public String toString() {
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
