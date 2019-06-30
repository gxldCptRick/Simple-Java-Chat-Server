package server.commands;

import server.ChatServer;

import java.io.IOException;

public class ListClientsServerCommand implements ServerCommand{
    @Override
    public boolean isApplicable(String message) {
        return "/list-users".equalsIgnoreCase(message);
    }

    @Override
    public void execute(ChatServer server, String user) throws IOException {
        var userList = server.collectUserString(user);
        server.writeMessageToUser(user, userList);
    }
}

