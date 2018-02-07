import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String args[]){
        try{
            final int BUFFER_SIZE = 1024;

            SocketChannel sock = SocketChannel.open();
            sock.connect(new InetSocketAddress("localhost", 6969));

            // CREATE BUFFER AND SEND INITIALIZATION MESSAGE
            ByteBuffer output = ByteBuffer.allocate(BUFFER_SIZE);
            output.put("<connection initialization message here>\n".getBytes());
            output.flip();
            while(output.hasRemaining()){
                sock.write(output);
            }

            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;

            // START OF CLIENT- SERVER MESSAGE EXCHANGE
            while(true) {
                System.out.println("Ready to send a command to the server...");
                playerMessage = playerInput.readLine();

                // STOPS THE EXCHANGE IF MESSAGE IS "stop"
                if (playerMessage.equals("stop")) {
                    output.clear();
                    output.put("Bye!\n".getBytes());
                    output.flip();
                    sock.write(output);
                    sock.close();
                    break;
                }

                // SENDS THE INPUT MESSAGE TO THE SERVER
                output.clear();
                output.put((playerMessage + '\n').getBytes());
                output.flip();
                sock.write(output);

                // READS BACK THE SERVER RESPONSE WITHOUT OVERFLOW
                do {
                    output.clear();
                    sock.read(output);
                    output.flip();
                    while (output.limit() > output.position()) {
                        System.out.print((char) output.get());
                    }
                }while(output.limit() >= output.capacity());
            }

        }catch(UnknownHostException exc){
            System.out.println("Issues locating the server");
        }catch(IOException exc) {
            System.out.println("Cannot connect to server");
        }
    }
}
