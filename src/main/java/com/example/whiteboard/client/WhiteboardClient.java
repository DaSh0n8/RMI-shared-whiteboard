package com.example.whiteboard.client;

import com.example.whiteboard.WhiteboardApplication;
import com.example.whiteboard.server.WhiteboardServer;
import javafx.application.Application;
import javafx.application.Platform;


public class WhiteboardClient {
    public static void main(String[] args) {
        try {

            if (!Platform.isFxApplicationThread()) {
                new Thread(() -> Application.launch(WhiteboardApplication.class)).start();
            } else {
                Application.launch(WhiteboardApplication.class);
            }

            if (!WhiteboardServer.isWhiteboardRunning()){
                WhiteboardServer.setWhiteboardRunning(true);
            }

            System.out.println("Whiteboard started");

        } catch (Exception var11) {
            var11.printStackTrace();
        }

    }
}
