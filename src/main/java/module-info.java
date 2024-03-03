module com.example.whiteboard {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.rmi;
    requires javafx.swing;

    opens com.example.whiteboard to javafx.fxml;
    exports com.example.whiteboard;
    exports com.example.whiteboard.client;
    exports com.example.whiteboard.server;

}

