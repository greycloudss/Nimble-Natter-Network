package com.example.clientHandlePackage.ServerPackage;

import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;

    private final int port;

    private final String meetingID;
    private final String password;
    private Messenger server_messenger;

    public Server(int port, String meetingID, String password) {
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
        System.out.println("Server constructor called as a server");

        this.meetingID = meetingID;
        this.password = password;
        this.port = port;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.err.printf("Port %d is in use. Trying another port.%n", this.port);
        }

        new Thread(() -> {
            while (true) {
                try {
                    socket = serverSocket.accept();

                    if (server_messenger == null)
                        server_messenger = new Messenger(serverSocket);

                    System.out.printf("Client connected from: %s%n", socket.getInetAddress());

                    //for (Socket s : server_messenger.getClientSockets())
                        handleClient(socket);
                } catch (Exception e) {
                    socket = null;
                    e.printStackTrace();
                    System.err.printf("Error accepting client: %s%n", e.getMessage());
                }
            }
        }).start();

    }


    private void handleClient(Socket clientSocket) throws IOException {
        try (
                DataInputStream clientReceiver = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream clientOutputer = new DataOutputStream(clientSocket.getOutputStream())
        ) {

            try  {
                String received = clientReceiver.readUTF();
                System.out.printf("Received message from client: %s%n", received);
                if (server_messenger.getClientSockets().contains(clientSocket) &&
                        server_messenger.verifyMessage(clientSocket, received.substring(0, received.indexOf(":")), received.substring(received.indexOf(":"))+1)) {
                    clientOutputer.writeUTF(server_messenger.getMessages().getLast().getKey() + server_messenger.getMessages().getLast().getValue());
                } else {
                    if (server_messenger.verifyConnection(received, clientSocket)) {
                        try {
                            clientOutputer.writeUTF("%s$-abcd_$%s".formatted(this.meetingID, this.password));
                        } catch (IOException e) {
                            System.out.print("Socket dead");
                            return;
                        }
                        server_messenger.addClientSocket(clientSocket);
                        System.out.println("Client authenticated successfully.");
                    } else {
                        try {
                            clientOutputer.writeUTF("Invalid Key");
                        } catch (IOException e) {
                            System.out.print("Socket dead");
                            return;
                        }
                        System.out.println("Invalid key received from client.");
                    }
                }
            } catch (Exception e) {
                System.out.print("nothing");
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

    public void close() {
        try {
            for (Socket s : server_messenger.getClientSockets())
                s.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Server closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}