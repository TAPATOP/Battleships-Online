import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String args[]){
        try{
            Socket sock = new Socket("loopback", 6969);
            PrintWriter output = new PrintWriter(sock.getOutputStream());
            BufferedReader serverOutput = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));

            output.println("Hi, server!");
            output.flush();

            String line;
            while((line = serverOutput.readLine()) != null) {
                System.out.println(line);
            }

        }catch(UnknownHostException exc){
            System.out.println("Issues locating the host");
        }catch(IOException exc) {
            System.out.println("Cannot connect to host");
        }
    }
}
