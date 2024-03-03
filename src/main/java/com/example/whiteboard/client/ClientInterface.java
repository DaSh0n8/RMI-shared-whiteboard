package com.example.whiteboard.client;

import javafx.scene.shape.Shape;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {

    void updateUserList (String username, Boolean manager) throws RemoteException;

    void clearUserList() throws RemoteException;
    void imageReceived(byte[] imageData) throws RemoteException;
    void eraserReceived(byte[] imageData) throws RemoteException;
    void quitApplication() throws  RemoteException;
    void receiveMessage(String message, String username) throws RemoteException;
    void newPane() throws RemoteException;
    void removeWaitingScreen() throws RemoteException;
    void updateWaitingList(String username, Boolean manager) throws RemoteException;
    void clearWaitingList() throws RemoteException;
}
