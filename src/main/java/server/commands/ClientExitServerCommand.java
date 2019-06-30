package server.commands;

import server.ChatServer;

import java.io.IOException;

public class ClientExitServerCommand implements ServerCommand {
    @Override
    public boolean isApplicable(String message) {
        return false;
    }

    @Override
    public void execute(ChatServer server, String user) throws IOException {

    }
}
