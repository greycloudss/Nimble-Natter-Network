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
    ArrayList<Server> Servers;
    Client curClient = new Client();

    private String pName;
    private String pPass;
    private String pMeetingID;
    
    public void onJoinBtn() {
        if (nickname.getText().isEmpty() || password.getText().isEmpty() || meetingID.getText().isEmpty())
            return;


        pName = nickname.getText();
        pPass = password.getText();
        pMeetingID = meetingID.getText();

        PortScanner pScan = new PortScanner();

        Servers = pScan.Servers();
        System.out.println(Servers);
        System.out.println(pScan.Servers());

        final boolean contains = ServerInfo.contains(new Pair<>(pMeetingID, pPass));
        final boolean connected = curClient.isConnected() && Servers.contains(curClient.getServer());

        switch (determineState(contains, connected)) {
            case NEITHER:  // create server & join
                curClient.setServer(new Server(pMeetingID, pPass,  Servers == null ? 0 : Servers.size()));
                Servers.add(curClient.getServer());
                ServerInfo.add(new Pair<>(pMeetingID, pPass));
                System.out.println("create server & join it");
                switchFXML("messenger.fxml");
                break;

            case ONLY_CONTAINS: // server exists & join
                for (Server s : Servers) {
                    if (Objects.equals(s.returnServerInfo(), new Pair<>(pMeetingID, pPass))) {
                        s.addClient(curClient);
                        curClient.setServer(s);
                    }
                }
                System.out.println("server exists & joining");
                switchFXML("messenger.fxml");
                break;

            case ONLY_CONNECTED: // bad outcome pray it doesnt happen
                curClient.connected(false);
                System.out.println("if you are reading this i officially dont know what happened");
                switchFXML("messenger.fxml");
                break;

            default: // exists and joined already
                switchFXML("Messenger");
                System.out.println("exists and joined already");
                // if somehow this part of the func is accessed that means the fxml file has not been switched yet
                break;
        }

        recipient = new Text(!recipient.getText().isEmpty() && pMeetingID != null ? pMeetingID : "not Able to load at this moment");
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
