package ru.kpfu.itis.kharakhashyan.server;

import ru.kpfu.itis.kharakhashyan.server.player.Player;
import ru.kpfu.itis.kharakhashyan.server.player.PlayerStatus;

import java.io.*;
import java.net.*;
import java.util.Date;

//this class hold two players and manage game between them
public class GameSession {

    private Server server;

    private Player player1;

    private Player player2;

    private final int PLAYERONE = 0;

    private final int PLAYERTWO = 1;

    private Thread player1Reader;//the player1 in thread

    private Thread player2Reader;//the player2 in thread

    private boolean continueToPlay;//continue to play flag

    private boolean[] playerIds;//array of player Ids


    //create game session thread
    public GameSession(Server server) {
        this.server = server;
        playerIds = new boolean[]{false, false};
        continueToPlay = false;
    }

    //method that use when new player joined the game
    protected boolean playerJoined(Socket playerSocket) {

        if (player1 == null) {
            player1 = new Player(PLAYERONE, playerSocket, this);
            server.serverLog.addElement(new Date() + ": Player 1 joined session " + '\n');
            server.serverLog.addElement("Player 1's IP address: " + playerSocket.getInetAddress().getHostAddress() + '\n');
            playerIds[PLAYERONE] = true;

            player1Reader = new IncomingReadThread(this, player1.getFromPlayer(), player1.getId());
            player1Reader.start();

            //update player
            Message msg = new Message();
            msg.setMessageType(PlayerStatus.CONNECTED);
            gameAction(player1.getId(), msg);

            return true;

        } else if (player2 == null) {
            player2 = new Player(PLAYERTWO, playerSocket, this);
            server.serverLog.addElement(new Date() + ": Player 2 joined session " + '\n');
            server.serverLog.addElement("Player 2's IP address: " + playerSocket.getInetAddress().getHostAddress() + '\n');
            playerIds[PLAYERTWO] = true;

            player2Reader = new IncomingReadThread(this, player2.getFromPlayer(), player2.getId());
            player2Reader.start();

            //update player
            Message msg = new Message();
            msg.setMessageType(PlayerStatus.CONNECTED);
            gameAction(player2.getId(), msg);

            return true;

        }

        return false;

    }

    //to handle player connection errors
    protected void playerDisconnect(int playerId) {
        if (player1.getId() == playerId) player1 = null;
        else if (player2.getId() == playerId) player2 = null;

        playerIds[playerId] = false;

        server.serverLog.addElement(new Date() + " player " + (playerId+1) + ": disconnected");
    }

    //message object received from player
    protected synchronized void messageReceived(int fromPlayerId, Object msg) {
        Message cmsg = (Message) msg;
        gameAction(fromPlayerId, cmsg);
    }

    //take action on received Message from the players
    private void gameAction(int playerId, Message clmsg) {
        Player player;
        Player opponent;

        if (PLAYERONE == playerId) {
            player = player1;
            opponent = player2;
        } else {
            player = player2;
            opponent = player1;
        }

        switch (clmsg.getMessageType()) {
            case CONNECTED:
                if (opponent == null) {
                    //send opponent is not connected to player
                    Message sm = new Message();
                    sm.setMessageType(PlayerStatus.NOT_CONNECTED);
                    sendToPlayer(player.getId(), sm);
                } else if (opponent.getStatus() == PlayerStatus.CONNECTED) {
                    //send opponent is connected to player
                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.CONNECTED);
                    sendToPlayer(player.getId(), sm1);
                    //send player is connected to opponent
                    sendToPlayer(opponent.getId(), sm1);

                } else if (opponent.getStatus() == PlayerStatus.READY) {
                    //send opponent field to player
                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.READY);
                    sm1.setMinePosition(opponent.getPlayerBoard());
                    sm1.setMineLocation(opponent.getMines());
                    sendToPlayer(player.getId(), sm1);

                    //send player is connected to opponent
                    Message sm2 = new Message();
                    sm2.setMessageType(PlayerStatus.CONNECTED);
                    sendToPlayer(opponent.getId(), sm2);

                }
                break;
            case READY:

                player.setStatus(PlayerStatus.READY);
                player.setPlayerBoard(clmsg.getMinePosition());
                player.setMines(clmsg.getMineLocation());

                if (opponent == null) {
                    //send not connected
                    Message sm = new Message();
                    sm.setMessageType(PlayerStatus.NOT_CONNECTED);
                    sendToPlayer(player.getId(), sm);
                } else if (opponent.getStatus() == PlayerStatus.CONNECTED) {
                    //send connected
                    //save mines
                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.CONNECTED);
                    sendToPlayer(player.getId(), sm1);

