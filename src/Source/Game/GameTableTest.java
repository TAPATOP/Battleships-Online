package Source.Game;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 #_####____
 #_________
 #_####____
 #_________
 #_________
 __________
 __________
 __________
 __________
 __________
 */
public class GameTableTest {
    @Test
    public void deployNextShipShouldDeployLegalShips() throws Exception {
        char[][] expectedResult = new char[][]{
                {'#', '_', '#', '#', '#', '#', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '#', '#', '#', '#', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };

        GameTable gt = new GameTable();
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);
        gt.deployNextShip("C3", false);
        gt.deployNextShip("F1", true);
        gt.deployNextShip("E1", false);
        assertTrue(
                "Deploys ship properly",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));

        gt.deployNextShip("C4", false);
        assertTrue(
                "Doesn't mess up when trying to deploy a ship that would collide with an already deployed ship",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));

        gt.deployNextShip("E50", false);
        gt.deployNextShip("W5", false);
        assertTrue(
                "Doesn't mess up when given incorrect coordinates",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));
    }

    @Test
    public void shouldProcessFireCommandProperly() throws Exception {
        char[][] expectedResult = new char[][]{
                {'X', 'O', '#', 'X', '#', '#', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '#', '#', '#', 'X', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };

        GameTable gt = new GameTable();
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);
        gt.deployNextShip("C3", false);

        gt.processFireCommand("A2");
        gt.processFireCommand("B1");
        gt.processFireCommand("A1");
        gt.processFireCommand("A4");
        gt.processFireCommand("C6");
        assertTrue(
                "Shots at ships and empty fields are recorded properly",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));

        gt.processFireCommand("C6");
        gt.processFireCommand("C6");
        assertTrue(
                "Doesn't mess up when firing somewhere it has already hit ships at",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));

        gt.processFireCommand("A2");
        gt.processFireCommand("A2");
        assertTrue(
                "Doesn't mess up when firing somewhere it has already missed ships at",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));

        gt.processFireCommand("E50");
        gt.processFireCommand("W500");
        assertTrue(
                "Doesn't mess up when firing at fields that don't really exist",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));
    }

    @Test
    public void shouldReturnProperMessagesWhenFiring(){
        char[][] expectedResult = new char[][]{
                {'#', '_', '#', '#', 'X', '#', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', 'O', 'X', 'X', 'X', 'X', 'O', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'}
        };

        GameTable gt = new GameTable();
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);
        gt.deployNextShip("C3", false);

        GameTable.FireResult result;

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("C2").charAt(0) - 48];
        assertEquals("First miss", result, GameTable.FireResult.MISS);

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("C3").charAt(0) - 48];
        assertEquals("First hit", result, GameTable.FireResult.HIT);

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("C4").charAt(0) - 48];
        assertEquals("Second hit", result, GameTable.FireResult.HIT);

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("C5").charAt(0) - 48];
        assertEquals("Third hit", result, GameTable.FireResult.HIT);

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("C6").charAt(0) - 48];
        assertEquals("Killing hit", result, GameTable.FireResult.DESTROYED);

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("C7").charAt(0) - 48];
        assertEquals("Seconds miss", result, GameTable.FireResult.MISS);

        result = GameTable.FireResult.values()[(int)gt.processFireCommand("A5").charAt(0) - 48];
        assertEquals("Hit on another ship", result, GameTable.FireResult.HIT);

        assertTrue(
                "Board looks like it should",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));
    }
}
