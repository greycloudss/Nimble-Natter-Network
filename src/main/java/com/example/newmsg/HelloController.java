package com.example.newmsg;

import com.example.newmsg.serverPack.Server;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;


public class HelloController {
    @FXML
    public TextField name, ip, port;
    @FXML
    public Button Btn;
    @FXML
    public ListView<String> list;
    serverStruct svStruct;
    @FXML
    private ObservableList<Pair<String, String>> messages = FXCollections.observableArrayList();
    @FXML
    ClientMiddleware clientMiddleware;

    String stringer(Pair<String, String> pair) {
        return pair.getKey() + ": " + pair.getValue();
    }

    public void jcBtn() {
        new File("chat.txt").delete();
        byte[] localIp = {127, 0, 0, 1};
        svStruct = new serverStruct(name.getText(), localIp, Integer.parseInt(port.getText()));

        System.out.println("Checking server status...");
        if (!svStruct.isServerRunning()) {
            System.out.println("No server detected. Starting a new server...");

            Thread serverThread = new Thread(() -> {
                new Server(svStruct.getPort()).start();
            });
            serverThread.start();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.out.println("Server is already running on port: " + svStruct.getPort());
        }

        System.out.println("Starting client middleware...");
        clientMiddleware = new ClientMiddleware(svStruct, messages);

        new Thread(() -> {
            while (clientMiddleware.writer== null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Platform.runLater(() -> switchFXML("MessagePage.fxml"));
        }).start();
    }



    public void switchFXML(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = fxmlLoader.load();

            Object controller = fxmlLoader.getController();

            if (controller instanceof MessagePageController) {
                ((MessagePageController) controller).setClientMiddleware(clientMiddleware);
            }

            Stage currentStage = (Stage) Btn.getScene().getWindow();
            currentStage.setTitle(fxml);
            currentStage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}