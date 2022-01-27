package server;

import network.TCPConnection;
import network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import static java.awt.Color.*;


public class ChatServer extends JFrame  implements TCPConnectionListener{



    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    private final Container container;
    private final JTextArea log = new JTextArea(10,1000);
    private final JTextField fieldNickname = new JTextField("Popka");
    private final JTextField fieldInput = new JTextField();
    private final JButton startButton = new JButton("Start");
    private final JButton stopButton = new JButton("Stop");
    ServerSocket serverSocket;

    public ChatServer() {
        super("Server settings");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(WIDTH, HEIGHT);
        this.container = getContentPane();


        setLocationRelativeTo(null);
        setVisible(true);
        log.setEnabled(false);
        log.setLineWrap(true);

        add(log, BorderLayout.CENTER);
        add(stopButton, BorderLayout.PAGE_END);
        add(startButton, BorderLayout.PAGE_START);

        setVisible(true);
                stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    print();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        setVisible(true);

        startServer();
    }

    public void print() throws IOException {
        final int cnt  = connections.size();
        for (int i =0; i <cnt; i++) {
            connections.get(i).disconnect();
        }
        serverSocket.close();
    }


    private void startServer(){
        printStatus("Start server...");
        try {
            this.serverSocket = new ServerSocket(6666);
            while(true){
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
                    printStatus("TCPConnection exception: "+ e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        //sendToAllConnections("Client connected " + tcpConnection );
        printStatus("Client connected " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public void onReceiveSticker(TCPConnection tcpConnection, String sticker) {
        sendToAllConnectionsFile(sticker);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        //sendToAllConnections("Client connected " + tcpConnection );
        printStatus("Client connected " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        //System.out.println("TCPConnection exception: " + e);
        printStatus("TCPConnection exception: " + e);
    }

    private void sendToAllConnections(String value){
        System.out.println(value);
        final int cnt  = connections.size();
        for (int i =0; i <cnt; i++) {
            connections.get(i).sendString(value);
        }
    }

    private void sendToAllConnectionsFile(String sticker){
        final int cnt  = connections.size();
        for (int i =0; i <cnt; i++) {
            connections.get(i).sendSticker(sticker);
        }
    }

    private synchronized  void printStatus(String msg) {
        log.append(msg + "\n");
        log.setCaretPosition((log.getDocument().getLength()));
    }


}
