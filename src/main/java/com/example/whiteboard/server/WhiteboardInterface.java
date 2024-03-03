package com.example.whiteboard.server;

import com.example.whiteboard.client.Client;
import com.example.whiteboard.client.ClientImpl;
import com.example.whiteboard.client.ClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface WhiteboardInterface extends Remote {
    void registerClient(String username, boolean manager, ClientInterface clientImpl) throws RemoteException;
    ArrayList<Client> getClientList () throws RemoteException;

    void receiveImageData (byte[] imageData, ClientInterface clientImpl) throws RemoteException;
    void receiveEraserData (byte[] imageData, ClientInterface clientImpl) throws RemoteException;
    void kickUser (String username) throws RemoteException;
    void rejectUser(String username) throws RemoteException;
    void receiveMessage (String message, String username) throws RemoteException;
    void distributeImageData (ClientInterface clientImpl) throws RemoteException;
    void setCurrentCanvas(byte[] imageData) throws RemoteException;
    byte[] getCurrentCanvas() throws RemoteException;
    void newPane() throws RemoteException;

    void openFile() throws RemoteException;
    void removeWaitingScreen(String username) throws RemoteException;
    void addManagerToClientList() throws RemoteException;

}