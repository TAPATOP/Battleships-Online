package Source.Game;

import Source.Account;

public class Player {
    Player(){

    }

    Player(Account acc){
        this.acc = acc;
    }

    public boolean logIntoAGame(int gameID){
        if(acc.getCurrentGameID() != 0){
            System.out.println("Account is already logged into another game");
            return false;
        }
        acc.setCurrentGameID(gameID);
        return true;
    }

    private Account acc;
    private GameTable gameTable;
}