                    //send player is connected to opponent
                    Message sm2 = new Message();
                    sm2.setMessageType(PlayerStatus.READY);
                    sm2.setMinePosition(player.getPlayerBoard());
                    sm2.setMineLocation(player.getMines());
                    sendToPlayer(opponent.getId(), sm2);


                } else if (opponent.getStatus() == PlayerStatus.READY) {
                    //start game
                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.WAIT); //First wait, start game
                    sendToPlayer(player.getId(), sm1);

                    //send player field to opponent
                    Message sm2 = new Message();
                    sm2.setMessageType(PlayerStatus.TURN); //First turn, start game
                    sm2.setMinePosition(player.getPlayerBoard());
                    sm2.setMineLocation(player.getMines());
                    sendToPlayer(opponent.getId(), sm2);

                    continueToPlay = true;
                    //Change first player to Turn - send turn to first
                    //Change second to wait - send wait to second
                }
                break;
            case TURN:
                //Player had done his turn and BOOM

                player.setHitAtCell(clmsg.getRow(), clmsg.getColumn());

                if (clmsg.isHit()) {

                    if (clmsg.isMineBoombed()) {
                        player.setMinesBoombed();

                        if (player.getTurn() == 3) {
                            //Game over
                            //send winner msg to both players
                            player.setStatus(PlayerStatus.GAMEOVER);
                            opponent.setStatus(PlayerStatus.GAMEOVER);

                            Message sm1 = new Message();
                            sm1.setMessageType(PlayerStatus.GAMEOVER);
                            sm1.setHit(true); //Player wins
                            sendToPlayer(player.getId(), sm1);

                            //send player field to opponent
                            Message sm2 = new Message();
                            sm2.setMessageType(PlayerStatus.GAMEOVER);
                            sm2.setHit(false); //Opponent loses
                            sendToPlayer(opponent.getId(), sm2);

                            resetPlayers();

                            return;
                        }
                    }
                    //send turn
                    player.setStatus(PlayerStatus.TURN);
                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.TURN);
                    sm1.setHit(false); //without coordinates
                    sendToPlayer(player.getId(), sm1);

                    //send player field to opponent
                    opponent.setStatus(PlayerStatus.WAIT);
                    Message sm2 = new Message();
                    sm2.setMessageType(PlayerStatus.WAIT);
                    sm2.setRow(clmsg.getRow());
                    sm2.setColumn(clmsg.getColumn());
                    sm2.setHit(true); //with coordinates
                    sendToPlayer(opponent.getId(), sm2);

                } else {
                    player.setStatus(PlayerStatus.WAIT);
                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.WAIT);
                    sm1.setHit(false); //without coordinates
                    sendToPlayer(player.getId(), sm1);

                    //send player field to opponent
                    opponent.setStatus(PlayerStatus.TURN);
                    Message sm2 = new Message();
                    sm2.setMessageType(PlayerStatus.TURN);
                    sm2.setRow(clmsg.getRow());
                    sm2.setColumn(clmsg.getColumn());
                    sm2.setHit(true); //with coordinates
                    sendToPlayer(opponent.getId(), sm2);
                }

                break;

            case WAIT:
                //Player had done his turn
                if (opponent.getStatus() == PlayerStatus.TURN) {

                } else if (opponent.getStatus() == PlayerStatus.GAMEOVER) {
                    //Do nothing
                }

                break;

            case GAMEOVER:
                if (opponent.getStatus() == PlayerStatus.GAMEOVER) {

                    Message sm1 = new Message();
                    sm1.setMessageType(PlayerStatus.GAMEOVER);
                    sm1.setHit(true); //Player wins
                    sendToPlayer(player.getId(), sm1);

                    //send player field to opponent
                    Message sm2 = new Message();
                    sm2.setMessageType(PlayerStatus.GAMEOVER);
                    sm2.setHit(false); //Opponent loses
                    sendToPlayer(opponent.getId(), sm2);

                    continueToPlay = false;
                }
        }
    }

    //reset players of the session
    private void resetPlayers() {
        player1.reset();
        player2.reset();
    }

    //send message to player
    private void sendToPlayer(int playerId, Message message) {

        try {

            if (PLAYERONE == playerId) {
                player1.getToPlayer().writeObject(message);
                player1.getToPlayer().flush();
            } else if (PLAYERTWO == playerId) {
                player2.getToPlayer().writeObject(message);
                player2.getToPlayer().flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

