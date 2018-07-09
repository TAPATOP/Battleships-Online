package Source;

import Source.Game.Game;
import Source.Game.GameTable;
import Source.Game.Player;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server {
    // MEMBER VARIABLES
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private Set<String> loggedInUsers = new HashSet<>();
    private Map<String, Game> pendingGames = new HashMap<>();
    private Map<String, Game> runningGames = new HashMap<>();
    private Map<Integer, String> gameStorageByID = new HashMap<>();
    private List<String> allPendingGames = new ArrayList<>();
    private int allGamesEverCount = 1;

    // Constructors //
    @SuppressWarnings("WeakerAccess")
    public Server() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(6969));
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch(IOException a) {
            System.out.println("IOException");
        }
    }

    // Public methods
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String args[]) {
        System.out.println("Server is working");
        Server server = new Server();
        server.run();
    }

    @SuppressWarnings("WeakerAccess")
    public void run() {
        try {
            SelectionKey key;
            SocketChannel chan;
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    if (key.isAcceptable()) {
                        // ACCEPT THE PENDING CONNECTIONS AND DESIGNATE THEM FOR READING
                        acceptConnections(selector, key);
                    } else if (key.isReadable()) {
                        // DO THE ACTUAL WORK
                        chan = (SocketChannel) key.channel();
                        try {
                            // READ THE CLIENT INPUT
                            //noinspection StatementWithEmptyBody
                            while (readFromClient(chan, key));
                        } catch(IOException | CancelledKeyException exc) {
                            System.out.println("Connection to client lost!");
                            logoutAccount(key, chan);
                            chan.close();
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch(IOException a) {
            System.out.println("IOException");
        }
    }

    // Private methods
    private void acceptConnections(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, new Account(sc));
        System.out.println("We have a new connection!");
    }

    private boolean readFromClient(SocketChannel chan, SelectionKey key) throws IOException {
        ByteBuffer buffer = getChannelBuffer(key);

        buffer.clear();
        try {
            if (chan.read(buffer) <= 0) {
                return false;
            }
        } catch(BufferOverflowException exc) {
            System.out.println("Client message too long!");
        }
        buffer.flip();

        ClientMessageType mesType = readClientMessageType(buffer);
        System.out.println("Processing a " + mesType + " message");
        String playerMessage = readClientMessage(buffer);
        System.out.println("Processing this message: " + playerMessage);

        // No reason not to notify the client about what's going on as soon as possible
        EnumStringMessage result = processMessage(mesType, playerMessage, chan, key);
        if (result == null) {
            return true;
        }
        writeToClient(result, chan, getChannelAccount(key));
        return true;
    }

    private ByteBuffer getChannelBuffer(SelectionKey key) {
        return getChannelAccount(key).getBufferForCommunicationWithServer();
    }

    private Account getChannelAccount(SelectionKey key) {
        return ((Account)key.attachment());
    }

    private ClientMessageType readClientMessageType(ByteBuffer buffer) {
        return ClientMessageType.values()[(int) buffer.get()];
    }

    private String readClientMessage(ByteBuffer buffer) {
        char[] playerInput = new char[buffer.limit()];
        int i = 0;
        while (buffer.limit() > buffer.position()) {
            playerInput[i++] = (char) buffer.get();
        }
        return new String(playerInput);
    }

    private EnumStringMessage processMessage(ClientMessageType mesType, String message, SocketChannel chan, SelectionKey key) throws IOException {
        switch (mesType) {
            case LOGIN:
                return loginAccount(message, key);
            case REGISTER:
                return registerAccount(message, key);
            case LOGOUT:
                return logoutAccount(key, chan);
            case CREATE_GAME:
                return createGame(message, key);
            case EXIT_GAME:
                return exitGame(key);
            case JOIN_GAME:
                return joinGame(message, key);
            case DEPLOY:
                return deployShip(message, key);
            case FIRE:
                return fire(message, key);
            case SEARCH_GAMES:
                return searchGames(key, chan);
            case SHOW_PLAYER_STATISTICS:
                return showPlayerStatistics(key, chan);
            default:
                System.out.println("I don't know how to handle this :c");
                return new EnumStringMessage(
                        ServerResponseType.NOTHING_OF_IMPORTANCE,
                        "I have no idea what to do with this so I will just repeat it: " + message
                );
        }
    }

    private EnumStringMessage searchGames(SelectionKey  key, SocketChannel chan) throws IOException {
        EnumStringMessage queryReturn = new EnumStringMessage(
                ServerResponseType.NOTHING_OF_IMPORTANCE,
                "Here you go your list of all games"
        );
        writeToClient(queryReturn, chan, getChannelAccount(key));

        bufferVolatileGameListSend(chan, key);

        return null;
    }

    private void bufferSafeGameListSend(
            SocketChannel chan,
            SelectionKey key
    ) throws IOException {
        EnumStringMessage queryReturn;
        for (String gameName :
                allPendingGames) {
            queryReturn = new EnumStringMessage(
                    ServerResponseType.NOTHING_OF_IMPORTANCE,
                    gameName + " PENDING\n"
            );
            writeToClient(queryReturn, chan, getChannelAccount(key));
        }

        for (Map.Entry<String, Game> entry:
                runningGames.entrySet()) {
            Game game = entry.getValue();
            queryReturn = new EnumStringMessage(
                    ServerResponseType.NOTHING_OF_IMPORTANCE,
                    game.getGameName() + " RUNNING\n"
            );
            writeToClient(queryReturn, chan, getChannelAccount(key));
        }
    }

    // By "volatile" I mean that the buffer might explode, e.g. overflow
    private void bufferVolatileGameListSend(
            SocketChannel chan,
            SelectionKey key
    ) throws IOException {
        EnumStringMessage queryReturn;
        StringBuilder messageBuilder = new StringBuilder();
        for (String gameName :
                allPendingGames) {
            messageBuilder.append(gameName).append(" PENDING\n");
        }

        for (Map.Entry<String, Game> entry :
                runningGames.entrySet()) {
            Game game = entry.getValue();
            messageBuilder.append(game.getGameName()).append(" RUNNING\n");
        }

        String message = removeLastCharacter(messageBuilder.toString());
        queryReturn = new EnumStringMessage(
                ServerResponseType.NOTHING_OF_IMPORTANCE,
                message
        );

        writeToClient(queryReturn, chan, getChannelAccount(key));
    }

    private EnumStringMessage showPlayerStatistics(SelectionKey key, SocketChannel chan) throws IOException {
        EnumStringMessage messageToClient;

        if (!channelIsLoggedIn(key)) {
            messageToClient = new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You're not logged in"
            );
            return messageToClient;
        }

        int[] statistics = getChannelAccount(key).loadStatistics();
        StringBuilder messageFromServer = new StringBuilder();

        for (int num :
                statistics) {
            messageFromServer.append(Integer.toString(num)).append('\n');
        }

        String stringToSend = removeLastCharacter(messageFromServer.toString());

        messageToClient = new EnumStringMessage(
                ServerResponseType.NOTHING_OF_IMPORTANCE,
                stringToSend
        );
        writeToClient(messageToClient, chan, getChannelAccount(key));

        return null;
    }

    private EnumStringMessage fire(String coordinates, SelectionKey key) throws IOException {
        if (!channelIsInGame(key)) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Please don't fire in the lobby... Join a game first"
            );
        }

        Game channelGame = getGameByAccount(getChannelAccount(key));

        // this shouldn't really execute but warnings are annoying
        if (channelGame == null) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You're not in a game, please join one first"
            );
        }

        if (channelGame.isInDeploymentPhase()) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Game is still in deployment mode"
            );
        }

        String coordsForWork = removeLastCharacter(coordinates);
        return executeFiring(coordsForWork, key, channelGame);
    }

    private EnumStringMessage executeFiring(
            String coordinates,
            SelectionKey key,
            Game channelGame
    ) throws IOException {
        Player channelPlayer = channelGame.getPlayerByAccount(getChannelAccount(key));
        EnumStringMessage result = channelGame.executeFiring(channelPlayer, coordinates);
        ServerResponseType SRT =
                convertGameTableFireResultToServerResponseType(
                        (GameTable.FireResult)result.getEnumValue()
                );

        if (!SRT.equals(ServerResponseType.INVALID)) {
            EnumStringMessage messageToOponent = new EnumStringMessage(
                    ServerResponseType.RECORD_SHOT,
                    channelPlayer.getName() + " fired at: " + coordinates + '\n'
            );
            writeToOpponent(
                    channelGame.getOtherPlayer(channelPlayer),
                    messageToOponent
            );
            if (SRT.equals(ServerResponseType.DESTROYED_LAST_SHIP)) {
                messageToOponent = new EnumStringMessage(
                        ServerResponseType.GAME_OVER,
                        channelPlayer.getName() + " wins!"
                );
                writeToOpponent(
                        channelGame.getOtherPlayer(channelPlayer),
                        messageToOponent
                );
            }
        }

        result = new EnumStringMessage(SRT, result.getMessage());

        return result;
    }

    private boolean channelIsInGame(SelectionKey key) {
        return getChannelAccount(key) != null && getChannelAccount(key).getCurrentGameID() != 0;
    }

    private ServerResponseType convertGameTableFireResultToServerResponseType(GameTable.FireResult fireResult) {
        switch(fireResult) {
            case MISS:
                return ServerResponseType.MISS;
            case HIT:
                return ServerResponseType.HIT;
            case DESTROYED:
                return ServerResponseType.DESTROYED;
            case DESTROYED_LAST_SHIP:
                return ServerResponseType.DESTROYED_LAST_SHIP;
            default:
                return ServerResponseType.INVALID;
        }
    }

    private EnumStringMessage deployShip(
            String coordinates,
            SelectionKey key
    ) throws IOException {
        if (coordinates == null || coordinates.length() == 0) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Please send coordinates in the required format"
            );
        }
        boolean isVertical;
        char c = coordinates.charAt(0);
        switch(c) {
            case 'v':
                isVertical = true;
                break;
            case 'h':
                isVertical = false;
                break;
            default:
                return new EnumStringMessage(
                        ServerResponseType.INVALID,
                        "First letter of coordinates must be h or v," +
                                " representing whether the ship is deployed horizontally or vertically."
                );
        }

        Game gameInQuestion = getGameByAccount(getChannelAccount(key));
        if (gameInQuestion == null) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You're not in a game"
            );
        }

        Player thisPlayer = gameInQuestion.getPlayerByAccount(getChannelAccount(key));

        // I don't think this body should ever be executed, should investigate
        if (thisPlayer == null) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You're not part of this game"
            );
        }

        if (thisPlayer.getGameTable().allShipsAreDeployed()) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You have already deployed all of your ships. I guess you should wait" +
                            " for the other player to deploy theirs"
            );
        }

        if (!gameInQuestion.isInDeploymentPhase()) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Game isn't in deployment phase"
            );
        }

        String remainsOfCoordinates = removeLastCharacter(coordinates.substring(1, coordinates.length()));
        EnumStringMessage result =
                initiateShipDeployment(remainsOfCoordinates, isVertical, gameInQuestion, thisPlayer);
        seeIfDeploymentIsFinalized(gameInQuestion, thisPlayer);

        return result;
    }

    private EnumStringMessage initiateShipDeployment(
            String coordinates,
            boolean isVertical,
            Game gameInQuestion,
            Player thisPlayer
    ) throws IOException {
        EnumStringMessage result = gameInQuestion.deployShip(thisPlayer, coordinates, isVertical);
        result = new EnumStringMessage(
                transformDeployedShipTypeToServerResponseType((GameTable.ShipType)result.getEnumValue()),
                result.getMessage()
        );

        return result;
    }

    private void seeIfDeploymentIsFinalized(Game game, Player thisPlayer) throws IOException {
        if (thisPlayer.getGameTable().allShipsAreDeployed()) {
            EnumStringMessage messageToTheOpponent = new EnumStringMessage(
                    ServerResponseType.NOTHING_OF_IMPORTANCE,
                    "Your opponent has just deployed their last ship."
            );
            writeToOpponent(game.getOtherPlayer(thisPlayer), messageToTheOpponent);
        }
    }

    private ServerResponseType transformDeployedShipTypeToServerResponseType(GameTable.ShipType original) {
        switch(original) {
            case DESTROYER:
                return ServerResponseType.DEPLOYED_DESTROYER;
            case CRUISER:
                return ServerResponseType.DEPLOYED_CRUISER;
            case BATTLESHIP:
                return ServerResponseType.DEPLOYED_BATTLESHIP;
            case AIRCRAFT_CARRIER:
                return ServerResponseType.DEPLOYED_CARRIER;
            default:
                return ServerResponseType.INVALID;
        }
    }

    private Game getGameByAccount(Account acc) {
        String nameOfGame = gameStorageByID.get(acc.getCurrentGameID());
        Game theGame = pendingGames.get(nameOfGame);
        if (theGame != null) {
            return theGame;
        }
        theGame = runningGames.get(nameOfGame);
        if (theGame != null) {
            return theGame;
        }
        return null;
    }

    private EnumStringMessage joinGame(
            String message,
            SelectionKey key
    ) throws IOException {
        boolean channelIsLoggedIn = channelIsLoggedIn(key);
        if (!channelIsLoggedIn) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You need to be logged in to join a game"
            );
        }

        message = removeLastCharacter(message);

        if (message.length() == 0) {
            // TODO: replace message with gameName
            message = getRandomPendingGame();
            if (message == null) {
                return new EnumStringMessage(
                        ServerResponseType.INVALID,
                        "No games are waiting for players at the moment"
                );
            }
        }

        if (!validateGameName(message)) {
            System.out.println(message);
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Invalid game name"
            );
        }

        Game desiredGame = pendingGames.get(message);
        if (desiredGame == null) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "No game with this name is waiting for a second player"
            );
        }

        boolean additionWasSuccessful = desiredGame.addPlayer(
                new Player(getChannelAccount(key),
                        desiredGame.getStartingShipsCount())
        );
        if (!additionWasSuccessful) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You can't be added to the game"
            );
        }
        return initializeGameJoin(key, desiredGame);
    }

    private EnumStringMessage initializeGameJoin(
            SelectionKey currentPlayerKey,
            Game desiredGame
    ) throws IOException {
        pendingGames.remove(desiredGame.getGameName());
        allPendingGames.remove(desiredGame.getGameName());
        runningGames.put(desiredGame.getGameName(), desiredGame);

        Player opponent = desiredGame.getOtherPlayer(getChannelAccount(currentPlayerKey));
        EnumStringMessage messageToOpponent = new EnumStringMessage(
                ServerResponseType.OTHER_PLAYER_CONNECTED,
                getChannelAccount(currentPlayerKey).getName() + " just joined your game!"
        );
        writeToOpponent(opponent, messageToOpponent);

        return new EnumStringMessage(
                ServerResponseType.OK,
                "You've just joined the game room " + desiredGame.getGameName() +
                        "! Your opponent is " + opponent.getName()
        );
    }

    private String getRandomPendingGame() {
        Random r = new Random();
        if (allPendingGames.size() < 1) {
            return null;
        }
        String randomGame = allPendingGames.get(r.nextInt(allPendingGames.size()));
        System.out.println("Fetching random game: " + randomGame);
        return randomGame;
    }

    /**
     * When any player exits a game, the game gets terminated because there is no "start game"
     * option; the game starts the moment both players enter the room. Therefore, there is no
     * reason for a player to be replaced with another one after leaving, thus making the room
     * no longer needed.
     * @param key the channel the player uses to communicate with the server on
     * @return an EnumStringMessage informing the player of whether he exited successfully
     */
    private EnumStringMessage exitGame(SelectionKey key) throws IOException {
        Account channelAccount = getChannelAccount(key);
        if (channelAccount.getCurrentGameID() == 0) {
            return new EnumStringMessage(ServerResponseType.INVALID, "You're not in a game");
        }

        String gameToBeClosedName = gameStorageByID.get(channelAccount.getCurrentGameID());
        Game gameToBeClosed = pendingGames.get(gameToBeClosedName);
        if (gameToBeClosed == null) {
            gameToBeClosed = runningGames.get(gameToBeClosedName);
        }

        // body shouldn't be executed, unless there is a bug
        if (gameToBeClosed == null) {
            return new EnumStringMessage(ServerResponseType.INVALID, "Cannot find the game you're in ??");
        }
        return initializeGameExit(key, gameToBeClosed);
    }

    private EnumStringMessage initializeGameExit(
            SelectionKey key,
            Game currentGame
    ) throws IOException {
        Account channelAccount = getChannelAccount(key);

        pendingGames.remove(currentGame.getGameName());
        allPendingGames.remove(currentGame.getGameName());
        runningGames.remove(currentGame.getGameName());
        currentGame.end();

        EnumStringMessage messageToOppponent = new EnumStringMessage(
                ServerResponseType.DISCONNECTED,
                "Other player disconnected from the game"
        );
        Player opponent = currentGame.getOtherPlayer(channelAccount);
        writeToOpponent(opponent, messageToOppponent);

        return new EnumStringMessage(
                ServerResponseType.DISCONNECTED,
                "You have been disconnected from the game"
        );
    }

    private EnumStringMessage createGame(String message, SelectionKey key) {
        boolean channelIsLoggedIn = channelIsLoggedIn(key);
        if (!channelIsLoggedIn) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You need to be logged in to create a game"
            );
        }

        String[] gameNameAndShipCount = removeLastCharacter(message).split(" ");
        String gameName = gameNameAndShipCount[0];
        if (!validateGameName(gameName)) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Invalid game name; It must start with a letter and only contain letters, digits and underscores"
            );
        }

        if (gameExists(gameName)) {
            return new EnumStringMessage(ServerResponseType.INVALID, "Game already exists, try another name");
        }

        String numberOfShipsAsText;
        try {
            numberOfShipsAsText = gameNameAndShipCount[1];
        } catch (ArrayIndexOutOfBoundsException exc) {
            numberOfShipsAsText = null;
        }

        int numberOfShipsAsNumeric;
        if (numberOfShipsAsText == null) {
            numberOfShipsAsNumeric = 10;
        } else if (numberOfShipsAsText.matches("\\d+")) {
            numberOfShipsAsNumeric = Integer.parseInt(numberOfShipsAsText);
            if (numberOfShipsAsNumeric > 10) {
                return new EnumStringMessage(ServerResponseType.INVALID, "The number is too big");
            }
        } else {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "There should be a single positive number after the command or no number at all"
            );
        }

        return initializeGameCreation(gameName, numberOfShipsAsNumeric, key);
    }

    private EnumStringMessage initializeGameCreation(String gameName, int numberOfShips, SelectionKey key) {
        Player hostingPlayer = new Player(getChannelAccount(key), numberOfShips);
        Game newGame = new Game(gameName, numberOfShips, allGamesEverCount, hostingPlayer);
        pendingGames.put(gameName, newGame);
        allPendingGames.add(gameName);
        gameStorageByID.put(allGamesEverCount, gameName);
        if (!hostingPlayer.joinAGame(newGame.getGameID())) {
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Cannot create a game while being in another one!"
            );
        }

        ++allGamesEverCount;
        return new EnumStringMessage(ServerResponseType.OK, "Game created successfully!");
    }

    private boolean gameExists(String gameName) {
        return pendingGames.get(gameName) != null || runningGames.get(gameName) != null;
    }

    private EnumStringMessage logoutAccount(SelectionKey key, SocketChannel channel) throws IOException {
        if (channelIsLoggedIn(key)) {
            System.out.println(getChannelAccount(key).getName() + " has logged out");
            logChannelOut(key, channel);
            return new EnumStringMessage(ServerResponseType.OK, "Successful logout. Bye!");
        }
        return new EnumStringMessage(ServerResponseType.INVALID, "You need to have logged in to log out...");
    }

    private EnumStringMessage registerAccount(String message, SelectionKey key) {
        String[] usernameAndPassword = splitUsernameAndPassword(message);
        if (usernameAndPassword == null) {
            return new EnumStringMessage(ServerResponseType.INVALID, "Unverified username/ password");
        }
        Account acc = new Account(usernameAndPassword[0], usernameAndPassword[1]);
        if (accountExists(acc)) {
            System.out.println(acc.getName() + " already exists");
            return new EnumStringMessage(ServerResponseType.INVALID, "User already exists...");
        }
        if (channelIsLoggedIn(key)) {
            System.out.println("This channel is already logged in");
            return new EnumStringMessage(ServerResponseType.INVALID, "You need to log out before you can log in");
        }
        acc.registerAccount();
    return new EnumStringMessage(ServerResponseType.OK, "Successful registration! Welcome aboard, " + acc.getName());
    }

    private boolean accountExists(Account acc) {
        return (new File(acc.getPersonalRecordFilePath()).isFile());
    }

    private void logChannelOut(SelectionKey key, SocketChannel channel) throws IOException {
        exitGame(key);
        loggedInUsers.remove(getChannelAccount(key).getName());
        key.attach(new Account(channel));
    }

    private EnumStringMessage loginAccount(String message, SelectionKey key) throws IOException {
        if (channelIsLoggedIn(key)) {
            System.out.println("This channel is already logged in");
            return new EnumStringMessage(ServerResponseType.INVALID, "You need to log out before you can log in");
        }

        String[] usernameAndPassword = splitUsernameAndPassword(message);
        if (usernameAndPassword == null) {
            return new EnumStringMessage(ServerResponseType.INVALID, "Unverified username/ password");
        }

        Account acc = new Account(usernameAndPassword[0], usernameAndPassword[1]);
        if (accountIsLoggedIn(acc)) {
            System.out.println(acc.getName() + " is already logged in");
            return new EnumStringMessage(ServerResponseType.INVALID, "User " + acc.getName() + " already logged in...");
        }

        return verifyLoginDataAndLogin(acc, (Account)(key.attachment()));
    }

    private EnumStringMessage verifyLoginDataAndLogin(Account requestedAcc, Account savedAcc) throws IOException {
        if (requestedAcc.exists()) {
            String savedPass = requestedAcc.loadPassword();
            if (savedPass.equals(requestedAcc.getPassword())) {
                savedAcc.setName(requestedAcc.getName());
                savedAcc.setPassword(requestedAcc.getPassword());
                System.out.println("Successful login!");
                loggedInUsers.add(requestedAcc.getName());
                return new EnumStringMessage(ServerResponseType.OK, "Successful login!");
            }
            System.out.println("Incorrect pass");
            return new EnumStringMessage(ServerResponseType.INVALID, "Incorrect password...");
        } else {
            System.out.println("Account not found.");
            return new EnumStringMessage(ServerResponseType.INVALID, "Account doesn't exist");
        }
    }

    private boolean channelIsLoggedIn(SelectionKey key) {
        return  ((Account)key.attachment()).getName() != null;
    }

    private boolean accountIsLoggedIn(Account acc) {
        return loggedInUsers.contains(acc.getName());
    }

    private String[] splitUsernameAndPassword(String input) {
        String[] usernameAndPassword = input.split(" ");
        if (usernameAndPassword.length != 2) {
            System.out.println("Username and password not validated...");
            return null;
        }
        usernameAndPassword[1] = removeLastCharacter(usernameAndPassword[1]);
        return usernameAndPassword;
    }

    private String removeLastCharacter(String input) {
        return input.substring(0, input.length() - 1);
    }

    private void writeToClient(
            EnumStringMessage message,
            SocketChannel channel,
            Account targetOfMessage
    ) throws IOException {
        ByteBuffer buffer = targetOfMessage.getBufferForCommunicationWithServer();

        buffer.clear();
        buffer.put(((byte) message.getEnumValue().ordinal()));
        buffer.put(message.getMessage().getBytes());
        buffer.flip();
        channel.write(buffer);
        System.out.println();
    }

    private void writeToOpponent(Player opponent, EnumStringMessage messageToOpponent) throws IOException {
        if (opponent != null) {
            Account opponentAccount = opponent.getAccount();
            SocketChannel opponentChannel = opponentAccount.getChannel();
            writeToClient(messageToOpponent, opponentChannel, opponentAccount);
        }
    }

    private boolean validateGameName(String gameName) {
        return gameName.matches("[a-zA-Z][\\w\\d_]*");
    }
}
