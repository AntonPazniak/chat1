package client;

import network.TCPConnection;
import network.TCPConnectionListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "150.254.106.219";
    private static final int PORT = 6666;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new ClientWindow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextField fieldNickname = new JTextField("Popka");
    private final JTextField fieldInput = new JTextField();
    private final JButton stickerButton = new JButton();

    private TCPConnection connection;

    private ClientWindow() throws IOException {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);


        log.setEnabled(false);
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);

        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);
        add(stickerButton, BorderLayout.WEST);

        stickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connection.sendSticker(fieldInput.getText());
            }
        });

        try {
            connection = new TCPConnection(this, IP_ADDR, PORT );
        } catch (IOException e) {
            printMsg("Connection exception: "+ e);
        }
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if(msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + msg);
    }



    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onReceiveSticker(TCPConnection tcpConnection, String sticker) {
        sow(sticker);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: "+ e);
    }

    private synchronized  void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg +"\n");
                log.setCaretPosition((log.getDocument().getLength()));
            }
        });
    }

    private synchronized void sow(String s){
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            try {
                displayImage(new File("test.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}


    public void displayImage(File file) throws IOException
    {
        BufferedImage img=ImageIO.read(file);
        ImageIcon icon=new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(530,500);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
    }


}
