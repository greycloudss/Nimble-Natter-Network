package com.example.messenger;

import com.example.clientHandlePackage.Client;
import com.example.clientHandlePackage.Server;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

    ArrayList<Pair<String, String>> ServerInfo = new ArrayList<>();
    ArrayList<Server> Servers = new ArrayList<>();
    Client curClient = new Client();

    private String pName;
    private String pPass;
    private String pMeetingID;
    
    public void onBtn() {

        if (nickname.getText().isEmpty() || password.getText().isEmpty() || meetingID.getText().isEmpty())
            return;

        //Task<Void> task = new Task<>() {
        //    @Override
        //    protected Void call() throws Exception {
            pName = nickname.getText();
            pPass = password.getText();
            pMeetingID = meetingID.getText();

            final boolean contains = ServerInfo.contains(new Pair<>(pMeetingID, pPass));
            final boolean connected = curClient.isConnected() && Servers.contains(curClient.getServer());

            switch (determineState(contains, connected)) {
                case NEITHER:  // create server & join
                    curClient.setServer(new Server(pMeetingID, pPass, Servers.size()));
                    Servers.add(curClient.getServer());
                    ServerInfo.add(new Pair<>(pMeetingID, pPass));
                    break;


                case ONLY_CONTAINS: // server exists & join
                    for (Server s : Servers) {
                        if (Objects.equals(s.returnServerInfo(), new Pair<>(meetingID, password))) {
                            s.addClient(curClient);
                            curClient.setServer(s);
                        }
                    }
                    break;


                case ONLY_CONNECTED: // bad outcome pray it doesnt happen
                    curClient.connected(false);
                    break;


                default: // exists and joined already

                    break;
            }
    }

    public void switchFXML(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = fxmlLoader.load();
            Stage currentStage = (Stage) Btn.getScene().getWindow();

            currentStage.setTitle(fxml);
            currentStage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    State determineState(boolean contains, boolean connected) {
        if (contains && connected) return State.BOTH_TRUE;
        if (contains) return State.ONLY_CONTAINS;
        if (connected) return State.ONLY_CONNECTED;
        return State.NEITHER;
    }
}
