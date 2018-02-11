package Source.Game;

import java.util.Vector;

public class GameTable {
    public GameTable(){
        deployedShipsCount = 0;
        allShips.add(new Carrier());

        allShips.add(new Battleship());
        allShips.add(new Battleship());

        allShips.add(new Cruiser());
        allShips.add(new Cruiser());
        allShips.add(new Cruiser());

        allShips.add(new Destroyer());
        allShips.add(new Destroyer());
        allShips.add(new Destroyer());
        allShips.add(new Destroyer());
    }

    /**
     * Used only to inform the players what ship they're about to deploy
     * @return name of ship as String( capitalized)
     */
    public String seeNextShipType(){
        return getShipType(allShips.get(deployedShipsCount)).toString();
    }

    /**
     * Deploys the next ship in line
     * @param squareCoordinates the coordinates of the ship in the format: [A-J][1-10]
     * @param isVertical whether the ship is vertically deployed or not
     * @return returns true if the ship was successfully deployed
     */
    public EnumStringMessage deployNextShip(String squareCoordinates, boolean isVertical){
        int[] coords = tranformCoordinatesForReading(squareCoordinates);
        if(coords[0] < 0){
            return new EnumStringMessage(
                    ShipType.INVALID,
                    "Invalid coordinates"
            );
        }
        int x = coords[0];
        int y = coords[1];
        return deployShip(allShips.get(deployedShipsCount), x, y, isVertical);
    }

    private EnumStringMessage deployShip(Ship ship, int x, int y, boolean isVertical){
        if(allShipsAreDeployed()){
            System.out.println("All ships are already deployed");
            return new EnumStringMessage(
                    ShipType.INVALID,
                    "All ships are already deployed"
            );
        }

        int xChange = 0;
        int yChange = 0;
        if(isVertical){
            xChange = 1;
        }else{
            yChange = 1;
        }

        if(canDeployShip(ship, x, y, isVertical)){
            for(int i = 0; i < ship.getSize(); i++){
                boardOfDeployments[x][y] = ship;
                x += xChange;
                y += yChange;
            }
            deployedShips.add(allShips.get(deployedShipsCount));
            deployedShipsCount++;
            return new EnumStringMessage(
                    getShipType(ship),
                    "You just deployed one of your " + getShipType(ship).toString()
            );
        }
        return new EnumStringMessage(
                ShipType.INVALID,
                "You can't position this " + getShipType(ship) + " here like this"
        );
    }

    private boolean canDeployShip(Ship ship, int x, int y, boolean isVertical){
        int xChange = 0;
        int yChange = 0;
        if(isVertical){
            xChange = 1;
        } else{
            yChange = 1;
        }

        for (int i = 0; i < ship.getSize(); i++) {
            if (!coordinatesAreValid(x, y)) {
                System.out.println("Ships aren't supposed to stick outside of the battlefield");
                return false;
            }

            // if null => can be deployed
            if (boardOfDeployments[x][y] == null) {
                x += xChange;
                y += yChange;
                continue;
            }
            return false;
        }
        return true;
    }

    public boolean allShipsAreDeployed(){
        return deployedShipsCount >= TOTAL_NUMBER_OF_SHIPS;
    }

    public char[][] visualizeBoard(){
        char[][] visualizedBoard = new char[DIMENTION_LIMIT][DIMENTION_LIMIT];

        for (int i = 0; i < DIMENTION_LIMIT; i++){
            for(int j = 0; j < DIMENTION_LIMIT; j++){
                visualizedBoard[i][j] = visualizeSquare(boardOfDeployments[i][j]);
            }
        }
        return visualizedBoard;
    }

