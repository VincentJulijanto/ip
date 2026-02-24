import java.util.Scanner;

public class Ui {
    public static final String LINE =
            "____________________________________________________________";

    private final Scanner scanner;

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

    public String readCommand() {
        return scanner.nextLine().trim();
    }

    public void showLine() {
        System.out.println(LINE);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showBye() {
        System.out.println(LINE);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(LINE);
    }

    public void close() {
        scanner.close();
    }
}