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
    public void loginShouldWorkWithValidAndInvalidLogins() throws IOException {
        SocketChannel socket = null;
        //BufferedReader playerInput = new BufferedReader(new InputStreamReader());
        //InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
        socket = SocketChannel.open();
        socket.connect(new InetSocketAddress("localhost", 6969));
        Client.setSocket(socket);
        Client.setBuffer(ByteBuffer.allocate(BUFFER_SIZE));

        assertFalse(Client.processPlayerCommand("login TAPATOP pesswerdlmao"));
        assertFalse(Client.processPlayerCommand("login w peswerdlmao"));
        assertFalse(Client.processPlayerCommand("login rrr"));
        assertFalse(Client.processPlayerCommand("login w w ww  w w w w"));
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
    }

}