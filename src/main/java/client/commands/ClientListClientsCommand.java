package client.commands;

import client.ChatClient;

import java.io.IOException;

public class ClientListClientsCommand implements ClientCommand {
    @Override
    public boolean isApplicable(String message) {
        return "/list-users".equalsIgnoreCase(message);
    }

    @Override
    public void execute(ChatClient client) throws IOException {
        client.listUsers();
    }
}
