package bit.task;

/**
 * Represents a todo task that contains only a description
 * and does not have any associated date or time.
 */
public class Todo extends Task {

    /**
     * Creates a todo task with the given description.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *   <li>{@code description} is not {@code null}</li>
     *   <li>{@code description} is not blank</li>
     * </ul>
     *
     * @param description Description of the todo task
     */
    public Todo(String description) {
        super(description);

        // Assumption inherited from Task constructor
        assert description != null : "Todo description must not be null";
        assert !description.isBlank() : "Todo description must not be blank";
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}