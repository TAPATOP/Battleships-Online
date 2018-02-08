package Source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static boolean readFromClient(ByteBuffer buffer, SocketChannel chan, SelectionKey key) throws IOException {
        buffer.clear();
        try {
            if (chan.read(buffer) <= 0) {
                return false;
            }
        } catch (BufferOverflowException exc) {
            System.out.println("Client message too long!");
        }
        buffer.flip();
        MessageType mesType = readClientMessageType(buffer);
        System.out.println("Processing a " + mesType + " message");
        String playerMessage = readClientMessage(buffer);
        System.out.println("Processing this message: " + playerMessage);
        writeToClient(processMessage(mesType, playerMessage, chan, key), buffer, chan);
        return true;
    }

    private static MessageType readClientMessageType(ByteBuffer buffer) {
        return MessageType.values()[(int) buffer.get()];
    }

    private static String readClientMessage(ByteBuffer buffer) {
        char[] playerInput = new char[buffer.limit()];
        int i = 0;
        while (buffer.limit() > buffer.position()) {
            playerInput[i++] = (char) buffer.get();
        }
        return new String(playerInput);
    }

    private static String processMessage(MessageType mesType, String message, SocketChannel chan, SelectionKey key) throws IOException {
        switch (mesType) {
            case LOGIN:
                return loginAccount(message, key);
            default:
                System.out.println("I don't know how to handle this :c");
                return message;
        }
    }

    private static String loginAccount(String message, SelectionKey key) throws IOException{
        if(channelIsLoggedIn(key)){
            System.out.println("This channel is already logged in");
            return 0 + "You need to log out before you can log in";
        }
        String[] usernameAndPassword = splitUsernameAndPassword(message);
        if (usernameAndPassword == null) {
            return 0 + "Unverified username/ password";
        }
        Account acc = new Account(usernameAndPassword[0], usernameAndPassword[1]);
        if(loggedInUsers.contains(acc.getName())){
            System.out.println(acc.getName() + " is already logged in");
            return 0 + "User already logged in...";
        }
        return verifyLoginDataAndLogin(acc, (Account)(key.attachment()));

    }

    private static String verifyLoginDataAndLogin(Account requestedAcc, Account savedAcc) throws IOException{
        if (requestedAcc.exists()) {
            String savedPass = loadPassword(requestedAcc);
            if(savedPass.equals(requestedAcc.getPassword())){
                savedAcc.setName(requestedAcc.getName());
                savedAcc.setPassword(requestedAcc.getPassword());
                System.out.println("Successful login!");
                loggedInUsers.add(requestedAcc.getName());
                return 1 + "Successful login!";
            }
            System.out.println("Incorrect pass");
            return 0 + "Incorrect password...";
        } else{
            System.out.println("Account not found.");
            return 0 + "Account doesn't exist";
        }
    }
    private static boolean channelIsLoggedIn(SelectionKey key){
        return ((Account)key.attachment()).getName() != null;
    }

    private static String loadPassword(Account acc) throws IOException{
        File f = new File(acc.getPathName());
        BufferedReader reader = new BufferedReader(new FileReader(f));
        return reader.readLine();
    }
    private static String[] splitUsernameAndPassword(String input) {
        String[] usernameAndPassword = input.split(" ");
        if (usernameAndPassword.length > 2) {
            System.out.println("Username and password not validated...");
            return null;
        }
        usernameAndPassword[1] = usernameAndPassword[1].substring(0, usernameAndPassword[1].length() - 1);
        return usernameAndPassword;
    }

    private static void writeToClient(String message, ByteBuffer buffer, SocketChannel chan) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        chan.write(buffer);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String args[]) {
        System.out.println("Server is working");
        try {
            // SERVER INITIALIZATION
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(6969));
            selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            loggedInUsers = new HashSet<>();

            // CHANNEL PROCESSING
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        // ACCEPT THE PENDING CONNECTIONS AND DESIGNATE THEM FOR READING
                        acceptConnections(selector, key);
                    } else if (key.isReadable()) {
                        // DO THE ACTUAL WORK
                        SocketChannel chan = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        try {
                            // READ THE CLIENT INPUT
                            while (readFromClient(buffer, chan, key));
                        } catch (IOException | CancelledKeyException exc) {
                            System.out.println("Connection to client lost!");
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
    private static HashSet<String> loggedInUsers;
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;
}