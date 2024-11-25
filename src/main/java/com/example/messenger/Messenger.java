package com.example.messenger;

import com.example.clientHandlePackage.Client;
import com.example.clientHandlePackage.Server;
import javafx.util.Pair;

import java.net.Socket;
import java.util.ArrayList;

public class Messenger {
    private Socket server;
    private ArrayList<Pair<String, String>> messages = new ArrayList<>();
    private final ArrayList<Socket> clientSockets = new ArrayList<>();

    public Messenger(Socket server) {
        this.server = server;
    }

    public boolean verifyMessage(Socket cl_socket, String name, String message) {
        if (clientSockets.contains(cl_socket) && !message.isEmpty() && message.length() <= 512) {
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

    public Socket getServer() {
        return server;
    }

    public final ArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    public boolean verifyConnection(String received, Socket cl_socket) {
        if (received.equals("Key%d".formatted(server.getPort())) && cl_socket != null && !clientSockets.contains(cl_socket)) {
            clientSockets.add(cl_socket);
            return true;
        } else return false;

    }
}