package network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener evenListener;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final DataOutputStream dataOutputStream;
    private final DataInputStream dataInputStream;
    private final ObjectOutputStream oOut;
    private ObjectInputStream obIn;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;



    String object;

    public TCPConnection(TCPConnectionListener evenListener, String ipAddr, int port)throws IOException{
        this(evenListener,new Socket(ipAddr,port));
    }

    public TCPConnection(TCPConnectionListener evenListener, Socket socket) throws IOException {
        this.evenListener = evenListener;
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.oOut = new ObjectOutputStream(socket.getOutputStream());
        this.obIn = new ObjectInputStream(socket.getInputStream());
        this.fileOutputStream = new FileOutputStream("test.png");

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    evenListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        while ((object = (String) obIn.readObject()) != null) {
                            if (object.equals("m")) {
                                String message = in.readLine();
                                evenListener.onReceiveString(TCPConnection.this, message);
                            }
                            else if (object.equals("f")) {
                                byte[] bytes = new byte[5*1024];
                                int count, total=0;
                                long lenght = dataInputStream.readLong();
                                while ((count = dataInputStream.read(bytes)) > -1) {
                                    total+=count;
                                    fileOutputStream.write(bytes, 0, count);
                                    if (total==lenght) break;
                                }
                                evenListener.onReceiveSticker(TCPConnection.this,"test.png");
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    evenListener.onException(TCPConnection.this, e);
                } finally {
                    evenListener.onDisconnect(TCPConnection.this);
                }
            }
            });
        rxThread.start();
    }

    //отправление сообщения
    public synchronized void sendString(String message){
        try {
            oOut.writeObject("m");
            out.write(message + "\r\n" );
            out.flush();
        } catch (IOException e) {
            evenListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }


    //отправление стикера
    public synchronized void sendSticker(String nameFile){
        try {
            File file = new File(nameFile);
            oOut.writeObject("f");
            fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[5*1024];
            int count;
            long lenght = file.length();
            dataOutputStream.writeLong(lenght);
            while ((count = fileInputStream.read(bytes)) > -1) {
                dataOutputStream.write(bytes, 0, count);
            }
        } catch (IOException e) {
            evenListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    //отключение
    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            evenListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString(){
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort() ;
    }

}
