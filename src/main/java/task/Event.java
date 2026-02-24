package task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an event task with a start and end time.
 */
public class Event extends Task {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private static final DateTimeFormatter OUTPUT_DATETIME =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mma");

    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
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

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("MMM dd yyyy");

        DateTimeFormatter dateTimeFormatter =
                DateTimeFormatter.ofPattern("MMM dd yyyy, h:mma");

        boolean startHasTime =
                !(from.getHour() == 0 && from.getMinute() == 0);

        boolean endHasTime =
                !(to.getHour() == 0 && to.getMinute() == 0);

        String fromStr = startHasTime
                ? from.format(dateTimeFormatter)
                : from.toLocalDate().format(dateFormatter);

        String toStr = endHasTime
                ? to.format(dateTimeFormatter)
                : to.toLocalDate().format(dateFormatter);

        return "[E]" + super.toString()
                + " (from: " + fromStr
                + " to: " + toStr + ")";
    }
}