package com.example.newmsg.serverPack;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private void broadcastMessage(String message, ClientHandler sender) {
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Ignored empty or null message.");
            return;
        }
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedWriter writer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                this.writer = writer;

                String handshake = reader.readLine();
                if ("PING".equals(handshake)) {
                    writer.write("PONG\n");
                    writer.flush();
                } else {
                    System.err.println("Invalid handshake message: " + handshake);
                    return;
                }

                String message;
                while ((message = reader.readLine()) != null)
                    broadcastMessage(message, this);

            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                clients.remove(this);
            }
        }


        public void sendMessage(String message) {
            try {
                if (writer != null) {
                    writer.write(message + "\n");
                    writer.flush();
                }
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }
}
