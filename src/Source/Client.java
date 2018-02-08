package Source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void sendMessageToServer(MessageType messageType, String message) throws IOException {
        buffer.clear();
        buffer.put((byte) messageType.ordinal());
        buffer.put((message).getBytes());
        buffer.flip();
        while(buffer.hasRemaining()){
            socket.write(buffer);
        }
    }

    private static void readMessageFromServer() throws IOException{
        do {
            buffer.clear();
            socket.read(buffer);
            buffer.flip();
            while (buffer.limit() > buffer.position()) {
                System.out.print((char) buffer.get());
            }
            System.out.println();
        }while(buffer.limit() >= buffer.capacity());
    }

    private static void loginMessage(BufferedReader playerInput) throws IOException {
        String username;
        String password;

        System.out.println("Hello to BattleshipsOnline!");
        System.out.println("username:");
        username = playerInput.readLine();
        System.out.println("password:");
        password  = playerInput.readLine();

        System.out.println("OK, let's see what the server has to say about that :>");
        sendMessageToServer(MessageType.LOGIN, username + " " + password);
        if(!socket.isConnected()){
            System.out.println("Source.Server is a fag");
            socket.close();
        }
        readMessageFromServer();
    }

    public void acceptPlayerMessage(String message){

    }

    public static void main(String args[]){
        try{
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress("localhost", 6969));

            // CREATE BUFFER AND CHANNEL AND INITIATE THE LOGIN SCREEN
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;
            loginMessage(playerInput);

            // START OF CLIENT- SERVER MESSAGE EXCHANGE
            while(true) {
                System.out.println("Ready to send a command to the server...");
                playerMessage = playerInput.readLine();

                // STOPS THE EXCHANGE IF MESSAGE IS "stop"
                if (playerMessage.equals("stop")) {
                    sendMessageToServer(MessageType.LOGOUT,"Bye!");
                    socket.close();
                    break;
                }

                // SENDS THE INPUT MESSAGE TO THE SERVER
                sendMessageToServer(MessageType.CUSTOM_MESSAGE, playerMessage);

                // READS BACK THE SERVER RESPONSE WITHOUT OVERFLOW
                // readMessageFromServer(output, sock);
            }

        }catch(UnknownHostException exc){
            System.out.println("Issues locating the server");
        }catch(IOException exc) {
            System.out.println("Cannot connect to server");
        }
    }
    // MEMBER VARIABLES
    static final int BUFFER_SIZE = 1024;
    static SocketChannel socket;
    static ByteBuffer buffer;
}
