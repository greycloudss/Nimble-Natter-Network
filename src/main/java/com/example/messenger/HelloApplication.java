package com.example.messenger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Server.fxml"));
        ControlUnit controlUnit = new ControlUnit();
        Scene scene = new Scene(fxmlLoader.load(), 360, 600);
        fxmlLoader.setController(controlUnit);
        stage.setResizable(false);
        stage.setTitle("Nimble Natter Network");
        stage.setScene(scene);
        stage.show();
    }

    public static void main() {

        launch();
    }
}

