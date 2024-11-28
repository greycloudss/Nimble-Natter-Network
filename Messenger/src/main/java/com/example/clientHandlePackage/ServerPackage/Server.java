package com.example.clientHandlePackage.ServerPackage;

import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;

    private final int port;

    private final String meetingID;
    private final String password;
    private Messenger server_messenger;

    public Server(int port, String meetingID, String password) {
        System.gc();
        System.out.println("Server constructor called as a struct");
        this.port = port;
        this.meetingID = meetingID;
        this.password = password;
        try {
            this.socket = new Socket(InetAddress.getLoopbackAddress(), this.port);
        } catch (IOException e) {
            this.socket = null;
        }
        System.out.printf("Struct server initialized with port: %d%n", port);
    }

    public Server(String meetingID, String password, int port) {
        this.meetingID = meetingID;
        this.password = password;
        this.port = port;

        try {
            serverSocket = new ServerSocket(this.port); // Initialize the server socket
            server_messenger = new Messenger(serverSocket);
            new Thread(this::acceptClients).start();
            System.out.printf("Server initialized on port: %d%n", this.port);
        } catch (IOException e) {
            System.err.printf("Failed to initialize server on port %d: %s%n", this.port, e.getMessage());
            serverSocket = null; // Ensure serverSocket is null if initialization fails
        }

        try {
            this.serverSocket = new ServerSocket(port);
            System.out.printf("Server initialized on port: %d%n", port);
        } catch (BindException e) {
            System.err.printf("Port %d is already in use.%n", port);
        } catch (SecurityException e) {
            System.err.printf("Permission denied to bind on port %d.%n", port);
        } catch (IOException e) {
            System.err.printf("Failed to initialize server on port: %d - %s%n", port, e.getMessage());
        }

    }


    private void acceptClients() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10); // Adjustable pool size
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); // Accept new client connection
                threadPool.execute(() -> handleClient(clientSocket)); // Handle client in a separate thread
            } catch (IOException e) {
                System.err.println("Error accepting client: " + e.getMessage());
                break; // Exit loop on server socket error
            }
        }
        threadPool.shutdown(); // Gracefully shutdown thread pool
    }



    private void broadcastMessage(String message) {
        for (Socket clientSocket : server_messenger.getClientSockets()) {
            try (DataOutputStream clientOutputer = new DataOutputStream(clientSocket.getOutputStream())) {
                clientOutputer.writeUTF(message);
            } catch (IOException e) {
                System.err.println("Error broadcasting to client: " + e.getMessage());
            }
        }
    }



    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream clientReceiver = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream clientOutputer = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String received = clientReceiver.readUTF();
            if (server_messenger.verifyConnection(received, clientSocket)) {
                clientOutputer.writeUTF("%s$-abcd_$%s".formatted(meetingID, password));
                server_messenger.addClientSocket(clientSocket);
                System.out.println("Client authenticated successfully.");
            } else {
                clientOutputer.writeUTF("Invalid Key");
                clientSocket.close();
                return;
            }

            while (!clientSocket.isClosed()) {
                try {
                    String clientMessage = clientReceiver.readUTF();
                    System.out.printf("Received from client: %s%n", clientMessage);
                    server_messenger.saveMessage("Client", clientMessage);
                    clientOutputer.writeUTF("Acknowledged: " + clientMessage);
                } catch (EOFException e) {
                    System.out.println("Client disconnected gracefully: " + clientSocket.getRemoteSocketAddress());
                    break;
                } catch (SocketException e) {
                    System.err.println("Client disconnected unexpectedly: " + clientSocket.getRemoteSocketAddress());
                    break;
                } catch (IOException e) {
                    System.err.println("Communication error with client: " + e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("Client socket closed: " + clientSocket.getRemoteSocketAddress());
                }
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }





    public void setSocket(Socket socket) {
        if (socket == null) {
            System.err.println("Cannot set socket: Provided socket is null.");
            return;
        }

        this.socket = socket; // Replace the internal socket with the new one
        System.out.printf("Socket updated to port %d on host %s%n", socket.getPort(), socket.getInetAddress());
    }

    private void sendUpdate() {
        if (server_messenger.getClientSockets().isEmpty())
            return;

        Iterator<Socket> iterator = server_messenger.getClientSockets().iterator();
        while (iterator.hasNext()) {
            Socket clientSocket = iterator.next();
            try (DataOutputStream clientOutputer = new DataOutputStream(clientSocket.getOutputStream())) {
                clientOutputer.writeUTF(server_messenger.getMessages().getLast().getKey() +
                        server_messenger.getMessages().getLast().getValue());
                System.out.println("Message sent.");
            } catch (IOException e) {
                System.err.printf("Communication error with client: %s. Removing client.%n", e.getMessage());
                iterator.remove(); // Remove the client socket from the list
            }
        }
    }


    public String getMeetingID() {
        return meetingID;
    }

    public String getPasswordField() {
        return password;
    }

    public Pair<String, String> returnServerInfo() {
        return new Pair<>(getMeetingID(), getPasswordField());
    }

    public Socket getSocket() {
        return socket;
    }

    public int getSocketPort() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void shutdownServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server shut down successfully.");
            } catch (IOException e) {
                System.err.println("Error shutting down server: " + e.getMessage());
            }
        }
    }
}