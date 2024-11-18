module com.example.messenger {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.desktop;

    opens com.example.messenger to javafx.fxml;
    exports com.example.messenger;
    exports com.example.clientHandlePackage;
    opens com.example.clientHandlePackage to javafx.fxml;
}