package client.commands;

import client.ChatClient;

import java.io.IOException;

public interface ClientCommand {
    boolean isApplicable(String message);
    void execute(ChatClient client) throws IOException;
}
