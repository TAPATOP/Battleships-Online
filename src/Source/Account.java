package Source;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Vector;

public class Account {
    // Member variables //
    private String name;
    private String password;
    private int currentGameID;

    private ByteBuffer bufferForCommunicationWithServer = ByteBuffer.allocate(1024);
    private SocketChannel channel;

    private String personalRecordFilePath;

    // Constructors //
    Account(SocketChannel channel) {
        currentGameID = 0;
        this.channel = channel;
    }

    public Account(String name, String password) {
        this.name = name;
        this.password = password;
        personalRecordFilePath = ".\\Accounts\\" + name + ".txt";
        currentGameID = 0;
    }

    // Members //
    public String getName() {
        return name;
    }

    String getPassword() {
        return password;
    }

    public int getCurrentGameID() {
        return currentGameID;
    }

    public String getPersonalRecordFilePath() {
        return personalRecordFilePath;
    }

    SocketChannel getChannel() {
        return channel;
    }

    void setName(String name) {
        this.name = name;
        personalRecordFilePath = ".\\Accounts\\" + name + ".txt";
    }

    void setPassword(String password) {
        this.password = password;
    }

    public void setCurrentGameID(int currentGameID) {
        this.currentGameID = currentGameID;
    }

    public void updateStatistics(int gameID) {
        File f = new File(personalRecordFilePath);
        if(!f.isFile()) {
            System.out.println("Account doesn't exist");
            return;
        }
        try( PrintWriter out = new PrintWriter( new FileOutputStream( new File(personalRecordFilePath), true))  ) {
            out.println(gameID);
            } catch(FileNotFoundException e) {
            System.out.println("Couldn't locate account");
        }
    }

    public int[] loadStatistics() {
        if(!exists()) {
            System.out.println("Account doesn't exist");
            return null;
        }
        Vector<Integer> gameIDs = new Vector<>();

        Scanner scanner;
        try {
            scanner = new Scanner(new File(personalRecordFilePath));

            // skips the password
            scanner.nextLine();
            while(scanner.hasNextInt()) {
                gameIDs.add(scanner.nextInt());
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        int[] gameIDArray = new int[gameIDs.size()];
        int i = 0;
        for(Integer element :
                gameIDs) {
            gameIDArray[i++] = element;
        }

        return gameIDArray;
    }

    void registerAccount() {
        if(exists()) {
            System.out.println("Account already exists");
            return ;
        }
        try( PrintWriter out = new PrintWriter(  new FileOutputStream(new File(personalRecordFilePath)))  ) {
            out.println(password);
        } catch(FileNotFoundException e) {
            System.out.println("Error registering account");
        }
    }

    public boolean exists() {
        File f = new File(personalRecordFilePath);
        return f.isFile();
    }

    public String loadPassword() throws IOException {
        File f = new File(personalRecordFilePath);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        return reader.readLine();
    }

    ByteBuffer getBufferForCommunicationWithServer() {
        return bufferForCommunicationWithServer;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return name.equals(account.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
