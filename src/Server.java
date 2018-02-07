import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static void main(String args[]){
        System.out.println("Server is working");
        try{
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
                    if(key.isAcceptable()) {
                        // ACCEPT THE PENDING CONNECTIONS AND DESIGNATE THEM FOR READING
                        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                        System.out.println("We have a new connection!");
                    }else if (key.isReadable()) {
                        SocketChannel chan = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        try {
                            while (true) {
                                // READ THE CLIENT INPUT
                                buffer.clear();
                                int r = chan.read(buffer);
                                if (r <= 0) {
                                    break;
                                }
                                buffer.flip();

                                // SEND INPUT TO THE CLIENT
                                char[] playerInput = new char[buffer.limit()];
                                int i = 0;
                                while (buffer.limit() > buffer.position()) {
                                    playerInput[i++] = (char)buffer.get();
                                }
                                System.out.print(playerInput);
                                String message = "You said: " + new String(playerInput);
                                buffer.clear();
                                buffer.put(message.getBytes());
                                buffer.flip();
                                chan.write(buffer);
                            }
                        }catch(IOException | CancelledKeyException exc){
                            System.out.println("Connection to client lost!");
                            chan.close();
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch(IOException a) {
            System.out.println("IOException");
        }
        System.out.println("Server is stopping");
    }
} 