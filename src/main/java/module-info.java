module com.discordcompressor {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.discordcompressor to javafx.fxml;
    exports com.discordcompressor;
}
