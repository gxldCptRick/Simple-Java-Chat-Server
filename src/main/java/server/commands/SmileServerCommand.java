package server.commands;

import server.ChatServer;

import java.io.IOException;

public class SmileServerCommand implements ServerCommand {

    private static String broadcastMessage =
            "    --------------\n" +
            "    | ()      () |\n" +
            "    | ^        ^ |\n" +
            "    | {        } |\n" +
            "    | |--------| |\n" +
            "    |____________|\n" +
            "SMILEY HAS BLESSED YOU!!";

    @Override
    public boolean isApplicable(String message) {
        return "/smile".equalsIgnoreCase(message);
    }

    @Override
    public void execute(ChatServer server, String user) throws IOException {
        server.broadcastMessageToAllClients(user, broadcastMessage);
    }
}
