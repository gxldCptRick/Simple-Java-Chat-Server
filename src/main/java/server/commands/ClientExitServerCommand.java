package server.commands;

import server.ChatServer;

import java.io.IOException;

public class ClientExitServerCommand implements ServerCommand {
    @Override
    public boolean isApplicable(String message) {
        return "/exit".equalsIgnoreCase(message);
    }

    @Override
    public void execute(ChatServer server, String user) throws IOException {
        server.closeClient(user);
    }
}
