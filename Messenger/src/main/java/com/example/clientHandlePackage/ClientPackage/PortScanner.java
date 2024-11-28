package com.example.clientHandlePackage.ClientPackage;

import com.example.clientHandlePackage.ServerPackage.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortScanner {
    private final List<Server> servers = Collections.synchronizedList(new ArrayList<>());
    private final int minPort = 80;
    private final int maxPort = 65535; // Max possible port
    private InetAddress localIP;
    private int firstUnusedPort = -1;
    private boolean scanCompleted = false;

    public PortScanner() {
        try {
            localIP = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        } catch (UnknownHostException e) {
            System.err.println("Error initializing local IP: " + e.getMessage());
            return;
        }

        Thread scanThread = new Thread(this::scanPorts);
        scanThread.start();
        awaitCompletion(scanThread);
    }

    private void scanPorts() {
        int currentPort = minPort;
        boolean firstUnusedPortFound = false;

        while (currentPort <= maxPort) {
            try (Socket tmpSocket = new Socket(localIP, currentPort)) {
                DataInputStream in = new DataInputStream(tmpSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(tmpSocket.getOutputStream());

                String handshake = "Key" + currentPort;
                out.writeUTF(handshake);

                String serverResponse = in.readUTF();
                if (serverResponse.contains("$-abcd_$")) {
                    String meetingID = serverResponse.substring(0, serverResponse.indexOf("$"));
                    String password = serverResponse.substring(serverResponse.lastIndexOf("$") + 1);
                    servers.add(new Server(currentPort, meetingID, password));
                    System.out.printf("Server found on port: %d%n", currentPort);
                }
            } catch (Exception e) {
                if (!firstUnusedPortFound) {
                    firstUnusedPort = currentPort;
                    firstUnusedPortFound = true;
                }
            }
            currentPort++;
        }
        scanCompleted = true;
    }

    public void awaitCompletion(Thread scanThread) {
        try {
            scanThread.join();
        } catch (InterruptedException e) {
            System.err.println("Port scanning interrupted: " + e.getMessage());
        }
    }

    public int getFirstUnusedPort() {
        return firstUnusedPort;
    }

    public List<Server> getServers() {
        return new ArrayList<>(servers);
    }

    public boolean isScanCompleted() {
        return scanCompleted;
    }

    public List<Server> Servers() {
        return Collections.unmodifiableList(servers);
    }
}
