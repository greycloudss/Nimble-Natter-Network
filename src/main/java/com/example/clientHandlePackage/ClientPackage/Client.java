package com.example.clientHandlePackage.ClientPackage;

import com.example.clientHandlePackage.ServerPackage.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Client {
    private boolean isConnected;
    private Server curServer;
    private final String pName;
    private DataInputStream dataReceiver;
    private DataOutputStream dataOutputer;

    public Client(Server server, String pName) {
        this.pName = pName;
        curServer = server;
        isConnected = ((curServer == null) ? false : true);
    }

    //Client(byte[] ipv4, int port, String pName) {
    //    this.pName = pName;
    //    PortScanner scanner = new PortScanner(ipv4, port);
    //    try {
    //        curServer = new Server(port, scanner.Servers().getFirst().getMeetingID(), scanner.Servers().getFirst().getPasswordField());
    //    } catch (IOException e) {
    //        curServer = null;
    //    }
    //}

    //public Client(String pName) {
    //    this.pName = pName;
    //    isConnected = false;
    //    curServer = null;
    //}

    public void connected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;                 // soon to be redundant
    }

    public Server getServer() {
        return curServer;
    }

    public void setServer(Server server) {
        curServer = server;
        isConnected = (curServer.getSocket() == null ? false : true);
    }

    public void receiveMsg() {
        try {
            if (getServer() == null)
                throw new IllegalStateException("Server is not initialized.");

            if (dataReceiver == null )
                dataReceiver = new DataInputStream(getServer().getSocket().getInputStream());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMsg(String msg) {
        try {
            if (curServer == null || !isConnected) {
                if (isConnected)
                    System.out.print("connected");
                else
                    System.out.print("not connected");

                System.out.println(curServer);

                System.err.println("Error: Client is not connected to any server.");
                return;
            }

            if (dataOutputer == null)
                dataOutputer = new DataOutputStream(getServer().getSocket().getOutputStream());

            dataOutputer.writeUTF("Key" + getServer().getSocketPort());
            dataOutputer.writeUTF(pName + msg);
        } catch (Exception e) {
            System.out.println("Unable to send message");

            throw new RuntimeException(e);
        }
    }
}
