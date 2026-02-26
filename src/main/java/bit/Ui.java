package bit;

import java.util.Scanner;

/**
 * Ui handles all user interaction for the Bit application.
 * <p>
 * By default, Ui prints messages to the console (CLI mode).
 * For the GUI, Ui can be switched into "capture mode" where output is collected
 * into an internal buffer and returned as a String.
 */
public class Ui {

    /** Divider line used for consistent UI output. */
    public static final String LINE = "____________________________________________________________";

    private final Scanner scanner;

    // ===== GUI support: capture mode =====
    private final StringBuilder buffer = new StringBuilder();
    private boolean captureMode = false;

    /**
     * Creates a new Ui instance and initializes the scanner
     * used to read user input from the console.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Enables capture mode. When enabled, Ui stores output in an internal buffer
     * instead of printing to the console. Used by the JavaFX GUI.
     */
    public void beginCapture() {
        captureMode = true;
        buffer.setLength(0);
    }

    /**
     * Disables capture mode and returns the accumulated output.
     *
     * @return captured output text (trimmed)
     */
    public String endCapture() {
        captureMode = false;
        return buffer.toString().trim();
    }

    /**
     * Writes output either to console (CLI) or to buffer (GUI capture mode).
     *
     * @param s text to output
     */
    private void write(String s) {
        if (captureMode) {
            buffer.append(s).append("\n");
        } else {
            System.out.println(s);
        }
    }

    /**
     * Sleeps briefly (CLI only). In capture mode, this is a no-op so GUI won't freeze.
     *
     * @param ms milliseconds to pause
     */
    private void pause(int ms) {
        if (captureMode) {
            return; // never block GUI
        }
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
        write(LINE);
        write("Hello! I'm Bit 🤖");
        write("Your personal task manager.");
        pause(1050);
        write("");

        write("What can I do for you?");
        pause(1050);
        write("");

        write("Commands:");
        write("-  todo <description>");
        write("-  deadline <description> /by <date>");
        write("-  event <description> /from <start> /to <end>");
        write("-  list");
        write("-  mark <number>");
        write("-  unmark <number>");
        write("-  delete <number>");
        write("-  find <keyword>");
        write("-  bye");
        write("");
        pause(950);

        write("Tips:");
        write("  • Dates can be written as:");
        write("      YYYY-MM-DD          (e.g. 2029-12-30)");
        write("      YYYY-MM-DD HHmm     (e.g. 2029-12-30 1400)");
        write("");
        write("  • '/' is optional:");
        write("      deadline submit report by 2029-12-30");
        write("      event meeting from 2029-12-30 to 2029-12-30");

        write(LINE);
    }

    /**
     * Reads the next command entered by the user (CLI only).
     *
     * @return The trimmed command string entered by the user
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Prints a divider line used to separate sections of output. */
    public void showLine() {
        write(LINE);
    }

    /**
     * Displays a message to the user.
     *
     * @param message The message to display
     */
    public void showMessage(String message) {
        write(message);
    }

    /** Displays the goodbye message when the user exits the program. */
    public void showBye() {
        write("Bye. Hope to see you again soon!");
    }

    /** Closes the scanner used for reading user input (CLI only). */
    public void close() {
        scanner.close();
    }
}