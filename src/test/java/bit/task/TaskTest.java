package bit.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskTest {

    @Test
    public void markDone_marksTaskCorrectly() {
        Todo t = new Todo("read book");

        t.markDone();

        assertTrue(t.isDone());
    }

    @Test
    public void markUndone_unmarksTaskCorrectly() {
        Todo t = new Todo("read book");

        t.markDone();
        t.markUndone();

        assertFalse(t.isDone());
    }

    @Test
    public void getStatusIcon_whenDone_returnsX() {
        Todo t = new Todo("read book");

        t.markDone();

        assertEquals("X", t.getStatusIcon());
    }

    @Test
    public void getStatusIcon_whenNotDone_returnsBlank() {
        Todo t = new Todo("read book");

        assertEquals(" ", t.getStatusIcon());
    }
}
