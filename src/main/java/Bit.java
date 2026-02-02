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

    /**
     * Entry point of the Bit application.
     * Continuously reads user commands and executes them
     * until the user enters "bye".
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Initial greeting and command guide
        System.out.println(LINE);
        System.out.println("Hello! I'm Bit");
        System.out.println("What can I do for you?");
        System.out.println();
        System.out.println("You can try commands like:");
        System.out.println("  - <any task description>  (adds a task)");
        System.out.println("  - list");
        System.out.println("  - mark <number>");
        System.out.println("  - unmark <number>");
        System.out.println("  - bye");
        System.out.println(LINE);

        /*
         * Parallel arrays to store task data.
         * Each index represents a single task.
         */
        String[] desc = new String[100];     // task description
        String[] type = new String[100];     // T, D, or E
        String[] extra = new String[100];    // deadline/event details
        boolean[] isDone = new boolean[100]; // completion status
        int count = 0;                       // number of tasks stored

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

                String status = removedDone ? "X" : " ";

                System.out.println(LINE);
                System.out.println("Noted. I've removed this task:");
                System.out.println("  [" + removedType + "][" + status + "] "
                        + removedDesc + removedExtra);
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
}

