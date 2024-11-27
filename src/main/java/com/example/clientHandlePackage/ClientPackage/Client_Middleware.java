package com.example.clientHandlePackage.ClientPackage;

import com.example.messenger.ControlUnit;

import java.net.Socket;

public class Client_Middleware {
    private final Socket serverSocket;
    private final String pName;
    private final boolean connected;
    private final ControlUnit controlUnit;
    Client_Middleware(String pName, Socket serverSocket, ControlUnit controlUnit) {
        this.pName = pName;
        this.serverSocket = serverSocket;
        this.controlUnit = controlUnit;
        connected = serverSocket != null ? true : false;
    }

    public String constructMessage(String message) {
        if (!connected) {
            return "Key%d".formatted(serverSocket.getLocalPort());
        }

        if (message == null || message.isEmpty())
            throw new NullPointerException("Message is null");

        if (message.length() > 512)
            throw new IllegalArgumentException("Message is too long");

        controlUnit.updateScreen(pName + ':' + message);

        return pName + ':' + message;
    }

    public void displayNewMessage(String message) {
        this.controlUnit.updateScreen(message);
    }
}