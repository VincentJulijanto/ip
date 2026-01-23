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
                for (int i = 0; i < count; i++) {
                    System.out.println((i + 1) + ". " + tasks[i]);
                }
                System.out.println(LINE);
                continue;
            }

            if (command.startsWith("read ")) {
                String desc = input.substring(5).trim();
                String task = "read " + desc;

                tasks[count] = task;
                count++;

                System.out.println(LINE);
                System.out.println("added: " + task);
                System.out.println(LINE);
                continue;
            }

            if (command.startsWith("return ")) {
                String desc = input.substring(7).trim();
                String task = "return " + desc;

                tasks[count] = task;
                count++;

                System.out.println(LINE);
                System.out.println("added: " + task);
                System.out.println(LINE);
                continue;
            }

            System.out.println(LINE);
            System.out.println("  " + input);
            System.out.println(LINE);
        }

        sc.close();
    }
}
