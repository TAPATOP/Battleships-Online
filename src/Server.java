import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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
        processMessage(mesType, playerMessage, chan, key);
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

    private static void processMessage(MessageType mesType, String message, SocketChannel chan, SelectionKey key) throws IOException {
        switch (mesType) {
            case LOGIN:
                String[] usernameAndPassword = splitUsernameAndPassword(message);
                if (usernameAndPassword == null) {
                    return;
                }
                //System.out.println("Here you go:" + usernameAndPassword[0] + " " + usernameAndPassword[1]);
                Account acc = new Account(usernameAndPassword[0], usernameAndPassword[1]);
                if (acc.exists()) {
                    System.out.println("Successful login!");
                    ((Account)key.attachment()).setName(usernameAndPassword[0]);
                    ((Account)key.attachment()).setPassword(usernameAndPassword[1]);
                } else{
                    System.out.println("Account not found. Disconnecting the imposer!");
                    chan.close();
                }
                break;
            default:
                System.out.println("I don't know how to handle this :c");
        }
    }

    private static String[] splitUsernameAndPassword(String input) {
        String[] usernameAndPassword = input.split(" ");
        if (usernameAndPassword.length > 2) {
            System.out.println("Username and password not validated...");
            return null;
        }
        return usernameAndPassword;
    }

    private static void writeToClient(String message, ByteBuffer buffer, SocketChannel chan) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        chan.write(buffer);
    }

    public static void main(String args[]) {
        System.out.println("Server is working");
        try {
            // SERVER INITIALIZATION
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(6969));
            Selector selector = Selector.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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
                            while (readFromClient(buffer, chan, key)) {

                                // SEND INPUT TO THE CLIENT
//                                char[] playerInput = new char[buffer.limit()];
//                                int i = 0;
//                                while (buffer.limit() > buffer.position()) {
//                                    playerInput[i++] = (char) buffer.get();
//                                }
//                                String message = "You said: " + new String(playerInput);
//                                writeToClient(message, buffer, chan);
//
//                                System.out.print(playerInput);
                            }
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
}