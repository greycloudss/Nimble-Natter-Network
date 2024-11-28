module com.example.messenger {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires jdk.incubator.vector;
    requires java.net.http;

    opens com.example.messenger to javafx.fxml;
    exports com.example.messenger;
    exports com.example.clientHandlePackage.ServerPackage;
    opens com.example.clientHandlePackage.ServerPackage to javafx.fxml;
    exports com.example.clientHandlePackage.ClientPackage;
    opens com.example.clientHandlePackage.ClientPackage to javafx.fxml;
}