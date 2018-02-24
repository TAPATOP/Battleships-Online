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
    private void sendMessageToServer(ClientMessageType clientMessageType, String message) throws IOException {
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

    private EnumStringMessage readMessageFromServer() throws IOException{
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

        EnumStringMessage result = new EnumStringMessage(serverResponse, messageFromServer.toString());
        processServerResponse(result);

        return result;
    }

    private void processServerResponse(EnumStringMessage serverMessage) throws IOException{
        ServerResponseType responseType = (ServerResponseType)serverMessage.getEnumValue();
        switch(responseType){
            case GAME_OVER:
            case RECORD_SHOT:
                recordShotFromOpponent(serverMessage);
                break;
        }
    }

    private void recordShotFromOpponent(EnumStringMessage message) throws IOException{
        int[] coords = findCoordinatesOfOpponentShotOut(message.getMessage());
        if(coords[0] == -1){
            return;
        }

        int x = coords[0];
        int y = coords[1];
        char c = visualizeOpponentShot(x, y);

        yourGameTable[x][y] = c;
        GameTable.stylizeAndPrintMatrix(yourGameTable);

        if(message.getEnumValue().equals(ServerResponseType.GAME_OVER)) {
            System.out.println("You win!");
            //processPlayerCommand("exit_game");
            yourGameTable = GameTable.initializeTabulaRasa();
            opponentGameTable = GameTable.initializeTabulaRasa();
        }
    }

    private int[] findCoordinatesOfOpponentShotOut(String coordinates){
        String possibleCoords = coordinates.substring(coordinates.length() - 4, coordinates.length() - 1);
        int[] coords = GameTable.tranformCoordinatesForReading(possibleCoords);

        if(coords[0] == -1){
            possibleCoords = possibleCoords.substring(1, possibleCoords.length());
            coords = GameTable.tranformCoordinatesForReading(possibleCoords);
        }
        return coords;
    }

    private void recordShotAtOpponent(int x, int y, ServerResponseType resultOfShot){
        char c = shotAtOpponentVisualization(resultOfShot);
        opponentGameTable[x][y] = c;
        GameTable.stylizeAndPrintMatrix(opponentGameTable);
    }

    private char shotAtOpponentVisualization(ServerResponseType resultOfShot){
        switch(resultOfShot){
            case HIT:
                return 'X';
            case MISS:
                return 'O';
            default:
                return '?';
        }
    }

    private char visualizeOpponentShot(int x, int y){
        switch(yourGameTable[x][y]){
            case '#':
                return 'X';
            case '_':
                return 'O';
            default:
                return '?';
        }
    }

    private EnumStringMessage login(String playerMessage) throws IOException {
        sendMessageToServer(ClientMessageType.LOGIN, playerMessage);
        return readMessageFromServer();
    }

    private EnumStringMessage register(String playerMessage) throws IOException{
        sendMessageToServer(ClientMessageType.REGISTER, playerMessage);
        return readMessageFromServer();
    }

    private EnumStringMessage logout() throws IOException{
        sendMessageToServer(ClientMessageType.LOGOUT, null);
        return readMessageFromServer();
    }

    private EnumStringMessage createGame(String gameName) throws IOException {
       sendMessageToServer(ClientMessageType.CREATE_GAME, gameName);
       return readMessageFromServer();
    }

    private EnumStringMessage exitGame() throws IOException{
        sendMessageToServer(ClientMessageType.EXIT_GAME, null);
        return readMessageFromServer();
    }

    private EnumStringMessage joinGame(String gameName) throws IOException{
        sendMessageToServer(ClientMessageType.JOIN_GAME, gameName);
        return readMessageFromServer();
    }

    /**
     * Send coordinates of where to start deploying the ship from
     * @param coordinates of where the ship is starting to deploy from( it deploys from left to right
     *                    or from top to bottom). Must be in the [h|v][A-J][1-10] format, where
     *                    h = horizontal and v = vertical
     * @return returns the report of what happenned
     * @throws IOException connection is lost with the server
     */
    private EnumStringMessage deploy(String coordinates) throws IOException{
        sendMessageToServer(ClientMessageType.DEPLOY, coordinates);

        // the enum of this should always be GameTable.ShipType, since that's the command
        EnumStringMessage result = readMessageFromServer();

        if(result == null){
            return null;
        }

        GameTable.ShipType shipType;
        try {
            ServerResponseType serverResponseAsRead = (ServerResponseType)result.getEnumValue();
            shipType = revertServerResponseTypeToShipType(serverResponseAsRead);
        }catch(ClassCastException exc){
            return result;
        }

        if(!shipType.equals(GameTable.ShipType.INVALID)) {
            tryDrawingDeployedShip(shipType, coordinates);
        }

        return new EnumStringMessage(ServerResponseType.OK, result.getMessage());
    }

    private void tryDrawingDeployedShip(GameTable.ShipType shipType, String coordinates){
        char c = coordinates.charAt(0);
        boolean isVertical;
        switch(c){
            case 'h':
                isVertical = false;
                break;
            case 'v':
                isVertical = true;
                break;
            default:
                return;
        }
        String restOfCoordinates = coordinates.substring(1, coordinates.length());

        int[] coords = GameTable.tranformCoordinatesForReading(restOfCoordinates);
        if(coords[0] == -1){
            System.out.println("Something's wrong with the coordinates");
            return;
        }
        drawDeployedShip(shipType, coords[0], coords[1], isVertical);
    }

    private void drawDeployedShip(GameTable.ShipType shipType, int x, int y, boolean isVertical){
        int shipSize = GameTable.getShipSizeByType(shipType);

        int xChange = 0;
        int yChange = 0;
        if(isVertical){
            xChange = 1;
        }else{
            yChange = 1;
        }

        for(int i = 0; i < shipSize; i++){
            yourGameTable[x][y] = '#';
            x += xChange;
            y += yChange;
        }
        GameTable.stylizeAndPrintMatrix(yourGameTable);
    }

    private GameTable.ShipType revertServerResponseTypeToShipType(ServerResponseType original){
        switch(original){
            case DEPLOYED_DESTROYER:
                return GameTable.ShipType.DESTROYER;
            case DEPLOYED_CRUISER:
                return GameTable.ShipType.CRUISER;
            case DEPLOYED_BATTLESHIP:
                return GameTable.ShipType.BATTLESHIP;
            case DEPLOYED_CARRIER:
                return GameTable.ShipType.AIRCRAFT_CARRIER;
            default:
                return GameTable.ShipType.INVALID;
        }
    }

    private EnumStringMessage fire(String coordinates) throws IOException{
        sendMessageToServer(ClientMessageType.FIRE, coordinates);
        EnumStringMessage result = readMessageFromServer();

        if(result == null){
            return null;
        }

        boolean shotIsNotInvalid = !result.getEnumValue().equals(ServerResponseType.INVALID);
        boolean shotIsProbablyIndeedAShot = !result.getEnumValue().equals(ServerResponseType.NOTHING_OF_IMPORTANCE);
        boolean shotKilledLastShip = result.getEnumValue().equals(ServerResponseType.DESTROYED_LAST_SHIP);

        if(shotIsNotInvalid && shotIsProbablyIndeedAShot){
            int[] coords = GameTable.tranformCoordinatesForReading(coordinates);
            recordShotAtOpponent(coords[0], coords[1], (ServerResponseType)result.getEnumValue());
            if(shotKilledLastShip){
                yourGameTable = GameTable.initializeTabulaRasa();
                opponentGameTable = GameTable.initializeTabulaRasa();
                // processPlayerCommand("exit_game");
            }
        }
        return result;
    }

    private EnumStringMessage searchGames() throws IOException{
        sendMessageToServer(ClientMessageType.SEARCH_GAMES, null);
        return readMessageFromServer();
    }

    private EnumStringMessage showPlayerStatistics() throws IOException{
        sendMessageToServer(ClientMessageType.SHOW_PLAYER_STATISTICS, null);
        return null;
    }

    private EnumStringMessage callCommand(ClientMessageType clientMessageType, String remainingMessage) throws IOException{
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
            case FIRE:
                return fire(remainingMessage);
            case SEARCH_GAMES:
                return searchGames();
            case SHOW_PLAYER_STATISTICS:
                return showPlayerStatistics();
            default:
                System.out.println("No idea what to do with this");
                return null;
        }
    }

    public boolean processPlayerCommand(String playerMessage) throws IOException{
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

    private ClientMessageType findMessageTypeOut(String string){
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
            case "fire":
                return ClientMessageType.FIRE;
            case "search_games":
                return ClientMessageType.SEARCH_GAMES;
            case "statistics":
                return ClientMessageType.SHOW_PLAYER_STATISTICS;
            default:
                return ClientMessageType.CUSTOM_MESSAGE;
        }
    }

    public void initialize(String host, int port) throws IOException{
        // INITIALIZE CLIENT STUFF
        socket = SocketChannel.open();
        socket.connect(new InetSocketAddress(host, port));
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void initialize() throws IOException{
        // INITIALIZE CLIENT STUFF
        socket = SocketChannel.open();
        socket.connect(new InetSocketAddress("localhost", 6969));
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void blockSocket() throws IOException{
        socket.configureBlocking(true);
    }

    public void unblockSocket() throws IOException{
        socket.configureBlocking(false);
    }

    public static void main(String args[]){
        try{
            Client client = new Client();
            client.initialize("localhost", 6969);

            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;
            final int refreshRate = 150;

            System.out.println("Welcome to BattleshipsOnline!");

            // START OF CLIENT- SERVER MESSAGE EXCHANGE
            while(true) {
                if(playerInput.ready()){
                    client.blockSocket();
                    playerMessage = playerInput.readLine();
                    // SENDS THE INPUT MESSAGE TO THE SERVER
                    client.processPlayerCommand(playerMessage);
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(refreshRate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                client.unblockSocket();
                EnumStringMessage message = client.readMessageFromServer();
                if(message != null){
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
    private final int BUFFER_SIZE = 1024;
    private SocketChannel socket;
    private ByteBuffer buffer;

    // GAME VISUALIZATION
    private char[][] yourGameTable = GameTable.initializeTabulaRasa();
    private char[][] opponentGameTable = GameTable.initializeTabulaRasa();

    // MEMBER VARIABLES- RELATED STUFF( shouldn't be needed outside of Testing)
    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}

// TODO: Remove ANY verifications by the client
