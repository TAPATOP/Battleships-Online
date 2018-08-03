package Source.Game;

import Source.Coordinates;
import Source.EnumStringMessage;

import java.util.Vector;

public class GameTable {
    // Constants
    private static final int DIMENTION_LIMIT = 10;

    // Member variables //
    private Vector<Ship> deployedShips = new Vector<>();
    private Vector<Ship> allShips = new Vector<>();
    private Ship[][] boardOfDeployments;
    private int deployedShipsCount;
    private int totalNumberOfShips = 2;

    // Used to flag the result of fire on the map after shooting at it
    private final DamagedPartOfShip damagedShip = new DamagedPartOfShip();
    private final MissedShip missedShip = new MissedShip();

    // Constructors //
    public GameTable() {
        deployedShipsCount = 0;
        totalNumberOfShips = 10;

        addShips(1, ShipType.AIRCRAFT_CARRIER);
        addShips(2, ShipType.BATTLESHIP);
        addShips(3, ShipType.CRUISER);
        addShips(4, ShipType.DESTROYER);

        initializeWhiteBoard();
    }

    public GameTable(int numberOfShips) {
        this();
        totalNumberOfShips = numberOfShips;
    }

    public GameTable(
            int numberOfCarriers,
            int numberOfBattleships,
            int numberOfCruisers,
            int numberOfDestroyers
    ) {
        deployedShipsCount = 0;
        totalNumberOfShips =
                numberOfBattleships + numberOfCarriers +
                numberOfCruisers + numberOfDestroyers
        ;

        addShips(numberOfCarriers, ShipType.AIRCRAFT_CARRIER);
        addShips(numberOfBattleships, ShipType.BATTLESHIP);
        addShips(numberOfCruisers, ShipType.CRUISER);
        addShips(numberOfDestroyers, ShipType.DESTROYER);

        initializeWhiteBoard();
    }

    private void addShips(int shipCount, ShipType shipType) {
        for (int i = 0; i < shipCount; i++) {
            allShips.add(createShipByShipType(shipType));
        }
    }

    // Methods //
    /**
     * Deploys the next ship in line
     * @param squareCoordinates the coordinates of the ship in the format: [A-J][1-10]
     * @param isVertical whether the ship is vertically deployed or not
     * @return returns true if the ship was successfully deployed
     */
    @SuppressWarnings("UnusedReturnValue")
    public EnumStringMessage deployNextShip(String squareCoordinates, boolean isVertical) {
        Coordinates coordinates = new Coordinates(squareCoordinates, isVertical);
        if (coordinates.getX() < 0) {
            return new EnumStringMessage(
                    ShipType.INVALID,
                    "Invalid coordinates"
            );
        }
        return deployShip(allShips.get(deployedShipsCount), coordinates);
    }

    /**
     * Deploys the next ship in line the list
     */
    public EnumStringMessage deployNextShip(Coordinates coordinates) {
        if (coordinates.getX() < 0) {
            return new EnumStringMessage(
                    ShipType.INVALID,
                    "Invalid coordinates"
            );
        }
        return deployShip(allShips.get(deployedShipsCount), coordinates);
    }

