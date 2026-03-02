package bit;

import bit.storage.Storage;
import bit.task.Task;
import bit.task.Todo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StorageTest {

    @Test
    public void saveAndLoadTasks_tasksPersistCorrectly() throws IOException {
        Path tempFile = Files.createTempFile("bit-test", ".txt");

        Storage storage = new Storage(tempFile);

        Task[] tasks = new Task[10];
        tasks[0] = new Todo("read book");

        storage.saveTasks(tasks, 1);

        Task[] loaded = new Task[10];
        int count = storage.loadTasks(loaded);

        assertEquals(1, count);
        assertEquals("read book", loaded[0].getDescription());
    }
}
