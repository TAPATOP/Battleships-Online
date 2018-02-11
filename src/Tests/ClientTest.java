package Tests;

import Source.ServerResponseType;
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
    private static SocketChannel socket = null;

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
    public void loginShouldWorkWithValidAndInvalidLogins() throws IOException {
        assertFalse(
                "Tries logging in with a typo in password",
                Client.processPlayerCommand("login TAPATOP pesswerdlmao"));
        assertFalse(
                "Tries logging with a legal password but with the wrong account",
                Client.processPlayerCommand("login w peswerdlmao"));
        assertFalse(
                "Tries loggin in by giving less than needed parameters",
                Client.processPlayerCommand("login rrr"));
        assertFalse(
                "Tries loggin in without giving any parameters",
                Client.processPlayerCommand("login"));
        assertFalse(
                "Tries logging in by giving too many parameters",
                Client.processPlayerCommand("login w w ww  w w w w"));
        assertTrue(
                "Tries logging in with legit username and password",
                Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertFalse(
                "Doesn't relogin after having logged in",
                Client.processPlayerCommand("login TAPATOP peswerdlmao"));
    }

    @Test
    public void shouldLogoutIfNotLoggedOut() throws IOException{
        Client.processPlayerCommand("logout");
        assertFalse(
                "Doesn't mess up when trying to log out without even having been logged in",
                Client.processPlayerCommand("logout"));

        Client.processPlayerCommand("login TAPATOP peswerdlmao");
        assertTrue(
                "Logs out successfully after having logged in",
                Client.processPlayerCommand("logout"));
        assertFalse(
                "Doesn't mess up when trying to log out after having logged out",
                Client.processPlayerCommand("logout"));
    }

    @Test
    public void shouldBeAbleToLogInAndOutMultipleTimes() throws IOException{
        // self- explanationary, no need for the "message" parameter
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(Client.processPlayerCommand("logout"));
        assertFalse(Client.processPlayerCommand("login wwww peswerqqqdlmao"));
        assertTrue(Client.processPlayerCommand("login hi hi"));
        assertTrue(Client.processPlayerCommand("logout"));
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(Client.processPlayerCommand("logout"));
    }

    @Test
    public void registeringShouldRegisteredUnregisteredAndNotRegisterRegistered() throws IOException{
        assertTrue(
                "Registers an inexistant account properly",
                Client.processPlayerCommand("register username password"));
        assertTrue(
                "Registers another legit account",
                Client.processPlayerCommand("register username2 password"));
        assertFalse(
                "Tries registering an already existant account",
                Client.processPlayerCommand("register username password"));
        assertTrue(
                "Can log in after all of that",
                Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertFalse(
                "Tries registering an account while being logged into another",
                Client.processPlayerCommand("register username3 password"));
        assertFalse(
                "Doesn't mess up while registering an account without giving any parameters",
                Client.processPlayerCommand("register"));
        assertFalse(
                "Doesn't mess up while registering an account while giving only 1 parameter",
                Client.processPlayerCommand("register username"));
        assertFalse(
                "Doesn't mess up while registering an account while giving too many parameters",
                Client.processPlayerCommand("register ay lmao spurdo sparde"));
        assertTrue(
                "Can logout after all of this",
                Client.processPlayerCommand("logout"));

    }

    @Test
    public void shouldNotBlowUpWhenGivenRandomMessage() throws IOException{
        // Logic: If you can send this three times in a row without throwing an IOException
        // and be able to login and logout, then you didn't kill the server
        Client.processPlayerCommand("hello mamma mia");
        Client.processPlayerCommand("hello mamma mia");
        Client.processPlayerCommand("hello mamma mia");

        Client.processPlayerCommand("logout");
        assertTrue(Client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(Client.processPlayerCommand("logout"));
    }

    @Test
    public void shouldBeAbleToCreateLegalGames() throws IOException{
        Client.processPlayerCommand("logout");
        assertFalse(
                "Doesn't create game without having logged in first",
                Client.processPlayerCommand("create_game hi")

        );

        Client.processPlayerCommand("login hi hi");

        assertFalse(
                "Doesn't create a game with invalid input",
                Client.processPlayerCommand("create_game")

        );
        assertFalse(
                "Doesn't create a game with invalid input",
                Client.processPlayerCommand("create_game w q w")

        );
        assertFalse(
                "Doesn't create a game with invalid input",
                Client.processPlayerCommand("create_game wwwwwwwww q")

        );
        assertFalse(
                "Doesn't create a game with invalid input",
                Client.processPlayerCommand("create_game 9hello")

        );
        assertTrue(
                "Creates a game just fine",
                Client.processPlayerCommand("create_game hi")

        );
        assertFalse(
                "Doesn't create a game while being in another one",
                Client.processPlayerCommand("create_game hi")

        );
    }

    @Test
    public void shouldExitGameProperlyWhenAloneInRoom()throws IOException{
        Client.processPlayerCommand("logout");
        Client.processPlayerCommand("login hi hi");

        Client.processPlayerCommand("create_game hi");
        assertTrue(
                "Exits the already created game",
                Client.processPlayerCommand("exit_game")
        );
        assertTrue(
                "Creates a game with the same name, implying the old one was removed",
                Client.processPlayerCommand("create_game hi")
        );
        assertTrue(
                "Exits the new game",
                Client.processPlayerCommand("exit_game")
        );
        assertFalse(
                "Knows you can't exit a room if you're not in one",
                Client.processPlayerCommand("exit_game")
        );
    }
}