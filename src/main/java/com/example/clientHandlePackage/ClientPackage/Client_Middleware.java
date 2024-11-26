package com.example.clientHandlePackage.ClientPackage;

import com.example.clientHandlePackage.ServerPackage.Messenger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;

public class Client_Middleware {
    private final Socket serverSocket;
    private final String pName;
    private boolean connected;

    Client_Middleware(String pName, Socket serverSocket) {
        this.pName = pName;
        this.serverSocket = serverSocket;
        connected = serverSocket != null ? true : false;
    }

    public boolean isConnected() {
        return connected;
    }

    public String constructMessage(String message) {
        if (!connected)
            return "Key%d".formatted(serverSocket.getLocalPort());

        if (message == null || message.isEmpty())
            throw new NullPointerException("Message is null");

        if (message.length() > 512)
            throw new IllegalArgumentException("Message is too long");

        return pName + ':' + message;
    }
}
