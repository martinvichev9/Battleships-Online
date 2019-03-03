package bg.sofia.uni.fmi.mjt.battleships.engine;

import bg.sofia.uni.fmi.mjt.battleships.enums.MapCharacter;
import bg.sofia.uni.fmi.mjt.battleships.utils.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class GameEngine {

    private static int ROWS_COLUMNS_SIZE = 10;

    private char[][] map;
    private Map<Ship, Boolean> ships;

    public GameEngine() {
        this.ships = new HashMap<>();
        initializeMap();
    }

    private void initializeMap() {
        map = new char[ROWS_COLUMNS_SIZE][ROWS_COLUMNS_SIZE];
        for (int i = 0; i < ROWS_COLUMNS_SIZE; ++i) {
            for (int j = 0; j < ROWS_COLUMNS_SIZE; ++j) {
                map[i][j] = MapCharacter.FREE.getSymbol();
            }
        }
    }

    public void addShip(Ship ship) {
        ships.putIfAbsent(ship, false);
    }

    public void placeShip(Ship ship) {
        for (Coordinate current : ship.getCoordinates()) {
            map[current.getRow() - 'A'][current.getCol() - 1] = MapCharacter.SHIP.getSymbol();
        }
    }

    public void changeMapCharacter(Coordinate coordinate, char newSymbol) {
        this.map[coordinate.getRow() - 'A'][coordinate.getCol() - 1] = newSymbol;
    }

    public boolean isPartOfShip(Coordinate coordinate) {
        return map[coordinate.getRow() - 'A'][coordinate.getCol() - 1] == MapCharacter.SHIP.getSymbol();
    }

    public boolean isFreeCell(Coordinate coordinate) {
        return map[coordinate.getRow() - 'A'][coordinate.getCol() - 1] == MapCharacter.FREE.getSymbol();
    }

    public void removeCoordinate(Coordinate toRemove) {
        for (Ship current : ships.keySet()) {
            if (current.containsCoordinate(toRemove)) {
                current.removeCoordinate(toRemove);
                if (current.getCoordinates().isEmpty()) {
                    ships.put(current, true);
                    return;
                }
                ships.put(current, false);
            }
        }
    }

    public Ship getShipByCoordinate(Coordinate coordinate) {
        return ships.keySet().stream()
                .filter(ship -> ship.containsCoordinate(coordinate))
                .findFirst()
                .get();
    }

    public boolean areAllShipsSunk() {
        return ships.values().stream().noneMatch(sunk -> sunk == false);
    }

    public char getMapCharacter(Coordinate c) {
        return map[c.getRow() - 'A'][c.getCol() - 1];
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("   1 2 3 4 5 6 7 8 9 10\n");

        for (int i = 0; i < ROWS_COLUMNS_SIZE; ++i) {
            result.append((char) ('A' + i) + " ");
            for (int j = 0; j < ROWS_COLUMNS_SIZE; ++j) {
                result.append(MapCharacter.DELIMITER.getSymbol());
                result.append(map[i][j]);
            }
            result.append(MapCharacter.DELIMITER.getSymbol());
            result.append("\n");
        }

        return result.toString();
    }

}
