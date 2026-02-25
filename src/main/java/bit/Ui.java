package bit;

import java.util.Scanner;

/**
 * Ui handles all user interaction for the Bit application.
 * It is responsible for displaying messages, reading user input,
 * and formatting output shown in the command-line interface.
 */
public class Ui {
    public static final String LINE =
            "____________________________________________________________";

    private final Scanner scanner;

    /**
     * Creates a new Ui instance and initializes the scanner
     * used to read user input from the console.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    // Small pause
    private void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Displays the welcome message, available commands,
     * and usage tips to the user when the program starts.
     */
    public void showWelcome() {
        System.out.println(LINE);
        System.out.println("Hello! I'm Bit 🤖");
        System.out.println("Your personal task manager.");
        pause(1050);
        System.out.println();

        System.out.println("What can I do for you?");
        pause(1050);
        System.out.println();

        System.out.println("Commands:");
        pause(1050);
        System.out.println("  todo <description>");
        System.out.println("  deadline <description> /by <date>");
        System.out.println("  event <description> /from <start> /to <end>");
        System.out.println("  list");
        System.out.println("  mark <number>");
        System.out.println("  unmark <number>");
        System.out.println("  delete <number>");
        System.out.println("  find <keyword>");
        System.out.println("  bye");
        System.out.println();
        pause(950);

        System.out.println("Tips:");
        pause(950);
        System.out.println("  • Dates can be written as:");
        System.out.println("      YYYY-MM-DD          (e.g. 2029-12-30)");
        System.out.println("      YYYY-MM-DD HHmm     (e.g. 2029-12-30 1400)");
        System.out.println();
        System.out.println("  • '/' is optional:");
        System.out.println("      deadline submit report by 2029-12-30");
        System.out.println("      event meeting from 2029-12-30 to 2029-12-30");

        System.out.println(LINE);
    }

    /**
     * Reads the next command entered by the user.
     *
     * @return The trimmed command string entered by the user
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /**
     * Prints a divider line used to separate sections of output.
     */
    public void showLine() {
        System.out.println(LINE);
    }

    /**
     * Displays a message to the user.
     *
     * @param message The message to display
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Displays the goodbye message when the user exits the program.
     */
    public void showBye() {
        System.out.println(LINE);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(LINE);
    }

    /**
     * Closes the scanner used for reading user input.
     */
    public void close() {
        scanner.close();
    }
}