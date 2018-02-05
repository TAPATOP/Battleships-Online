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
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(6969));
            Selector selector = Selector.open();

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if(key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        SocketChannel chan = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        byte[] message = new byte[1024];

                        try {
                            while (true) {
                                buffer.clear();

                                int r = chan.read(buffer);
                                if (r <= 0) {
                                    break;
                                }
                                buffer.flip();
                                while (buffer.limit() > buffer.position()) {
                                    System.out.print((char) buffer.get());
                                }

                                String string = "Hello, Client!";
                                for (int i = 0; i < string.length(); i++) {
                                    message[i] = (byte) string.charAt(i);
                                }

                                buffer.clear();
                                buffer.put(message);
                                buffer.flip();

                                chan.write(buffer);
                                chan.close();
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