package ru.kpfu.itis.kharakhashyan.server;

import java.io.*;

//class to receive client meassages asynchroniously
public class IncomingReadThread extends Thread {

	GameSession gameSession;

	ObjectInputStream in;

	int playerId;
	
	//initialize a new instance
	public IncomingReadThread(GameSession gameSession, ObjectInputStream in, int playerId){
		this.gameSession = gameSession;
		this.in = in;
		this.playerId = playerId;
	}

    public void run() {
        Object message;
        try {
            while ((message = in.readObject()) != null)
            {
            	gameSession.messageReceived(playerId, message);
            }
        } catch (ClassNotFoundException e) {
			gameSession.playerDisconnect(playerId);
		} catch (IOException ex) {
        	//when client is closed or connection error.
			gameSession.playerDisconnect(playerId);
        }
    }
}  
