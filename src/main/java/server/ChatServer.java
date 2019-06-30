package server;

import configuration.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ChatServer {
    private Selector selector = null;
    private ServerSocketChannel server = null;

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

    private void registerNewClient() {
        try {
            SocketChannel client = server.accept();
            System.out.println("New Client connected! " + client.toString());
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            String msg = String.format("%" + Configuration.BUFFER_SIZE + "s", Configuration.WELCOME_MESSAGE);
            client.write(ByteBuffer.wrap(msg.getBytes()));
            // client.close();
            
        } catch(IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void bindAndListenToPort() throws IOException {
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", Configuration.BIND_PORT));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Bound to port: " + Configuration.BIND_PORT);
    }
}