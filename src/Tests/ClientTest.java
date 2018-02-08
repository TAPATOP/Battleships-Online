package Tests;

import Source.MessageType;
import org.junit.Test;
import Source.Client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;

public class ClientTest {
    @Test
    public void main(){
        Client cl = new Client();
        try {
            Client.sendMessageToServer(MessageType.LOGIN, "");
        }catch(IOException exc){
            System.out.println("The server must be working");
        }
    }

}