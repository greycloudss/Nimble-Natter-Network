package com.example.clientHandlePackage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.http.WebSocket;
import java.util.Arrays;

public class Server {
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private int port = 80;

    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    private String meetingID;
    private String password;

    public Server(int port, String meetingID, String password) throws IOException {
        System.out.println("Server constructor called as a struct");
        this.port = port;
        this.meetingID = meetingID;
        this.password = password;
        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), port); //serverSocket
    }

    public Server(String meetingID, String password, int instance) {
        System.out.println("Server constructor called as a server");
        try {
            this.meetingID = meetingID;
            this.password = password;
            port += instance;
            serverSocket = new ServerSocket(port);

            new Thread(() -> {
                try {
                    socket = serverSocket.accept();
                    in = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Server init failed, cause: threads start");
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server init failed");
        }
    }


    public String getMeetingID() {
        return meetingID;
    }

    public String getPasswordField() {
        return password;
    }

    public ObservableList<Client> getClients() {
        return clients;
    }

    public boolean clientExists(Client client) {
        return clients.contains(client);
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public Pair<String, String> returnServerInfo() {
        return new Pair<>(getMeetingID(), getPasswordField());
    }

    public Socket getSocket() {
        return socket;
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Server closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}