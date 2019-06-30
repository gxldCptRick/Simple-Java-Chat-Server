package server.commands;

import client.ChatClient;
import server.ChatServer;

import java.io.IOException;

public interface ServerCommand {
    boolean isApplicable(String message);
    void execute(ChatServer server, String user) throws IOException;
}
