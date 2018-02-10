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
                {'_', '_', '_', '_', '_', '_', '_', '#', '#' ,'#'}
        };

        GameTable gt = new GameTable();
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);
        gt.deployNextShip("C3", false);
        gt.deployNextShip("F1", true);
        gt.deployNextShip("E1", false);
        gt.deployNextShip("J10", false);
        gt.deployNextShip("J8", false);
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
                {'#', 'O', 'X', 'X', 'X', 'X', 'O', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'#', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'_', '_', '_', '_', '_', '_', '_', '_', '_' ,'O'}
        };

        GameTable gt = new GameTable();
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);

        GameTable.FireResult result;

        result = (GameTable.FireResult)gt.processFireCommand("A2").getEnumValue();
        assertEquals("First miss", result, GameTable.FireResult.MISS);

        result = (GameTable.FireResult)gt.processFireCommand("A3").getEnumValue();
        assertEquals("First hit", result, GameTable.FireResult.HIT);

        result = (GameTable.FireResult)gt.processFireCommand("A4").getEnumValue();
        assertEquals("Second hit", result, GameTable.FireResult.HIT);

        result = (GameTable.FireResult)gt.processFireCommand("A5").getEnumValue();
        assertEquals("Third hit", result, GameTable.FireResult.HIT);

        result = (GameTable.FireResult)gt.processFireCommand("A6").getEnumValue();
        assertEquals("Killing hit", result, GameTable.FireResult.DESTROYED);

        result = (GameTable.FireResult)gt.processFireCommand("A7").getEnumValue();
        assertEquals("Seconds miss", result, GameTable.FireResult.MISS);

        result = (GameTable.FireResult)gt.processFireCommand("A3").getEnumValue();
        assertEquals("Hit on another ship", result, GameTable.FireResult.INVALID);

        result = (GameTable.FireResult)gt.processFireCommand("C1").getEnumValue();
        assertEquals("Hit on another ship", result, GameTable.FireResult.HIT);

        result = (GameTable.FireResult)gt.processFireCommand("69 XD").getEnumValue();
        assertEquals("Passing gibberish as coordinates 1", result, GameTable.FireResult.INVALID);

        result = (GameTable.FireResult)gt.processFireCommand("6").getEnumValue();
        assertEquals("Passing gibberish as coordinates 2", result, GameTable.FireResult.INVALID);

        result = (GameTable.FireResult)gt.processFireCommand("B").getEnumValue();
        assertEquals("Passing gibberish as coordinates 3", result, GameTable.FireResult.INVALID);

        result = (GameTable.FireResult)gt.processFireCommand("WS").getEnumValue();
        assertEquals("Passing gibberish as coordinates", result, GameTable.FireResult.INVALID);

        result = (GameTable.FireResult)gt.processFireCommand("J10").getEnumValue();
        assertEquals("Can fire at the corner of the map", result, GameTable.FireResult.MISS);

        assertTrue(
                "Board looks like it should",
                (Arrays.deepToString(gt.visualizeBoard()).equals(Arrays.deepToString(expectedResult))));
    }
}
