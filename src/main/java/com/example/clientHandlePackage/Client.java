package com.example.clientHandlePackage;

import java.io.IOException;

public class Client {
    private boolean isConnected;
    private Server curServer;
    private String pName;

    Client(Server server, String pName) {
        this.pName = pName;
        this.curServer = server;
        curServer = server;
    }

    Client(byte[] ipv4, int port, String pName) {
        this.pName = pName;
        PortScanner scanner = new PortScanner(ipv4, port);
        try {
            curServer = new Server(port, scanner.Servers().getFirst().getMeetingID(), scanner.Servers().getFirst().getPasswordField());
        } catch (IOException e) {
            curServer = null;
        }
    }

    public Client(String pName) {
        this.pName = pName;
        isConnected = false;
        curServer = null;
    }

    public void connected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Server getServer() {
        return curServer;
    }

    public void setServer(Server server) {
        curServer = server;
        connected(true);
    }
}
