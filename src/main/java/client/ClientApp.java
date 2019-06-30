package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp {
    private BufferedReader userInput;
    private String username;
    public ClientApp(){
        userInput = new BufferedReader(new InputStreamReader(System.in));
    }
    public  void run(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Inavlid number of arguemnts!!");
            System.err.println("Expected 1 or 2, got " + args.length);
            return;
        }
        try(var client = new ChatClient(args)){
            initialHandShake(client);
            while(client.isConnectedToServer()){
                if(client.hasResponse()){
                    System.out.println(client.readResponseFromServer());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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