package ru.kpfu.itis.kharakhashyan.server;

import java.io.IOException;

//thread to accept connection from client sockets, have only one instance (singleton)
public class ClientAcceptThread extends Thread {

    private static volatile ClientAcceptThread instance = null;

    Server server;

    private ClientAcceptThread(Server server) {
        this.server = server;
    }

    //returns only one instance of this class
    public static ClientAcceptThread getInstance(Server server) {
        if (instance == null) {
            synchronized (ClientAcceptThread.class) {
                if (instance == null) {
                    instance = new ClientAcceptThread(server);
                }
            }
        }
        return instance;
    }

    public void run() {
        server.serverLog.addElement("Wait for players to join game");
        while (true) {
            try {
                server.playerJoined(server.serverSocket.accept());
            } catch (IOException e) {
                server.serverLog.addElement("Server Socket IO error");
                break;
            }
        }
    }

}
