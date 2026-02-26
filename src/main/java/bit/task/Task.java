package bit.task;

/**
 * Represents a basic task in the Bit task manager.
 * A {@code Task} has a non-null, non-blank description and a completion status.
 */
public class Task {

    /** Non-null, non-blank task description. */
    protected String description;

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
        this.isDone = true;
    }

    /**
     * Marks the task as not done.
     *
     * <p><b>Assumption:</b> Marking a task undone is always valid regardless of current state.
     */
    public void markUndone() {
        this.isDone = false;
    }

    /**
     * Returns whether the task is done.
     *
     * @return {@code true} if the task is completed, {@code false} otherwise.
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * Returns the status icon for the task.
     *
     * @return {@code "X"} if done, otherwise {@code " "}.
     */
    public String getStatusIcon() {
        return this.isDone ? "X" : " ";
    }

    /**
     * Returns the task description.
     *
     * @return The non-null description of this task.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Updates the description of this task.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code description} is not {@code null}.</li>
     *   <li>{@code description} is not blank.</li>
     * </ul>
     *
     * @param description The new description of the task.
     */
    public void setDescription(String description) {
        assert description != null : "Task description must not be null";
        assert !description.isBlank() : "Task description must not be blank";

        this.description = description;
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

        return description.toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + this.description;
    }
}
