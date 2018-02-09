package Source;

import Source.Game.GameTable;

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
        if(message != null) {
            buffer.put((message).getBytes());
        }
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

    private static String login(String playerMessage) throws IOException {
        sendMessageToServer(MessageType.LOGIN, playerMessage);
        return readMessageFromServer();
    }

    private static String register(String playerMessage) throws IOException{
        sendMessageToServer(MessageType.REGISTER, playerMessage);
        return readMessageFromServer();
    }

    private static String logout() throws IOException{
        sendMessageToServer(MessageType.LOGOUT, null);
        return readMessageFromServer();
    }
//    private static String[] splitUsernameAndPassword(String input) {
//        String[] usernameAndPassword = input.split(" ");
//        if (usernameAndPassword.length != 2) {
//            System.out.println("Username and password not validated...");
//            return null;
//        }
//        return usernameAndPassword;
//    }

    public static boolean processPlayerCommand(String playerMessage) throws IOException{
        String playerMessageType = playerMessage.split(" ")[0];
        MessageType messageType = findMessageTypeOut(playerMessageType);
        String remainingMessage = null;
        if(playerMessageType.length() + 1 < playerMessage.length()){
            remainingMessage = playerMessage.substring(playerMessageType.length() + 1, playerMessage.length());
        }

        String result = callCommand(messageType, remainingMessage);

        // return false if result is null or message starts with '0'
        return !(result == null || result.charAt(0) == '0');
    }

    private static MessageType findMessageTypeOut(String string){
        switch(string){
            case "login":
                return MessageType.LOGIN;
            case "register":
                return MessageType.REGISTER;
            case "logout":
                return MessageType.LOGOUT;
            default:
                return MessageType.CUSTOM_MESSAGE;
        }
    }

    private static String callCommand(MessageType messageType, String remainingMessage) throws IOException{
        switch(messageType){
            case LOGIN:
                if(remainingMessage == null){
                    System.out.println("Username and password format is not okay");
                    return null;
                }
                return login(remainingMessage);
            case REGISTER:
                if(remainingMessage == null){
                    System.out.println("There is nothing to register...");
                    return null;
                }
                return register(remainingMessage);
            case LOGOUT:
                return logout();
            default:
                System.out.println("No idea what to do with this");
                return null;
        }
    }

    public static void main(String args[]){
        GameTable gt = new GameTable();
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);
        gt.deployNextShip("C3", false);
        gt.processFireCommand("A1");
        gt.processFireCommand("B1");
        gt.processFireCommand("D1");
        gt.stylizeAndPrintBoard();
        try{
            // INITIALIZE BUFFER AND CHANNEL AND INITIATE THE LOGIN SCREEN
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress("localhost", 6969));
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;

            System.out.println("Welcome to BattleshipsOnline!");

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
    private static SocketChannel socket;
    private static ByteBuffer buffer;
    private static BufferedReader playerInput;

    // MEMBER VARIABLES- RELATED STUFF( shouldn't be needed outside of Testing)

    public static void setSocket(SocketChannel socket) {
        Client.socket = socket;
    }

    public static void setBuffer(ByteBuffer buffer) {
        Client.buffer = buffer;
    }

    public static void setPlayerInput(BufferedReader playerInput) {
        Client.playerInput = playerInput;
    }
}
