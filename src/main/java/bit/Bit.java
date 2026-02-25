package bit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import bit.task.Task;
import bit.task.Todo;
import bit.task.Deadline;
import bit.task.Event;

/**
 * Bit is a simple command-line task manager.
 * It supports adding tasks (todo, deadline, event),
 * listing tasks, marking/unmarking tasks as done,
 * deleting tasks, and exiting the program.
 *
 * Tasks are stored using parallel arrays for simplicity.
 */
public class Bit {

    public static final String DATA_FILE_PATH = "./data/bit.txt";
    public static final int MAX_TASKS = 100;
    public static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter INPUT_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");

    /**
     * Entry point of the Bit application.
     * Continuously reads user commands and executes them
     * until the user enters "bye".
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Ui ui = new Ui();

        Path filePath = Paths.get(DATA_FILE_PATH);
        Storage storage = new Storage(filePath);

        // Initial greeting and command guide
        ui.showWelcome();

        /*
         * Array of Task objects to store task data.
         */
        Task[] tasks = new Task[MAX_TASKS];
        int count = 0;                       // number of tasks stored

        count = storage.loadTasks(tasks);

        // Main command-processing loop
        while (true) {
            String input = ui.readCommand();

            // Ignore empty input
            if (input.isEmpty()) {
                continue;
            }

            String command = input.toLowerCase();

            // Exit command
            if (command.equals("bye")) {
                ui.showBye();
                break;
            }

            // List all tasks
            if (command.equals("list")) {
                handleList(ui, tasks, count);
                continue;
            }

            if (command.equals("mark") || command.equals("unmark") || command.equals("delete")) {
                ui.showLine();
                ui.showMessage("Please provide a task number.");
                ui.showLine();
                continue;
            }

            // Mark a task as done
            if (command.startsWith("mark ")) {
                handleMark(ui, storage, command, tasks, count);
                continue;
            }

            // Unmark a task
            if (command.startsWith("unmark ")) {
                handleUnmark(ui, storage, command, tasks, count);
                continue;
            }

            // Delete
            if (command.startsWith("delete ")) {
                count = handleDelete(ui, storage, command, tasks, count);
                continue;
            }

            // Add todo
            if (command.startsWith("todo ")) {
                count = handleTodo(ui, storage, input, tasks, count);
                continue;
            }

            // Add deadline
            if (command.startsWith("deadline ")) {
                count = handleDeadline(ui, storage, input, tasks, count);
                continue;
            }

            // Add event
            if (command.startsWith("event ")) {
                count = handleEvent(ui, storage, input, tasks, count);
                continue;
            }

            // Unknown command fallback
            ui.showLine();
            ui.showMessage("OOPS!!! I'm sorry, but I don't know what that means :-(");
            ui.showLine();
        }

