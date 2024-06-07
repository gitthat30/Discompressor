package com.discordcompressor;

import javafx.scene.media.Media;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Controller {

    @FXML
    private TextField videoTextField;
    @FXML
    private TextField destinationTextField;
    @FXML
    private Button videoFileBrowseButton;
    @FXML
    private Button destinationBrowseButton;
    @FXML
    private Button compressButton;
    @FXML
    private Button openButton;

    private File currentFile;

    public void initialize() {
        videoTextField.setEditable(false);
        destinationTextField.setEditable(false);
    }



    public void test(ActionEvent ae) {
        //ProcessBuilder processBuilder = new ProcessBuilder("./bin/ffmpeg", "-version"); <- Use this code when compiling to JAR
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                // Append output to TextArea on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> videoTextField.appendText(finalLine + "\n"));
            }
            int exitCode = process.waitFor();
            javafx.application.Platform.runLater(() -> videoTextField.appendText("Process exited with code: " + exitCode + "\n"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void browseVideoFile(ActionEvent e) {
        FileChooser browse = new FileChooser();
        browse.setTitle("Select Video File");
        browse.setInitialDirectory(new File(System.getProperty("user.home")));
        browse.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi", "*.mov", "*.wmv"));

        File selectedFile = browse.showOpenDialog(new Stage());

        if (selectedFile != null) {
            currentFile = selectedFile;
            videoTextField.setText(selectedFile.getAbsolutePath());
        }
        else {
            videoTextField.setText("");
        }
    }

    public void chooseDestinationLocation(ActionEvent e) {
        DirectoryChooser folder = new DirectoryChooser();
        folder.setTitle("Select Destination Folder");

        folder.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedFolder = folder.showDialog(new Stage());

        if (selectedFolder != null) {
            destinationTextField.setText(selectedFolder.getAbsolutePath());
        }
        else {
            destinationTextField.setText("");
        }
    }

    public void openDestinationFolder(ActionEvent e) {
        ProcessBuilder processBuilder = new ProcessBuilder("explorer.exe", destinationTextField.getText());
        try {
            processBuilder.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int calculateOutputBitrate() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", videoTextField.getText());

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())); //Read output of ffmpeg

            String l = reader.readLine();
            String durString = "None";
            while((l != null)) {
                if(l.contains("Duration")) {
                    durString = l.split(",")[0].split(" ")[3];
                }

                l = reader.readLine();
            }

            System.out.println(durString);

            String [] parts = durString.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2].split("\\.")[0]) + (minutes * 60) + (hours * 3600);

            int bitrate = (int) ((7.5 * 8192) / seconds);

            System.out.println(bitrate);

            return bitrate;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void compress(ActionEvent e) {
         int outputBitrate = calculateOutputBitrate();

        System.out.println(outputBitrate);

        //Todo
        /* Compression logic
        *  Browse from the folder you left off
        *  Input validation for compress*/
    }
}
