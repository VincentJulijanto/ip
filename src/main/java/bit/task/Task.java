package bit.task;

/**
 * Represents a basic task in the Bit task manager.
 * A {@code Task} has a non-null, non-blank description and a completion status.
 */
public class Task {

    /** Non-null, non-blank task description. */
    protected final String description;

    /** Whether the task has been marked as done. */
    protected boolean isDone;

    /**
     * Creates a new task with the given description.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code description} is not {@code null}.</li>
     *   <li>{@code description} is not blank.</li>
     * </ul>
     *
     * @param description Task description.
     */
    public Task(String description) {
        assert description != null : "Task description must not be null";
        assert !description.isBlank() : "Task description must not be blank";

        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks the task as done.
     *
     * <p><b>Assumption:</b> Marking a task done is always valid regardless of current state.
     */
    public void markDone() {
        isDone = true;
    }

    /**
     * Marks the task as not done.
     *
     * <p><b>Assumption:</b> Marking a task undone is always valid regardless of current state.
     */
    public void markUndone() {
        isDone = false;
    }

    /**
     * Returns whether the task is done.
     *
     * @return {@code true} if the task is completed, {@code false} otherwise.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the status icon for the task.
     *
     * @return {@code "X"} if done, otherwise {@code " "}.
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns the task description.
     *
     * @return The non-null description of this task.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks whether the task description contains the given keyword.
     * The comparison is case-insensitive.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code keyword} is not {@code null}.</li>
     * </ul>
     *
     * @param keyword Keyword to search for (non-null).
     * @return {@code true} if the description contains the keyword (case-insensitive),
     *         {@code false} otherwise.
     */
    public boolean containsKeyword(String keyword) {
        assert keyword != null : "Keyword must not be null";

        // Defensive: even if asserts are disabled, avoid NPE in production runs.
        if (keyword == null) {
            return false;
        }

        return description.toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
