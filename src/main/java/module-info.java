module com.example.newmsg {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;

    opens com.example.newmsg to javafx.fxml;
    exports com.example.newmsg;
}