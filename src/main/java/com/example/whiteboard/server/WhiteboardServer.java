package com.example.whiteboard.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WhiteboardServer {
    private static boolean whiteboardRunning = false;
    private static ExecutorService executorService;

    public static void main(String[] args) {
        try {
            WhiteboardInterface whiteboard = new Whiteboard();
            Registry registry = LocateRegistry.createRegistry(1233);
            registry.bind("Whiteboard", whiteboard);
            executorService = Executors.newFixedThreadPool(10);
            System.out.println("Whiteboard server ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWhiteboardRunning() {
        return whiteboardRunning;
    }

    public static void setWhiteboardRunning(boolean run){
        whiteboardRunning = run;
    }
}