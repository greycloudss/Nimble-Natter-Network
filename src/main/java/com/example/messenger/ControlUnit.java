package com.example.messenger;

import com.example.clientHandlePackage.Client;
import com.example.clientHandlePackage.PortScanner;
import com.example.clientHandlePackage.Server;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

enum State {
    BOTH_TRUE,
    ONLY_CONTAINS,
    ONLY_CONNECTED,
    NEITHER
}

public class ControlUnit {
    @FXML
    public TextField meetingID;
    @FXML
    public TextField nickname;
    @FXML
    public Button Btn;
    @FXML
    public PasswordField password;
    @FXML
    public Text recipient = new Text();
    @FXML
    public TextField messageField = new TextField();

    ArrayList<Pair<String, String>> ServerInfo = new ArrayList<>();
    List<Server> Servers;
    Thread msgThread;
    private String pName;
    private String pPass;
    private String pMeetingID;

    Client curClient = new Client(pName);

    Messenger msg;

    public void onJoinBtn() {
        if (nickname.getText().isEmpty() || password.getText().isEmpty() || meetingID.getText().isEmpty())
            return;

        pName = nickname.getText();
        pPass = password.getText();
        pMeetingID = meetingID.getText();

        PortScanner pScan = new PortScanner();
        Servers = pScan.Servers();

        for (Server server : Servers)
            ServerInfo.add(new Pair<>(server.getMeetingID(), server.getPasswordField()));

        System.out.println(Servers);

        final boolean contains = ServerInfo.contains(new Pair<>(pMeetingID, pPass));
        final boolean connected = curClient.isConnected() && Servers.contains(curClient.getServer());

        switch (determineState(contains, connected)) {
            case NEITHER: // Create server & join

                //curClient.setServer(new Server(pMeetingID, pPass, pScan.returnFirstNotUsedPort()));
                if (!ServerInfo.contains(new Pair<>(pMeetingID, pPass))) {
                    curClient.setServer(new Server(pMeetingID, pPass, pScan.returnFirstNotUsedPort()));
                    Servers.add(curClient.getServer());
                    ServerInfo.add(new Pair<>(pMeetingID, pPass));
                    System.out.println("Create server & join it");
                }

                for (Server server : Servers) {
                    System.out.println(server.getMeetingID() + " " + server.getPasswordField() + " "  + server.getSocketPort());
                }

                switchFXML("messenger.fxml");
                break;

            case ONLY_CONTAINS: // Server exists & join
                List<Server> serverCopy = new ArrayList<>(Servers);
                for (Server s : serverCopy) {
                    if (Objects.equals(s.returnServerInfo(), new Pair<>(pMeetingID, pPass))) {
                        s.addClient(curClient);
                        curClient.setServer(s);
                    }
                }
                System.out.println("Server exists & joining");
                switchFXML("messenger.fxml");
                break;

            case ONLY_CONNECTED: // Unexpected case
                curClient.connected(false);
                try {
                    curClient.getServer().close();
                } catch (Exception e) {
                    curClient.setServer(null);
                }
                System.out.println("Unexpected state occurred");
                System.exit(0);
                switchFXML("messenger.fxml");
                break;

            default: // Exists and joined already
                switchFXML("Messenger");
                System.out.println("Exists and joined already");
                break;
        }

        recipient = new Text(!recipient.getText().isEmpty() && pMeetingID != null ? pMeetingID : "Not able to load at this moment");
        msgThread = new Thread(() -> {
            this.msg = new Messenger(curClient);
        });
        msgThread.start();
    }

    public void awaitCompletion() {
        try {
            msgThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void switchFXML(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = fxmlLoader.load();

            ControlUnit controller = fxmlLoader.getController();
            controller.setRoomId(pMeetingID);

            Stage currentStage = (Stage) Btn.getScene().getWindow();
            currentStage.setTitle(fxml);
            currentStage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRoomId(String roomId) {
        if (recipient != null) {
            recipient.setText(roomId != null ? roomId : "Unable to load room ID");
        }
    }

    State determineState(boolean contains, boolean connected) {
        if (contains && connected) return State.BOTH_TRUE;
        if (contains) return State.ONLY_CONTAINS;
        if (connected) return State.ONLY_CONNECTED;
        return State.NEITHER;
    }
}
