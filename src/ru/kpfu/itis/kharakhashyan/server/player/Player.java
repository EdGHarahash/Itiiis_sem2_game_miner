package ru.kpfu.itis.kharakhashyan.server.player;

import ru.kpfu.itis.kharakhashyan.server.GameSession;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//this class contains player's connection, streams, status and some useful inforamtion
public class Player {

    private int id;//player id

    private GameSession session;//player's game session

    private Socket playerSocket;//player's socket

    private ObjectInputStream fromPlayer;

    private ObjectOutputStream toPlayer;

    private boolean isPlaying;

    private int[][][] playerBoard;//the player's field

    private int[][] mines;

    private int turn;

    private PlayerStatus status;//enum value of player status


    //initialize a new player object
    public Player(int id, Socket playerSocket, GameSession session) {
        this.id = id;
        this.playerSocket = playerSocket;
        this.session = session;

        try {
            toPlayer = new ObjectOutputStream(playerSocket.getOutputStream());
            toPlayer.flush();
            fromPlayer = new ObjectInputStream(playerSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        status = PlayerStatus.CONNECTED;
        turn = 0;
    }

    //reset status and number of mines for new game
    public void reset() {
        status = PlayerStatus.CONNECTED;
        turn = 0;
    }

    public int getId() {
        return id;
    }

    public int[][][] getPlayerBoard() {
        return playerBoard;
    }

    public void setPlayerBoard(int[][][] playerBoard) {
        this.playerBoard = playerBoard;
    }

    public int[][] getMines() {
        return mines;
    }

    public void setMines(int[][] mines) {
        this.mines = mines;
    }

    public int getTurn() {
        return turn;
    }

    public void setMinesBoombed() {
        this.turn++;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public ObjectInputStream getFromPlayer() {
        return fromPlayer;
    }

    public ObjectOutputStream getToPlayer() {
        return toPlayer;
    }

    //Sets hit at cell
    public void setHitAtCell(int row, int column) {
        playerBoard[row][column][1] = 1; //isHit
    }

}
