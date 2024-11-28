package com.example.clientHandlePackage.ServerPackage;

import javafx.util.Pair;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Messenger {
    private final ServerSocket serverSocket;
    private final ArrayList<Pair<String, String>> messages = new ArrayList<>();
    private final CopyOnWriteArrayList<Socket> clientSockets = new CopyOnWriteArrayList<>();


    public Messenger(ServerSocket server) {
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

    public void saveMessage(String name, String message) {
        if (message.length() <= 512) {
            messages.add(new Pair<>(name, message));
            System.out.printf("Saved message from %s: %s%n", name, message);
        } else {
            System.err.println("Message too long, not saved.");
        }
    }


    public void addClientSocket(Socket cl_socket) {
        clientSockets.add(cl_socket);
    }

    public ArrayList<Pair<String, String>> getMessages() {
        return messages;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public final CopyOnWriteArrayList<Socket> getClientSockets() {
        return clientSockets;
    }

    public boolean verifyConnection(String received, Socket cl_socket) {
        if (received.contains("Key") && cl_socket != null && !clientSockets.contains(cl_socket)) {
            clientSockets.add(cl_socket);
            return true;
        } else {
            System.err.printf("Key mismatch or invalid socket.\nReceived: %s%n", received);
            return false;
        }
    }
}