package network;

import java.io.File;
import java.io.IOException;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);

    void onReceiveString(TCPConnection tcpConnection,String value);

    void onReceiveSticker(TCPConnection tcpConnection, String file);

    void onDisconnect(TCPConnection tcpConnection);

    void onException(TCPConnection tcpConnection, Exception e);

}
