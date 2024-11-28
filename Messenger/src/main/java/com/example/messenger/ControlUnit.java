package com.example.messenger;

import com.example.clientHandlePackage.ClientPackage.Client;
import com.example.clientHandlePackage.ClientPackage.PortScanner;
import com.example.clientHandlePackage.ServerPackage.Server;
import javafx.application.Platform;
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

    private final ArrayList<Pair<String, String>> serverInfo = new ArrayList<>();
    private List<Server> servers = new ArrayList<>();
    private String pName;
    private String pPass;
    private String pMeetingID;
    private Client curClient;

    public void onJoinBtn() {
        if (nickname.getText().isEmpty() || password.getText().isEmpty() || meetingID.getText().isEmpty()) {
            System.err.println("All fields are required to join or create a server.");
            return;
        }

        Btn.setDisable(true); // Disable button to prevent multiple clicks

        Task<Void> joinTask = new Task<>() {
            @Override
            protected Void call() {
                System.gc();
                try {
                    pName = nickname.getText();
                    pPass = password.getText();
                    pMeetingID = meetingID.getText();

                    PortScanner portScanner = new PortScanner(); // Use original PortScanner
                    servers = portScanner.Servers();

                    for (Server server : servers) {
                        serverInfo.add(new Pair<>(server.getMeetingID(), server.getPasswordField()));
                        System.out.printf("Found server - MeetingID: %s, Password: %s%n", server.getMeetingID(), server.getPasswordField());
                    }

                    boolean contains = serverInfo.contains(new Pair<>(pMeetingID, pPass));
                    boolean connected = (curClient != null && curClient.isConnected());

                    switch (determineState(contains, connected)) {
                        case NEITHER:
                            Platform.runLater(() -> {
                                System.out.println("No matching server found. Creating a new server...");
                                createServerAndJoin(portScanner);
                            });
                            break;

                        case ONLY_CONTAINS:
                            Platform.runLater(() -> {
                                System.out.println("Server exists, attempting to join...");
                                joinExistingServer();
                            });
                            break;

                        case ONLY_CONNECTED:
                            Platform.runLater(() -> System.err.println("Unexpected state: client is connected but server not found."));
                            break;

                        default:
                            Platform.runLater(() -> {
                                System.out.println("Server exists and already joined.");
                                switchFXML("Messenger.fxml");
                            });
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error during join operation: " + e.getMessage());
                }
                return null;
            }
        };

        joinTask.setOnSucceeded(event -> {
            Platform.runLater(() -> Btn.setDisable(false)); // Re-enable button after operation
        });
        joinTask.setOnFailed(event -> {
            Platform.runLater(() -> Btn.setDisable(false));
            System.err.println("Error during join operation: " + joinTask.getException().getMessage());
        });



        new Thread(joinTask).start(); // Run the task on a separate thread
    }


    private void createServerAndJoin(PortScanner portScanner) {
        int availablePort = portScanner.getFirstUnusedPort();
        Server newServer = new Server(pMeetingID, pPass, availablePort);

        if (newServer.getSocket() == null || newServer.getServerSocket() == null) {
            System.err.println("Failed to initialize server for creation.");
            return;
        }

        servers.add(newServer);
        serverInfo.add(new Pair<>(pMeetingID, pPass));

        curClient = new Client(newServer, pName, this);
        if (curClient.isConnected()) {
            System.out.println("New server created and joined successfully.");
            switchFXML("Messenger.fxml");
        } else {
            System.err.println("Failed to join newly created server.");
        }
    }


    private void joinExistingServer() {
        for (Server server : servers) {
            if (serverInfo.contains(new Pair<>(server.getMeetingID(), server.getPasswordField()))) {
                curClient = new Client(server, pName, this);
                if (curClient.isConnected()) {
                    System.out.println("Joined existing server successfully.");
                    switchFXML("Messenger.fxml");
                    return;
                }
            }
        }
        System.err.println("Failed to join any existing server.");
    }

    public void switchFXML(String fxml) {
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = fxmlLoader.load();

                ControlUnit controller = fxmlLoader.getController();
                controller.setRoomId(pMeetingID);
                controller.setCurClient(curClient);

                Stage currentStage = (Stage) Btn.getScene().getWindow();
                currentStage.setTitle(fxml);
                currentStage.setScene(new Scene(root));
            } catch (IOException e) {
                System.err.println("Error switching FXML: " + e.getMessage());
            }
        });
    }


    private void setRoomId(String roomId) {
        if (recipient != null) {
            recipient.setText(roomId != null ? roomId : "Unable to load room ID");
        }
    }

    private void setCurClient(Client client) {
        curClient = client;
    }

    public void onSendBtn() {
        String message = messageField.getText();

        if (message.isEmpty()) {
            System.err.println("Cannot send an empty message.");
            return;
        }

        if (curClient != null && curClient.isConnected()) {
            curClient.sendMessage(message);
            System.out.printf("Message sent: %s%n", message);
            messageField.setText(""); // Clear input field after sending
        } else {
            System.err.println("Client is not connected to a server.");
        }
    }

    public void updateScreen(String message) {
        // Assuming there's a TextArea or similar UI element to display messages
        System.out.println("New message: " + message); // Replace with UI update logic
    }



    State determineState(boolean contains, boolean connected) {
        if (contains && connected) return State.BOTH_TRUE;
        if (contains) return State.ONLY_CONTAINS;
        if (connected) return State.ONLY_CONNECTED;
        return State.NEITHER;
    }
}
