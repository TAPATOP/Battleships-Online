package Tests;

import Source.Game.GameTable;
import org.junit.*;
import Source.Client;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ClientTest {
    private static Client client;
    private static Client secondClient;
    private static Client thirdClient;

    @BeforeClass
    public static void setup() throws IOException {
        client = new Client("localhost", 6969);
        secondClient = new Client("localhost", 6969);
        thirdClient = new Client("localhost", 6969);
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
        secondClient.processPlayerCommand("logout");
        thirdClient.processPlayerCommand("logoout");
    }
    
    private void setupAStandardGame() throws IOException {
        /*
        char[][] expectedClientResult = new char[][]{
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '#', '#', '#' ,'#'}
        };
        char[][] expectedSecondClientResult = new char[][]{
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '#', '#', '#', '#', '#', '#', '#', '#' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };
        */
        
        client.processPlayerCommand("login TAPATOP peswerdlmao");
        client.processPlayerCommand("create_game hi");
        secondClient.processPlayerCommand("login borat kazahstan");
        secondClient.processPlayerCommand("join_game hi");

        client.processPlayerCommand("deploy vA1");
        client.processPlayerCommand("deploy hJ7");
        secondClient.processPlayerCommand("deploy hD5");
        secondClient.processPlayerCommand("deploy hD1");
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
        client.processPlayerCommand("hello mamma mia");
        client.processPlayerCommand("hello mamma mia");
        client.processPlayerCommand("hello mamma mia");
        client.processPlayerCommand("server did nothing wrong");
        client.processPlayerCommand("black suns and celtic crosses sitting on a tree");

        client.processPlayerCommand("logout");
        assertTrue(client.processPlayerCommand("login TAPATOP peswerdlmao"));
        assertTrue(client.processPlayerCommand("logout"));
    }

    @Test
    public void shouldBeAbleToCreateLegalGames() throws IOException {
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
        assertTrue(
                "A second account can log in",
                secondClient.processPlayerCommand("login borat kazahstan")
        );
        assertTrue(
                "A third account ccan log in",
                thirdClient.processPlayerCommand("login hi hi")
        );

        assertTrue(
                "Second account can log out",
                secondClient.processPlayerCommand("logout")
        );
        assertTrue(
                "First client can log out",
                client.processPlayerCommand("logout")
        );
        assertTrue(
                "Third client can log out",
                thirdClient.processPlayerCommand("logout")
        );
    }

    @Test
    public void canJoinGame()throws IOException {
        secondClient.processPlayerCommand("login borat kazahstan");

        client.processPlayerCommand("login TAPATOP peswerdlmao");
        client.processPlayerCommand("create_game hi");
        assertTrue("Second client joins the game", secondClient.processPlayerCommand("join_game hi"));
        secondClient.processPlayerCommand("logout");
    }

    @Test
    public void canDeployShips() throws IOException{
        client.processPlayerCommand("login TAPATOP peswerdlmao");
        client.processPlayerCommand("create_game hi");
        secondClient.processPlayerCommand("login borat kazahstan");
        secondClient.processPlayerCommand("join_game hi");

        // keep in mind these are made for games with a max of 2 ships per side, cause im a lazy ****
        assertTrue(
                "Player1 can deploy a ship vertically at A1 without colliding with anything",
                client.processPlayerCommand("deploy vA1")
        );
        assertFalse("Player1 can't deploy a ship if it collides with another ship",
                client.processPlayerCommand("deploy hB1")
        );
        assertFalse("Player1 can't deploy a ship if it collides with the edges of the map",
                client.processPlayerCommand("deploy hA10")
        );
        assertFalse("Player1 can't deploy a ship if it collides with the edges of the map",
                client.processPlayerCommand("deploy hA9")
        );
        assertTrue("Player1 can deploy a ship in the lower corners of the map",
                client.processPlayerCommand("deploy hJ7")
        );

        assertFalse("Player2 can't deploy a ship horizontally at the edge of the map",
                secondClient.processPlayerCommand("deploy hJ10")
        );
        assertFalse("Player2 can't deploy a ship vertically at the edge of the map",
                secondClient.processPlayerCommand("deploy vJ10")
        );
        assertTrue(
                "Player2 can deploy a ship in the middle of the map",
                secondClient.processPlayerCommand("deploy hD5")
        );
        assertFalse("Player2 can't deploy a ship if it's tip is onto another ship",
                secondClient.processPlayerCommand("deploy hD2")
        );
        assertTrue("Player2 can deploy a ship if it's tip is next to another ship's",
                secondClient.processPlayerCommand("deploy hD1")
        );
    }

    @Test
    public void battleRoomShouldBeImmuneToSpam() throws IOException {
        client.processPlayerCommand("login TAPATOP peswerdlmao");
        client.processPlayerCommand("create_game hi");
        secondClient.processPlayerCommand("login borat kazahstan");
        secondClient.processPlayerCommand("join_game hi");

        client.processPlayerCommand("deploy vA1");
        client.processPlayerCommand("deploy hJ7");
        secondClient.processPlayerCommand("deploy hD5");
        secondClient.processPlayerCommand("deploy hD1");

        client.processPlayerCommand("weqweqweqw ad w ");
        client.processPlayerCommand("@!w ad w ");
        client.processPlayerCommand("");
        assertFalse(
                "It's not the other player's turn even after the first one spamming",
                secondClient.processPlayerCommand("fire A1")
        );
        assertTrue(
                "It's still player's turn to fire even after spamming",
                client.processPlayerCommand("fire A1")
        );
    }

    //@Test
    public void canFireAtShipsAndReceiveDamageAndMiss() throws IOException {
        char[][] expectedClientOwnTable = new char[][]{
                {'#', 'O', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '#', '#', '#' ,'#'}
        };

        char[][] expectedClientOpponentTable = new char[][]{
                {'O', '_', '_', '_', 'O', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };

        char[][] expectedSecondClientOwnTable = new char[][]{
                {'O', '_', '_', '_', 'O', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '#', '#', '#', '#', '#', '#', '#', '#' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };

        char[][] expectedSecondClientOpponentTable = new char[][]{
                {'_', 'O', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };
        
        setupAStandardGame();
        client.processPlayerCommand("fire A1");
        secondClient.processPlayerCommand("fire A2");
        client.processPlayerCommand("fire A1");
        client.processPlayerCommand("fire A5");
        secondClient.processPlayerCommand("fire B1");
//        client.processPlayerCommand("fire D2");
//        secondClient.processPlayerCommand("fire B1");
//        secondClient.processPlayerCommand("fire lol");
//        secondClient.processPlayerCommand("fire C1");
//        client.processPlayerCommand("fire D5");
//        secondClient.processPlayerCommand("fire H8");

//        client.thisPlayerGameTable.stylizeAndPrintMatrix();
//        client.opponentGameTable.stylizeAndPrintMatrix();
//        secondClient.thisPlayerGameTable.stylizeAndPrintMatrix();
//        secondClient.opponentGameTable.stylizeAndPrintMatrix();

        client.processPlayerCommand("hi");
        secondClient.processPlayerCommand("hi");
        assertTrue(
                "All of Player1's shots were recorder properly",
                Arrays.deepEquals(expectedClientOwnTable, client.exportOwnGameTable())
        );
        assertTrue(
                "All of Player1's opponent shots were recorder properly",
                Arrays.deepEquals(expectedClientOpponentTable, client.exportOpponentGameTable())
        );
        assertTrue(
                "All of Player2's shots were recorder properly",
                Arrays.deepEquals(expectedSecondClientOwnTable, secondClient.exportOwnGameTable())
        );
        assertTrue(
                "All of Player2's opponent shots were recorder properly",
                Arrays.deepEquals(expectedSecondClientOpponentTable, secondClient.exportOpponentGameTable())
        );
    }
}
