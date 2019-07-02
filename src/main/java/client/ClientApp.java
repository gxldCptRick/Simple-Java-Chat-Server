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
        Thread inputThread = null;
        try (var client = new ChatClient(args)) {
            initialHandShake(client);
            inputThread = new Thread(() -> {
                var wroteMessage = false;
                while (client.isConnectedToServer()) {
                    try {
                        if(!wroteMessage){
                            System.out.print("Message: ");
                        }
                        wroteMessage = true;
                        if(userInput.ready()){
                            var message = userInput.readLine();
                            if(!message.isBlank()){
                                client.writeMessageToChatServer(message);
                            }
                            wroteMessage = false;
                        }
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
            if(inputThread != null){
                try {
                    inputThread.join(1000);
                } catch (InterruptedException e1) {
                    System.err.println("Something went wrong during cleanup of write thread!!");
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Something went wrong in cleanup of read thread!!");
        }
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