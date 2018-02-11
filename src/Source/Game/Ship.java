package Source.Game;

abstract class Ship {
    Ship(){
        healthPoints = 0;
        size = 0;
    }

    /**
     * This constructor is intended for use when loading a ship from a saved game
     * @param remainingHealth only parameter is the saved remaining health of the ship
     */
    Ship(int remainingHealth){
        healthPoints = remainingHealth;
        size = 0;
    }

    /**
     * Used when a player tries hitting the enemy's ships
     *
     * @return true if the ship got destroyed and false if it
     * was already destroyed( should only reach this in a case of a bug) or survived
     */
    boolean takeOneHit(){
        if(healthPoints <= 0){
            System.out.println("Why are you firing at a dead ship??");
            return false;
        }
        return --healthPoints <= 0;
    }

    int getSize() {
        return size;
    }

    int healthPoints;
    int size;
}
