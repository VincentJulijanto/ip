import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Bit is a simple command-line task manager.
 * It supports adding tasks (todo, deadline, event),
 * listing tasks, marking/unmarking tasks as done,
 * deleting tasks, and exiting the program.
 *
 * Tasks are stored using parallel arrays for simplicity.
 */
public class Bit {

    /** Divider line used for consistent UI output */
    public static final String LINE =
            "____________________________________________________________";

    public static final String DATA_FILE_PATH = "./data/bit.txt";
    public static final int MAX_TASKS = 100;

    /**
     * Entry point of the Bit application.
     * Continuously reads user commands and executes them
     * until the user enters "bye".
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Path filePath = Paths.get(DATA_FILE_PATH);

        // Initial greeting and command guide
        System.out.println(LINE);
        System.out.println("Hello! I'm Bit");
        System.out.println("What can I do for you?");
        System.out.println();
        System.out.println("You can try commands like:");
        System.out.println("  - todo <description>");
        System.out.println("  - deadline <description> /by <time>");
        System.out.println("  - event <description> /from <start> /to <end>");
        System.out.println("  - list");
        System.out.println("  - mark <number>");
        System.out.println("  - unmark <number>");
        System.out.println("  - bye");
        System.out.println(LINE);

        /*
         * Parallel arrays to store task data.
         * Each index represents a single task.
         */
        String[] desc = new String[MAX_TASKS];     // task description
        String[] type = new String[MAX_TASKS];     // T, D, or E
        String[] extra = new String[MAX_TASKS];    // deadline/event details
        boolean[] isDone = new boolean[MAX_TASKS]; // completion status
        int count = 0;                       // number of tasks stored

        count = loadTasks(filePath, desc, type, extra, isDone);

        // Main command-processing loop
        while (true) {
            String input = sc.nextLine().trim();

            // Ignore empty input
            if (input.isEmpty()) {
                continue;
            }

            String command = input.toLowerCase();

            // Exit command
            if (command.equals("bye")) {
                System.out.println(LINE);
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(LINE);
                break;
            }

            // List all tasks
            if (command.equals("list")) {
                System.out.println(LINE);

                if (count == 0) {
                    System.out.println("Hey, your task list is empty.");
                } else {
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < count; i++) {
                        String status = isDone[i] ? "X" : " ";
                        System.out.println((i + 1) + ".[" + type[i] + "][" + status + "] "
                                + desc[i] + extra[i]);
                    }
                }

                System.out.println(LINE);
                continue;
            }

            // Mark a task as done
            if (command.startsWith("mark ")) {
                int idx = parseIndex(command.substring(5));

                // Validate task index
                if (idx < 1 || idx > count) {
                    System.out.println(LINE);
                    System.out.println("Invalid task number.");
                    System.out.println(LINE);
                    continue;
                }

                isDone[idx - 1] = true;
                saveTasks(filePath, desc, type, extra, isDone, count);

                System.out.println(LINE);
                System.out.println("Nice! I've marked this task as done:");
                System.out.println("  [" + type[idx - 1] + "][X] "
                        + desc[idx - 1] + extra[idx - 1]);
                System.out.println(LINE);
                continue;
            }

            // Unmark a task
            if (command.startsWith("unmark ")) {
                int idx = parseIndex(command.substring(7));

                if (idx < 1 || idx > count) {
                    System.out.println(LINE);
                    System.out.println("Invalid task number.");
                    System.out.println(LINE);
                    continue;
                }

                isDone[idx - 1] = false;
                saveTasks(filePath, desc, type, extra, isDone, count);

                System.out.println(LINE);
                System.out.println("OK, I've marked this task as not done yet:");
                System.out.println("  [" + type[idx - 1] + "][ ] "
                        + desc[idx - 1] + extra[idx - 1]);
                System.out.println(LINE);
                continue;
            }

