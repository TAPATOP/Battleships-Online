import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Account {
    Account(){

    }

    Account(String name, String password){
        this.name = name;
        this.password = password;
        pathName = ".\\Accounts\\" + name + ".txt";
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public int getCurrentGameID() {
        return currentGameID;
    }

    void setName(String name) {
        this.name = name;
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

    public Errors registerAccount() {
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

    private String name;
    private String password;
    private int currentGameID;

    private String pathName;

    enum Errors{
        SUCCESS,
        CANNOT_REGISTER_ACCOUNT,
        ACCOUNT_ALREADY_EXISTS,
        CANNOT_LOCATE_ACCOUNT
    }
}
