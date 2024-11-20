package com.example.clientHandlePackage;

import javafx.util.Pair;
import jdk.incubator.vector.VectorOperators;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/*
*
*
* worst comes to worst encode and send the meetingID to each server up to port 65k and if none of those are the ones we need
* which means that we need to create a server instead
* since the ports are finite, the amount of strain the router can get is also finite
* and the ports start from 80 that means that we can complete this scan in seconds
*
*
*/

public class PortScanner {
    private ArrayList<Server> Servers;
    private final int overallMaxPorts = 65536;
    private int maxPort;
    private final int minPort = 80;
    private InetAddress localIP;

    public PortScanner(byte[] ipv4, int port) {
        int curPort = minPort;
        int counter = 0;
        try (Socket tmpSocket = new Socket(InetAddress.getByAddress(ipv4), port)) {
            DataInputStream in = new DataInputStream(tmpSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(tmpSocket.getOutputStream());

            String str = "Key";
            out.writeUTF(str);


            String serverResponse = in.readUTF();
            if (serverResponse.contains("$-abcd_$")) {
                String meetingID = serverResponse.substring(serverResponse.indexOf("$"));
                String password = serverResponse.substring(serverResponse.lastIndexOf("$") + 1, serverResponse.length());
                Servers.add(new Server(curPort, meetingID, password));
            }
        } catch (Exception e) {
            //server straight up doesnt exist
            System.out.println(e);
        }
    }


    public PortScanner() { // for parsing through ports on local
        int curPort = minPort;
        int counter = 0;

        try {
            localIP = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        } catch (UnknownHostException e) {
            // I sincerely hope this never happens. not being able to resolve yourself? what the wok
        }

        while (curPort <= overallMaxPorts && counter < 10) {
            try (Socket tmpSocket = new Socket(localIP, curPort)) {
                DataInputStream in = new DataInputStream(tmpSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(tmpSocket.getOutputStream());

                String str = "Key";
                out.writeUTF(str);


                String serverResponse = in.readUTF();
                if (serverResponse.contains("$-abcd_$")) {
                    String meetingID = serverResponse.substring(serverResponse.indexOf("$"));
                    String password = serverResponse.substring(serverResponse.lastIndexOf("$") + 1, serverResponse.length());

                }
                counter ^= counter;
            } catch (Exception e) {
                counter++;
            }
            curPort++;
        }
    }

    public int returnFirstNotUsedPort() {
        return maxPort;
    }

    public ArrayList<Server> Servers() {
        return Servers != null ? Servers : new ArrayList<>();
    }
}