            /*
             * Delete a task by shifting all subsequent tasks
             * one position to the left.
             */
            if (command.startsWith("delete ")) {
                int idx = parseIndex(command.substring(7));

                if (idx < 1 || idx > count) {
                    System.out.println(LINE);
                    System.out.println("Invalid task number.");
                    System.out.println(LINE);
                    continue;
                }

                int removeIndex = idx - 1;

                // Store removed task for feedback message
                String removedType = type[removeIndex];
                String removedDesc = desc[removeIndex];
                String removedExtra = extra[removeIndex];
                boolean removedDone = isDone[removeIndex];

                // Shift tasks left to fill the gap
                for (int i = removeIndex; i < count - 1; i++) {
                    type[i] = type[i + 1];
                    desc[i] = desc[i + 1];
                    extra[i] = extra[i + 1];
                    isDone[i] = isDone[i + 1];
                }

                // Clear last slot
                type[count - 1] = null;
                desc[count - 1] = null;
                extra[count - 1] = null;
                isDone[count - 1] = false;

                count--;
                saveTasks(filePath, desc, type, extra, isDone, count);

                String status = removedDone ? "X" : " ";

                System.out.println(LINE);
                System.out.println("Noted. I've removed this task:");
                System.out.println("  [" + removedType + "][" + status + "] "
                        + removedDesc + removedExtra);
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // Add todo
            if (command.startsWith("todo ")) {
                String taskDesc = input.substring(5).trim();

                if (taskDesc.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("The description of a todo cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                if (count >= MAX_TASKS) {
                    System.out.println(LINE);
                    System.out.println("Task list is full.");
                    System.out.println(LINE);
                    continue;
                }

                type[count] = "T";
                desc[count] = taskDesc;
                extra[count] = "";
                isDone[count] = false;
                count++;

                saveTasks(filePath, desc, type, extra, isDone, count);

                System.out.println(LINE);
                System.out.println("Got it. I've added this task:");
                System.out.println("  [T][ ] " + taskDesc);
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // Add deadline
            if (command.startsWith("deadline ")) {
                int byIndex = input.toLowerCase().indexOf(" /by ");
                if (byIndex == -1) {
                    System.out.println(LINE);
                    System.out.println("Please use: deadline <description> /by <time>");
                    System.out.println(LINE);
                    continue;
                }

                String taskDesc = input.substring(9, byIndex).trim(); // after "deadline "
                String by = input.substring(byIndex + 5).trim();      // after " /by "

                if (taskDesc.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("The description of a deadline cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                if (by.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("The /by time cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                if (count >= MAX_TASKS) {
                    System.out.println(LINE);
                    System.out.println("Task list is full.");
                    System.out.println(LINE);
                    continue;
                }

                type[count] = "D";
                desc[count] = taskDesc;
                extra[count] = " (by: " + by + ")";
                isDone[count] = false;
                count++;

                saveTasks(filePath, desc, type, extra, isDone, count);

                System.out.println(LINE);
                System.out.println("Got it. I've added this task:");
                System.out.println("  [D][ ] " + taskDesc + " (by: " + by + ")");
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // Add event
            if (command.startsWith("event ")) {
                String lower = input.toLowerCase();
                int fromIndex = lower.indexOf(" /from ");
                int toIndex = lower.indexOf(" /to ");

                if (fromIndex == -1 || toIndex == -1 || toIndex < fromIndex) {
                    System.out.println(LINE);
                    System.out.println("Please use: event <description> /from <start> /to <end>");
                    System.out.println(LINE);
                    continue;
                }

                String taskDesc = input.substring(6, fromIndex).trim(); // after "event "
                String from = input.substring(fromIndex + 7, toIndex).trim(); // after " /from "
                String to = input.substring(toIndex + 5).trim(); // after " /to "

                if (taskDesc.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("The description of an event cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                if (from.isEmpty() || to.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("The /from and /to times cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                if (count >= MAX_TASKS) {
                    System.out.println(LINE);
                    System.out.println("Task list is full.");
                    System.out.println(LINE);
                    continue;
                }

                type[count] = "E";
                desc[count] = taskDesc;
                extra[count] = " (from: " + from + " to: " + to + ")";
                isDone[count] = false;
                count++;

                saveTasks(filePath, desc, type, extra, isDone, count);

                System.out.println(LINE);
                System.out.println("Got it. I've added this task:");
                System.out.println("  [E][ ] " + taskDesc + " (from: " + from + " to: " + to + ")");
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // Unknown command fallback
            System.out.println(LINE);
            System.out.println("OOPS!!! I'm sorry, but I don't know what that means :-(");
            System.out.println(LINE);
        }

        sc.close();
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
     * Ensures that the data file and its parent directory exist.
     *
     * @param filePath Path to the data file
     * @throws IOException if file creation fails
     */
    private static void ensureDataFileExists(Path filePath) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
    }

    /**
     * Loads tasks from disk into the parallel arrays.
     *
     * @param filePath Path to the data file
     * @param desc Array storing task descriptions
     * @param type Array storing task types (T/D/E)
     * @param extra Array storing additional task info
     * @param isDone Array storing task completion status
     * @return number of tasks loaded
     */
    private static int loadTasks(Path filePath, String[] desc, String[] type,
                                 String[] extra, boolean[] isDone) {
        try {
            ensureDataFileExists(filePath);

            List<String> lines = Files.readAllLines(filePath);
            int count = 0;

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s*\\|\\s*", -1);
                if (parts.length != 4) {
                    continue;
                }

                String t = parts[0].trim();
                String d = parts[1].trim();
                String description = parts[2];
                String ex = parts[3];

                if (!(t.equals("T") || t.equals("D") || t.equals("E"))) {
                    continue;
                }
                if (!(d.equals("0") || d.equals("1"))) {
                    continue;
                }
                if (count >= MAX_TASKS) {
                    break;
                }

                type[count] = t;
                isDone[count] = d.equals("1");
                desc[count] = description;
                extra[count] = ex;
                count++;
            }

            return count;

        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Saves all current tasks to disk.
     *
     * @param filePath Path to the data file
     * @param desc Array storing task descriptions
     * @param type Array storing task types
     * @param extra Array storing additional task info
     * @param isDone Array storing task completion status
     * @param count Number of active tasks
     */
    private static void saveTasks(Path filePath, String[] desc, String[] type,
                                  String[] extra, boolean[] isDone, int count) {
        try {
            ensureDataFileExists(filePath);

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                for (int i = 0; i < count; i++) {
                    String done = isDone[i] ? "1" : "0";
                    String line = type[i] + " | " + done + " | "
                            + desc[i] + " | " + extra[i];
                    writer.write(line);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println(LINE);
            System.out.println("Oops, I couldn't save your tasks to disk.");
            System.out.println(LINE);
        }
    }
}

