package Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Account {
//    Account(){
//        currentGameID = 0;
//    }

    Account(SocketChannel channel){
        currentGameID = 0;
        this.channel = channel;
    }

    Account(String name, String password){
        this.name = name;
        this.password = password;
        pathName = ".\\Accounts\\" + name + ".txt";
        currentGameID = 0;
    }

    public String getName() {
        return name;
    }

    String getPassword() {
        return password;
    }

    public int getCurrentGameID() {
        return currentGameID;
    }

    String getPathName() {
        return pathName;
    }

    SocketChannel getChannel() {
        return channel;
    }

    void setName(String name) {
        this.name = name;
        pathName = ".\\Accounts\\" + name + ".txt";
    }

    void setPassword(String password) {
        this.password = password;
    }

    public void setCurrentGameID(int currentGameID) {
        this.currentGameID = currentGameID;
    }

    public Errors updateAccountStatistics(int gameID){
        File f = new File(pathName);
        if(!f.isFile()) {
            System.out.println("Account doesn't exist");
            return Errors.CANNOT_LOCATE_ACCOUNT;
        }
        try( PrintWriter out = new PrintWriter( new FileOutputStream( new File(pathName), true))  ){
            out.println(gameID);
            }catch (FileNotFoundException e) {
            System.out.println("Couldn't locate account");
            return Errors.CANNOT_LOCATE_ACCOUNT;
        }
        return Errors.SUCCESS;
    }

    Errors registerAccount() {
        File f = new File(pathName);
        if(f.isFile()) {
            System.out.println("Account already exists");
            return Errors.ACCOUNT_ALREADY_EXISTS;
        }
        try( PrintWriter out = new PrintWriter(  new FileOutputStream(new File(pathName )))  ){
            out.println(password);
        } catch (FileNotFoundException e) {
            System.out.println("Error registering account");
            return Errors.CANNOT_REGISTER_ACCOUNT;
        }
        return Errors.SUCCESS;
    }

    boolean exists(){
        File f = new File(pathName);
        return f.isFile();
    }

    ByteBuffer getBufferForCommunicationWithServer(){
        return bufferForCommunicationWithServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return name.equals(account.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    // MEMBER VARIABLES
    private String name;
    private String password;
    private int currentGameID;
    private ByteBuffer bufferForCommunicationWithServer = ByteBuffer.allocate(1024);
    private SocketChannel channel;

    private String pathName;

    enum Errors{
        SUCCESS,
        CANNOT_REGISTER_ACCOUNT,
        ACCOUNT_ALREADY_EXISTS,
        CANNOT_LOCATE_ACCOUNT
    }
}
