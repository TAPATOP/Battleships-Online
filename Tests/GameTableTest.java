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

        GameTable gt = new GameTable(7);
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

        GameTable gt = new GameTable(3);
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);
        gt.deployNextShip("C3", false);

        gt.recordShotAt("A2");
        gt.recordShotAt("B1");
        gt.recordShotAt("A1");
        gt.recordShotAt("A4");
        gt.recordShotAt("C6");
        assertEquals(
                "Shots at ships and empty fields are recorded properly",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.recordShotAt("C6");
        gt.recordShotAt("C6");
        assertEquals(
                "Doesn't mess up when firing somewhere it has already hit ships at",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.recordShotAt("A2");
        gt.recordShotAt("A2");
        assertEquals(
                "Doesn't mess up when firing somewhere it has already missed ships at",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));

        gt.recordShotAt("E50");
        gt.recordShotAt("W500");
        assertEquals(
                "Doesn't mess up when firing at fields that don't really exist",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));
    }

    @Test
    public void shouldReturnProperMessagesWhenFiring() {
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

        GameTable gt = new GameTable(2);
        gt.deployNextShip("A1", true);
        gt.deployNextShip("A3", false);

        GameTable.FireResult resultOfFire;

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A2").getEnumValue();
        assertEquals("First miss", GameTable.FireResult.MISS, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A3").getEnumValue();
        assertEquals("First hit", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A4").getEnumValue();
        assertEquals("Second hit", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A5").getEnumValue();
        assertEquals("Third hit", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A6").getEnumValue();
        assertEquals("Killing hit", GameTable.FireResult.DESTROYED, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A7").getEnumValue();
        assertEquals("Seconds miss", GameTable.FireResult.MISS, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A3").getEnumValue();
        assertEquals("Hit on another ship", GameTable.FireResult.INVALID, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("C1").getEnumValue();
        assertEquals("Hit on another ship", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("A1").getEnumValue();
        assertEquals("Can fire at the first deployed ship 1", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("B1").getEnumValue();
        assertEquals("Can fire at the first deployed ship 1", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("D1").getEnumValue();
        assertEquals("Can fire at the first deployed ship 1", GameTable.FireResult.HIT, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("E1").getEnumValue();
        assertEquals("Recognizes win condition", GameTable.FireResult.DESTROYED_LAST_SHIP, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("J10").getEnumValue();
        assertEquals("Can fire at the corner of the map", GameTable.FireResult.MISS, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("J10").getEnumValue();
        assertEquals("Can not fire at the corner of the map again", GameTable.FireResult.INVALID, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("69 XD").getEnumValue();
        assertEquals("Passing gibberish as coordinates 1", GameTable.FireResult.INVALID, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("6").getEnumValue();
        assertEquals("Passing gibberish as coordinates 2", GameTable.FireResult.INVALID, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("B").getEnumValue();
        assertEquals("Passing gibberish as coordinates 3", GameTable.FireResult.INVALID, resultOfFire);

        resultOfFire = (GameTable.FireResult)gt.recordShotAt("WS").getEnumValue();
        assertEquals("Passing gibberish as coordinates 4", GameTable.FireResult.INVALID, resultOfFire);


        resultOfFire = (GameTable.FireResult)gt.recordShotAt("B4 54").getEnumValue();
        assertEquals("Passing gibberish as coordinates 5", GameTable.FireResult.INVALID, resultOfFire);

        assertEquals(
                "Board looks like it should",
                Arrays.deepToString(expectedResult),
                (Arrays.deepToString(gt.visualizeBoard())));
    }
}
