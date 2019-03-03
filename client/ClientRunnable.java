package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.exceptions.MySocketException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientRunnable implements Runnable {

    private Socket socket;

    public ClientRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (true) {
                String line = reader.readLine();

                if (line.equals("Disconnected from server")) {
                    System.out.println(line);
                    break;
                } else if (line.equals("menu> ") || line.equals("Enter 'play': ")
                        || line.equals("Write 'start' to start the game: ") || line.equals("Enter your turn: ")
                        || line.equals("Placement of ship(horizontal/vertical): ")
                        || line.equals("Enter ships's coordinates: ")
                        || line.equals("Enter your turn or 'save-game' if want to save game's state: ")) {
                    System.out.print(line);
                } else {
                    System.out.println(line);
                }
            }

            socket.close();

        } catch (IOException io) {
            throw new MySocketException("Socket is closed", io);
        }
    }
}
