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

    private static String readMessageFromServer() throws IOException{
        StringBuilder messageFromServer = new StringBuilder();
        char c;
        do {
            buffer.clear();
            socket.read(buffer);
            buffer.flip();
            while (buffer.limit() > buffer.position()) {
                c = (char) buffer.get();
                System.out.print(c);
                messageFromServer.append(c);
            }
            System.out.println();
        }while(buffer.limit() >= buffer.capacity());
        return messageFromServer.toString();
    }

    private static void loginMessage() throws IOException {
        String username;
        String password;

        System.out.println("Hello to BattleshipsOnline!");
        System.out.println("username:");
        username = playerInput.readLine();
        System.out.println("password:");
        password = playerInput.readLine();

        System.out.println("OK, let's see what the server has to say about that :>");
        sendMessageToServer(MessageType.LOGIN, username + " " + password);
        if (!socket.isConnected()) {
            System.out.println("Server is a fag");
            socket.close();
        }
        readMessageFromServer();
    }

    public static void processPlayerCommand(String command) throws IOException{
        String commandType = command.split(" ")[0];
        MessageType messageType = findMessageTypeOut(commandType);

        callCommand(messageType);
    }

    private static MessageType findMessageTypeOut(String string){
        switch(string){
            case "login":
                return MessageType.LOGIN;
            default:
                return MessageType.CUSTOM_MESSAGE;
        }
    }

    private static void callCommand(MessageType commandType) throws IOException{
        switch(commandType){
            case LOGIN: loginMessage();
            break;
            default:
                System.out.println("No idea what to do with this");
        }
    }

    public static void main(String args[]){
        try{
            // INITIALIZE BUFFER AND CHANNEL AND INITIATE THE LOGIN SCREEN
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress("localhost", 6969));
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;

            //loginMessage(playerInput);

            // START OF CLIENT- SERVER MESSAGE EXCHANGE
            while(true) {
                System.out.println("Ready to send a command to the server...");
                playerMessage = playerInput.readLine();

                // SENDS THE INPUT MESSAGE TO THE SERVER
                processPlayerCommand(playerMessage);
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
    static BufferedReader playerInput;
}
