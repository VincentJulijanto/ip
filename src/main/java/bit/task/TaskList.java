package bit.task;

/**
 * Represents a list of tasks stored in a fixed-size array.
 * <p>
 * TaskList is responsible for:
 * <ul>
 *     <li>Storing tasks in memory</li>
 *     <li>Managing the current number of tasks</li>
 *     <li>Providing operations such as add, delete, mark, unmark, update</li>
 *     <li>Providing formatted outputs for list and find</li>
 * </ul>
 * <p>
 * This class also temporarily exposes its internal array to support existing
 * storage logic without refactoring {@code Storage} immediately.
 */
public class TaskList {

    /** Internal array that stores tasks. */
    private final Task[] tasks;

    /** Number of tasks currently stored in the list. */
    private int count;

    /**
     * Constructs a TaskList with a fixed capacity.
     *
     * @param capacity Maximum number of tasks this list can hold
     */
    public TaskList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        tasks = new Task[capacity];
        count = 0;
    }

    /**
     * Returns the internal task array.
     * <p>
     * This is a temporary "bridge" method to keep {@code Storage} compatible
     * while refactoring the application away from direct array handling.
     *
     * @return The internal array storing tasks
     */
    public Task[] getInternalArray() {
        return tasks;
    }

    /**
     * Sets the current number of tasks in this list.
     * <p>
     * This is mainly used after loading tasks from disk using {@code Storage}.
     * The value is clamped between 0 and capacity.
     *
     * @param newCount The number of tasks loaded
     */
    public void setCount(int newCount) {
        this.count = Math.max(0, Math.min(newCount, tasks.length));
    }

    /**
     * Returns the number of tasks currently in the list.
     *
     * @return Current number of tasks
     */
    public int size() {
        return count;
    }

    /**
     * Returns whether the task list is empty.
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns whether the task list is full (cannot add more tasks).
     *
     * @return {@code true} if full, {@code false} otherwise
     */
    public boolean isFull() {
        return count >= tasks.length;
    }

    /**
     * Converts a 1-based index into a 0-based index.
     *
     * @param oneBasedIndex Index provided by the user (1-based)
     * @return Converted 0-based index
     */
    private int toZeroBasedIndex(int oneBasedIndex) {
        return oneBasedIndex - 1;
    }

    /**
     * Retrieves the task at the given 1-based index.
     *
     * @param oneBasedIndex Task number (1-based)
     * @return The task at that index
     * @throws IndexOutOfBoundsException If the index is invalid
     */
    public Task getByOneBasedIndex(int oneBasedIndex) {
        int idx = toZeroBasedIndex(oneBasedIndex);
        if (idx < 0 || idx >= count) {
            throw new IndexOutOfBoundsException("Invalid task number.");
        }
        return tasks[idx];
    }

    /**
     * Adds a new task into the list.
     *
     * @param task Task to add
     * @return The task that was added
     * @throws IllegalArgumentException If task is {@code null}
     * @throws IllegalStateException If the list is already full
     */
    public Task add(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (isFull()) {
            throw new IllegalStateException("Task list is full.");
        }
        tasks[count++] = task;
        return task;
    }

    /**
     * Deletes a task at the given 1-based index and shifts remaining tasks left.
     *
     * @param oneBasedIndex Task number (1-based)
     * @return The removed task
     * @throws IndexOutOfBoundsException If the index is invalid
     */
    public Task delete(int oneBasedIndex) {
        int removeIndex = toZeroBasedIndex(oneBasedIndex);
        if (removeIndex < 0 || removeIndex >= count) {
            throw new IndexOutOfBoundsException("Invalid task number.");
        }

        Task removed = tasks[removeIndex];

        for (int i = removeIndex; i < count - 1; i++) {
            tasks[i] = tasks[i + 1];
        }

        tasks[count - 1] = null;
        count--;
        return removed;
    }

    /**
     * Marks the task at the given index as done.
     *
     * @param oneBasedIndex Task number (1-based)
     * @return The updated task
     * @throws IndexOutOfBoundsException If the index is invalid
     */
    public Task mark(int oneBasedIndex) {
        Task t = getByOneBasedIndex(oneBasedIndex);
        t.markDone();
        return t;
    }

    /**
     * Marks the task at the given index as not done.
     *
     * @param oneBasedIndex Task number (1-based)
     * @return The updated task
     * @throws IndexOutOfBoundsException If the index is invalid
     */
    public Task unmark(int oneBasedIndex) {
        Task t = getByOneBasedIndex(oneBasedIndex);
        t.markUndone();
        return t;
    }

    /**
     * Updates the description of the task at the given index.
     *
     * @param oneBasedIndex Task number (1-based)
     * @param newDesc New description
     * @return The updated task
     * @throws IndexOutOfBoundsException If index is invalid
     * @throws IllegalArgumentException If new description is empty
     */
    public Task update(int oneBasedIndex, String newDesc) {
        if (newDesc == null || newDesc.trim().isEmpty()) {
            throw new IllegalArgumentException("New description cannot be empty.");
        }
        Task t = getByOneBasedIndex(oneBasedIndex);
        t.setDescription(newDesc.trim());
        return t;
    }

    /**
     * Returns a formatted string listing all tasks.
     *
     * @return Formatted list output (or empty message if no tasks)
     */
    public String formatList() {
        if (count == 0) {
            return "Your task list is empty.";
        }

        StringBuilder sb = new StringBuilder("Here are the tasks in your list:\n");
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%2d) %s%n", i + 1, tasks[i]));
        }
        return sb.toString().trim();
    }

    /**
     * Returns a formatted string listing only tasks that match the keyword.
     * Matching is case-insensitive.
     *
     * @param keyword Keyword to search for
     * @return Formatted list output, or a no-match message
     * @throws IllegalArgumentException If keyword is empty
     */
    public String formatFind(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Please provide a keyword to search.");
        }

        String k = keyword.trim().toLowerCase();
        StringBuilder sb = new StringBuilder("Here are the matching tasks in your list:\n");

        int matches = 0;
        for (int i = 0; i < count; i++) {
            if (tasks[i].containsKeyword(k)) {
                matches++;
                sb.append(String.format("%2d) %s%n", i + 1, tasks[i]));
            }
        }

        if (matches == 0) {
            return "No matching tasks found.";
        }

        return sb.toString().trim();
    }
}
