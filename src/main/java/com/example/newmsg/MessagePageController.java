package com.example.newmsg;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MessagePageController {
    @FXML
    private TextField messageField;
    @FXML
    public Button Sender;
    @FXML
    private ListView<Pair<String, String>> messageListView;
    private ClientMiddleware clientMiddleware;

    @FXML
    public void initialize() {
        if (clientMiddleware != null) {
            messageListView.setItems(clientMiddleware.allMessagesReceived);
            messageListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Pair<String, String> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getKey() + ": " + item.getValue());
                        try {
                            output(item);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }//127.0.0.1
            });
        }

        messageField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    sendMessage();
                    break;
                default:
                    break;
            }
        });
    }

    public void setClientMiddleware(ClientMiddleware clientMiddleware) {
        this.clientMiddleware = clientMiddleware;
        if (messageListView != null) {
            initialize();
        }
    }

    @FXML
    private void sendMessage() {
        if (clientMiddleware != null && messageField != null) {
            String message = messageField.getText();
            if (message != null && !message.isEmpty()) {
                clientMiddleware.sendMessage(message);
                messageField.clear();
            }
        }
    }


    private void output(Pair<String, String> pair) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("chat.txt", true))) {
            bufferedWriter.write(pair.getKey());
        }
    }
}
