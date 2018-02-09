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

    public boolean deployNextShip(String squareCoordinates, boolean isVertical){
        int[] coords = tranformCoordinatesForReading(squareCoordinates);
        int x = coords[0];
        int y = coords[1];
        return deployShip(ships.get(deployedShipsCount), x, y, isVertical);
    }

    private boolean deployShip(Ship ship, int x, int y, boolean isVertical){
        if(allShipsAreDeployed()){
            System.out.println("All ships are already deployed");
            return false;
        }
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

    boolean allShipsAreDeployed(){
        return deployedShipsCount >= TOTAL_NUMBER_OF_SHIPS;
    }

    private char[][] visualizeBoard(){
        char[][] visualizedBoard = new char[DIMENTION_LIMIT][DIMENTION_LIMIT];

        for (int i = 0; i < DIMENTION_LIMIT; i++){
            for(int j = 0; j < DIMENTION_LIMIT; j++){
                visualizedBoard[i][j] = visualizeSquare(boardOfDeployments[i][j]);
            }
        }
        return visualizedBoard;
    }

    public void stylizeAndPrintBoard(char[][] visualizedBoard ){
        System.out.print("/|");
        for(int i = 1; i <= DIMENTION_LIMIT; i++){
            System.out.print(i + "|");
        }
        System.out.println();

        for(int i = 0; i < DIMENTION_LIMIT; i++){
            System.out.print((char)(i + 65) + "|");
            for (char c :
                    visualizedBoard[i]) {
                System.out.print(c + "|");
            }
            System.out.println();
        }
    }

    public void stylizeAndPrintBoard(){
        char[][] visualizedBoard = visualizeBoard();
        stylizeAndPrintBoard(visualizedBoard);
    }

    private char visualizeSquare(Ship shipOccupyingTheSquare){
        if(shipOccupyingTheSquare == null){
            return '_';
        }
        switch(shipOccupyingTheSquare.getSize()) {
            case -1:
                return 'X';
            case -2:
                return 'O';
            default:
                return '#';
        }
    }

    public boolean processFireCommand(String squareCoordinates){
        int[] coords = tranformCoordinatesForReading(squareCoordinates);
        int x = coords[0];
        int y = coords[1];
        try {
            if (!coordinatesAreValid(x, y)) {
                return false;
            }
            if (boardOfDeployments[x][y] == null) {
                System.out.println("Miss!");
                boardOfDeployments[x][y] = missedShip;
                return true;
            }
            if (boardOfDeployments[x][y].getSize() < 0) {
                System.out.println("Can't fire there");
                return false;
            }
            boardOfDeployments[x][y].takeOneHit();
            boardOfDeployments[x][y] = damagedShip;

            return true;
        }catch(NullPointerException exc){
            System.out.println("Something messed up with firing at targets; NULLPTR");
            return false;
        }
    }

    /**
     * Transforms coordinates of the [A-J][1-10] format to [0-9][0-9] format. Using this
     * method implies the coordinates have been validated to the said initial format.
     * @param squareCoordinates [A-J][1-10] format
     * @return returns an int[2] array, where arr[0] is x and arr[1] is y;
     */
    private int[] tranformCoordinatesForReading(String squareCoordinates){
        char x = squareCoordinates.toUpperCase().charAt(0);
        String digitsOfCoords = squareCoordinates.substring(1, squareCoordinates.length());

        int y = Integer.parseInt(digitsOfCoords);
        int[] transformedCoords = new int[2];
        transformedCoords[0] = x - 'A';
        transformedCoords[1] = y - 1;

        return transformedCoords;
    }


    /**
     * Checks if the given coordinates actually fit on the GameTable.
     * !! WARNING !!
     * Both x and y need to be reduced to the [0; DIMENTION_LIMIT] interval before being passed on;
     * e.g.: If the input coordinates are [1;6], they should be passed to the class as [0;5]
     * @param x height depth
     * @param y width depth
     * @return returns if firing at given coordinates is possible/ meaningful; firing at a position
     * you've already fired will return false
     */
    private boolean coordinatesAreValid(int x, int y){
        return (x < DIMENTION_LIMIT && x >= 0 && y < DIMENTION_LIMIT && y >= 0);
    }

    // MEMBER VARIABLES
    private final int TOTAL_NUMBER_OF_SHIPS = 10;
    private final int DIMENTION_LIMIT = 10;
    private Vector<Ship> ships = new Vector<>();
    private Ship[][] boardOfDeployments = new Ship[DIMENTION_LIMIT][DIMENTION_LIMIT];
    private int deployedShipsCount;

    // LITERALLY TRASH
    DamagedPartOfShip damagedShip = new DamagedPartOfShip();
    MissedShip missedShip = new MissedShip();
}
