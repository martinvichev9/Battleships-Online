package bg.sofia.uni.fmi.mjt.battleships.engine;

import bg.sofia.uni.fmi.mjt.battleships.utils.Coordinate;

import java.util.List;

public class Ship {

    private List<Coordinate> coordinates;

    public Ship(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public void removeCoordinate(Coordinate toRemove) {
        coordinates.removeIf(coordinate -> coordinate.getCol() == toRemove.getCol() &&
                coordinate.getRow() == toRemove.getRow());
    }

    public boolean containsCoordinate(Coordinate coordinate) {
        return coordinates.stream()
                .anyMatch(coord -> coord.getCol() == coordinate.getCol() &&
                        coord.getRow() == coordinate.getRow());
    }

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

}
