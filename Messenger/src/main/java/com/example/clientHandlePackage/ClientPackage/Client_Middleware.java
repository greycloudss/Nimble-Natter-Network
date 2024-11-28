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
        System.gc();
        connected = serverSocket != null;
    }

    public String constructMessage(String message) {
        if (message == null || message.isEmpty())
            throw new NullPointerException("Message is null");

        if (message.length() > 512) {
            System.err.println("Message truncated to prevent overflow.");
            message = message.substring(0, 512); // Truncate message
        }

        return pName + ':' + message;
    }

    public void displayNewMessage(String message) {
        controlUnit.updateScreen(message);
    }

    //public void displayNewMessage(String message) {
    //    this.controlUnit.updateScreen(message);
    //}
}