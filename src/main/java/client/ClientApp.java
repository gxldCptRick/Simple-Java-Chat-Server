package client;

import configuration.MessageTypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp {
    private BufferedReader userInput;

    public ClientApp() {
        userInput = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run(String[] args) {
        validateArgs(args);
        try (var client = new ChatClient(args)) {
            initialHandShake(client);
            var inputThread = new Thread(() -> {
                while (client.isConnectedToServer()) {
                    try {
                        client.writeMessageToChatServer(getInputFromUser("Message"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("Error getting input from user");
                    }
                }
            });
            inputThread.start();

            while (client.isConnectedToServer()) {
                var response = client.readResponseFromServer();
                if (response.startsWith(MessageTypes.ERROR_HEADER)) {
                    System.err.println(response);
                } else {
                    System.out.println(response);
                }
            }
            inputThread.join(1000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Something went wrong!!");
        }
    }

    private String getInputFromUser(String message) throws IOException {
        System.out.print(message+ ": ");
        return userInput.readLine();
    }

    private void validateArgs(String[] args) {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Invalid number of arguments!!\nExpected 1 or 2, and got " + args.length);
        }
    }

    private void initialHandShake(ChatClient client) throws IOException {
        client.run();
        System.out.println(client.readResponseFromServer());
        var input = userInput.readLine();
        client.writeMessageToChatServer(input);
    }
}