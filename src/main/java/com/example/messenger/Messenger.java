package com.example.messenger;

import com.example.clientHandlePackage.Client;
import com.example.clientHandlePackage.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.net.Socket;

public class Messenger {
    Server server;
    Client client;

    Messenger(Client client) {
        this.client = client;
        server = this.client.getServer();
        System.out.println("Messenger");
    }
}