package com.example.clientHandlePackage.ClientPackage;

import com.example.clientHandlePackage.ServerPackage.Server;
import com.example.messenger.ControlUnit;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Client {
    private boolean isConnected;
    private final Server curServer;
    private final String pName;
    Client_Middleware mw;
    public Client(Server server, String pName, ControlUnit controlUnit) {
        this.pName = pName;
        curServer = server;

        if (curServer != null) {
            isConnected = true;
            mw = new Client_Middleware(this.pName, this.curServer.getSocket(), controlUnit);
            handleServer();
        } else isConnected = false;

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

    public void handleServer() {
        new Thread(() -> {
            try (
                    DataOutputStream dataOutputer = new DataOutputStream(curServer.getSocket().getOutputStream());
                    DataInputStream dataReceiver = new DataInputStream(curServer.getSocket().getInputStream())
            ) {
                dataOutputer.writeUTF(mw.constructMessage("Key%d".formatted(curServer.getSocketPort())));
                if (dataReceiver.readUTF().contains(curServer.getMeetingID()) && dataReceiver.readUTF().contains(curServer.getPasswordField())) {
                    this.isConnected = true;
                    System.out.println("Server connected");
                }
                //at this point i dont care about verification just please let me message

                while (true) {
                    String received = dataReceiver.readUTF();
                    System.out.println("AAAAA");
                    System.out.println(received);
                    dataOutputer.writeUTF(mw.constructMessage(dataReceiver.readUTF()));
                }
            } catch (Exception e) {
                isConnected = false;
            }

        }).start();
    }
}
