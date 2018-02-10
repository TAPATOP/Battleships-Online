package Tests;

import Source.Game.GameTable;
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
        assertEquals(
                "Deploys ship properly",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.deployNextShip("C4", false);
        assertEquals(
                "Doesn't mess up when trying to deploy a ship that would collide with an already deployed ship",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.deployNextShip("E50", false);
        gt.deployNextShip("W5", false);
        assertEquals(
                "Doesn't mess up when given incorrect coordinates",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));
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
        assertEquals(
                "Shots at ships and empty fields are recorded properly",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.processFireCommand("C6");
        gt.processFireCommand("C6");
        assertEquals(
                "Doesn't mess up when firing somewhere it has already hit ships at",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.processFireCommand("A2");
        gt.processFireCommand("A2");
        assertEquals(
                "Doesn't mess up when firing somewhere it has already missed ships at",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.processFireCommand("E50");
        gt.processFireCommand("W500");
        assertEquals(
                "Doesn't mess up when firing at fields that don't really exist",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));
    }

    @Test
    public void shouldReturnProperMessagesWhenFiring(){
        char[][] expectedResult = new char[][]{
                {'X', 'O', 'X', 'X', 'X', 'X', 'O', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
                {'X', '_', '_', '_', '_', '_', '_', '_', '_' ,'_'},
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
        assertEquals("First miss", GameTable.FireResult.MISS, result);

        result = (GameTable.FireResult)gt.processFireCommand("A3").getEnumValue();
        assertEquals("First hit", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("A4").getEnumValue();
        assertEquals("Second hit", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("A5").getEnumValue();
        assertEquals("Third hit", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("A6").getEnumValue();
        assertEquals("Killing hit", GameTable.FireResult.DESTROYED, result);

        result = (GameTable.FireResult)gt.processFireCommand("A7").getEnumValue();
        assertEquals("Seconds miss", GameTable.FireResult.MISS, result);

        result = (GameTable.FireResult)gt.processFireCommand("A3").getEnumValue();
        assertEquals("Hit on another ship", GameTable.FireResult.INVALID, result);

        result = (GameTable.FireResult)gt.processFireCommand("C1").getEnumValue();
        assertEquals("Hit on another ship", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("A1").getEnumValue();
        assertEquals("Can fire at the first deployed ship 1", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("B1").getEnumValue();
        assertEquals("Can fire at the first deployed ship 1", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("D1").getEnumValue();
        assertEquals("Can fire at the first deployed ship 1", GameTable.FireResult.HIT, result);

        result = (GameTable.FireResult)gt.processFireCommand("E1").getEnumValue();
        assertEquals("Recognizes win condition", GameTable.FireResult.DESTROYED_LAST_SHIP, result);

        result = (GameTable.FireResult)gt.processFireCommand("J10").getEnumValue();
        assertEquals("Can fire at the corner of the map", GameTable.FireResult.MISS, result);

        result = (GameTable.FireResult)gt.processFireCommand("J10").getEnumValue();
        assertEquals("Can not fire at the corner of the map again", GameTable.FireResult.INVALID, result);

        result = (GameTable.FireResult)gt.processFireCommand("69 XD").getEnumValue();
        assertEquals("Passing gibberish as coordinates 1", GameTable.FireResult.INVALID, result);

        result = (GameTable.FireResult)gt.processFireCommand("6").getEnumValue();
        assertEquals("Passing gibberish as coordinates 2", GameTable.FireResult.INVALID, result);

        result = (GameTable.FireResult)gt.processFireCommand("B").getEnumValue();
        assertEquals("Passing gibberish as coordinates 3", GameTable.FireResult.INVALID, result);

        result = (GameTable.FireResult)gt.processFireCommand("WS").getEnumValue();
        assertEquals("Passing gibberish as coordinates", GameTable.FireResult.INVALID, result);

        assertEquals(
                "Board looks like it should",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));
    }
}
