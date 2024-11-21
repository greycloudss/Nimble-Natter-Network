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

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataReceiver;
    private DataOutputStream dataoutputer;
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

    public Server(String meetingID, String password, int port) {
        System.out.println("Server constructor called as a server");
        try {
            this.meetingID = meetingID;
            this.password = password;
            this.port = port;
            serverSocket = new ServerSocket(this.port);

            new Thread(() -> {
                while (true) {
                    try {
                        socket = serverSocket.accept();
                        dataReceiver = new DataInputStream(socket.getInputStream());
                        dataoutputer = new DataOutputStream(socket.getOutputStream());

                        if (dataReceiver.readUTF().equals("Key" + port)) {
                            dataoutputer.writeUTF(this.meetingID + "$-abcd_$" + this.password);
                            dataoutputer.flush();
                            System.out.println("dadasdasdasddadasdasdasddadasdasdasddadasdasdasd");
                        } else {
                            dataoutputer.writeUTF("Hello");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Server init failed, cause: threads start");
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
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

    public int getSocketPort() {
        return port;
    }

    public void close() {
        try {
            if (dataReceiver != null) dataReceiver.close();
            if (dataoutputer != null) dataoutputer.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("Server closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}