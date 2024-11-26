package com.example.messenger;

import javafx.util.Pair;

import java.net.Socket;
import java.util.ArrayList;

public class Messenger {
    private final Socket serverSocket;
    private final ArrayList<Pair<String, String>> messages = new ArrayList<>();
    private final ArrayList<Socket> clientSockets = new ArrayList<>();

    public Messenger(Socket server) {
        this.serverSocket = server;
    }

    public boolean verifyMessage(Socket cl_socket, String name, String message) {
        if (clientSockets.contains(cl_socket) && message.length() <= 512) {
            System.out.printf("Message from %s: %s%n", cl_socket.getInetAddress(), message);
            messages.add(new Pair<>(name, message));
            return true;
        }
        return false;
    }

    public void addClientSocket(Socket cl_socket) {
        clientSockets.add(cl_socket);
    }

    public ArrayList<Pair<String, String>> getMessages() {
        return messages;
    }

    public Socket getServerSocket() {
        return serverSocket;
    }

    public final ArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    public boolean verifyConnection(String received, Socket cl_socket) {
        if (received.equals("Key%d".formatted(serverSocket.getLocalPort())) && cl_socket != null && !clientSockets.contains(cl_socket)) {
            clientSockets.add(cl_socket);
            return true;
        } else {
            System.err.printf("Key mismatch\nExpected [%s], Received [%s]%n", "Key%d".formatted(serverSocket.getLocalPort()), received);
            return false;
        }
    }
}