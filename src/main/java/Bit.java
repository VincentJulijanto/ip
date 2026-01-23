import java.util.Scanner;

public class Bit {
    public static final String LINE = "____________________________________________________________";
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println(LINE);
        System.out.println("Hello! I'm Bit");
        System.out.println("What can I do for you?");
        System.out.println(LINE);

        String[] tasks = new String[100];
        boolean[] isDone = new boolean[100];
        int count = 0;

        while (true) {
            String input = sc.nextLine().trim();
            String command = input.toLowerCase();

            if (command.equals("bye")) {
                System.out.println(LINE);
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(LINE);
                break;
            }

            if (command.equals("list")) {
                System.out.println(LINE);
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < count; i++) {
                    String status = isDone[i] ? "[X] " : "[ ] ";
                    System.out.println((i + 1) + "." + status + tasks[i]);
                }
                System.out.println(LINE);
                continue;
            }

            if (command.startsWith("mark ")) {
                int idx = parseIndex(command.substring(5));
                if (idx < 1 || idx > count) {
                    System.out.println(LINE);
                    System.out.println("Invalid task number.");
                    System.out.println(LINE);
                    continue;
                }
                isDone[idx - 1] = true;

                System.out.println(LINE);
                System.out.println("Nice! I've marked this task as done:");
                System.out.println("  [X] " + tasks[idx - 1]);
                System.out.println(LINE);
                continue;
            }

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
                System.out.println("  [ ] " + tasks[idx - 1]);
                System.out.println(LINE);
                continue;
            }

            if (!input.isEmpty()) {
                tasks[count] = input;
                isDone[count] = false;
                count++;

                System.out.println(LINE);
                System.out.println("added: " + input);
                System.out.println(LINE);
            }
        }

        sc.close();
    }

    private static int parseIndex(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
