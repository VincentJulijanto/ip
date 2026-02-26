package bit.task;

/**
 * Represents a todo task that contains only a description
 * and does not have any associated date or time.
 */
public class Todo extends Task {

    /**
     * Creates a todo task with the given description.
     *
     * <p>The description validation is handled by the {@code Task} constructor.</p>
     *
     * @param description description of the todo task
     */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
