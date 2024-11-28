package com.example.clientHandlePackage.ClientPackage;

import com.example.clientHandlePackage.ServerPackage.Server;
import com.example.messenger.ControlUnit;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private boolean isConnected;
    private Server curServer;
    private final String pName;
    Client_Middleware mw;

    public Client(Server server, String pName, ControlUnit controlUnit) {
        this.pName = pName;
        this.curServer = server;
        System.gc();
        if (curServer != null) {
            isConnected = true;
            mw = new Client_Middleware(this.pName, this.curServer.getSocket(), controlUnit);
            handleServer();
        } else {
            System.out.println("No server found, attempting to create one...");
            createAndConnectServer(controlUnit);
        }
    }

    private void createAndConnectServer(ControlUnit controlUnit) {
        int availablePort = new PortScanner().getFirstUnusedPort();
        System.out.printf("Attempting to create server on port: %d%n", availablePort);

        curServer = new Server(pName, "default_password", availablePort);

        if (curServer.getSocket() != null) { // Server created successfully
            System.out.printf("Server created and running on port: %d%n", availablePort);
            mw = new Client_Middleware(this.pName, this.curServer.getSocket(), controlUnit);
            isConnected = true;
            handleServer();
        } else { // Server creation failed
            System.err.printf("Failed to create server on port: %d%n", availablePort);
        }
    }




    public void handleServer() {
        new Thread(this::run).start();
    }

    private void run() {
        if (curServer == null || curServer.getSocket() == null) {
            System.err.println("Cannot run client: Server or socket is null.");
            return;
        }

        try (
                DataOutputStream dataOutputer = new DataOutputStream(curServer.getSocket().getOutputStream());
                DataInputStream dataReceiver = new DataInputStream(curServer.getSocket().getInputStream())
        ) {
            dataOutputer.writeUTF(mw.constructMessage("Key%d".formatted(curServer.getSocketPort())));

            while (!curServer.getSocket().isClosed()) {
                try {
                    String received = dataReceiver.readUTF();
                    System.out.printf("Received: %s%n", received);

                    if (!received.startsWith("Acknowledged")) {
                        dataOutputer.writeUTF(mw.constructMessage("Acknowledged: " + received));
                    }
                } catch (SocketException e) {
                    System.err.println("Connection error: " + e.getMessage());
                    reconnect();
                    if (curServer.getSocket() == null || curServer.getSocket().isClosed()) {
                        System.err.println("Reconnection failed. Exiting run loop.");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error in client run loop: " + e.getMessage());
        }
    }


    public void reconnect() {
        if (curServer == null || curServer.getSocket() == null) {
            System.err.println("Reconnection failed: Server or socket is null.");
            return;
        }

        try {
            curServer.setSocket(new Socket("127.0.0.1", curServer.getSocketPort()));
            System.out.println("Reconnected to the server successfully.");
        } catch (IOException e) {
            System.err.println("Error reconnecting to the server: " + e.getMessage());
        }
    }


    public void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Cannot send an empty message.");
            return;
        }

        if (curServer == null || curServer.getSocket() == null || curServer.getSocket().isClosed()) {
            System.err.println("Socket is closed or not initialized. Attempting to reconnect...");
            reconnect();
            if (curServer.getSocket() == null || curServer.getSocket().isClosed()) {
                System.err.println("Reconnection failed. Cannot send message.");
                return;
            }
        }

        try (DataOutputStream dataOutputer = new DataOutputStream(curServer.getSocket().getOutputStream())) {
            String constructedMessage = mw.constructMessage(message);
            dataOutputer.writeUTF(constructedMessage);
            System.out.printf("Sent to server: %s%n", constructedMessage);
        } catch (IOException e) {
            System.err.println("Error sending message to server: " + e.getMessage());
        }
    }





    public void setSocket(Socket socket) {
        if (curServer == null) {
            System.err.println("Cannot set socket: Server is null.");
            return;
        }

        if (socket == null) {
            System.err.println("Cannot set socket: Provided socket is null.");
            return;
        }

        curServer.setSocket(socket); // Update the socket in the existing server instance
        System.out.println("Socket updated for current server.");
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

}
