package bg.sofia.uni.fmi.mjt.battleships.enums;

public enum MapCharacter{

    SHIP('*'), FREE('_'), HIT_SHIP('X'), DELIMITER('|'), MISSED('O');

    private char symbol;

    private MapCharacter(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return this.symbol;
    }

}
