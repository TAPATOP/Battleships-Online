package Source.Game;

import Source.Account;

public class Player {
    // Member variables //
    private Account acc;
    private GameTable gameTable;

    // Constructors //
    public Player(Account acc) {
        this.acc = acc;
        gameTable = new GameTable();
    }

    // Methods //
    public boolean joinAGame(int gameID) {
        if(acc.getCurrentGameID() != 0) {
            System.out.println("Account is already logged into another game");
            return false;
        }
        acc.setCurrentGameID(gameID);
        return true;
    }

    public GameTable getGameTable() {
        return gameTable;
    }

    void removeFromGame() {
        acc.setCurrentGameID(0);
    }

    public Account getAccount() {
        return acc;
    }

    public String getName() {
        return acc.getName();
    }

    void updateStatistics(int gameID) {
        acc.updateStatistics(gameID);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return acc.equals(player.acc);
    }

    @Override
    public int hashCode() {
        return acc.hashCode();
    }
}