    public void stylizeAndPrintBoard(){
        char[][] visualizedBoard = visualizeBoard();
        stylizeAndPrintMatrix(visualizedBoard);
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

    public EnumStringMessage fireAt(String squareCoordinates){
        int[] coords = tranformCoordinatesForReading(squareCoordinates);
        if(coords[0] < 0){
            return new EnumStringMessage(FireResult.INVALID, "Invalid coordinate");
        }
        int x = coords[0];
        int y = coords[1];

        try {
            EnumStringMessage resultMessage = executeFiring(x, y);
            FireResult result = (FireResult)resultMessage.getEnumValue();
            if(result.equals(FireResult.DESTROYED) && deployedShips.isEmpty()){
                return new EnumStringMessage(FireResult.DESTROYED_LAST_SHIP, "Game over");
            }

            return resultMessage;
        }catch(NullPointerException exc){
            System.out.println("Something messed up with firing at targets; NULLPTR");
            return new EnumStringMessage(FireResult.INVALID, "Invalid coordinate");
        }
    }

    private EnumStringMessage executeFiring(int x, int y){
        if (boardOfDeployments[x][y] == null) {
            // System.out.println("Miss!");
            boardOfDeployments[x][y] = missedShip;
            return new EnumStringMessage(FireResult.MISS, "Miss!");
        }

        // e.g. if the field has already been fired at
        if (boardOfDeployments[x][y].getSize() < 0) {
            // System.out.println("Can't fire there again");
            return new EnumStringMessage(FireResult.INVALID, "You've already fired there");
        }
        boolean shipIsDead = boardOfDeployments[x][y].takeOneHit();

        EnumStringMessage result = checkIfShipDied(shipIsDead, x, y);
        if(result != null){
            return result;
        }

        // System.out.println("HIT!");
        boardOfDeployments[x][y] = damagedShip;
        return new EnumStringMessage(FireResult.HIT, "HIT!");
    }

    private EnumStringMessage checkIfShipDied(boolean shipIsDead, int x, int y){
        if(shipIsDead){
            Ship affectedShip = boardOfDeployments[x][y];
            deployedShips.remove(affectedShip);

            String result = getShipType(affectedShip) + " destroyed!";
            System.out.println(result);
            boardOfDeployments[x][y] = damagedShip;
            return new EnumStringMessage(FireResult.DESTROYED, result);
        }
        return null;
    }

    public static void stylizeAndPrintMatrix(char[][] visualizedBoard ){
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

    /**
     * Transforms coordinates of the [A-J][1-10] format to [0-9][0-9] format. The method
     * uses another method to validate it's own parameters, so if the given parameter isn't
     * in the format above, a handled error will occur
     * @param squareCoordinates [A-J][1-10] format
     * @return returns an int[2] array, where arr[0] is x and arr[1] is y;
     * If the given coordinates are invalid in some way, arr[0] will be -1
     */
    public static int[] tranformCoordinatesForReading(String squareCoordinates){
        int[] transformedCoords = new int[2];
        if(!validateCoordinatesString(squareCoordinates)){
            transformedCoords[0] = -1;
            return transformedCoords;
        }

        char x = squareCoordinates.toUpperCase().charAt(0);
        String digitsOfCoords = squareCoordinates.substring(1, squareCoordinates.length());

        int y = Integer.parseInt(digitsOfCoords);

        transformedCoords[0] = x - 'A';
        transformedCoords[1] = y - 1;

        return transformedCoords;
    }

    private static boolean validateCoordinatesString(String squareCoordinates){
        if(squareCoordinates.length() > 3 || squareCoordinates.length() < 2){
            return false;
        }
        if(squareCoordinates.charAt(0) < 'A' || squareCoordinates.charAt(0) >= 'A' + DIMENTION_LIMIT){
            return false;
        }

        String supposedNumericValue = squareCoordinates.substring(1, squareCoordinates.length());
        if(supposedNumericValue.matches("[0-9]*")){
            int number = Integer.parseInt(supposedNumericValue);
            if(number > 0 && number <= DIMENTION_LIMIT){
                return true;
            }
        }
        return false;
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

    public static char[][] initializeTabulaRasa(){
        char[][] table = new char[DIMENTION_LIMIT][DIMENTION_LIMIT];
        for(int i = 0; i < DIMENTION_LIMIT; i++){
            for (int j = 0; j < DIMENTION_LIMIT; j++){
                table[i][j] = '_';
            }
        }
        return table;
    }

    private static ShipType getShipType(Ship ship){
        switch(ship.getSize()){
            case 2:
                return ShipType.DESTROYER;
            case 3:
                return ShipType.CRUISER;
            case 4:
                return ShipType.BATTLESHIP;
            case 5:
                return ShipType.AIRCRAFT_CARRIER;
            default:
                return ShipType.UNKNOWN;
        }
    }

    public static int getShipSizeByType(ShipType shipType){
        switch(shipType){
            case DESTROYER:
                return 2;
            case CRUISER:
                return 3;
            case BATTLESHIP:
                return 4;
            case AIRCRAFT_CARRIER:
                return 5;
            default:
                return 0;
        }
    }

    // CONSTANTS
    private static final int TOTAL_NUMBER_OF_SHIPS = 1;
    public static final int DIMENTION_LIMIT = 10;

    // MEMBER VARIABLES
    private Vector<Ship> deployedShips = new Vector<>();
    private Vector<Ship> allShips = new Vector<>();
    private Ship[][] boardOfDeployments = new Ship[DIMENTION_LIMIT][DIMENTION_LIMIT];
    private int deployedShipsCount;

    public enum FireResult{
        MISS,
        HIT,
        DESTROYED,
        INVALID,
        DESTROYED_LAST_SHIP
    }

    public enum ShipType{
        INVALID,
        DESTROYER,
        CRUISER,
        BATTLESHIP,
        AIRCRAFT_CARRIER,
        UNKNOWN,
    }

    // LITERALLY TRASH
    private DamagedPartOfShip damagedShip = new DamagedPartOfShip();
    private MissedShip missedShip = new MissedShip();
}
