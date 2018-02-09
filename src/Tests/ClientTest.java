package Tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import Source.Client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;

public class ClientTest {
    private final static int BUFFER_SIZE = 1024;
    static SocketChannel socket = null;

    @BeforeClass
    public static void setup() {
        try{
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress("localhost", 6969));
        }catch(IOException exc){
            System.out.println("Server offline");
        }
        Client.setSocket(socket);
        Client.setBuffer(ByteBuffer.allocate(BUFFER_SIZE));
    }

    @AfterClass
    public static void deleteTempRegistration(){
        File file = new File(".\\Accounts\\username.txt");
        boolean a = file.delete();
        if(!a){
            System.out.println("Something messed up while deleting tempregs");
            return;
        }
        file = new File(".\\Accounts\\username2.txt");
        a = file.delete();
        if(!a){
            System.out.println("Something messed up while deleting tempregs");
        }
    }

    @Test
    public void runTestsInOrder() throws IOException{
        loginShouldWorkWithValidAndInvalidLogins();
        loginDoesntReloginWhileLoggedIn();
        logoutShouldLogoutIfNotLoggedOut();
        shouldBeAbleToLogInAndOutMultipleTimes();
        registeringShouldRegisteredUnregisteredAndNotRegisterRegistered();
    }

    private void loginShouldWorkWithValidAndInvalidLogins() throws IOException {
        assertFalse(Client.processPlayerCommand("login TAPATOP pesswerdlmao"));
        assertFalse(Client.processPlayerCommand("login w peswerdlmao"));
        assertFalse(Client.processPlayerCommand("login rrr"));
        assertFalse(Client.processPlayerCommand("login w w ww  w w w w"));
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
    }

    private void loginDoesntReloginWhileLoggedIn() throws IOException{
        assertFalse(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
    }

    private void logoutShouldLogoutIfNotLoggedOut() throws IOException{
        System.out.println("logout test 1");
        // assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(Client.processPlayerCommand("logout"));
        assertFalse(Client.processPlayerCommand("logout"));
    }

    private void shouldBeAbleToLogInAndOutMultipleTimes() throws IOException{
        System.out.println("logoout test 2");
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(Client.processPlayerCommand("logout"));
        assertTrue(Client.processPlayerCommand("login hi hi"));
        assertTrue(Client.processPlayerCommand("logout"));
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(Client.processPlayerCommand("logout"));
    }

    public void registeringShouldRegisteredUnregisteredAndNotRegisterRegistered() throws IOException{
        assertTrue(Client.processPlayerCommand("register username password"));
        assertTrue(Client.processPlayerCommand("register username2 password"));
        assertFalse(Client.processPlayerCommand("register username password"));

    }

}