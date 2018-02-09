package Source.Game;

import java.util.Vector;

public class GameTable {
    public GameTable(){
        deployedShipsCount = 0;
        ships.add(new Carrier());

        ships.add(new Battleship());
        ships.add(new Battleship());

        ships.add(new Cruiser());
        ships.add(new Cruiser());
        ships.add(new Cruiser());

        ships.add(new Destroyer());
        ships.add(new Destroyer());
        ships.add(new Destroyer());
        ships.add(new Destroyer());
    }

    /**
     * Used only to inform the players what ship they're about to deploy
     * @return name of ship as String( capitalized)
     */
    public String seeNextShipType(){
        switch(ships.get(deployedShipsCount).getSize()){
            case 2:
                return "Destroyer";
            case 3:
                return "Cruiser";
            case 4:
                return "Battleship";
            case 5:
                return "Aircraft Carrier";
            default:
                return "Unknown ship type??";
        }
    }

    public boolean deployNextShip(int x, int y, boolean isVertical){
        return deployShip(ships.get(deployedShipsCount), x, y, isVertical);
    }

    private boolean deployShip(Ship ship, int x, int y, boolean isVertical){
        int xChange = 0;
        int yChange = 0;
        if(isVertical){
            xChange = 1;
        }
        else{
            yChange = 1;
        }

        if(canDeployShip(ship, x, y, isVertical)){
            for(int i = 0; i < ship.getSize(); i++){
                boardOfDeployments[x][y] = ship;
                x += xChange;
                y += yChange;
            }
            deployedShipsCount++;
            return true;
        }
        return false;
    }

    private boolean canDeployShip(Ship ship, int x, int y, boolean isVertical){
        int xChange = 0;
        int yChange = 0;
        if(isVertical){
            xChange = 1;
        }
        else{
            yChange = 1;
        }
        for(int i = 0; i < ship.getSize(); i++){
            // if null => can be deployed
            if(boardOfDeployments[x][y] == null){
                x += xChange;
                y += yChange;
                continue;
            }
            return false;
        }
        return true;
    }

    public void visualizeBoard(){
        for (Ship[] line:
             boardOfDeployments) {
            for (Ship square :
                    line) {
                System.out.print(visualizeSquare(square));
            }
            System.out.println();
        }
        System.out.println();
    }

    private char visualizeSquare(Ship shipOccupyingTheSuare){
        if(shipOccupyingTheSuare == null){
            return '_';
        }
        if(shipOccupyingTheSuare.getSize() == 0){
            return 'X';
        }
        return 'O';
    }

    // MEMBER VARIABLES
    private Vector<Ship> ships = new Vector<>();
    private Ship[][] boardOfDeployments = new Ship[11][11];
    private int deployedShipsCount;
}
