package Source;

import Source.Game.EnumStringMessage;
import Source.Game.GameTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void sendMessageToServer(ClientMessageType clientMessageType, String message) throws IOException {
        buffer.clear();
        buffer.put((byte) clientMessageType.ordinal());
        if(message != null) {
            buffer.put((message).getBytes());
        }
        buffer.flip();
        while(buffer.hasRemaining()){
            socket.write(buffer);
        }
    }

    private static EnumStringMessage readMessageFromServer() throws IOException{
        ServerResponseType serverResponse = null;

        StringBuilder messageFromServer = new StringBuilder();
        char c;

        do {
            buffer.clear();
            socket.read(buffer);
            if(buffer.position() == 0){
                return null;
            }
            buffer.flip();
            if(serverResponse == null){
                serverResponse = ServerResponseType.values()[(int) buffer.get()];
            }
            while (buffer.limit() > buffer.position()) {
                c = (char) buffer.get();
                messageFromServer.append(c);
            }
        }while(buffer.limit() >= buffer.capacity());
        return new EnumStringMessage(serverResponse, messageFromServer.toString());
    }

    private static EnumStringMessage login(String playerMessage) throws IOException {
        sendMessageToServer(ClientMessageType.LOGIN, playerMessage);
        return readMessageFromServer();
    }

    private static EnumStringMessage register(String playerMessage) throws IOException{
        sendMessageToServer(ClientMessageType.REGISTER, playerMessage);
        return readMessageFromServer();
    }

    private static EnumStringMessage logout() throws IOException{
        sendMessageToServer(ClientMessageType.LOGOUT, null);
        return readMessageFromServer();
    }

    private static EnumStringMessage createGame(String gameName) throws IOException {
       sendMessageToServer(ClientMessageType.CREATE_GAME, gameName);
       return readMessageFromServer();
    }

    /**
     * Send coordinates of where to start deploying the ship from
     * @param coordinates of where the ship is starting to deploy from( it deploys from left to right
     *                    or from up to bottom). Must be in the [h|v][A-J][1-10] format, where
     *                    h = horizontal and v = vertical
     * @return returns the report of what happenned
     * @throws IOException connection is lost with the server
     */
    private static EnumStringMessage deploy(String coordinates) throws IOException{
        sendMessageToServer(ClientMessageType.DEPLOY, coordinates);
        return readMessageFromServer();
    }

    private static EnumStringMessage callCommand(ClientMessageType clientMessageType, String remainingMessage) throws IOException{
        switch(clientMessageType){
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
            case CREATE_GAME:
                return createGame(remainingMessage);
            case EXIT_GAME:
                return exitGame();
            case JOIN_GAME:
                return joinGame(remainingMessage);
            case EXIT_CLIENT:
                logout();
                socket.close();
                return null;
            case DEPLOY:
                return deploy(remainingMessage);
            default:
                System.out.println("No idea what to do with this");
                return null;
        }
    }

    public static boolean processPlayerCommand(String playerMessage) throws IOException{
        String playerMessageType = playerMessage.split(" ")[0];
        ClientMessageType clientMessageType = findMessageTypeOut(playerMessageType);
        String remainingMessage = null;
        if(playerMessageType.length() + 1 < playerMessage.length()){
            remainingMessage = playerMessage.substring(playerMessageType.length() + 1, playerMessage.length());
            remainingMessage = remainingMessage.trim().replaceAll(" +", " ");
        }

        EnumStringMessage result = callCommand(clientMessageType, remainingMessage);
        if(result != null){
            System.out.println(result.getMessage());
        }

        // return false if result is null or message returns a "bad" message code
        return !(result == null || result.getEnumValue().equals(ServerResponseType.INVALID));
    }

    private static ClientMessageType findMessageTypeOut(String string){
        switch(string){
            case "login":
                return ClientMessageType.LOGIN;
            case "register":
                return ClientMessageType.REGISTER;
            case "logout":
                return ClientMessageType.LOGOUT;
            case "create_game":
                return ClientMessageType.CREATE_GAME;
            case "exit_game":
                return ClientMessageType.EXIT_GAME;
            case "join_game":
                return ClientMessageType.JOIN_GAME;
            case "exit":
                return ClientMessageType.EXIT_CLIENT;
            case "deploy":
                return ClientMessageType.DEPLOY;
            default:
                return ClientMessageType.CUSTOM_MESSAGE;
        }
    }

    private static EnumStringMessage exitGame() throws IOException{
        sendMessageToServer(ClientMessageType.EXIT_GAME, null);
        return readMessageFromServer();
    }

    private static EnumStringMessage joinGame(String gameName) throws IOException{
        sendMessageToServer(ClientMessageType.JOIN_GAME, gameName);
        return readMessageFromServer();
    }

    public static void main(String args[]){
        try{
            // INITIALIZE CLIENT STUFF
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress("localhost", 6969));
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;
            final int refreshRate = 150;

            System.out.println("Welcome to BattleshipsOnline!");

            // START OF CLIENT- SERVER MESSAGE EXCHANGE
            while(true) {
                if(playerInput.ready()){
                    socket.configureBlocking(true);
                    playerMessage = playerInput.readLine();
                    // SENDS THE INPUT MESSAGE TO THE SERVER
                    processPlayerCommand(playerMessage);
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(refreshRate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                socket.configureBlocking(false);
                EnumStringMessage message = readMessageFromServer();
                if(message != null){
                    System.out.println("This is a server- initiated message:");
                    System.out.println(message.getMessage());
                }
            }

        }catch(UnknownHostException exc){
            System.out.println("Issues locating the server");
        }catch(IOException exc) {
            System.out.println("Cannot connect to server");
        }
    }

    // MEMBER VARIABLES
    private static final int BUFFER_SIZE = 1024;
    private static SocketChannel socket;
    private static ByteBuffer buffer;
    private static BufferedReader playerInput;

    // GAME VISUALIZATION
    char[][] yourGameTable = GameTable.initializeTabulaRasa();
    char[][] opponentGameTable = GameTable.initializeTabulaRasa();

    // MEMBER VARIABLES- RELATED STUFF( shouldn't be needed outside of Testing)
    public static void setSocket(SocketChannel socket) {
        Client.socket = socket;
    }

    public static void setBuffer(ByteBuffer buffer) {
        Client.buffer = buffer;
    }
}

// TODO: Remove ANY verifications by the client
