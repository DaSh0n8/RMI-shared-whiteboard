package com.example.whiteboard.client;

import java.io.Serializable;
import java.rmi.RemoteException;

public class Client implements Serializable {
    public static int clientIdCounter = 0;
    private int clientId;
    private String username;
    private Boolean manager;
    private ClientInterface clientImpl;
    public Client(int clientId, String username, Boolean manager, ClientInterface clientImpl){
        this.clientId = clientId;
        this.username = username;
        this.manager = manager;
        this.clientImpl =  clientImpl;
    }

    public int getClientId(){
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getStatus() {
        return manager;
    }

    public void setManager(Boolean status) {
        this.manager = status;
    }

    public void setClientImpl(ClientImpl clientImpl){
        this.clientImpl = clientImpl;
    }

    public ClientInterface getClientImpl (){
        return clientImpl;
    }

    private static synchronized int generateClientId() {
        return ++clientIdCounter;
    }
}
