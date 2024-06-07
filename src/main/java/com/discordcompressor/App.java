package com.discordcompressor;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();

        Scene scene = new Scene(root, Color.WHITE);

        stage.setTitle("Hello World");

        Text text = new Text();
        text.setText("Hello World");

        text.setX(50);
        text.setY(50);

        root.getChildren().add(text);

        stage.setScene(scene);
        stage.show();
    }
}