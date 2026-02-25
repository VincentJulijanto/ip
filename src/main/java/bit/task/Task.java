package bit.task;

/**
 * Represents a basic task in the Bit task manager.
 * A task has a description and a completion status.
 */
public class Task {

    protected final String description;
    protected boolean isDone;

    /**
     * Creates a new task with the given description.
     *
     * @param description task description
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks the task as done.
     */
    public void markDone() {
        isDone = true;
    }

    /**
     * Marks the task as not done.
     */
    public void markUndone() {
        isDone = false;
    }

    /**
     * Returns whether the task is done.
     *
     * @return {@code true} if the task is completed, {@code false} otherwise
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Returns the status icon for the task.
     *
     * @return "X" if done, otherwise " "
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns the task description.
     *
     * @return task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks whether the task description contains the given keyword.
     * The comparison is case-insensitive.
     *
     * @param keyword keyword to search for
     * @return {@code true} if the description contains the keyword,
     *         {@code false} otherwise
     */
    public boolean containsKeyword(String keyword) {
        return description.toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}