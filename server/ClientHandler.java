package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.engine.Game;
import bg.sofia.uni.fmi.mjt.battleships.enums.Command;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.MySocketException;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.WaitingThreadInterruptedException;
import bg.sofia.uni.fmi.mjt.battleships.utils.Validator;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final int INDEX_OF_COMMAND = 0;

    /**
     * For some commands like (list-games, saved-games, etc.) that
     * field is not necessary.
     */
    private static final int INDEX_OF_GAME_NAME = 1;

    private static final int LENGTH_LINE_WITH_COMMAND_ONLY = 1;

    private Socket socket;
    private String username;

    private static GameServer server = GameServer.getInstance();

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.username = reader.readLine();

        server.addUser(this.username, this.socket);

        System.out.println("New user '" + username + "' connected to server");

        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        writer.println("Welcome to 'Battleships Online'. Write help if you want to see server's commands");
        writer.println("menu> ");
    }


    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {

                String input = reader.readLine();

                if (input != null) {

                    String command;
                    String[] components = input.split(" ");
                    command = components[INDEX_OF_COMMAND];

                    if (command.equalsIgnoreCase("start")) {

                        writer.println("Enter 'play': ");

                        while (server.isUserPlaying(username)) {
                            // If we are here, then someone has invited us to play game and has
                            // started a playing thread (GameSession), which fields are our input
                            // and output streams and the opponent's ones. So we do not have to read
                            // the input here (it will be read in a GameSession thread).
                        }

                    } else if (command.equals("join-game")) {

                        if (server.noFreeGames()) {
                            writer.println("Server cannot find any game for you. Please" +
                                    " try again in a few minutes");
                            writer.println("menu> ");
                            continue;
                        }

                        //Will be joined to a random game
                        if (components.length == LENGTH_LINE_WITH_COMMAND_ONLY) {

                            Game random = server.getRandomGame(username);
                            if (random == null) {
                                writer.println("Server cannot find any game for you. " +
                                        "Maybe all users are playing, maybe there is no free game to join");
                                writer.println("menu> ");
                                continue;
                            }
                            playGame(reader, writer, random);

                        } else {

                            String gameName = components[INDEX_OF_GAME_NAME];
                            Game game = server.getGameByName(gameName);
                            if (game == null) {
                                writer.println("There is no such game. Write 'list-games' to see all current games");
                            } else if (!Validator.isFreeGame(game)) {
                                writer.println("That game is playing now. Please choose another game to play");
                            } else if (server.isUserPlaying(game.getCreator())) {
                                writer.println("Opponent is playing right now. Please try again later");
                            } else {
                                playGame(reader, writer, game);
                            }
                        }
                    } else if (command.equalsIgnoreCase(Command.CREATE.getCommand())) {
                        if (components.length == LENGTH_LINE_WITH_COMMAND_ONLY) {
                            writer.println("<game-name> is missing.  Write 'help' if you want to see"
                                    + " the server's commands");
                        } else {
                            String gameName = components[INDEX_OF_GAME_NAME];
                            createGame(writer, gameName);
                        }
                    } else if (command.equalsIgnoreCase(Command.LIST.getCommand())) {
                        listGames(writer);
                    } else if (command.equalsIgnoreCase(Command.HELP.getCommand())) {
                        showCommands(writer);
                    } else if (command.equalsIgnoreCase(Command.SAVED.getCommand())) {
                        savedGames(username, writer);
                    } else if (command.equalsIgnoreCase(Command.DELETE.getCommand())) {
                        if (components.length == LENGTH_LINE_WITH_COMMAND_ONLY) {
                            writer.println("<game-name> is missing.  Write 'help' if you want to see"
                                    + " the server's commands");
                        } else {
                            String gameName = components[INDEX_OF_GAME_NAME];
                            deleteGame(writer, gameName);
                        }
                    } else if (input.split(" ")[0].toLowerCase().equals("exit")) {
                        exitServer(writer, username);
                        break;
                    } else {
                        invalidCommand(writer);
                    }
                    writer.println("menu> ");
                }
            }

            socket.close();

        } catch (IOException io) {
            throw new MySocketException("I/O error occurred in socket", io);
        }
    }

    private void createGame(PrintWriter writer, String gameName) {

        if (Validator.existsGameName(gameName)) {
            writer.println("Game with that name exists. Please choose another name");
        } else if (!Validator.isValidGameNameLength(gameName)) {
            writer.println("Game length is too long. Max length is 8");
        } else {
            Game toAdd = new Game(gameName, username);
            server.addGame(toAdd);
            System.out.println("User '" + username + "' created new game");
            writer.println("Successfully created game");
        }
    }

    private void listGames(PrintWriter writer) {

        if (server.getGames().isEmpty()) {
            writer.println("There are no created games");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-10s", "| " + "NAME") + "|");
        builder.append(String.format("%-10s", " " + "CREATOR") + "|");
        builder.append(String.format("%-12s", " " + "STATUS") + "|");
        builder.append(String.format("%-10s", " " + "PLAYERS") + "|\n");
        builder.append("+---------+----------+------------+----------+" + "\n");
        for (Game game : server.getGames()) {
            builder.append(game.toString() + "\n");
        }

        writer.println(builder.toString());
    }

    private void showCommands(PrintWriter writer) {
        StringBuilder result = new StringBuilder();
        result.append("Available server commands:\n");
        result.append(String.format("%15s", Command.CREATE.getCommand()) + " <game-name>" + "\n");
        result.append(String.format("%13s", Command.JOIN.getCommand()) + " <game-name>" + "\n");
        result.append(String.format("%13s", Command.JOIN.getCommand()) + " //Join to random game" + "\n");
        result.append(String.format("%15s", "  " + Command.SAVED.getCommand()) + "\n");
        result.append(String.format("%13s", "  " + Command.SAVE.getCommand()) + " //It is used, " +
                "when you are playing" + "\n");
        result.append(String.format("%13s", "  " + Command.LOAD.getCommand()) + " <game-name>" + "\n");
        result.append(String.format("%15s", "  " + Command.DELETE.getCommand()) + " <game-name>" + "\n");
        result.append(String.format("%8s", "  " + Command.EXIT.getCommand()) + "\n");

        writer.println(result.toString());
    }

    private void exitServer(PrintWriter writer, String username) {
        System.out.println("User '" + username + "' left the server");
        server.removeUser(username);
        server.removeAllUserGames(username);
        writer.println("Disconnected from server");
    }

    private void deleteGame(PrintWriter writer, String gameName) {

        if (!Validator.existsGameName(gameName)) {
            writer.println("Game '" + gameName + "' does not exist");
        } else if (!Validator.canUserDeleteGame(username, gameName)
                && !server.isUserSavedGame(username, gameName)) {
            writer.println("You have no rights to delete that game");
        } else {

            Game toRemove = server.getGameByName(gameName);

            if (toRemove.getCreator().equals(username)) {

                server.removeSavedGameFromAllUsers(toRemove);
                server.removeGameFiles(toRemove);
                server.removeGame(toRemove.getName());

                writer.println("Successfully deleted game '" + gameName);
                System.out.println("Game '" + gameName + "' was deleted by '" + username + "'");

            } else {

                writer.println("Successfully deleted game's state");
                System.out.println("User '" + username + "' deleted his saved state of game '" + gameName + "'");
                server.removeUserSavedGame(toRemove, username);
            }
        }
    }

    private void playGame(BufferedReader reader, PrintWriter writer, Game game) {
        try {

            writer.println("Loading...");
            String opponent = game.getCreator();
            Socket oppSocket = server.getUser(opponent);

            BufferedReader opponentReader = new BufferedReader(
                    new InputStreamReader(oppSocket.getInputStream()));
            PrintWriter opponentWriter = new PrintWriter(oppSocket.getOutputStream(), true);

            opponentWriter.println("\nUser '" + username + "' joined game '" + game.getName() + "'");
            opponentWriter.println("Write 'start' to start the game: ");

            server.addPlayingUsers(username, opponent);
            server.startGame(game.getName());

            Thread play = new Thread(new GameSession(reader, writer, username,
                    opponentWriter, opponentReader, opponent, game.getName()));
            play.start();
            play.join();

            server.removePlayingUsers(username, opponent);
            server.finishGame(game.getName());

        } catch (IOException io) {
            throw new MySocketException("An I/O error occurred in a new created socket", io);
        } catch (InterruptedException ie) {
            throw new WaitingThreadInterruptedException("Waiting thread has been interrupted", ie);
        }
    }

    private void savedGames(String username, PrintWriter writer) {
        if (server.getUserSavedGames(username) == null
                || server.getUserSavedGames(username).isEmpty()) {
            writer.println("No saved games found");
        } else {
            writer.print("Your saved games: ");
            for (Game current : server.getUserSavedGames(username)) {
                writer.print(current.getName() + " ");
            }
            writer.println();
        }
    }

    private void invalidCommand(PrintWriter writer) {
        writer.println("No such command. Write 'help' if you want to see " +
                "the server's commands.");
    }



}
