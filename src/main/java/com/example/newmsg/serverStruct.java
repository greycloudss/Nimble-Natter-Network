package com.example.newmsg;

import javafx.scene.control.TextField;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class serverStruct {
    private String name;
    private byte[] ip;
    private int port;

    private boolean state;
    serverStruct(TextField name, TextField ip, TextField port) {
        try {
            this.name = name.getText();

            String[] ipStr = ip.getText().split(".");

            this.ip = new byte[ipStr.length];
            for (int i = 0; i < ipStr.length; i++) {
                this.ip[i] = (byte) Integer.parseInt(ipStr[i]);
            }

            this.port = Integer.parseInt(port.getText());
            if (this.port < 0 || this.port > 65000)
                throw new Exception("Invalid port");

            isServerRunning();

        } catch (Exception e) {
            state = false;
            System.out.println("Error: " + e.getMessage());
        }
    }

    public boolean status() {
        return state;
    }

    Thread checkServerT;

    public serverStruct(String name, byte[] ip, int port) {
        this.name = name;

        if (ip == null || ip.length != 4) {
            throw new IllegalArgumentException("Invalid IP address. Expected 4 bytes for IPv4, got: " + (ip == null ? 0 : ip.length));
        }
        this.ip = ip;

        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        this.port = port;
        this.state = false;
    }

    boolean isServerRunning() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try (
                    Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), this.port);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                writer.write("PING\n");
                writer.flush();

                String response = reader.readLine();
                if ("PONG".equals(response)) {
                    System.out.println("Server is active and responding.");
                    state = true;
                    return true;
                } else {
                    System.out.println("Unexpected response from server: " + response);
                }
            } catch (IOException e) {
                System.out.println("Attempt " + attempt + ": No server detected. Retrying...");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        state = false;
        System.out.println("Server is not running on port: " + this.port);
        return false;
    }

    private void threadCheck () {
        try {
            checkServerT.join();
        } catch (Exception e) {
            System.out.println("Processing thread");
        }
    }

    public byte[] getIp() {
        return ip;
    }
    public int getPort() {
        return port;
    }
    public String getName() {
        return name;
    }
}