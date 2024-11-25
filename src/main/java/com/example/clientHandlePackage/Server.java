package com.example.clientHandlePackage;

import com.example.messenger.Messenger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;

    private int port = 80;
    //private final ObservableList<Client> clients = FXCollections.observableArrayList();

    private String meetingID;
    private String password;
    private Messenger server_messenger;

    public Server(int port, String meetingID, String password) {
        System.out.println("Server constructor called as a struct");
        this.port = port;
        this.meetingID = meetingID;
        this.password = password;
        System.out.printf("Struct server initialized with port: %d%n", port);
    }

    public Server(String meetingID, String password, int port) {
        System.out.println("Server constructor called as a server");
        server_messenger = new Messenger(getSocket());
        try {
            this.meetingID = meetingID;
            this.password = password;
            this.port = port;
            serverSocket = new ServerSocket(this.port);

            new Thread(() -> {
                while (true) {
                    try {
                        socket = serverSocket.accept();
                        System.out.printf("Client connected from: %s%n", socket.getInetAddress());

                        handleClient(socket);

                    } catch (IOException e) {
                        System.err.printf("Error accepting client: %s%n", e.getMessage());
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                DataInputStream clientReceiver = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream clientOutputer = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String received = clientReceiver.readUTF();
            System.out.printf("Received message from client: %s%n", received);

            if (server_messenger.getClientSockets().contains(clientSocket)) {
                if (server_messenger.verifyMessage(clientSocket, received.substring(0, received.indexOf(":")), received.substring(received.indexOf(":"))))
                    sendUpdate();
            }else {

                if (server_messenger.verifyConnection(received, clientSocket)) {
                    clientOutputer.writeUTF("%s$-abcd_$%s".formatted(this.meetingID, this.password));
                    clientOutputer.flush();
                    System.out.println("Client authenticated successfully.");
                } else {
                    clientOutputer.writeUTF("Invalid Key");
                    System.out.println("Invalid key received from client.");
                }
            }
        } catch (IOException e) {
            System.err.printf("Communication error with client: %s%n", e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.printf("Client disconnected: %s%n", clientSocket.getInetAddress());
            } catch (IOException e) {
                System.err.printf("Error closing client socket: %s%n", e.getMessage());
            }
        }
    }

    private void sendUpdate() {
        if (server_messenger.getClientSockets().isEmpty())
            return;
        for (Socket clientSocket : server_messenger.getClientSockets()) {
            try (DataOutputStream clientOutputer = new DataOutputStream(clientSocket.getOutputStream())) {

               clientOutputer.writeUTF(server_messenger.getMessages().getLast().getKey() + server_messenger.getMessages().getLast().getValue());
               System.out.println("Message sent.");
            } catch (IOException e) {
                System.err.printf("Communication error with client: %s%n", e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.printf("Client disconnected: %s%n", clientSocket.getInetAddress());
                } catch (IOException e) {
                    System.err.printf("Error closing client socket: %s%n", e.getMessage());
                }
            }
        }
    }

    public void addClient(Client client) {
        //placeholder
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

    public void close() {
        try {
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Server closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}