package Source.Game;

import Source.Coordinates;
import Source.ServerResponseType;

public class SimplifiedGameTable {
    // Constants
    protected static final int DIMENTION_LIMIT = 10;

    // Member variables
    protected char[][] table;

    // Constructors
    public SimplifiedGameTable() {
        initializeWhiteBoard();
    }

    private void initializeWhiteBoard() {
        table  = new char[DIMENTION_LIMIT][DIMENTION_LIMIT];
        for (int i = 0; i < DIMENTION_LIMIT; i++) {
            for (int j = 0; j < DIMENTION_LIMIT; j++) {
                table[i][j] = '_';
            }
        }
    }

    // Methods
    public char[][] exportTableAsRawData(){
        return table;
    }

    public void recordShotAt(String squareCoordinates, ServerResponseType fireResult) {
        try {
            Coordinates coordinates = new Coordinates(squareCoordinates);
            char fireResultVisualization = visualizeServerFireResult(fireResult);
            table[coordinates.getX()][coordinates.getY()] = fireResultVisualization;
        } catch (IndexOutOfBoundsException exc) {
            System.out.println("The fire position is out of bounds");
        }
    }

    private char visualizeServerFireResult(ServerResponseType fireResult) {
        switch(fireResult) {
            case HIT:
                return 'X';
            case MISS:
                return 'O';
            default:
                return '?';
        }
    }

    public void stylizeAndPrintMatrix() {
        System.out.print("/|");
        for (int i = 1; i <= DIMENTION_LIMIT; i++) {
            System.out.print(i + "|");
        }
        System.out.println();
        for (int i = 0; i < DIMENTION_LIMIT; i++) {
            System.out.print((char)(i + 65) + "|");
            for (int j = 0; j < DIMENTION_LIMIT; j++) {
                System.out.print(table[i][j] + "|");
            }
            System.out.println();
        }
    }
}
