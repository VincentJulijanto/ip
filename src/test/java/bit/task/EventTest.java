package bit.task;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventTest {

    @Test
    public void constructor_validTimes_createsEvent() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 18, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 19, 0);

        Event event = new Event("meeting", start, end);

        assertEquals(start, event.getFrom());
        assertEquals(end, event.getTo());
    }

    @Test
    public void constructor_endBeforeStart_throwsException() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 18, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 17, 0);

        assertThrows(IllegalArgumentException.class, () -> {
            new Event("invalid event", start, end);
        });
    }

    @Test
    public void toString_containsEventTag() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 18, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 19, 0);

        Event event = new Event("meeting", start, end);

        String output = event.toString();

        assertEquals(true, output.contains("[E]"));
    }
}
