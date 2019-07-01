package client;

import client.commands.ClientCommand;
import client.commands.ClientExitCommand;
import client.commands.ClientListClientsCommand;
import configuration.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChatClient implements AutoCloseable {
    private volatile boolean connectedToServer = false;
    private InputStream serverInStream;
    private OutputStream serverOutStream;
    private Socket serverConnection;
    private int port;
    private String host;
    private ClientCommand[] commands;

    public ChatClient(String[] args) {
        parseHostAndPort(args);
        commands = new ClientCommand[]{new ClientExitCommand(), new ClientListClientsCommand()};
    }

    public boolean isConnectedToServer() {
        return this.connectedToServer;
    }

    public boolean hasResponse() throws IOException {
        return serverInStream.available() > 0;
    }

    public void listUsers() throws IOException {
        writeStringToServer("/list-users");
    }

    private void parseHostAndPort(String[] args) {
        this.port = Configuration.BIND_PORT;
        if (args.length == 2) {
            this.port = parsePort(args[1]);
        }
        this.host = args[0];
    }

    public void run() throws IOException {
        connectToServer(host, port);
    }

    private int parsePort(String port) {
        try {
            int temp = Integer.parseInt(port);
            if (temp < 1 || temp > 65535) {
                throw new NumberFormatException();
            }
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number! Using default " + Configuration.BIND_PORT);
            return Configuration.BIND_PORT;
        }
    }

    public String readResponseFromServer() throws IOException {
        if (!connectedToServer) throw new IllegalStateException("Connection to server is not established");
        var buffer = new byte[Configuration.BUFFER_SIZE];
        var charactersRead = serverInStream.read(buffer);
        if (charactersRead < buffer.length) throw new RuntimeException("Server did not respond correctly");
        return new String(buffer).trim();
    }

    public void writeMessageToChatServer(String message) {
        try{
            if (message.startsWith("/")) {
                for (var command : commands) {
                    if (command.isApplicable(message)) {
                        command.execute(this);
                        return;
                    }
                }
            }
            writeStringToServer(message);
        }catch(IOException e){
            System.err.println("Lost Connection to server...");
            try {
                close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void writeStringToServer(String message) throws IOException, IllegalStateException {
        if (message.length() > Configuration.BUFFER_SIZE)
            throw new IllegalArgumentException("message is too long for buffer");
        if (!isConnectedToServer()) {
            throw new IllegalStateException("Tried to write to a server when no connection was established.");
        }
        var request = String.format("%-" + Configuration.BUFFER_SIZE + "s", message);
        serverOutStream.write(request.getBytes());
    }

    private void connectToServer(String host, int port) throws IOException {
        this.serverConnection = new Socket(host, port);
        System.out.println("Connected to server!\nHost: '" + host + "\nPort: '" + port + "'\n\n");
        connectedToServer = true;
        this.serverInStream = serverConnection.getInputStream();
        this.serverOutStream = serverConnection.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        if (serverConnection != null) {
            writeStringToServer("/exit");
            connectedToServer = false;
            serverConnection.close();
        }
    }
}