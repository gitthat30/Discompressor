package com.discordcompressor;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    private String IDEString = "ffmpeg";
    private String compileString = "./bin/ffmpeg";
    private double videoBitrate = 0;
    private double audioBitrate = 0;
    private double totalSeconds = 0;

    @FXML
    private TextField videoTextField;
    @FXML
    private TextField destinationTextField;
    @FXML
    private TextField filenameField;

    @FXML
    private Button videoFileBrowseButton;
    @FXML
    private Button destinationBrowseButton;
    @FXML
    private Button compressButton;
    @FXML
    private Button openButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Text ffmpegText;
    @FXML
    private ProgressBar progressBar;

    private File currentFile;
    private File selectedFolder;

    public void detectFFMPEG() {
        //ProcessBuilder processBuilder = new ProcessBuilder("./bin/ffmpeg", "-version"); <- Use this code when compiling to JAR
        ProcessBuilder processBuilder = new ProcessBuilder(IDEString, "-version");

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            ffmpegText.setText("FFMPEG Detected ");
        } catch (IOException | InterruptedException e) {
            ffmpegText.setText("FFMPEG not found ");
        }

    }

    public void initialize() {
        detectFFMPEG();
        videoTextField.setEditable(false);
        destinationTextField.setEditable(false);
    }



    public void test(ActionEvent ae) {
        //ProcessBuilder processBuilder = new ProcessBuilder("./bin/ffmpeg", "-version"); <- Use this code when compiling to JAR
        ProcessBuilder processBuilder = new ProcessBuilder(IDEString, "-version");

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

    public File checkLastDirectory(String field) {
        System.out.println("HERE");
        if(field.equals("File")) {
            if(currentFile != null) {
                System.out.println(currentFile.getAbsolutePath());
                int last = currentFile.getAbsolutePath().lastIndexOf("\\");
                String lastPath = currentFile.getAbsolutePath().substring(0, last);

                return new File(lastPath);
            }
            else {
                System.out.println("No file selected");
                return new File(System.getProperty("user.home"), "Videos");
            }
        }
        else {
            if(selectedFolder != null) {
                return new File(selectedFolder.getAbsolutePath());
            }
            else {
                return new File(System.getProperty("user.home"), "Videos");
            }
        }

    }


    public void browseVideoFile(ActionEvent e) {
        checkLastDirectory("File");
        FileChooser browse = new FileChooser();
        browse.setTitle("Select Video File");

        try {
            browse.setInitialDirectory(checkLastDirectory("File"));
        } catch (Exception ex) {
            browse.setInitialDirectory(new File(System.getProperty("user.home"), "Videos"));
        }

        browse.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mkv", "*.avi", "*.mov", "*.wmv"));

        try {
            currentFile = browse.showOpenDialog(new Stage());
        }
        catch(IllegalArgumentException ex) {
            browse.setInitialDirectory(new File(System.getProperty("user.home"), "Videos"));
            currentFile = browse.showOpenDialog(new Stage());
        }

        if (currentFile != null) {
            videoTextField.setText(currentFile.getAbsolutePath());
        }
        else {
            videoTextField.setText("");
        }
    }

    public void chooseDestinationLocation(ActionEvent e) {
        DirectoryChooser folder = new DirectoryChooser();
        folder.setTitle("Select Destination Folder");


        folder.setInitialDirectory(checkLastDirectory("Folder"));

        try {
            selectedFolder = folder.showDialog(new Stage());
        }
        catch(IllegalArgumentException ex) {
            folder.setInitialDirectory(new File(System.getProperty("user.home"), "Videos"));
            selectedFolder = folder.showDialog(new Stage());
        }

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

    public void calculateOutputBitrate() {
        try {
            ProcessBuilder pb = new ProcessBuilder(IDEString, "-i", videoTextField.getText());

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

            totalSeconds = (double) seconds;


            videoBitrate = (int) ((7.5 * 8192) / seconds) * 0.8;
            audioBitrate = (int) ((7.5 * 8192) / seconds) * 0.2;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void updatePercentage(String l) {
        Pattern timeREGEX = Pattern.compile("time=(\\d{2}:\\d{2}:\\d{2})\\.\\d{2}");
        Matcher timeMatcher = timeREGEX.matcher(l);

        if(timeMatcher.find()) {
            String time = timeMatcher.group(1);

            int hours = Integer.parseInt(time.split(":")[0]);
            int minutes = Integer.parseInt(time.split(":")[1]);
            double seconds = Integer.parseInt(time.split(":")[2]) + (hours * 3600) + (minutes * 60);

            System.out.println(seconds/totalSeconds);
            progressBar.setProgress((seconds / totalSeconds));
        }


    }

    private void readStream(InputStream inputStream, boolean isOutputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                if (isOutputStream) {
                    Platform.runLater(() -> System.out.println(finalLine + "\n"));
                } else {
                    if(finalLine.contains("time")) {

                        Platform.runLater(() -> updatePercentage(finalLine));
                    }

                }
            }
        } catch (IOException ex) {
            Platform.runLater(() -> {
                throw new RuntimeException(ex);
            });
        }
    }

    public void ffmpegCompress() {
        String[] command = {
                "ffmpeg",
                "-i", "\"" +videoTextField.getText() + "\"",
                "-c:v", "libx264",
                "-b:v " + videoBitrate + "k",
                "-b:a " + audioBitrate + "k",
                "\"" + destinationTextField.getText() + "\\output.mp4\""
        };

        String videoURI = "\"" +videoTextField.getText() + "\"";
        String outputName = "\\" + filenameField.getText() + ".mp4\"";
        String destinationURI = "\"" +destinationTextField.getText() + outputName;
        String videoBitrateString = videoBitrate + "k";
        String audioBitrateString = audioBitrate + "k";


        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg",
                "-i", videoURI,
                "-c:v", "libx264",
                "-b:v", videoBitrateString,
                "-b:a", audioBitrateString,
                destinationURI);

        try {
            Process p = processBuilder.start();

            new Thread(() -> readStream(p.getErrorStream(), false)).start();
            int exitCode = p.waitFor();

            System.out.println(exitCode);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void compress(ActionEvent e) {

        calculateOutputBitrate();
        new Thread(() -> ffmpegCompress()).start();

        //Todo
        /* Browse from the folder you left off
        *  Input validation for compress
        *  Remember to change the file location for ffmpeg when compiling into JAR file*/
    }
}
