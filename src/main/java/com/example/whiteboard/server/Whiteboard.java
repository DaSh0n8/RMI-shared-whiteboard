package com.example.whiteboard.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.whiteboard.client.Client;
import com.example.whiteboard.client.ClientImpl;
import com.example.whiteboard.client.ClientInterface;
import javafx.scene.paint.Color;

public class Whiteboard extends UnicastRemoteObject implements WhiteboardInterface {
    private static boolean whiteboardRunning;
    private byte[] currentCanvas;
    private ArrayList<Client> clients;
    private ArrayList<Client> waitingClients;

    public Whiteboard() throws RemoteException {
        clients = new ArrayList<>();
        waitingClients = new ArrayList<>();
    }

    @Override
    public synchronized void registerClient(String username, boolean manager, ClientInterface clientImpl) throws RemoteException {
        Client client = new Client (waitingClients.size()+1, username, manager, clientImpl);
        waitingClients.add(client);
        for (Client aClient: waitingClients){
            aClient.getClientImpl().clearWaitingList();
            for (Client everyClient: waitingClients){
                if (everyClient.getClientId() != 1){
                    aClient.getClientImpl().updateWaitingList(everyClient.getUsername(),everyClient.getStatus());
                }
            }
        }
    }

    public ArrayList<Client> getClientList (){
        return waitingClients;
    }


    @Override
    public synchronized void receiveImageData(byte[] imageData, ClientInterface clientInterface) throws RemoteException {
        this.currentCanvas = imageData;
        for (Client client : clients) {
            client.getClientImpl().imageReceived(imageData);
        }
    }

    @Override
    public synchronized void receiveEraserData(byte[] imageData, ClientInterface clientImpl) throws RemoteException {
        this.currentCanvas = imageData;
        for (Client client : clients) {
            client.getClientImpl().eraserReceived(imageData);
        }
    }

    @Override
    public synchronized void kickUser(String username) throws RemoteException {
        Client clientToBeKicked = null;
        Iterator<Client> iterator = clients.iterator();

        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.getUsername().equals(username)) {
                clientToBeKicked = client;
                iterator.remove(); // Remove the client safely from the list
                break;
            }
        }

        if (clientToBeKicked != null) {
            try {
                clientToBeKicked.getClientImpl().quitApplication();
            } catch (RemoteException e) {
                // Handle any remote exception that occurs during the call to quitApplication()
            }
        }

        for (Client aClient : clients) {
            aClient.getClientImpl().clearUserList();
            for (Client everyClient : clients) {
                aClient.getClientImpl().updateUserList(everyClient.getUsername(), everyClient.getStatus());
            }
        }
    }

    @Override
    public synchronized void rejectUser(String username) throws RemoteException {
        Client clientToBeKicked = null;
        Iterator<Client> iterator = waitingClients.iterator();

        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.getUsername().equals(username)) {
                clientToBeKicked = client;
                iterator.remove(); // Remove the client safely from the list
                break;
            }
        }

        if (clientToBeKicked != null) {
            try {
                clientToBeKicked.getClientImpl().quitApplication();
            } catch (RemoteException e) {
                // Handle any remote exception that occurs during the call to quitApplication()
            }
        }
        waitingClients.remove(clientToBeKicked);

        for (Client aClient : waitingClients) {
            aClient.getClientImpl().clearWaitingList();
            for (Client everyClient : waitingClients) {
                if(everyClient.getClientId() != 1){
                    aClient.getClientImpl().updateWaitingList(everyClient.getUsername(), everyClient.getStatus());
                }
            }
        }
    }

    @Override
    public synchronized void receiveMessage(String message, String username) throws RemoteException {
        for (Client client : clients){
            client.getClientImpl().receiveMessage(message, username);
        }
    }

    @Override
    public synchronized void distributeImageData(ClientInterface clientImpl) throws RemoteException {
        for (Client client : clients) {
            client.getClientImpl().imageReceived(getCurrentCanvas());
        }
    }

    @Override
    public synchronized void setCurrentCanvas(byte[] imageData) throws RemoteException {
        this.currentCanvas = imageData;
    }

    @Override
    public byte[] getCurrentCanvas() throws RemoteException {
        return currentCanvas;
    }

    @Override
    public synchronized void newPane() throws RemoteException {
        for (Client client : clients) {
            client.getClientImpl().newPane();
        }
    }

    @Override
    public synchronized void openFile() throws RemoteException {
        for (Client client : clients) {
            client.getClientImpl().imageReceived(getCurrentCanvas());
        }
    }

    @Override
    public synchronized void removeWaitingScreen(String username) throws RemoteException {
        Client clientToBeApproved = null;
        Iterator<Client> iterator = waitingClients.iterator();

        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.getUsername().equals(username)) {
                clientToBeApproved = client;
                iterator.remove(); // Remove the client safely from the list
                break;
            }
        }

        if (clientToBeApproved != null) {
            try {
                clientToBeApproved.getClientImpl().removeWaitingScreen();
            } catch (RemoteException e) {
                // Handle any remote exception that occurs during the call to quitApplication()
            }
        }
        clients.add(clientToBeApproved);
        waitingClients.remove(clientToBeApproved);
        for (Client aClient : clients) {
            aClient.getClientImpl().clearUserList();
            for (Client everyClient : clients) {
                aClient.getClientImpl().updateUserList(everyClient.getUsername(), everyClient.getStatus());
            }
        }

        for (Client aClient: waitingClients){
            aClient.getClientImpl().clearWaitingList();
            for (Client everyClient: waitingClients){
                if (everyClient.getClientId() != 1){
                    aClient.getClientImpl().updateWaitingList(everyClient.getUsername(),everyClient.getStatus());
                }
            }
        }

    }

    @Override
    public void addManagerToClientList() throws RemoteException {
        clients.add(waitingClients.get(0));
        clients.get(0).getClientImpl().updateUserList(clients.get(0).getUsername(), clients.get(0).getStatus());
    }
}