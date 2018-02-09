package Source.Game;

import Source.Account;

public class Player {
    Player(){

    }

    Player(Account acc){
        this.acc = acc;
    }

    public boolean joinAGame(int gameID){
        if(acc.getCurrentGameID() != 0){
            System.out.println("Account is already logged into another game");
            return false;
        }
        acc.setCurrentGameID(gameID);
        return true;
    }

    public GameTable getGameTable(){
        return gameTable;
    }

    private Account acc;
    private GameTable gameTable;
}
