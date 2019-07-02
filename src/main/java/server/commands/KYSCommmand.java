package server.commands;

import server.ChatServer;

import java.io.IOException;

public class KYSCommmand implements ServerCommand{

    private String emote =
            "    |_______________``\\\n"+
            "    [/]           [  ]\n"+
            "    [\\]           | ||\n"+
            "    [/]           |  |\n"+
            "    [\\]           |  |\n"+
            "    [/]           || |\n"+
            "   [---]          |  |\n"+
            "   [---]          |@ |\n"+
            "   [---]          |  |\n"+
            "  oOOOOOo         |  |\n"+
            " oOO___OOo        | @|\n"+
            "oO/|||||\\Oo       |  |\n"+
            "OO/|||||\\OOo      |  |\n" +
            "*O\\ x x /OO*      |  |\n" +
            "/*|  c  |O*\\      |  |\n"+
            "   \\_O_/    \\     |  |\n"+
            "    \\#/     |     |  |\n" +
            " |       |  |     | ||\n"+
            " |       |  |_____| ||__\n"+
            "_/_______\\__|  \\  ||||  \\\n"+
            "/         \\_|\\  _ | ||\\  \\\n"+
            "|    V    |\\  \\//\\  \\  \\  \\\n"+
            "|    |    | __//  \\  \\  \\  \\\n"+
            "|    |    |\\|//|\\  \\  \\  \\  \\\n"+
            "------------\\--- \\  \\  \\  \\  \\\n"+
            "\\  \\  \\  \\  \\  \\  \\  \\  \\  \\  \\\n"+
            "_\\__\\__\\__\\__\\__\\__\\__\\__\\__\\__\\\n"+
            "__|__|__|__|__|__|__|__|__|__|__|\n"+
            "|___| |___|\n"+
            "|###/ \\###|\n"+
            "\\##/   \\##/";

    @Override
    public boolean isApplicable(String message) {
        return "/kys".equalsIgnoreCase(message);
    }

    @Override
    public void execute(ChatServer server, String user) throws IOException {
        server.broadcastMessageToAllClients(user, emote);
    }
}
