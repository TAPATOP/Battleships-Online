package Source;

import Source.Game.EnumStringMessage;
import Source.Game.Game;
import Source.Game.Player;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private static void acceptConnections(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, new Account(sc));
        System.out.println("We have a new connection!");
    }

    private static boolean readFromClient(SocketChannel chan, SelectionKey key) throws IOException {
        ByteBuffer buffer = getChannelBuffer(key);

        buffer.clear();
        try {
            if (chan.read(buffer) <= 0) {
                return false;
            }
        } catch (BufferOverflowException exc) {
            System.out.println("Client message too long!");
        }
        buffer.flip();

        ClientMessageType mesType = readClientMessageType(buffer);
        System.out.println("Processing a " + mesType + " message");
        String playerMessage = readClientMessage(buffer);
        System.out.println("Processing this message: " + playerMessage);

        // No reason not to notify the client about what's going on as soon as possible
        writeToClient(processMessage(mesType, playerMessage, chan, key), chan, getChannelAccount(key));
        return true;
    }

    private static Account getChannelAccount(SelectionKey key){
        return ((Account)key.attachment());
    }

    private static ByteBuffer getChannelBuffer(SelectionKey key){
        return getChannelAccount(key).getBufferForCommunicationWithServer();
    }

    private static ClientMessageType readClientMessageType(ByteBuffer buffer) {
        return ClientMessageType.values()[(int) buffer.get()];
    }

    private static String readClientMessage(ByteBuffer buffer) {
        char[] playerInput = new char[buffer.limit()];
        int i = 0;
        while (buffer.limit() > buffer.position()) {
            playerInput[i++] = (char) buffer.get();
        }
        return new String(playerInput);
    }

    private static EnumStringMessage processMessage(ClientMessageType mesType, String message, SocketChannel chan, SelectionKey key) throws IOException {
        switch (mesType) {
            case LOGIN:
                return loginAccount(message, key);
            case REGISTER:
                return registerAccount(message, key);
            case LOGOUT:
                return logoutAccount(key, chan);
            case CREATE_GAME:
                return createGame(message, key, chan);
            case EXIT_GAME:
                return exitGame(key, chan);
            case JOIN_GAME:
                return joinGame(message, key, chan);
            default:
                System.out.println("I don't know how to handle this :c");
                return new EnumStringMessage(
                        ServerResponseType.NOTHING_OF_IMPORTANCE,
                        "I have no idea what to do with this so I will just repeat it: " + message
                );
        }
    }

    private static EnumStringMessage joinGame(
            String message,
            SelectionKey key,
            SocketChannel channel
    ) throws IOException{
        boolean channelIsLoggedIn = channelIsLoggedIn(key);
        if(!channelIsLoggedIn){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You need to be logged in to join a game"
            );
        }

        message = removeLastCharacter(message);
        if(!validateGameName(message)){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Invalid game name"
            );
        }

        Game desiredGame = pendingGames.get(message);
        boolean additionWasSuccessful = desiredGame.addPlayer(new Player(getChannelAccount(key), channel));
        if(!additionWasSuccessful){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You're already in another game"
            );
        }
        return initializeGameJoin(key, desiredGame);
    }

    private static EnumStringMessage initializeGameJoin(
            SelectionKey currentPlayerKey,
            Game desiredGame
    ) throws IOException{

        Player opponent = desiredGame.getOtherPlayer(getChannelAccount(currentPlayerKey));
        EnumStringMessage messageToOpponent = new EnumStringMessage(
                ServerResponseType.OTHER_PLAYER_CONNECTED,
                getChannelAccount(currentPlayerKey).getName() + " just joined your game!"
        );
        writeToOpponent(opponent, messageToOpponent);

        return new EnumStringMessage(
                ServerResponseType.OK,
                "You've just joined a game! Your opponent is " + opponent.getName()
        );
    }

    /**
     * When any player exits a game, the game gets terminated because there is no "start game"
     * option; the game starts the moment both players enter the room. Therefore, there is no
     * reason for a player to be replaced with another one after leaving, thus making the room
     * no longer needed.
     * @param key the channel the player uses to communicate with the server on
     * @return an EnumStringMessage informing the player of whether he exited successfully
     */
    private static EnumStringMessage exitGame(SelectionKey key, SocketChannel channel) throws IOException{
        Account channelAccount = getChannelAccount(key);
        if(channelAccount.getCurrentGameID() == 0){
            return new EnumStringMessage(ServerResponseType.INVALID, "You're not in a game");
        }

        String gameToBeClosedName = gameIDtoGameNameHash.get(channelAccount.getCurrentGameID());
        Game gameToBeClosed = pendingGames.get(gameToBeClosedName);
        if(gameToBeClosed == null){
            gameToBeClosed = runningGames.get(gameToBeClosedName);
        }

        // body shouldn't be executed, unless there is a bug
        if(gameToBeClosed == null){
            return new EnumStringMessage(ServerResponseType.INVALID, "Cannot find the game you're in ??");
        }
        return initializeGameExit(key, channel, gameToBeClosed);
    }

    private static EnumStringMessage initializeGameExit(
            SelectionKey key,
            SocketChannel channel,
            Game currentGame
    ) throws IOException {
        Account channelAccount = getChannelAccount(key);

        if(pendingGames.containsKey(currentGame.getGameName())){
            pendingGames.remove(currentGame.getGameName());
        }
        else{
            runningGames.remove(currentGame.getGameName());
        }
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

    private static EnumStringMessage createGame(String gameName, SelectionKey key, SocketChannel channel){
        boolean channelIsLoggedIn = channelIsLoggedIn(key);
        if(!channelIsLoggedIn){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You need to be logged in to create a game"
            );
        }

        gameName = removeLastCharacter(gameName);
        if(!validateGameName(gameName)){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Invalid game name; It must start with a letter and only contain letters, digits and underscores"
            );
        }

        if(gameExists(gameName)){
            return new EnumStringMessage(ServerResponseType.INVALID, "Game already exists, try another name");
        }

        return initializeGameCreation(gameName, key, channel);
    }

    private static EnumStringMessage initializeGameCreation(String gameName, SelectionKey key, SocketChannel channel){
        Player hostingPlayer = new Player(getChannelAccount(key), channel);
        Game newGame = new Game(gameName, allGamesEverCount, hostingPlayer);
        pendingGames.put(gameName, newGame);
        gameIDtoGameNameHash.put(allGamesEverCount, gameName);
        if(!hostingPlayer.joinAGame(newGame.getGameID())){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Cannot join a game while being in another one!"
            );
        }

        ++allGamesEverCount;
        return new EnumStringMessage(ServerResponseType.OK, "Game created successfully!");
    }

    private static boolean gameExists(String gameName){
        return pendingGames.get(gameName) != null || runningGames.get(gameName) != null;
    }

    private static EnumStringMessage logoutAccount(SelectionKey key, SocketChannel channel) throws IOException{
        if(channelIsLoggedIn(key)){
            System.out.println(getChannelAccount(key).getName() + " has logged out");
            logChannelOut(key, channel);
            return new EnumStringMessage(ServerResponseType.OK, "Successful logout. Bye!");
        }
        return new EnumStringMessage(ServerResponseType.INVALID, "You need to have logged in to log out...");
    }

    private static EnumStringMessage registerAccount(String message, SelectionKey key){
        String[] usernameAndPassword = splitUsernameAndPassword(message);
        if (usernameAndPassword == null) {
            return new EnumStringMessage(ServerResponseType.INVALID, "Unverified username/ password");
        }
        Account acc = new Account(usernameAndPassword[0], usernameAndPassword[1]);
        if(accountExists(acc)){
            System.out.println(acc.getName() + " already exists");
            return new EnumStringMessage(ServerResponseType.INVALID, "User already exists...");
        }
        if(channelIsLoggedIn(key)){
            System.out.println("This channel is already logged in");
            return new EnumStringMessage(ServerResponseType.INVALID, "You need to log out before you can log in");
        }
        acc.registerAccount();
    return new EnumStringMessage(ServerResponseType.OK, "Successful registration! Welcome aboard, " + acc.getName());
    }

    private static boolean accountExists(Account acc) {
        return (new File(acc.getPathName()).isFile());
    }

    private static void logChannelOut(SelectionKey key, SocketChannel channel) throws IOException{
        exitGame(key, channel);
        loggedInUsers.remove(getChannelAccount(key).getName());
        key.attach(new Account(channel));
    }

    private static EnumStringMessage loginAccount(String message, SelectionKey key) throws IOException{
        if(channelIsLoggedIn(key)){
            System.out.println("This channel is already logged in");
            return new EnumStringMessage(ServerResponseType.INVALID, "You need to log out before you can log in");
        }
        String[] usernameAndPassword = splitUsernameAndPassword(message);
        if (usernameAndPassword == null) {
            return new EnumStringMessage(ServerResponseType.INVALID, "Unverified username/ password");
        }
        Account acc = new Account(usernameAndPassword[0], usernameAndPassword[1]);
        if(accountIsLoggedIn(acc)){
            System.out.println(acc.getName() + " is already logged in");
            return new EnumStringMessage(ServerResponseType.INVALID, "User already logged in...");
        }
        return verifyLoginDataAndLogin(acc, (Account)(key.attachment()));
    }

    private static EnumStringMessage verifyLoginDataAndLogin(Account requestedAcc, Account savedAcc) throws IOException{
        if (requestedAcc.exists()) {
            String savedPass = loadPassword(requestedAcc);
            if(savedPass.equals(requestedAcc.getPassword())){
                savedAcc.setName(requestedAcc.getName());
                savedAcc.setPassword(requestedAcc.getPassword());
                System.out.println("Successful login!");
                loggedInUsers.add(requestedAcc.getName());
                return new EnumStringMessage(ServerResponseType.OK, "Successful login!");
            }
            System.out.println("Incorrect pass");
            return new EnumStringMessage(ServerResponseType.INVALID, "Incorrect password...");
        } else{
            System.out.println("Account not found.");
            return new EnumStringMessage(ServerResponseType.INVALID, "Account doesn't exist");
        }
    }

    private static boolean channelIsLoggedIn(SelectionKey key){
        return  ((Account)key.attachment()).getName() != null;
    }

    private static boolean accountIsLoggedIn(Account acc){
        return loggedInUsers.contains(acc.getName());
    }

    private static String loadPassword(Account acc) throws IOException{
        File f = new File(acc.getPathName());
        BufferedReader reader = new BufferedReader(new FileReader(f));
        return reader.readLine();
    }

    private static String[] splitUsernameAndPassword(String input) {
        String[] usernameAndPassword = input.split(" ");
        if (usernameAndPassword.length != 2) {
            System.out.println("Username and password not validated...");
            return null;
        }
        usernameAndPassword[1] = removeLastCharacter(usernameAndPassword[1]);
        return usernameAndPassword;
    }

    private static String removeLastCharacter(String input){
        return input.substring(0, input.length() - 1);
    }

    private static void writeToClient(
            EnumStringMessage message,
            SocketChannel channel,
            Account targetOfMessage
    ) throws IOException {
        ByteBuffer buffer = targetOfMessage.getBufferForCommunicationWithServer();
        SocketChannel chan = channel;

        buffer.clear();
        buffer.put(((byte) message.getEnumValue().ordinal()));
        buffer.put(message.getMessage().getBytes());
        buffer.flip();
        chan.write(buffer);
    }

    private static void writeToOpponent(Player opponent, EnumStringMessage messageToOpponent) throws IOException{
        if(opponent != null){
            Account opponentAccount = opponent.getAccount();
            SocketChannel opponentChannel = opponentAccount.getChannel();
            writeToClient(messageToOpponent, opponentChannel, opponentAccount);
        }
    }
    private static boolean validateGameName(String gameName){
        return gameName.matches("[a-zA-Z][\\w\\d_]*");
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String args[]) {
        System.out.println("Server is working");
        SelectionKey key = null;
        try {
            // SERVER INITIALIZATION
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(6969));
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            SocketChannel chan;

            // CHANNEL PROCESSING
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
                            while (readFromClient(chan, key));
                        } catch (IOException | CancelledKeyException exc) {
                            System.out.println("Connection to client lost!");
                            logoutAccount(key, chan);
                            chan.close();
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException a) {
            System.out.println("IOException");
        }
    }

    // MEMBER VARIABLES
    private static HashSet<String> loggedInUsers = new HashSet<>();
    private static HashMap<String, Game> pendingGames = new HashMap<>();
    private static HashMap<String, Game> runningGames = new HashMap<>();
    private static HashMap<Integer, String> gameIDtoGameNameHash = new HashMap<>();
    private static int allGamesEverCount = 1;
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;
}