        ui.close();
    }

    /**
     * Parses a task index from user input.
     *
     * @param s String containing the index
     * @return Parsed integer index, or -1 if invalid
     */
    private static int parseIndex(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parses either a date-time (yyyy-MM-dd HHmm) or a date (yyyy-MM-dd).
     * If only a date is provided, it is treated as start of day (00:00).
     * Shows an error message through the UI if the input is invalid.
     *
     * @param ui  User interface used to display error messages
     * @param raw Raw date/time input from the user
     * @return Parsed LocalDateTime, or {@code null} if the input is invalid
     */
    private static LocalDateTime parseDateOrDateTime(Ui ui, String raw) {
        try {
            return LocalDateTime.parse(raw, INPUT_DATETIME);
        } catch (DateTimeParseException ignoredDt) {
            try {
                LocalDate d = LocalDate.parse(raw, INPUT_DATE);
                return d.atStartOfDay();
            } catch (DateTimeParseException ignoredDate) {
                ui.showLine();
                ui.showMessage("Please use: YYYY-MM-DD or YYYY-MM-DD HHmm (e.g. 2019-10-15 or 2019-10-15 1400)");
                ui.showLine();
                return null;
            }
        }
    }

    /**
     * Saves the current tasks to storage.
     * If saving fails, shows an error message through the UI.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist tasks
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     * @return {@code true} if saving succeeds, {@code false} otherwise
     */
    private static boolean saveOrShowError(Ui ui, Storage storage, Task[] tasks, int count) {
        try {
            storage.saveTasks(tasks, count);
            return true;
        } catch (IOException e) {
            ui.showLine();
            ui.showMessage("Oops, I couldn't save your tasks to disk.");
            ui.showLine();
            return false;
        }
    }

    /**
     * Displays all tasks currently stored in the task list.
     *
     * @param ui    User interface used to display messages
     * @param tasks Array containing all stored tasks
     * @param count Number of tasks currently in the list
     */
    private static void handleList(Ui ui, Task[] tasks, int count) {
        ui.showLine();

        if (count == 0) {
            ui.showMessage("Hey, your task list is empty.");
            ui.showLine();
            return;
        }

        ui.showMessage("Here are the tasks in your list:");
        for (int i = 0; i < count; i++) {
            ui.showMessage((i + 1) + "." + tasks[i]);
        }

        ui.showLine();
    }

    /**
     * Marks the specified task as done.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist updated tasks
     * @param command Full user command containing the task index (e.g. "mark 2")
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     */
    private static void handleMark(Ui ui, Storage storage, String command, Task[] tasks, int count) {
        int idx = parseIndex(command.substring(5)); // after "mark "

        if (idx < 1 || idx > count) {
            ui.showLine();
            ui.showMessage("Invalid task number.");
            ui.showLine();
            return;
        }

        Task task = tasks[idx - 1];
        task.markDone();

        if (!saveOrShowError(ui, storage, tasks, count)) {
            return;
        }

        ui.showLine();
        ui.showMessage("Nice! I've marked this task as done:");
        ui.showMessage("  " + task);
        ui.showLine();
    }

    /**
     * Marks the specified task as not done.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist updated tasks
     * @param command Full user command containing the task index (e.g. "unmark 2")
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     */
    private static void handleUnmark(Ui ui, Storage storage, String command, Task[] tasks, int count) {
        int idx = parseIndex(command.substring(7)); // after "unmark "

        if (idx < 1 || idx > count) {
            ui.showLine();
            ui.showMessage("Invalid task number.");
            ui.showLine();
            return;
        }

        Task task = tasks[idx - 1];
        task.markUndone();

        if (!saveOrShowError(ui, storage, tasks, count)) {
            return;
        }

        ui.showLine();
        ui.showMessage("OK, I've marked this task as not done yet:");
        ui.showMessage("  " + task);
        ui.showLine();
    }

    /**
     * Deletes the specified task from the task list and shifts remaining tasks.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist updated tasks
     * @param command Full user command containing the task index (e.g. "delete 3")
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     * @return Updated number of tasks after deletion
     */
    private static int handleDelete(Ui ui, Storage storage, String command, Task[] tasks, int count) {
        int idx = parseIndex(command.substring(7)); // after "delete "

        if (idx < 1 || idx > count) {
            ui.showLine();
            ui.showMessage("Invalid task number.");
            ui.showLine();
            return count;
        }

        int removeIndex = idx - 1;
        Task removed = tasks[removeIndex];

        for (int i = removeIndex; i < count - 1; i++) {
            tasks[i] = tasks[i + 1];
        }

        tasks[count - 1] = null;
        count--;

        if (!saveOrShowError(ui, storage, tasks, count)) {
            return count;
        }

        ui.showLine();
        ui.showMessage("Noted. I've removed this task:");
        ui.showMessage("  " + removed);
        ui.showMessage("Now you have " + count + " tasks in the list.");
        ui.showLine();

        return count;
    }

    /**
     * Adds a new todo task to the task list.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist updated tasks
     * @param input   Full user input containing the todo description
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     * @return Updated number of tasks after adding the todo
     */
    private static int handleTodo(Ui ui, Storage storage, String input, Task[] tasks, int count) {
        String taskDesc = input.substring(5).trim(); // after "todo "

        if (taskDesc.isEmpty()) {
            ui.showLine();
            ui.showMessage("The description of a todo cannot be empty.");
            ui.showLine();
            return count;
        }

        if (count >= MAX_TASKS) {
            ui.showLine();
            ui.showMessage("Task list is full.");
            ui.showLine();
            return count;
        }

        Task newTask = new Todo(taskDesc);
        tasks[count] = newTask;
        count++;

        if (!saveOrShowError(ui, storage, tasks, count)) {
            return count;
        }

        ui.showLine();
        ui.showMessage("Got it. I've added this task:");
        ui.showMessage("  " + newTask);
        ui.showMessage("Now you have " + count + " tasks in the list.");
        ui.showLine();

        return count;
    }

    /**
     * Adds a deadline task with a due date.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist updated tasks
     * @param input   Full user input containing description and /by time
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     * @return Updated number of tasks after adding the deadline
     */
    private static int handleDeadline(Ui ui, Storage storage, String input, Task[] tasks, int count) {
        String lower = input.toLowerCase();

        int byIndex = lower.indexOf(" /by ");
        boolean hasSlash = true;

        if (byIndex == -1) {
            byIndex = lower.indexOf(" by ");
            hasSlash = false;
        }

        if (byIndex == -1) {
            ui.showLine();
            ui.showMessage("Please use: deadline <description> /by <time>");
            ui.showLine();
            return count;
        }

        String taskDesc = input.substring(9, byIndex).trim(); // after "deadline "
        String byRaw = input.substring(byIndex + (hasSlash ? 5 : 4)).trim(); // after " /by " or " by "

        if (taskDesc.isEmpty()) {
            ui.showLine();
            ui.showMessage("The description of a deadline cannot be empty.");
            ui.showLine();
            return count;
        }

        if (byRaw.isEmpty()) {
            ui.showLine();
            ui.showMessage("The /by time cannot be empty.");
            ui.showLine();
            return count;
        }

        if (count >= MAX_TASKS) {
            ui.showLine();
            ui.showMessage("Task list is full.");
            ui.showLine();
            return count;
        }

        LocalDateTime dt = parseDateOrDateTime(ui, byRaw);
        if (dt == null) {
            return count;
        }

        // Preserve whether user gave date-only or date+time (so storage/toString can keep it nice)
        Task newTask;
        if (byRaw.contains(" ")) {
            newTask = new Deadline(taskDesc, dt);
        } else {
            newTask = new Deadline(taskDesc, dt.toLocalDate());
        }

        tasks[count] = newTask;
        count++;

        if (!saveOrShowError(ui, storage, tasks, count)) {
            return count;
        }

        ui.showLine();
        ui.showMessage("Got it. I've added this task:");
        ui.showMessage("  " + newTask);
        ui.showMessage("Now you have " + count + " tasks in the list.");
        ui.showLine();

        return count;
    }

    /**
     * Adds an event task with start and end times.
     *
     * @param ui      User interface used to display messages
     * @param storage Storage used to persist updated tasks
     * @param input   Full user input containing description, /from and /to times
     * @param tasks   Array containing all stored tasks
     * @param count   Number of tasks currently in the list
     * @return Updated number of tasks after adding the event
     */
    private static int handleEvent(Ui ui, Storage storage, String input, Task[] tasks, int count) {
        String lower = input.toLowerCase();

        // accept both "/from" and "from"
        int fromIndex = lower.indexOf(" /from ");
        boolean fromSlash = true;
        if (fromIndex == -1) {
            fromIndex = lower.indexOf(" from ");
            fromSlash = false;
        }

        // accept both "/to" and "to"
        int toIndex = lower.indexOf(" /to ");
        boolean toSlash = true;
        if (toIndex == -1) {
            toIndex = lower.indexOf(" to ");
            toSlash = false;
        }

        if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
            ui.showLine();
            ui.showMessage("Please use: event <description> /from <start> /to <end>");
            ui.showLine();
            return count;
        }

        String taskDesc = input.substring(6, fromIndex).trim(); // after "event "
        String fromRaw = input.substring(fromIndex + (fromSlash ? 7 : 6), toIndex).trim(); // after " /from " or " from "
        String toRaw = input.substring(toIndex + (toSlash ? 5 : 4)).trim(); // after " /to " or " to "

        if (taskDesc.isEmpty()) {
            ui.showLine();
            ui.showMessage("The description of an event cannot be empty.");
            ui.showLine();
            return count;
        }

        if (fromRaw.isEmpty() || toRaw.isEmpty()) {
            ui.showLine();
            ui.showMessage("The /from and /to times cannot be empty.");
            ui.showLine();
            return count;
        }

        if (count >= MAX_TASKS) {
            ui.showLine();
            ui.showMessage("Task list is full.");
            ui.showLine();
            return count;
        }

        LocalDateTime start = parseDateOrDateTime(ui, fromRaw);
        if (start == null) {
            return count;
        }

        LocalDateTime end = parseDateOrDateTime(ui, toRaw);
        if (end == null) {
            return count;
        }

        Task newTask = new Event(taskDesc, start, end);
        tasks[count] = newTask;
        count++;

        if (!saveOrShowError(ui, storage, tasks, count)) {
            return count;
        }

        ui.showLine();
        ui.showMessage("Got it. I've added this task:");
        ui.showMessage("  " + newTask);
        ui.showMessage("Now you have " + count + " tasks in the list.");
        ui.showLine();

        return count;
    }
}
