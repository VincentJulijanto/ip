import java.util.Scanner;

public class Bit {
    public static final String LINE = "____________________________________________________________";
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

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

        String[] desc = new String[100];
        String[] type = new String[100];
        String[] extra = new String[100];
        boolean[] isDone = new boolean[100];
        int count = 0;

        while (true) {
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String command = input.toLowerCase();

            if (command.equals("bye")) {
                System.out.println(LINE);
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(LINE);
                break;
            }

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
                System.out.println("  [" + type[idx - 1] + "][X] " + desc[idx - 1] + extra[idx - 1]);
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
                System.out.println("  [" + type[idx - 1] + "][ ] " + desc[idx - 1] + extra[idx - 1]);
                System.out.println(LINE);
                continue;
            }

            // TODO
            if (command.equals("todo") || command.startsWith("todo ")) {
                String d = input.substring(4).trim();
                if (d.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The description of a todo cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                type[count] = "T";
                desc[count] = d;
                extra[count] = "";
                isDone[count] = false;
                count++;

                System.out.println(LINE);
                System.out.println("Got it. I've added this task:");
                System.out.println("  [T][ ] " + d);
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // DEADLINE
            if (command.equals("deadline") || command.startsWith("deadline ")) {
                String rest = input.substring(8).trim();
                if (rest.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The description of a deadline cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                int byPos = rest.toLowerCase().indexOf(" /by ");
                if (byPos == -1) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! Use: deadline <description> /by <time>");
                    System.out.println(LINE);
                    continue;
                }

                String d = rest.substring(0, byPos).trim();
                String by = rest.substring(byPos + 5).trim(); // after " /by "

                if (d.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The description of a deadline cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }
                if (by.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The /by part of a deadline cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                type[count] = "D";
                desc[count] = d;
                extra[count] = " (by: " + by + ")";
                isDone[count] = false;
                count++;

                System.out.println(LINE);
                System.out.println("Got it. I've added this task:");
                System.out.println("  [D][ ] " + d + " (by: " + by + ")");
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // EVENT
            if (command.equals("event") || command.startsWith("event ")) {
                String rest = input.substring(5).trim();
                if (rest.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The description of an event cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                String lowerRest = rest.toLowerCase();
                int fromPos = lowerRest.indexOf(" /from ");
                int toPos = lowerRest.indexOf(" /to ");

                if (fromPos == -1 || toPos == -1 || toPos < fromPos) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! Use: event <description> /from <start> /to <end>");
                    System.out.println(LINE);
                    continue;
                }

                String d = rest.substring(0, fromPos).trim();
                String from = rest.substring(fromPos + 7, toPos).trim();
                String to = rest.substring(toPos + 5).trim();

                if (d.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The description of an event cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }
                if (from.isEmpty() || to.isEmpty()) {
                    System.out.println(LINE);
                    System.out.println("OOPS!!! The /from and /to parts of an event cannot be empty.");
                    System.out.println(LINE);
                    continue;
                }

                type[count] = "E";
                desc[count] = d;
                extra[count] = " (from: " + from + " to: " + to + ")";
                isDone[count] = false;
                count++;

                System.out.println(LINE);
                System.out.println("Got it. I've added this task:");
                System.out.println("  [E][ ] " + d + " (from: " + from + " to: " + to + ")");
                System.out.println("Now you have " + count + " tasks in the list.");
                System.out.println(LINE);
                continue;
            }

            // UNKNOWN COMMAND
            System.out.println(LINE);
            System.out.println("OOPS!!! I'm sorry, but I don't know what that means :-(");
            System.out.println(LINE);
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
