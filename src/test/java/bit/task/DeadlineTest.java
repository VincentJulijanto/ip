package bit.task;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeadlineTest {

    @Test
    public void constructor_setsDeadlineCorrectly() {
        LocalDateTime deadlineTime = LocalDateTime.of(2029, 12, 30, 14, 0);
        Deadline d = new Deadline("submit report", deadlineTime);

        assertEquals("submit report", d.getDescription());
    }

    @Test
    public void toString_containsDeadlineTag() {
        LocalDateTime deadlineTime = LocalDateTime.of(2029, 12, 30, 14, 0);
        Deadline d = new Deadline("submit report", deadlineTime);

        String output = d.toString();

        assertTrue(output.contains("[D]"));
    }

    @Test
    public void toString_containsTime() {
        LocalDateTime deadlineTime = LocalDateTime.of(2029, 12, 30, 14, 0);
        Deadline d = new Deadline("submit report", deadlineTime);

        String output = d.toString();

        assertTrue(output.contains("2:00PM"));
    }

    @Test
    public void markDone_deadlineMarkedDone() {
        LocalDateTime deadlineTime = LocalDateTime.of(2029, 12, 30, 14, 0);
        Deadline d = new Deadline("submit report", deadlineTime);

        d.markDone();

        assertTrue(d.isDone());
    }
}
