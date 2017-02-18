package ru.kpfu.itis.kharakhashyan.client;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import ru.kpfu.itis.kharakhashyan.client.field.GameField;
import ru.kpfu.itis.kharakhashyan.server.Message;
import ru.kpfu.itis.kharakhashyan.server.player.PlayerStatus;

//this class create client window with two grids with seven mines
public class Client extends JFrame {

    private Socket clientSocket;

    private ServerReadThread reader;//the in thread

    private ObjectOutputStream out;

    private PlayerStatus status;//the player status

    private GameField player;//the player field

    private GameField opponent;//the opponent field

    private boolean isConnected;//connected flag

    private boolean playerWin;//win flag

    private boolean gameOver;//game over flag

    //labels
    private JLabel lblPlayer;

    private JLabel lblOpponent;

    private JLabel lblPlayerMineLeft;

    private JLabel lblOpponentMineLeft;

    private JLabel lblPlayerStatus;

    private JLabel lblOpponentStatus;

    private JLabel lblPlayerMessage;

    private JLabel lblOpponentMessage;

    private JButton btnConnect;//connect button

    private JButton btnPlay;//play button

    private JButton btnNewGame;//new game button

    //create a client window
    public Client(String title) {
        super(title);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        isConnected = false;
        gameOver = false;
        playerWin = false;

        player = new GameField(this);
        player.setBackground(Color.CYAN);
        player.setBounds(10, 150, 300, 300);
        getContentPane().add(player);

		/* Opponent's mines are invisible
		   only show BOOMbed mines */

        opponent = new GameField(this);
        opponent.setBackground(Color.cyan);
        opponent.setBounds(320, 150, 300, 300);
        getContentPane().add(opponent);
        opponent.disableMouseEvents(2);
        opponent.hideAllMines();

        lblPlayer = new JLabel("Player");
        lblPlayer.setBounds(10, 11, 300, 14);
        getContentPane().add(lblPlayer);

        lblOpponent = new JLabel("Opponent");
        lblOpponent.setBounds(320, 11, 300, 14);
        getContentPane().add(lblOpponent);

        lblPlayerMineLeft = new JLabel("Mine Destroyed:");
        lblPlayerMineLeft.setBounds(10, 36, 300, 14);
        getContentPane().add(lblPlayerMineLeft);

        lblOpponentMineLeft = new JLabel("Mine Desroyed:");
        lblOpponentMineLeft.setBounds(320, 36, 300, 14);
        getContentPane().add(lblOpponentMineLeft);

        lblPlayerStatus = new JLabel("Status");
        lblPlayerStatus.setBounds(10, 61, 300, 14);
        getContentPane().add(lblPlayerStatus);

        lblOpponentMessage = new JLabel("Message");
        lblOpponentMessage.setBounds(320, 86, 300, 14);
        getContentPane().add(lblOpponentMessage);

        lblPlayerMessage = new JLabel("Message");
        lblPlayerMessage.setBounds(10, 86, 300, 14);
        getContentPane().add(lblPlayerMessage);

        lblOpponentStatus = new JLabel("Status");
        lblOpponentStatus.setBounds(320, 61, 300, 14);
        getContentPane().add(lblOpponentStatus);

        btnConnect = new JButton("Connect");
        btnConnect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                connectToServer();
            }
        });
        btnConnect.setBounds(10, 111, 89, 23);
        getContentPane().add(btnConnect);

        btnPlay = new JButton("Play");
        btnPlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startGame();
            }
        });
        btnPlay.setEnabled(false);
        btnPlay.setBounds(109, 111, 89, 23);
        getContentPane().add(btnPlay);

        btnNewGame = new JButton("New Game");
        btnNewGame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                newGame();
            }
        });
        btnNewGame.setEnabled(false);
        btnNewGame.setBounds(208, 111, 89, 23);
        getContentPane().add(btnNewGame);

        player.disableMouseEvents(1);
        opponent.disableMouseEvents(2);

        status = PlayerStatus.NOT_CONNECTED;
        lblPlayerStatus.setText("Not Connected");
        lblPlayerMineLeft.setText("BOOM: " + player.getMineBoombed());
        lblOpponentMineLeft.setText("BOOM: " + opponent.getMineBoombed());

    }

    //sets the opponent mine position
    public void setOpponentMinePosition(int[][][] flags) {
        opponent.setOpponentMinePosition(flags);
    }

    //gets the player's mine position
    public int[][][] getPlayerMinePosition() {
        return player.getPlayerMinePosition();
    }

    //sets the opponent mine location
    public void setOpponentMineLocation(int[][] mineLocation) {
        opponent.setOpponentMineLocation(mineLocation);
    }

    //gets the player's mine location
    public int[][] getPlayerMineLocation() {
        return player.getPlayerMineLocation();
    }


    //connect to server socket
    public void connectToServer() {
        //Connect Button Clicked

        //start a socket thread, in and out thread
        //Notify user connection status
        lblPlayerMessage.setText("trying to connect\n");
        if (setUpNetworking()) {
            lblPlayerMessage.setText("Set mines and start game\n");
            player.enableMouseEvents(1);
            btnPlay.setEnabled(true);
            btnConnect.setEnabled(false);
            status = PlayerStatus.CONNECTED;
            lblPlayerStatus.setText("Connected");
        }
    }

    //create socket and connect to server
    private boolean setUpNetworking() {
        try {
            clientSocket = new Socket(InetAddress.getLocalHost(), 8080);

            if (clientSocket.isConnected()) {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();

                reader = ServerReadThread.getInstance(this, new ObjectInputStream(clientSocket.getInputStream()));
                reader.start();

                return true;
            }
        } catch (IOException ex) {
            lblPlayerMessage.setText("Server is not running\n");
        }
        lblPlayerMessage.setText("Networking could not established\n");
        return false;
    }

    //send player's mines position and layout and start new game
    public void startGame() {
        //Start Button Clicked

        player.disableMouseEvents(1);

        //Send mines flags to server
        //wait for mines flags to arrive
        Message msg = new Message();
        msg.setMessageType(PlayerStatus.READY);
        msg.setMinePosition(getPlayerMinePosition());
        msg.setMineLocation(getPlayerMineLocation());
        sendMessage(msg);

        status = PlayerStatus.READY;
        lblPlayerStatus.setText("Ready");
        lblPlayerMessage.setText("Waiting for opponent");
        btnPlay.setEnabled(false);
    }

    //reset games and games boards
    public void newGame() {

        player.resetGameBoard();
        opponent.resetGameBoard();
        opponent.hideAllMines();

        player.enableMouseEvents(1);
        opponent.disableMouseEvents(2);

        btnPlay.setEnabled(true);
        btnNewGame.setEnabled(false);

        lblPlayerMessage.setText("Set mines and start game\n");
        lblPlayerStatus.setText("Connected");
        lblPlayerMineLeft.setText("hp left: " + (3 - player.getMineBoombed()));
        lblOpponentMineLeft.setText("hp left: " + (3 - opponent.getMineBoombed()));

        status = PlayerStatus.CONNECTED;
        Message msg = new Message();
        msg.setMessageType(PlayerStatus.CONNECTED);
        sendMessage(msg);

        repaint();
    }

    //message object received from server
    public void messageReceived(Object message) {
        Message serverMsg = (Message) message;
        updateClient(serverMsg);
    }

    //update client with received message
    private void updateClient(Message msg) {

        switch (msg.getMessageType()) {
            case NOT_CONNECTED:
                lblOpponentStatus.setText("Not Connected");
                break;
            case CONNECTED:
                lblOpponentStatus.setText("Connected");
                break;
            case READY:
                //Set oponent mines
                lblOpponentStatus.setText("Ready");
                setOpponentMinePosition(msg.getMinePosition());
                setOpponentMineLocation(msg.getMineLocation());
                break;
            case TURN:
                //if first turn game start
                if (status == PlayerStatus.READY) {
                    setOpponentMinePosition(msg.getMinePosition());
                    setOpponentMineLocation(msg.getMineLocation());

                    lblPlayerStatus.setText("Game Started");
                    lblOpponentStatus.setText("Game Started");
                }

                status = PlayerStatus.TURN;
                opponent.enableMouseEvents(2);
                if (msg.isHit()) {
                    //with coordinate
                    player.setHitAtCell(msg.getRow(), msg.getColumn());
                }

                lblPlayerMineLeft.setText("try left: " + (2 - opponent.getMineBoombed()));
                lblOpponentMineLeft.setText("try left: " + (2 - player.getMineBoombed()));
                lblPlayerMessage.setText("Your turn");
                lblOpponentMessage.setText("Waiting for your turn");
                break;
            case WAIT:
                if (status == PlayerStatus.READY) {
                    lblPlayerStatus.setText("Game Started");
                    lblOpponentStatus.setText("Game Started");
                }

                status = PlayerStatus.WAIT;
                opponent.disableMouseEvents(2);
                if (msg.isHit()) {
                    //with coordinate
                    player.setHitAtCell(msg.getRow(), msg.getColumn());
                }

                lblPlayerMineLeft.setText("try left: " + (2 - opponent.getMineBoombed()));
                lblOpponentMineLeft.setText("try left: " + (2 - player.getMineBoombed()));

                lblPlayerMessage.setText("Waiting for opponent's turn");
                lblOpponentMessage.setText("Opponent's turn");

                break;
            case GAMEOVER:
                status = PlayerStatus.GAMEOVER;
                opponent.disableMouseEvents(2);
                btnNewGame.setEnabled(true);

                lblPlayerStatus.setText("Game Over");
                lblOpponentStatus.setText("Game Over");

                if (msg.isHit()) {
                    lblOpponentMessage.setText("Opponent won!");
                    lblPlayerMessage.setText("You lost");
                } else {
                    lblPlayerMessage.setText("You won!");
                    lblOpponentMessage.setText("Opponent lost");
                }
        }
    }

    //opponent move action
    public void opponentMouseHit(int row, int column, boolean hit, boolean mineBoombed) {
        Message msg = new Message();
        msg.setMessageType(PlayerStatus.TURN);
        msg.setRow(row);
        msg.setColumn(column);
        msg.setHit(hit);
        msg.setMineBoombed(mineBoombed);

        opponent.disableMouseEvents(2);
        sendMessage(msg);
    }

    //send Message object to server.
    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            lblPlayerStatus.setText("Connection Error");
        }
    }

    //handles server connection error and resets client
    public void serverDisconnect() {
        lblPlayerStatus.setBackground(Color.red);
        lblPlayerStatus.setText("Disconnected");

        lblOpponentStatus.setBackground(Color.red);
        lblOpponentStatus.setText("Disconnectfed");

        newGame();
        clientSocket = null;
        btnPlay.setEnabled(false);
        btnConnect.setEnabled(true);
    }

    public static void main(String[] args) {
        Client frame = new Client("Miner");
        frame.setSize(640, 500);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
