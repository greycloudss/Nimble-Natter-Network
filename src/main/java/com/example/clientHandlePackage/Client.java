package com.example.clientHandlePackage;

public class Client {
    private boolean isConnected;
    private Server curServer;

    Client(Server server) {

    }

    Client(byte[] a, int port) {

    }

    public Client() {
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
