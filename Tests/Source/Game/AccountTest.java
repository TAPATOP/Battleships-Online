package Source.Game;

import Source.Account;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class AccountTest {

    private Account acc = null;

    private String readLastLineOfPlayerStatistics() {
        try {
            BufferedReader input = new BufferedReader(new FileReader(acc.getPersonalRecordFilePath()));
            String last = "";
            String line;

            while ((line = input.readLine()) != null) {
                last = line;
            }
            return last;
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        return null;
    }

//    @Before
//    public void setup() {
//        acc = new Account("1_temp", "pass");
//        acc.registerAccount();
//    }
//
//    @After
//    public void tearDown() {
//        File file = new File(acc.getPersonalRecordFilePath());
//        boolean a = file.delete();
//        if (!a) {
//            System.out.println("Something messed up while deleting tempregs");
//        }
//    }

    @Test
    public void shouldUpdateAccountStatistics() throws Exception {
        acc = new Account("1_temp", "pass");
        acc.updateStatistics(5);
        String result = readLastLineOfPlayerStatistics();
        assertEquals("Updates account statistics properly", "5", result);

        acc.updateStatistics(22);
        result = readLastLineOfPlayerStatistics();
        assertEquals("Updates account statistics properly", "22", result);

        acc.updateStatistics(200);
        result = readLastLineOfPlayerStatistics();
        assertEquals("Updates account statistics properly", "200", result);
    }

    @Test
    public void loadStatistics() throws Exception {
        acc = new Account("2_loadStatsAcc", "password");
        int[] result = acc.loadStatistics();
        int[] expected = {2, 3, 4, 1};

        assertTrue("Loads data properly", Arrays.equals(expected, result));
    }

    @Test
    public void exists() throws Exception {
        acc = new Account("1_temp", null);
        assertTrue("Checks if existant account exists", acc.exists());

        acc = new Account("3_fake", null);
        assertFalse("Checks if fake account doesn't exist", acc.exists());
    }

    @Test
    public void loadPassword() throws Exception {
        acc = new Account("3_passwordAcc", "muhpass");
        String result = acc.loadPassword();
        assertTrue("Loads pass properly", result.equals("muhpass"));

        result = acc.loadPassword();
        assertFalse("Knows when a pass isn't true", result.equals("password"));
    }

}