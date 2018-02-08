package Tests;

import Source.MessageType;
import org.junit.Before;
import org.junit.Test;
import Source.Client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ClientTest {
    private final int BUFFER_SIZE = 1024;
    @Test
    public void loginShouldWorkWithValidAndInvalidLogins(){
        SocketChannel socket = null;
        //BufferedReader playerInput = new BufferedReader(new InputStreamReader());
        //InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
        try {
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress("localhost", 6969));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Client.setSocket(socket);
        Client.setBuffer(ByteBuffer.allocate(BUFFER_SIZE));

        //assert(Client.processPlayerCommand();)
    }

}