package com.example.whiteboard.client;

import com.example.whiteboard.WhiteboardController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientImpl extends UnicastRemoteObject implements ClientInterface {
    private WhiteboardController controller;

    public void setController(WhiteboardController controller){
        this.controller = controller;
    }
    public ClientImpl() throws RemoteException {
    }


    @Override
    public synchronized void updateUserList(String username, Boolean manager) throws RemoteException {
        controller.addName(username);
    }

    @Override
    public synchronized void clearUserList() throws RemoteException {
        controller.clearUserList();
    }

    @Override
    public synchronized void imageReceived(byte[] imageData) throws RemoteException {
        controller.updateWhiteboard(imageData);
    }

    @Override
    public synchronized void eraserReceived(byte[] imageData) throws RemoteException {
        controller.updateErasedWhiteboard(imageData);
    }

    @Override
    public void quitApplication() throws RemoteException {
        System.exit(0);
    }

    @Override
    public synchronized void receiveMessage(String message, String username) throws RemoteException {
        controller.addMessage(message, username);
    }

    @Override
    public synchronized void newPane() throws RemoteException {
        controller.newPanes();
    }


    @Override
    public synchronized void removeWaitingScreen() throws RemoteException {
        controller.setWaitingScreen(false);
    }

    @Override
    public synchronized void updateWaitingList(String username, Boolean manager) throws RemoteException {
        controller.addWaitingName(username);
    }
    @Override
    public synchronized void clearWaitingList() throws RemoteException {
        controller.clearWaitingList();
    }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

}
