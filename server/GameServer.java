package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.engine.Game;
import bg.sofia.uni.fmi.mjt.battleships.enums.Status;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    private static final String PATH_DIRECTORY = "resources";

    private static final int PORT = 9999;
    private static final int MAX_COUNT_ONLINE_USERS = 16;

    private Map<String, List<Game>> savedGames;
    private Map<String, Socket> users;
    private List<Game> games;
    private Set<String> activePlayers;
    private ExecutorService onlineUsers;

    //Singleton design pattern created by lazy initialization with double check locking
    private static GameServer instance;

    private GameServer() {
        this.users = new HashMap<>();
        this.onlineUsers = Executors.newFixedThreadPool(MAX_COUNT_ONLINE_USERS);
        this.games = new LinkedList<>();
        this.activePlayers = new HashSet<>();
        this.savedGames = new HashMap<>();
    }

    public static GameServer getInstance() {
        if (instance == null) {
            synchronized (GameServer.class) {
                if (instance == null) {
                    instance = new GameServer();
                }
            }
        }
        return instance;
    }

    //Users' methods
    public synchronized void addUser(String username, Socket connection) {
        users.putIfAbsent(username, connection);
    }

    public synchronized void removeUser(String username) {
        users.remove(username);

        //TODO remove all games from user

    }

    public synchronized Game getRandomGame(String creator) {

        return games.stream()
                .filter(game -> game.getStatus().equals(Status.PENDING.getStatus()))
                .filter(game -> !game.getCreator().equals(creator))
                .filter(game -> !isUserPlaying(game.getCreator()))
                .findAny()
                .orElse(null);
    }

    public synchronized Socket getUser(String username) {
        if (users.containsKey(username)) {
            return users.get(username);
        }
        return null;
    }

    public synchronized boolean isUserPlaying(String username) {
        return this.activePlayers.contains(username);
    }

    public synchronized void addPlayingUsers(String name1, String name2) {
        activePlayers.add(name1);
        activePlayers.add(name2);
    }

    public synchronized void removePlayingUsers(String name1, String name2) {
        activePlayers.remove(name1);
        activePlayers.remove(name2);
    }

    //Games' methods
    public synchronized List<Game> getGames() {
        return this.games;
    }

    public synchronized void addGame(Game game) {
        games.add(game);
    }

    public synchronized void removeAllUserGames(String username) {
        games.removeIf(game -> game.getCreator().equals(username));
    }

    public synchronized void removeGame(String gameName) {
        games.removeIf(game -> game.getName().equals(gameName));
    }

    public synchronized Game getGameByName(String gameName) {
        if (games.stream().map(Game::getName).anyMatch(name -> name.equals(gameName))) {
            return games.stream().filter(game -> game.getName().equals(gameName)).findFirst().get();
        }
        return null;
    }

    public synchronized void startGame(String gameName) {

        games.stream().filter(game -> game.getName().equals(gameName))
                .findFirst()
                .get()
                .startGame();
    }

    public synchronized void finishGame(String gameName) {
        games.stream().filter(game -> game.getName().equals(gameName))
                .findFirst()
                .get()
                .finishGame();
    }

    public synchronized boolean noFreeGames() {
        if (games.isEmpty() || games.stream().noneMatch(game -> game.getStatus()
                .equals(Status.PENDING.getStatus()))) {
            return true;
        }
        return false;
    }

    public synchronized List<Game> getUserSavedGames(String username) {
        if (savedGames.containsKey(username)) {
            return savedGames.get(username);
        }
        return null;
    }

    public boolean isUserSavedGame(String username, String gameName) {

        if (getUserSavedGames(username) == null) {
            return false;
        }

        return getUserSavedGames(username).stream()
                .map(Game::getName)
                .anyMatch(name -> name.equals(gameName));
    }

    public synchronized void addSavedGame(String player1, String player2, String gameName) {
        if (savedGames.containsKey(player1)) {
            savedGames.get(player1).add(getGameByName(gameName));
        } else {
            savedGames.put(player1, new LinkedList<>());
            savedGames.get(player1).add(getGameByName(gameName));
        }
        if (savedGames.containsKey(player2)) {
            savedGames.get(player2).add(getGameByName(gameName));
        } else {
            savedGames.put(player2, new LinkedList<>());
            savedGames.get(player2).add(getGameByName(gameName));
        }
    }

    public synchronized void removeSavedGameFromAllUsers(Game toRemove) {
        for (String current : savedGames.keySet()) {
            savedGames.get(current).removeIf(game -> game.equals(toRemove));
        }
    }

    public synchronized void removeGameFiles(Game toRemove) {
        File directory = new File(PATH_DIRECTORY);

        for (File current : directory.listFiles()) {
            if (current.isFile() && current.getName().contains(toRemove.getName())) {
                current.delete();
            }
        }

    }

    public synchronized void removeUserSavedGame(Game game, String username) {

        List<String> opponents = getAllOpponents(game, username);
        savedGames.get(username).remove(game);

        for (String current : opponents) {
            savedGames.get(current).remove(game);
        }
    }

    public synchronized List<String> getAllOpponents(Game game, String user) {
        File directory = new File(PATH_DIRECTORY);

        List<String> opponents = new LinkedList<>();

        for (File current : directory.listFiles()) {
            if (current.isFile() && current.getName().contains(game.getName())
                    && current.getName().contains(user)) {
                String[] fileNameComponents = current.getName().split("_");
                String toAdd = !fileNameComponents[2].equals(user)
                        ? fileNameComponents[2]
                        : fileNameComponents[4];
                opponents.add(toAdd);
                current.delete();
            }
        }
        return opponents;
    }


    public void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {

                if (users.size() < MAX_COUNT_ONLINE_USERS) {
                    Socket socket = serverSocket.accept();
                    onlineUsers.execute(new ClientHandler(socket));
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        GameServer gameServer = GameServer.getInstance();
        gameServer.run();

    }
}
