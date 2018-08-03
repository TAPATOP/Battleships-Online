package Source;

public class Coordinates {
    // Member variables //
    private int x;
    private int y;
    private boolean isVertical;

    // Constructor
    public Coordinates() {
        x = 0;
        y = 0;
        isVertical = false;
    }

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
        isVertical = false;
    }

    public Coordinates(int x, int y, boolean isVertical) {
        this.x = x;
        this.y = y;
        this.isVertical = isVertical;
    }

    public Coordinates(String coordinatesAsText) {
        if(coordinatesAsText == null) {
            // TODO: Throw an exception here
            x = -1;
            return;
        }

        char firstSymbol  = coordinatesAsText.charAt(0);
        if(firstSymbol == 'h' || firstSymbol == 'v') {
            isVertical = checkIfVertical(firstSymbol);
            coordinatesAsText = coordinatesAsText.substring(1, coordinatesAsText.length());
        }

        coordinatesAsText = coordinatesAsText.toLowerCase();
        tranformCoordinates(coordinatesAsText);
    }

    public Coordinates(String coordinatesAsText, boolean isVertical) {
        this(coordinatesAsText);
        this.isVertical = isVertical;
    }

    // Methods //
    private void tranformCoordinates(String coordinates) {
        if (!validateCoordinatesString(coordinates)) {
            x = -1;
            return ;
        }

        char x = coordinates.charAt(0);
        String digitsOfCoords = coordinates.substring(1, coordinates.length());
        int y = Integer.parseInt(digitsOfCoords);

        this.x = x - 'a';
        this.y = y - 1;
    }

    private static boolean validateCoordinatesString(String squareCoordinates) {
        if (squareCoordinates.length() < 2) {
            return false;
        }
        if (squareCoordinates.charAt(0) < 'a') {
            return false;
        }

        String supposedNumericValue = squareCoordinates.substring(1, squareCoordinates.length());
        // TODO: replace regex with [1-90-9*]
        if (supposedNumericValue.matches("[0-9]*")) {
            int number = Integer.parseInt(supposedNumericValue);
            if (number > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfVertical(char c){
        switch(c) {
            case 'h':
                return false;
            case 'v':
                return true;
            default:
                return false;
        }
    }

    // Boring methods //
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVertical() {
        return isVertical;
    }
}
