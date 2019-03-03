package bg.sofia.uni.fmi.mjt.battleships.utils;

import bg.sofia.uni.fmi.mjt.battleships.engine.Game;
import bg.sofia.uni.fmi.mjt.battleships.enums.Status;
import bg.sofia.uni.fmi.mjt.battleships.server.GameServer;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public final class Validator {

    private static final int INDEX_FIRST_ELEMENT = 0;
    private static final int INDEX_SECOND_ELEMENT = 1;

    private static final int LENGTH_TWO = 2;
    private static final int LENGTH_THREE = 3;

    private static final int MAX_GAME_NAME_LENGTH = 8;

    private static GameServer server = GameServer.getInstance();

    private static final String VERTICAL_PLACEMENT = "vertical";
    private static final String HORIZONTAL_PLACEMENT = "horizontal";

    public static boolean isValidCoordinate(String coordinate) {
        return isValidXComponent(coordinate) && isValidYComponent(coordinate);
    }

    public static boolean isValidXComponent(String coordinate) {
        return coordinate.toUpperCase().charAt(INDEX_FIRST_ELEMENT) >= 'A' &&
                coordinate.toUpperCase().charAt(INDEX_FIRST_ELEMENT) <= 'J' &&
                coordinate.length() <= LENGTH_THREE;
    }

    public static boolean isValidYComponent(String coordinate) {
        if (coordinate.length() == LENGTH_THREE &&
                coordinate.substring(INDEX_SECOND_ELEMENT).equals("10")) {
            return true;
        } else if (coordinate.length() == LENGTH_THREE) {
            return false;
        } else if (coordinate.length() == LENGTH_TWO) {
            return coordinate.charAt(INDEX_SECOND_ELEMENT) >= '1' &&
                    coordinate.charAt(INDEX_SECOND_ELEMENT) <= '9';
        } else {
            return false;
        }
    }

    public static boolean isValidGameNameLength(String name) {
        return name.length() <= MAX_GAME_NAME_LENGTH;
    }

    public static boolean existsGameName(String gameName) {
        return server.getGames().stream()
                .map(Game::getName)
                .collect(Collectors.toList())
                .contains(gameName);
    }

    public static boolean canUserDeleteGame(String username, String gameName) {
        Game toDelete = server.getGames()
                .stream()
                .filter(game -> game.getName().equals(gameName))
                .findFirst()
                .get();
        return toDelete.getCreator().equals(username);
    }

    public static boolean isFreeGame(Game game) {
        return game.getStatus().equals(Status.PENDING.getStatus());
    }

    public static boolean isCorrectInput(String line, int length, String placement) {

        String[] cellsCoordinates = line.split(" ");

        if (cellsCoordinates.length != length) {
            return false;
        }

        for (String current : cellsCoordinates) {
            if (!isValidCoordinate(current)) {
                return false;
            }

            if (placement.equalsIgnoreCase(VERTICAL_PLACEMENT)) {
                if (!current.substring(1).equalsIgnoreCase(cellsCoordinates[0].substring(1))) {
                    return false;
                }
            }

            if (placement.equalsIgnoreCase(HORIZONTAL_PLACEMENT)) {
                if (current.charAt(0) != cellsCoordinates[0].charAt(0)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isCorrectInputPlacement(String input) {
        return input.equalsIgnoreCase(VERTICAL_PLACEMENT) || input.equalsIgnoreCase(HORIZONTAL_PLACEMENT);
    }

    public static void main(String[] args) {

        Game one = new Game("game", "creator");
        Game two = new Game("game1", "creator1");
        Game three = new Game("game2", "creator2");

        List<Game> games = new LinkedList<>();
        games.add(one);
        games.add(two);
        games.add(three);

        games.removeIf(game -> game.equals(one));

        System.out.println(games.size());

    }
}
