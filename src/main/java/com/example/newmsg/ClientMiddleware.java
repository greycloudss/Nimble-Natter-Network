package com.example.newmsg;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientMiddleware {
    private final String name;
    private final byte[] ip;
    private final int port;
    protected final ObservableList<Pair<String, String>> allMessagesReceived;
    protected BufferedWriter writer;

    public ClientMiddleware(serverStruct struct, ObservableList<Pair<String, String>> allMessagesReceived) {
        this.name = struct.getName();
        this.ip = struct.getIp();
        this.port = struct.getPort();
        this.allMessagesReceived = allMessagesReceived;

        // Start connection thread
        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try (
                    Socket svSocket = new Socket(InetAddress.getByAddress(ip), port);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(svSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(svSocket.getOutputStream()))
            ) {
                this.writer = writer;
                writer.write("PING\n");
                writer.flush();

                String handshakeResponse = reader.readLine();
                if (!"PONG".equals(handshakeResponse)) {
                    System.err.println("Invalid handshake response: " + handshakeResponse);
                    return;
                }

                String incomingMessage;
                while ((incomingMessage = reader.readLine()) != null) {
                    processIncomingMessage(incomingMessage);
                }
            } catch (IOException e) {
                System.err.println("Error in client middleware: " + e.getMessage());
            }
        }).start();
    }

    private void processIncomingMessage(String message) {
        Pair<String, String> parsedMessage = chopMessage(message);
        //System.out.println(parsedMessage.getKey() + ": " + parsedMessage.getValue());
        Platform.runLater(() -> allMessagesReceived.add(parsedMessage));
    }

    private Pair<String, String> chopMessage(String message) {
        int separatorIndex = message.indexOf(':');
        if (separatorIndex == -1 || separatorIndex == 0 || separatorIndex == message.length() - 1) {
            System.err.println("Invalid message format: " + message);
            return new Pair<>("Unknown", message);
        }
        String sender = message.substring(0, separatorIndex);
        String content = message.substring(separatorIndex + 1);
        return new Pair<>(sender, content);
    }

    public synchronized void sendMessage(String message) {
        if (writer == null) {
            System.err.println("Cannot send message: Not connected to the server.");
            return;
        }

        try {
            String formattedMessage = name + ":" + message;
            Platform.runLater(() -> allMessagesReceived.add(new Pair<>(name, message)));
            writer.write(formattedMessage + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public byte[] getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
