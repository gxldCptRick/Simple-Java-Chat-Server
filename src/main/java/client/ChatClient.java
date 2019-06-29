package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;

import configuration.Commands;
import configuration.Configuration;

public class ChatClient {

    private volatile boolean connectedToServer = false;

    public void run(String[] args) {
        int port = Configuration.BIND_PORT;

        if (args.length == 2) {
            port = parsePort(args[1]);
        }
        connectToServer(args[0], port);

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

    private void connectToServer(String host, int port) {
        try (Socket server = new Socket(host, port)) {
            System.out.println("Connected to server!\nHost: '" + host + "\nPort: '" + port + "'\n\n");
            connectedToServer = true;

            new Thread(() -> {
                var reader = new BufferedReader(new InputStreamReader(System.in));
                String input = "";

                try {
                    var out = server.getOutputStream();
                    while (!(input = reader.readLine()).equals(Commands.EXIT)) {
                        out.write(input.getBytes());
                    }
                    connectedToServer = false;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }

            }).start();

            var br = new BufferedReader(new InputStreamReader(server.getInputStream()));
            String res;
            while ((res = br.readLine()) != null) {
                // byte[] responseBuffer = new byte[Configuration.BUFFER_SIZE];
                // server.getInputStream().read(responseBuffer);
                // String rawResponse = new String(responseBuffer).trim();
                // System.out.println("Server said: " + rawResponse);

                System.out.println("Server: " + res);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}