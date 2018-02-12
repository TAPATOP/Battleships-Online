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

    public boolean addPlayer(Player joinedPlayer){
        boolean joinSuccessful = joinedPlayer.joinAGame(gameID);
        if(!joinSuccessful){
            return false;
        }
        player2 = joinedPlayer;

        player1.getAccount().updateAccountStatistics(gameID);
        player2.getAccount().updateAccountStatistics(gameID);
        playerInTurn = player1;
        System.out.println("Deployment phase for game " + gameName + " has just started!");
        return true;
    }

    public boolean isInDeploymentPhase() {
        return player2 != null && !(player1.getGameTable().allShipsAreDeployed() && player2.getGameTable().allShipsAreDeployed());
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


    /**
     * Use this when a game should finish, which is either when it ends naturally by
     * one of the players winning or by being terminated due to one player
     * exiting prematurely
     */
    public void end(){
        player1.removeFromGame();
        if(player2 != null){
            player2.removeFromGame();
        }
    }

    public EnumStringMessage deployShip(Player owner, String coordinates, boolean isVertical){
        return owner.getGameTable().deployNextShip(coordinates, isVertical);
    }

    public EnumStringMessage executeFiring(Player attacker, String coordinates){
        if(gameOver){
            return new EnumStringMessage(
                    GameTable.FireResult.INVALID,
                    "Game is over, no need to keep firing..."
            );
        }

        if(!attacker.equals(playerInTurn)){
            return new EnumStringMessage(
                    GameTable.FireResult.INVALID,
                    "It's not your turn to fire yet"
            );
        }

        EnumStringMessage result = getOtherPlayer(attacker).getGameTable().fireAt(coordinates);
        boolean firingWasLegal = !(result.getEnumValue().equals(GameTable.FireResult.INVALID));
        if(firingWasLegal){
            switchTurns();
            if(result.getEnumValue().equals(GameTable.FireResult.DESTROYED_LAST_SHIP)){
                gameOver = true;
            }
        }

        return result;
    }

    private void switchTurns(){
        if(player1.equals(playerInTurn)){
            playerInTurn = player2;
        } else{
            playerInTurn = player1;
        }
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public Player getPlayerByAccount(Account acc){
        if(player1.getAccount().equals(acc)){
            return player1;
        }
        if(player2.getAccount().equals(acc)){
            return player2;
        }
        return null;
    }

    private Player player1;
    private Player player2;
    private Player playerInTurn;
    private int gameID;
    private String gameName;
    private boolean gameOver = false;
}
