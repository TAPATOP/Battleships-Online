package Source.Game;

public class Game {
    public Game(int gameID, Player host){
        player1 = host;
        this.gameID = gameID;
        player1.logIntoAGame(gameID);
    }

    public void addPlayer(Player joinedPlayer){
        joinedPlayer.logIntoAGame(gameID);
    }

    public boolean roomIsFull(){
        return player1 != null && player2 != null;
    }

    public int getGameID() {
        return gameID;
    }

    private Player player1;
    private Player player2;
    private int gameID;
}