    private EnumStringMessage deployShip(Ship ship, Coordinates coordinates) {
        if (allShipsAreDeployed()) {
            System.out.println("All ships are already deployed");
            return new EnumStringMessage(
                    ShipType.INVALID,
                    "All ships are already deployed"
            );
        }

        int[] xAndYChanges = generateXAndYChange(coordinates.isVertical());
        int x = coordinates.getX();
        int y = coordinates.getY();
        int xChange = xAndYChanges[0];
        int yChange = xAndYChanges[1];

        if (canDeployShip(ship, coordinates)) {
            for (int i = 0; i < ship.getSize(); i++) {
                boardOfDeployments[x][y] = ship;
                x += xChange;
                y += yChange;
                //stylizeAndPrintMatrix();
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

    private int[] generateXAndYChange(boolean isVertical) {
        int xChange = 0;
        int yChange = 0;
        int[] result = new int[2];

        if (isVertical) {
            xChange = 1;
        } else {
            yChange = 1;
        }

        result[0] = xChange;
        result[1] = yChange;

        return result;
    }

    /**
     * Used by the Client when trying to deploy a ship
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean deployShip(ShipType shipType, String coordinateInfo){
        Coordinates coordinates = new Coordinates(coordinateInfo);

        if (coordinates.getX() < 0) {
            System.out.println("Something's wrong with the coordinates");
            return false;
        }

        Ship ship = createShipByShipType(shipType);
        if (ship == null){
            System.out.println("Something's wrong with the ship");
            return false;
        }

        deployShip(ship, coordinates);

        return true;
    }

    private Ship createShipByShipType(ShipType shipType){
        switch(shipType){
            case DESTROYER:
                return new Destroyer();
            case CRUISER:
                return new Cruiser();
            case BATTLESHIP:
                return new Battleship();
            case AIRCRAFT_CARRIER:
                return new Carrier();
            case UNKNOWN:
            case INVALID:
                default:
                    return null;
        }
    }

    private boolean canDeployShip(Ship ship, Coordinates coordinates) {
        int xChange = 0;
        int yChange = 0;
        if (coordinates.isVertical()) {
            xChange = 1;
        } else {
            yChange = 1;
        }

        int x = coordinates.getX();
        int y = coordinates.getY();
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

    public boolean allShipsAreDeployed() {
        return deployedShipsCount >= totalNumberOfShips;
    }

    public char[][] visualizeBoard() {
        char[][] visualizedBoard = new char[DIMENTION_LIMIT][DIMENTION_LIMIT];

        for (int i = 0; i < DIMENTION_LIMIT; i++) {
            for (int j = 0; j < DIMENTION_LIMIT; j++) {
                visualizedBoard[i][j] = visualizeSquare(boardOfDeployments[i][j]);
            }
        }
        return visualizedBoard;
    }

    private char visualizeSquare(Ship shipOccupyingTheSquare) {
        if (shipOccupyingTheSquare == null) {
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

    public EnumStringMessage recordShotAt(String squareCoordinates) {
        Coordinates coords = new Coordinates(squareCoordinates);
        if (coords.getX() < 0 || coords.getX() >= DIMENTION_LIMIT || coords.getY() >= DIMENTION_LIMIT) {
            return new EnumStringMessage(FireResult.INVALID, "Invalid coordinate");
        }

        return recordShotAt(coords.getX(), coords.getY());
    }

    public EnumStringMessage recordShotAt(int x, int y){

        try {
            EnumStringMessage resultMessage = executeFiring(x, y);
            FireResult result = (FireResult)resultMessage.getEnumValue();
            if (result.equals(FireResult.DESTROYED) && deployedShips.isEmpty()) {
                return new EnumStringMessage(FireResult.DESTROYED_LAST_SHIP, "Game over");
            }

            return resultMessage;
        } catch(NullPointerException exc) {
            System.out.println("Something messed up with firing at targets; NULLPTR");
            return new EnumStringMessage(FireResult.INVALID, "Invalid coordinate");
        }

    }

    private EnumStringMessage executeFiring(int x, int y) {
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
        if (result != null) {
            return result;
        }

        // System.out.println("HIT!");
        boardOfDeployments[x][y] = damagedShip;
        return new EnumStringMessage(FireResult.HIT, "HIT!");
    }

    private EnumStringMessage checkIfShipDied(boolean shipIsDead, int x, int y) {
        if (shipIsDead) {
            Ship affectedShip = boardOfDeployments[x][y];
            deployedShips.remove(affectedShip);

            String result = getShipType(affectedShip) + " destroyed!";
            System.out.println(result);
            boardOfDeployments[x][y] = damagedShip;
            return new EnumStringMessage(FireResult.DESTROYED, result);
        }
        return null;
    }

    public void stylizeAndPrintMatrix() {
        System.out.print("/|");
        for (int i = 1; i <= DIMENTION_LIMIT; i++) {
            System.out.print(i + "|");
        }
        System.out.println();
        char[][] theTable = exportTableAsRawData();
        for (int i = 0; i < DIMENTION_LIMIT; i++) {
            System.out.print((char)(i + 65) + "|");
            for (char c :
                    theTable[i]) {
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
    private boolean coordinatesAreValid(int x, int y) {
        return (x < DIMENTION_LIMIT && x >= 0 && y < DIMENTION_LIMIT && y >= 0);
    }

    private void initializeWhiteBoard() {
        boardOfDeployments  = new Ship[DIMENTION_LIMIT][DIMENTION_LIMIT];
        for (Ship[] line :
                boardOfDeployments) {
            for (Ship ship :
                    line) {
                ship = null;
            }
        }
    }

    private ShipType getShipType(Ship ship) {
        switch(ship.getSize()) {
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

    public static int getShipSizeByType(ShipType shipType) {
        switch(shipType) {
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

    public char[][] exportTableAsRawData(){
        char[][] charTable = new char[DIMENTION_LIMIT][DIMENTION_LIMIT];
        for (int i = 0; i < DIMENTION_LIMIT; i++){
            for (int j = 0; j < DIMENTION_LIMIT; j++){
                charTable[i][j] = visualizeSquare(boardOfDeployments[i][j]);
            }
        }
        return charTable;
    }

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
}
