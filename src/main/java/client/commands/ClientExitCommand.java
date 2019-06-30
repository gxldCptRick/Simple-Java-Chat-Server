package client.commands;

import client.ChatClient;

import java.io.IOException;

public class ClientExitCommand implements ClientCommand {
    @Override
    public boolean isApplicable(String message) {
        return "/exit".equalsIgnoreCase(message);
    }

    @Override
    public void execute(ChatClient client) throws IOException {
        client.close();
    }
}
