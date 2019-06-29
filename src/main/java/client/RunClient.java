package client;

public class RunClient {
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Inavlid number of arguemnts!!");
            System.out.println("Expected 1 or 2, got " + args.length);
            return;
        }

        new ChatClient().run(args);
    }
}