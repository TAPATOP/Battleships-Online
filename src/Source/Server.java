package Source;

import Source.Game.EnumStringMessage;
import Source.Game.Game;
import Source.Game.Player;

import java.io.*;
import java.net.InetSocketAddress;
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
        sc.register(selector, SelectionKey.OP_READ, new Account());
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
        writeToClient(processMessage(mesType, playerMessage, chan, key), buffer, chan);
        return true;
    }

    private static Account getChannelAccount(SelectionKey key){
        return ((Account)key.attachment());
    }

    private static ByteBuffer getChannelBuffer(SelectionKey key){
        return getChannelAccount(key).getInputFromServerBuffer();
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
                return logoutAccount(key);
            case CREATE_GAME:
                return createGame(message, key);
            case EXIT_GAME:
                //return exitGame(key);
            default:
                System.out.println("I don't know how to handle this :c");
                return new EnumStringMessage(
                        ServerResponseType.NOTHING_OF_IMPORTANCE,
                        "I have no idea what to do with this so I will just repeat it: " + message
                );
        }
    }

    private static EnumStringMessage createGame(String gameName, SelectionKey key){
        boolean channelIsLoggedIn = channelIsLoggedIn(key);
        if(!channelIsLoggedIn){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "You need to be logged in to create a game"
            );
        }

        boolean channelIsAlreadyInAGame = getChannelAccount(key).getCurrentGameID() != 0;
        if(channelIsAlreadyInAGame){
            return new EnumStringMessage(
                    ServerResponseType.INVALID,
                    "Cannot create a game while you're in one"
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

        return initializeGameCreation(gameName, key);
    }

    private static EnumStringMessage initializeGameCreation(String gameName, SelectionKey key){
        Player hostingPlayer = new Player(getChannelAccount(key));
        Game newGame = new Game(allGamesEverCount++, hostingPlayer);
        pendingGames.put(gameName, newGame);

        return new EnumStringMessage(ServerResponseType.OK, "Game created successfully!");
    }

    private static boolean gameExists(String gameName){
        return pendingGames.get(gameName) != null || runningGames.get(gameName) != null;
    }

    private static EnumStringMessage logoutAccount(SelectionKey key){
        if(channelIsLoggedIn(key)){
            System.out.println(((Account)key.attachment()).getName() + " has logged out");
            logChannelOut(key);
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

    private static void logChannelOut(SelectionKey key){
        loggedInUsers.remove(((Account)key.attachment()).getName());
        key.attach(new Account());
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

    private static void writeToClient(EnumStringMessage message, ByteBuffer buffer, SocketChannel chan) throws IOException {
        buffer.clear();
        buffer.put(((byte) message.getEnumValue().ordinal()));
        buffer.put(message.getMessage().getBytes());
        buffer.flip();
        chan.write(buffer);
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
                        SocketChannel chan = (SocketChannel) key.channel();
                        try {
                            // READ THE CLIENT INPUT
                            while (readFromClient(chan, key));
                        } catch (IOException | CancelledKeyException exc) {
                            System.out.println("Connection to client lost!");
                            logoutAccount(key);
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
    private static int allGamesEverCount = 1;
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;
}
