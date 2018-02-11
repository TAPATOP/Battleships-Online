package Source.Game;

import Source.Account;

import java.nio.channels.SocketChannel;

public class Player {
    public Player(Account acc, SocketChannel channel){
        this.acc = acc;
        gameTable = new GameTable();
    }

    /**
     * Used when comparing an Account with a Player
     * @param acc account we already know
     */
    public Player(Account acc){
        this.acc = acc;
        gameTable = new GameTable();
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

    public void removeFromGame(){
        acc.setCurrentGameID(0);
    }

    public Account getAccount(){
        return acc;
    }

    public String getName(){
        return acc.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return acc.equals(player.acc);
    }

    @Override
    public int hashCode() {
        return acc.hashCode();
    }

    private Account acc;
    private GameTable gameTable;
}
