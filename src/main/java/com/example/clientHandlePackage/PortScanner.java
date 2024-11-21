package com.example.clientHandlePackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
*
*
* worse comes to worst encode and send the meetingID to each server up to port 65k and if none of those are the ones we need
* which means that we need to create a server instead
* since the ports are finite, the amount of strain the router can get is also finite
* and the ports start from 80 that means that we can complete this scan in seconds
*
*
*/

public class PortScanner {
    private List<Server> Servers = Collections.synchronizedList(new ArrayList<>());
    private int firstPort;
    private final int minPort = 80;
    int port;
    private InetAddress localIP;
    Thread scanThread;

    public PortScanner(byte[] ipv4, int port) { // specific ip/port
        try (Socket tmpSocket = new Socket(InetAddress.getByAddress(ipv4), port)) {
            DataInputStream in = new DataInputStream(tmpSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(tmpSocket.getOutputStream());

            String str = "Key";
            out.writeUTF(str);


            String serverResponse = in.readUTF();
            if (serverResponse.contains("$-abcd_$")) {
                String meetingID = serverResponse.substring(serverResponse.indexOf("$"));
                String password = serverResponse.substring(serverResponse.lastIndexOf("$") + 1, serverResponse.length());
                Servers.add(new Server(port, meetingID, password));
            }
            this.port = port;
        } catch (Exception e) {
            //server straight up doesnt exist
            this.port = -1;
            System.out.println(e);
        }
        firstPort = -1;
        scanThread.start();
    }


    public PortScanner() { // Scan all ports locally
        try {
            localIP = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        } catch (UnknownHostException e) {
            return;
        }

        int overallMaxPorts = 65536;
        this.scanThread = new Thread(() -> {
            int curPort = minPort;
            int counter = 0;
            boolean found = false;

            while (curPort <= overallMaxPorts && counter < 10) {
                try (Socket tmpSocket = new Socket(localIP, curPort)) {
                    DataInputStream in = new DataInputStream(tmpSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(tmpSocket.getOutputStream());

                    String str = "Key" + curPort;
                    out.writeUTF(str);
                    out.flush();

                    String serverResponse = in.readUTF();
                    if (serverResponse.contains("$-abcd_$")) {
                        String meetingID = serverResponse.substring(0,serverResponse.indexOf("$"));
                        String password = serverResponse.substring(serverResponse.lastIndexOf("$") + 1, serverResponse.length());
                        Servers.add(new Server(curPort, meetingID, password));
                        System.out.println("Server found on port: " + curPort);
                    }
                    counter = 0;
                } catch (Exception e) {
                    this.firstPort = found ? firstPort : curPort;
                    found = true;
                    counter++;
                }
                curPort++;
            }
        });
        scanThread.start();
        this.awaitCompletion();
    }

    public void awaitCompletion() {
        try {
            scanThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int returnFirstNotUsedPort() {
        return firstPort;
    }

    public List<Server> Servers() {
        return Servers != null ? Servers : new ArrayList<>();
    }
}
