package Source.Game;

import Source.Account;

public class Game {
    /**
     * Game constructor
     * @param gameID the corresponding ID of the game according to the server
     * @param host player that requested the creation of the game; he is
     *             automatically logged into the game
     */
    public Game(String gameName, int gameID, Player host){
        player1 = host;
        this.gameID = gameID;
        this.gameName = gameName;
    }

    public void addPlayer(Player joinedPlayer){
        joinedPlayer.joinAGame(gameID);
        player2 = joinedPlayer;
    }

    public boolean roomIsFull(){
        return (player1 != null && player2 != null);
    }

    public boolean isInDeploymentPhase(){
        if(player2 == null){
            return false;
        }
        return !(player1.getGameTable().allShipsAreDeployed() && player2.getGameTable().allShipsAreDeployed());
    }

    public Player getOtherPlayer(Player playerWeAlreadyKnow){
        if(player1.equals(playerWeAlreadyKnow)){
            return player2;
        }
        return player1;
    }

    /**
     * Compares an Account with a Player. This works because of how Player's .equals() method
     * is implemented
     * @param accountOfPlayerWeAlreadyKnow Player of whom we wish to learn the opponent
     * @return returns the opponent of the account we already know about
     */
    public Player getOtherPlayer(Account accountOfPlayerWeAlreadyKnow){
        Player playerWeAlreadyKnow = new Player(accountOfPlayerWeAlreadyKnow);
        if(player1.equals(playerWeAlreadyKnow)){
            return player2;
        }
        return player1;
    }


//    /**
//     * Returns an array containing two ESMs. Use this when a game should finish,
//     * which is either when it ends naturally by one of the players winning or by
//     * being terminated due to one player exiting prematurely
//     * @return contains an array of two ESMs- [0] is for the host and [1] is for the guest
//     */
    public void end(){
        player1.removeFromGame();
        if(player2 != null){
            player2.removeFromGame();
        }
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    private Player player1;
    private Player player2;
    private int gameID;
    private String gameName;
}
