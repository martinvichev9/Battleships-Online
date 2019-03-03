package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.engine.Game;
import bg.sofia.uni.fmi.mjt.battleships.engine.GameEngine;
import bg.sofia.uni.fmi.mjt.battleships.engine.Ship;
import bg.sofia.uni.fmi.mjt.battleships.enums.Command;
import bg.sofia.uni.fmi.mjt.battleships.enums.MapCharacter;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.MyFileException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.MySocketException;
import bg.sofia.uni.fmi.mjt.battleships.utils.Coordinate;
import bg.sofia.uni.fmi.mjt.battleships.utils.Validator;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class GameSession implements Runnable {

    private static final int COUNT_SHIPS = 10;
    private static final String PATH_DIRECTORY = "resources";

    private String gameName;
    private BufferedReader reader;
    private PrintWriter writer;
    String username;
    private PrintWriter opponentWriter;
    private BufferedReader opponentReader;
    String opponentUsername;

    public GameSession(BufferedReader reader, PrintWriter writer, String username, PrintWriter opponentWriter,
                       BufferedReader opponentReader, String opponentUsername, String gameName) {
        this.reader = reader;
        this.writer = writer;
        this.username = username;
        this.opponentWriter = opponentWriter;
        this.opponentReader = opponentReader;
        this.opponentUsername = opponentUsername;
        this.gameName = gameName;
    }

    @Override
    public void run() {
        try {

            GameEngine user = new GameEngine();
            GameEngine opponent = new GameEngine();

            String fromOpponent = opponentReader.readLine();
            while (!fromOpponent.equalsIgnoreCase("play")) {
                opponentWriter.println("Incorrect input. While you don't type 'play', the game won't start");
                opponentWriter.println("Enter 'play': ");
                writer.println("Please wait until " + opponentUsername + " join");
                fromOpponent = opponentReader.readLine();
            }

            System.out.println("Game '" + gameName + "' started - playing " + username + " vs " + opponentUsername);

            writer.println("Game started");

            opponentWriter.println("Game started");
            opponentWriter.println("Please wait until player '" + username + "' enters all his ships");

            //User's ships
            inputShips(reader, writer, user);
            writer.println("Please wait until player '" + opponentUsername + "' places all his ships");

            //Opponent's ships
            inputShips(opponentReader, opponentWriter, opponent);

            opponentWriter.println("Player '" + username + "' starts");

            //Play game
            while (true) {

                printElementsBeforeAttack(writer, user);
                String fromUser = reader.readLine();

                if (fromUser.equalsIgnoreCase(Command.SAVE.getCommand())) {
                    printSaveGameMessage(writer, opponentWriter);
                    GameServer.getInstance().addSavedGame(username, opponentUsername, gameName);
                    saveGame(username, user, opponentUsername, opponent, gameName, username);
                    break;
                }

                attack(reader, writer, fromUser, opponent);

                if (loseGame(opponent)) {
                    printWinLoseMessage(writer, user, opponentWriter, opponent);
                    System.out.println(username + " defeated " + opponentUsername);
                    break;
                }

                printElementsBeforeAttack(opponentWriter, opponent);
                fromOpponent = opponentReader.readLine();

                if (fromOpponent.equalsIgnoreCase(Command.SAVE.getCommand())) {
                    printSaveGameMessage(opponentWriter, writer);
                    GameServer.getInstance().addSavedGame(opponentUsername, username, gameName);
                    saveGame(opponentUsername, opponent, username, user, gameName, opponentUsername);
                    break;
                }

                attack(opponentReader, opponentWriter, fromOpponent, user);

                if (loseGame(user)) {
                    printWinLoseMessage(opponentWriter, opponent, writer, user);
                    System.out.println(opponentUsername + " defeated " + username);
                    break;
                }
            }
        } catch (IOException io) {
            throw new MySocketException("An I/O error occurred in a some socket", io);
        }
    }

    private void inputShips(BufferedReader reader, PrintWriter writer, GameEngine engine) throws IOException {

        String line;
        String placement;
        Ship toAdd;
        for (int i = 0; i < 1; ++i) {

            int countCells = 0;

            writer.println("Placement of ship(horizontal/vertical): ");
            placement = reader.readLine();
            while (!Validator.isCorrectInputPlacement(placement)) {
                writer.println("No such placement");
                writer.println("Placement of ship(horizontal/vertical): ");
                placement = reader.readLine();
            }

            if (i == 0) {
                countCells = 5;
                writer.println("Please enter a ship with " + countCells + " cells");
            } else if (0 < i && i <= 2) {
                countCells = 4;
                writer.println("Please enter a ship with " + countCells + " cells");
            } else if (2 < i && i <= 5) {
                countCells = 3;
                writer.println("Please enter a ship with " + countCells + " cells");
            } else {
                countCells = 2;
                writer.println("Please enter a ship with " + countCells + " cells");
            }

            writer.println("Enter ships's coordinates: ");

            line = reader.readLine();
            while (!Validator.isCorrectInput(line, countCells, placement)) {
                writer.println("Incorrect input. Please make sure that you satisfy all restrictions.");
                writer.println("Ship's placement: " + placement.toLowerCase());
                writer.println("Cells count: " + countCells);
                writer.println("Rows are from 'A' to 'J' and columns are from 1 to 10");
                writer.println("Enter ships's coordinates: ");
                line = reader.readLine();
            }

            toAdd = createShipByInput(line);
            engine.addShip(toAdd);
            engine.placeShip(toAdd);
        }
    }

    private Ship createShipByInput(String input) {

        List<Coordinate> shipsCoordinates = new LinkedList<>();
        for (String current : input.split(" ")) {
            char x = current.toUpperCase().charAt(0);
            int y = Integer.parseInt(current.substring(1));
            Coordinate coordinate = new Coordinate(x, y);
            shipsCoordinates.add(coordinate);
        }

        return new Ship(shipsCoordinates);
    }

    private Coordinate createCoordinateFromInput(String input) {
        char x = input.toUpperCase().charAt(0);
        int y = Integer.parseInt(input.substring(1));
        return new Coordinate(x, y);
    }

    private void attack(BufferedReader reader, PrintWriter writer, String input, GameEngine map) throws IOException {

        while (!Validator.isValidCoordinate(input)) {
            writer.println("Invalid cell input. Rows of map are from 'A' to 'J', columns - from 1 to 10");
            writer.println("Enter your turn: ");
            input = reader.readLine();
        }

        Coordinate cell = createCoordinateFromInput(input);

        if (map.isPartOfShip(cell)) {
            Ship hitShip = map.getShipByCoordinate(cell);

            //That means that we have hit the last cell of ship
            if (hitShip.getCoordinates().size() == 1) {
                writer.println("You hit ship");
                writer.println("Ship is sunk\n");
            } else {
                writer.println("You hit ship\n");
            }

            map.removeCoordinate(cell);
            map.changeMapCharacter(cell, MapCharacter.HIT_SHIP.getSymbol());
            return;
        }
        if (map.isFreeCell(cell)) {
            map.changeMapCharacter(cell, MapCharacter.MISSED.getSymbol());
        }
        writer.println("You missed\n");
    }

    private boolean loseGame(GameEngine loserEngine) {
        return loserEngine.areAllShipsSunk();
    }

    private void saveGame(String player1, GameEngine engine1, String player2,
                          GameEngine engine2, String gameName, String playerTurn) {

        String fileName = "Game_" + gameName + "_" + player1 + "_VS_" + player2 + "_.txt";
        File file = new File(PATH_DIRECTORY, fileName);

        //Some version of save-game command, i.e., only one time game can be saved, for now.
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {

            bw.write("Player1 name: " + player1 + "\n");
            bw.write("Player2 name: " + player2 + "\n");

            bw.newLine();
            bw.write("Player1 map\n");
            bw.write(engine1.toString());
            bw.newLine();
            bw.write("Player2 map\n");
            bw.write(engine2.toString());
            bw.newLine();
            bw.write("Turn: " + playerTurn);
            bw.newLine();

            System.out.println("Game '" + gameName + "' ended");

        } catch (IOException io) {
            throw new MyFileException("An I/O error occurred in a created file", io);
        }
    }

    private void printElementsBeforeAttack(PrintWriter writer, GameEngine engine) {
        writer.println();
        writer.println(String.format("%18s", "YOUR BOARD"));
        writer.println(engine.toString());
        writer.println("Enter your turn or 'save-game' if want to save game's state: ");
    }

    private void printWinLoseMessage(PrintWriter winner, GameEngine winEngine,
                                     PrintWriter loser, GameEngine loseEngine) {

        winner.println(winEngine.toString());
        winner.println(String.format("%15s", "WINNER"));

        loser.println(loseEngine.toString());
        loser.println(String.format("%15s", "LOSER"));
    }

    private void printSaveGameMessage(PrintWriter saveWriter, PrintWriter otherWriter) {
        otherWriter.println("Player '" + username + "' entered 'save-game'");
        otherWriter.println("Your game's state was saved");

        saveWriter.println("Your game's state was saved");
    }

}
