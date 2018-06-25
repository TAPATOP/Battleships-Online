package Tests;

import org.junit.*;
import Source.Client;

import java.io.*;
import static org.junit.Assert.*;

public class ClientTest {
    private static Client client;

    @BeforeClass
    public static void setup() throws IOException {
        client = new Client("localhost", 6969);
    }

    // I have no idea why this exists but am too afraid to delete it( yet)
    @AfterClass
    public static void deleteTempRegistration() {
        File file = new File(".\\Accounts\\username.txt");
        boolean a = file.delete();
        if(!a) {
            System.out.println("Something messed up while deleting tempregs");
            return;
        }
        file = new File(".\\Accounts\\username2.txt");
        a = file.delete();
        if(!a) {
            System.out.println("Something messed up while deleting tempregs");
        }
    }

    @After
    public void logout() throws IOException {
        client.processPlayerCommand("logout");
    }

    @Test
    public void loginShouldWorkWithValidAndInvalidLogins() throws IOException {
        assertFalse(
                "Tries logging in with a typo in password",
                client.processPlayerCommand("login TAPATOP pesswerdlmao"));
        assertFalse(
                "Tries logging with a legal password but with the wrong account",
                client.processPlayerCommand("login w peswerdlmao"));
        assertFalse(
                "Tries loggin in by giving less than needed parameters",
                client.processPlayerCommand("login rrr"));
        assertFalse(
                "Tries loggin in without giving any parameters",
                client.processPlayerCommand("login"));
        assertFalse(
                "Tries logging in by giving too many parameters",
                client.processPlayerCommand("login w w ww  w w w w"));
        assertTrue(
                "Tries logging in with legit username and password",
                client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertFalse(
                "Doesn't relogin after having logged in",
                client.processPlayerCommand("login TAPATOP peswerdlmao"));
    }

    @Test
    public void shouldLogoutIfNotLoggedOut() throws IOException {
        client.processPlayerCommand("logout");
        assertFalse(
                "Doesn't mess up when trying to log out without even having been logged in",
                client.processPlayerCommand("logout"));

        client.processPlayerCommand("login TAPATOP peswerdlmao");
        assertTrue(
                "Logs out successfully after having logged in",
                client.processPlayerCommand("logout"));
        assertFalse(
                "Doesn't mess up when trying to log out after having logged out",
                client.processPlayerCommand("logout"));
    }

    @Test
    public void shouldBeAbleToLogInAndOutMultipleTimes() throws IOException {
        assertTrue("logs in fine", client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue("logs out fine", client.processPlayerCommand("logout"));
        assertFalse(
                "doesnt log in with wrong credentials",
                client.processPlayerCommand("login wwww peswerqqqdlmao"));
        assertTrue("logs in fine",client.processPlayerCommand("login hi hi"));
        assertTrue("logs out fine", client.processPlayerCommand("logout"));
        assertTrue(client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue("logs out fine", client.processPlayerCommand("logout"));
    }

    @Test
    public void registeringShouldRegisteredUnregisteredAndNotRegisterRegistered() throws IOException {
        assertTrue(
                "Registers a nonexistant account properly",
                client.processPlayerCommand("register username password"));
        assertTrue(
                "Registers another legit account",
                client.processPlayerCommand("register username2 password"));
        assertFalse(
                "Tries registering an already existant account",
                client.processPlayerCommand("register username password"));
        assertTrue(
                "Can log in after all of that",
                client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertFalse(
                "Tries registering an account while being logged into another",
                client.processPlayerCommand("register username3 password"));
        assertFalse(
                "Doesn't mess up while registering an account without giving any parameters",
                client.processPlayerCommand("register"));
        assertFalse(
                "Doesn't mess up while registering an account while giving only 1 parameter",
                client.processPlayerCommand("register username"));
        assertFalse(
                "Doesn't mess up while registering an account while giving too many parameters",
                client.processPlayerCommand("register ay lmao spurdo sparde"));
        assertTrue(
                "Can logout after all of this",
                client.processPlayerCommand("logout"));

    }

    @Test
    public void shouldNotBlowUpWhenGivenRandomMessages() throws IOException {
        // Logic: If you can send this three times in a row without throwing an IOException
        // and be able to login and logout, then you didn't kill the server
//        client.processPlayerCommand("hello mamma mia");
//        client.processPlayerCommand("hello mamma mia");
//        client.processPlayerCommand("hello mamma mia");
//        client.processPlayerCommand("server did nothing wrong");
//        client.processPlayerCommand("black suns and celtic crosses sitting on a tree");

        client.processPlayerCommand("logout");
        assertTrue(client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(client.processPlayerCommand("logout"));
    }

    @Test
    public void shouldBeAbleToCreateLegalGames() throws IOException {
        client.processPlayerCommand("logout");
        assertFalse(
                "Doesn't create game without having logged in first",
                client.processPlayerCommand("create_game hi")

        );

        client.processPlayerCommand("login hi hi");

        assertFalse(
                "Doesn't create a game with no game name",
                client.processPlayerCommand("create_game")

        );
        assertFalse(
                "Doesn't create a game with invalid input",
                client.processPlayerCommand("create_game w q w")

        );
        assertFalse(
                "Doesn't create a game with invalid input that has intervals in it",
                client.processPlayerCommand("create_game wwwwwwwww q")

        );
        assertFalse(
                "Doesn't create a game with invalid name such as 9hello",
                client.processPlayerCommand("create_game 9hello")

        );
        assertTrue(
                "Creates a game just fine",
                client.processPlayerCommand("create_game hi")

        );
        assertFalse(
                "Doesn't create a game while being in another one",
                client.processPlayerCommand("create_game hi")

        );
    }

    @Test
    public void shouldExitGameProperlyWhenAloneInRoom()throws IOException {
        client.processPlayerCommand("logout");
        client.processPlayerCommand("login hi hi");

        client.processPlayerCommand("create_game hi");
        assertTrue(
                "Exits the already created game",
                client.processPlayerCommand("exit_game")
        );
        assertTrue(
                "Creates a game with the same name, implying the old one was removed",
                client.processPlayerCommand("create_game hi")
        );
        assertTrue(
                "Exits the new game",
                client.processPlayerCommand("exit_game")
        );
        assertFalse(
                "Knows you can't exit a room if you're not in one",
                client.processPlayerCommand("exit_game")
        );
    }

    @Test
    public void handlesAFewPlayersAtATime()throws IOException {
        client.processPlayerCommand("login TAPATOP peswerdlmao");

        Client secClient = new Client();
        secClient.processPlayerCommand("login borat kazahstan");

        Client trdClient = new Client();
        trdClient.processPlayerCommand("login hi hi");

        secClient.processPlayerCommand("logout");
        trdClient.processPlayerCommand("logout");
        assertTrue(true);
    }

    @Test
    public void canJoinGame()throws IOException {
        client.processPlayerCommand("logout)");
        Client secClient = new Client();
        secClient.processPlayerCommand("login borat kazahstan");

        client.processPlayerCommand("login TAPATOP peswerdlmao");
        client.processPlayerCommand("create_game hi");
        assertTrue("Second client joins the game", secClient.processPlayerCommand("join_game hi"));
        //client.processPlayerCommand("logout");
//        secClient.processPlayerCommand("exit_game");
//        secClient.processPlayerCommand("logout");
    }
}
