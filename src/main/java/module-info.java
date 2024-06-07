module com.discordcompressor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.discordcompressor to javafx.fxml;
    exports com.discordcompressor;
}
