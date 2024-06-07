package com.discordcompressor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class App extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(App.class.getResource("fxml/MainLayout.fxml"));

        Scene scene = new Scene(root, Color.WHITE);

        stage.setTitle("Discompressor");

        stage.setScene(scene);
        stage.show();
    }
}