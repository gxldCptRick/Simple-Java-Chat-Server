package server;

import configuration.Configuration;
import server.commands.ClientExitServerCommand;
import server.commands.ServerCommand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatServer {
    private Selector selector = null;
    private ServerSocketChannel server = null;
    private int port;
    private Map<String, SocketChannel> userToChannel;
    private Map<SocketChannel, String> channelToUser;
    private ServerCommand[] commands;

    public ChatServer(int port){
        this.port = port;
        userToChannel = new HashMap<>();
        channelToUser = new HashMap<>();
        commands = new ServerCommand[]{ new ClientExitServerCommand() };
    }

    public ChatServer(){
        this(Configuration.BIND_PORT);
    }

    public void run() {
        try {
            selector = Selector.open();
            bindAndListenToPort();
            System.out.println("Server running...");
            do{
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    processKey(key);
                    iterator.remove();
                }
            }while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processKey(SelectionKey key) {
        if (key.isAcceptable()) {
            registerNewClient();
        }
        if (key.isReadable()) {
            handleIncomingMessage(key);
        }
    }

    private void handleIncomingMessage(SelectionKey key) {
        try{
            SocketChannel clientChannel = (SocketChannel) key.channel();
            var message = readMessageFromSocketChannel(clientChannel);
            if(message.startsWith("/")){
                for (var command: commands) {
                    if(command.isApplicable(message)){
                        command.execute(this, channelToUser.get(clientChannel));
                    }
                }
            }else{
                processMessageRequest(message, clientChannel);
            }
        }catch (IOException e){
            logError("Could not process the client request.");
        }

    }

    private void processMessageRequest(String message, SocketChannel clientChannel) throws IOException {
        if(!message.contains(":")){
            writeErrorMessageToSocketChannel("request must be a command or be <username>: <message> format.", clientChannel);
            logError("client sent invalid request: " + message);
        }else{
           var requestParts  = message.split(":");
           var requestedUser = requestParts[0].trim();
           var sender = channelToUser.get(clientChannel);
            if(userToChannel.containsKey(requestedUser)){
               var messageToSend = sender+":" + requestParts[1].trim();
               writeMessageToSocketChannel(messageToSend, userToChannel.get(requestedUser));
               logMessage(String.format("%s sent %s to %s", sender, messageToSend, requestedUser));
           }else{
               writeErrorMessageToSocketChannel("no user by the name " + requestedUser + " exists", clientChannel);
               logMessage( sender + " requested a user that did not exist: " + requestedUser);
           }
        }
    }

    private void writeErrorMessageToSocketChannel(String message, SocketChannel clientChannel) throws IOException {
        writeMessageToSocketChannel("Error: " + message, clientChannel);
    }

    private void logMessage(String message){
        System.out.println("!! " + message +  " !!");
    }

    private void logError(String errorMessage){
        System.err.println("## " +errorMessage +  " ##");
    }


    private void writeMessageToSocketChannel(String message, SocketChannel channel) throws IOException {
        String msg = String.format("%-" + Configuration.BUFFER_SIZE + "s", message);
        channel.write(ByteBuffer.wrap(msg.getBytes()));
    }


    private String readMessageFromSocketChannel(SocketChannel client) throws IOException {
        var readBuffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
        client.read(readBuffer);
        return new String(readBuffer.array()).trim();
    }

    private void registerNewClient() {
        try {
            SocketChannel client = server.accept();
            logMessage("New client connection received");
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            writeMessageToSocketChannel(Configuration.WELCOME_MESSAGE, client);
            var clientName = readMessageFromSocketChannel(client);
            if(userToChannel.containsKey(clientName)){
                writeErrorMessageToSocketChannel("User already connected with that name. please reconnect with new name.", client);
                logError("Client tried to connect with a username that was taken");
                client.close();
            }else{
                writeMessageToSocketChannel("Welcome " + clientName, client);
                userToChannel.put(clientName, client);
                channelToUser.put(client, clientName);
                logMessage(clientName + " has connected");
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            logError("Something went wrong with the initial connection with the client.");
        }
    }

    private void bindAndListenToPort() throws IOException {
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        logMessage("Bound to port: " + port);
    }

    public void closeClient(String user) throws IOException {
        var channel = userToChannel.get(user);
        try{
            logMessage("Broadcasting exit to all clients");
            broadcastMessageToAllClients(user, user + " is leaving :'(");
        }catch(IOException e){
            e.printStackTrace();
            logError("Broadcasting to all the clients failed.");
        }
        userToChannel.remove(user, channel);
        channelToUser.remove(channel, user);
        logMessage("Removed "+  user+ " from our memory.");
        channel.close();
        logMessage("Closed socket for "+ user);
    }

    public void broadcastMessageToAllClients(String client, String broadcastMessage) throws IOException {
        for (var user : userToChannel.keySet()) {
            if(!user.equalsIgnoreCase(client)){
                writeMessageToSocketChannel(broadcastMessage, userToChannel.get(user));
            }
        }
    }
}