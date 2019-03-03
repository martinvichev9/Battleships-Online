package bg.sofia.uni.fmi.mjt.battleships.utils;

public class Coordinate {

    private char row;
    private int col;

    public Coordinate(char row, int col) {
        this.row = row;
        this.col = col;
    }

    public char getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

}
