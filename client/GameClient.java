package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.enums.Command;
import bg.sofia.uni.fmi.mjt.battleships.exceptions.MySocketException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient {

    private static final int INDEX_OF_COMMAND = 0;
    private static final int INDEX_OF_USERNAME = 1;
    private static final int PORT = 9999;
    private static final String localhost = "localhost";

    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isConnected;

    public GameClient() {
        this.isConnected = false;
    }

    public void run() {

        try (Scanner input = new Scanner(System.in)) {

            while (true) {

                String line = input.nextLine();
                String[] components = line.split(" ");
                String command = components[INDEX_OF_COMMAND].toLowerCase();

                if (command.equals(Command.CONNECT.getCommand())) {
                    String username = components[INDEX_OF_USERNAME];
                    connect(username);
                } else if (!isConnected) {
                    System.out.println("Not allowed to use the server. Please make sure you are connected");
                } else {
                    writer.println(line);
                    if (command.equals(Command.EXIT.getCommand())) {
                        break;
                    }
                }
            }
        }
    }

    public void connect(String username) {
        try {
            Socket socket = new Socket(localhost, PORT);
            isConnected = true;

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(username);

            Thread clientRunnable = new Thread(new ClientRunnable(socket));
            clientRunnable.start();
        } catch (IOException io) {
            throw new MySocketException("An I/O error occurred in socket", io);
        }
    }

    public static void main(String[] args) {
        GameClient gameClient = new GameClient();
        gameClient.run();
    }

}
