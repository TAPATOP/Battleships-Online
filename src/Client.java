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
            SocketChannel sock = SocketChannel.open();
            sock.connect(new InetSocketAddress("localhost", 6969));

            ByteBuffer output = ByteBuffer.allocate(1024);
            output.put("<connection initialization message here>\n".getBytes());
            output.flip();

            while(output.hasRemaining()){
                sock.write(output);
            }
//            BufferedReader serverOutput = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
            String playerMessage;

            while(true) {
                System.out.println("Ready to send a command to the server...");
                playerMessage = playerInput.readLine();
                if (playerMessage.equals("stop")) {
                    output.clear();
                    output.put("Bye!".getBytes());
                    output.flip();
                    sock.write(output);
                    break;
                }

                output.clear();
                output.put((playerMessage + '\n').getBytes());
                output.flip();
                sock.write(output);

                output.clear();

                sock.read(output);
                output.flip();
                while (output.limit() > output.position()) {
                    System.out.print((char) output.get());
                }
                        //                output.println(playerMessage);
//                output.flush();
//
//                String line;
//                while ((line = serverOutput.readLine()) != null) {
//                    System.out.println(line);
//                }
            }

        }catch(UnknownHostException exc){
            System.out.println("Issues locating the server");
        }catch(IOException exc) {
            System.out.println("Cannot connect to server");
        }
    }
}
