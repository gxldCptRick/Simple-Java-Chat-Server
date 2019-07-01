package client;

import configuration.MessageTypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp {
    private BufferedReader userInput;
    private String username;
    public ClientApp(){
        userInput = new BufferedReader(new InputStreamReader(System.in));
    }
    public void run(String[] args) {
        validateArgs(args);
        try(var client = new ChatClient(args)){
            initialHandShake(client);

            var inputThread = new Thread(() -> {
                var br = new BufferedReader(new InputStreamReader(System.in));
                String input;

                try {
                    while((input = br.readLine()) != null && client.isConnectedToServer()){
                        if (input.isBlank()){
                            System.out.println("Message cannot be blank or whitespace!");
                            continue;
                        }

                        client.writeMessageToChatServer(input);
                    }
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                
            });

            inputThread.start();
            while(client.isConnectedToServer()){
                if(client.hasResponse()){
                    var response = client.readResponseFromServer();
                    if(response.startsWith(MessageTypes.ERROR_HEADER)){
                        System.err.println(response);
                    }else{
                        System.out.println(response);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        client.writeToChatServer(input);
        username = input;
    }
}