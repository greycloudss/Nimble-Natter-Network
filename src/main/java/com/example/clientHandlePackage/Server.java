package com.example.clientHandlePackage;

import javafx.collections.ObservableList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket        serverSocket = null;
    private Socket              socket       = null;
    private DataInputStream     in           = null;
    private DataOutputStream    out          = null;
    private int                 port         =   -1;

    private ObservableList<String> clients;

    private String meetingID;
    private String password;

    Server(String meetingID, String password, int instance)  {

        try {
            this.meetingID = meetingID;
            this.password = password;
            port = instance + 80;
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getMeetingID() {
        return meetingID;
    }

    public String getPasswordField() {
        return password;
    }

    public ObservableList<String> getClients() {
        return clients;
    }

    public boolean clientExists(String client) {
        return clients.contains(client);
    }
}
