package ru.kpfu.itis.kharakhashyan.client;

import java.io.*;


//this will create an saparate process to accept connection from clients, have only one instance(singleton)
public class ServerReadThread extends Thread {

    private static volatile ServerReadThread instance = null;

    Client client;

    ObjectInputStream in;//input stream

    //returns only one instance of ServerReadThread
    public static ServerReadThread getInstance(Client client, ObjectInputStream reader) {
        if (instance == null) {
            synchronized (ServerReadThread.class) {
                if (instance == null) {
                    instance = new ServerReadThread(client, reader);
                }
            }
        }
        return instance;
    }


    //initialize fields
    private ServerReadThread(Client client, ObjectInputStream in) {
        this.client = client;
        this.in = in;
    }

    public void run() {
        Object message;
        try {
            while ((message = in.readObject()) != null) {
                client.messageReceived(message);
            }
        } catch (ClassNotFoundException e) {
            client.serverDisconnect();
        } catch (IOException ex) {
            client.serverDisconnect();
        }
    }
}  
