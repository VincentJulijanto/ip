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

            tasks[count] = input;
            count++;

            System.out.println(LINE);
            System.out.println("added: " + input);
            System.out.println(LINE);
        }

        sc.close();
    }
}
