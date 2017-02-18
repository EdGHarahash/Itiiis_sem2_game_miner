package ru.kpfu.itis.kharakhashyan.server;

import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.util.Date;
import java.io.*;

//frame of server
public class Server extends JFrame {

    private JPanel contentPanel;

    DefaultListModel<String> serverLog;

    ClientAcceptThread clientaccept;//ClientAcceptThread thread

    protected ServerSocket serverSocket;

    private final int PORT = 8080;

    private GameSession gameSession;//session of game

    //create server window
    public Server() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPanel);

        serverLog = new DefaultListModel();
        JList logList = new JList(serverLog);
        JScrollPane scrollPane = new JScrollPane(logList);
        contentPanel.add(scrollPane, BorderLayout.NORTH);

        JButton startButton = new JButton("Start");//button for starting server
        startButton.addMouseListener(new MouseAdapter() { //button click function
            @Override
            public void mouseClicked(MouseEvent e) {
                startServer();
            }
        });
        contentPanel.add(startButton, BorderLayout.SOUTH);

        gameSession = new GameSession(this);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            serverLog.addElement(new Date() + ": Server started at port: " + PORT + "\n");

            clientaccept = ClientAcceptThread.getInstance(this);
            clientaccept.setName("clientAccept");
            clientaccept.start();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }


    protected synchronized void playerJoined(Socket playerSocket) {
        serverLog.addElement(new Date() + ": New Player joined ");

        if (!gameSession.playerJoined(playerSocket)) {
            try {
                ObjectOutputStream pw = new ObjectOutputStream(playerSocket.getOutputStream());
                pw.writeObject("Connection closed.");
                pw.flush();
                playerSocket.close();
                serverLog.addElement(new Date() + ": New Player Closed ");
            } catch (IOException e) {
                serverLog.addElement(new Date() + ": New Player Disconnected ");
            }
        }
    }

    public static void main(String[] args) {
        Server frame = new Server();
        frame.setVisible(true);
    }
}
