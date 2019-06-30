package server;

import configuration.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
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

    public ChatServer(int port){
        this.port = port;
        userToChannel = new HashMap<>();
        channelToUser = new HashMap<>();
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

                    if (key.isAcceptable()) {
                        registerNewClient();
                    }

                    if (key.isReadable()) {
                        handleIncomingMessage(key);
                    }

                    iterator.remove();
                }
            }while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingMessage(SelectionKey key) {
        try (SocketChannel client = (SocketChannel) key.channel()) {
            ByteBuffer requestBuffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
            client.read(requestBuffer);

            String request = new String(requestBuffer.array()).trim();
            String response = null;
            System.out.println(request);

        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
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
                writeMessageToSocketChannel("User already connected with that name. please reconnect with new name.", client);
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
        System.out.println("Bound to port: " + Configuration.BIND_PORT);
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