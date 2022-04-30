import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToe {
    private char status = '\0';
    private char possibleStatus[] = {'X', 'O'};
    private char gameStatus[] = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private boolean gameReady = false;
    private Socket socket = null;
    private BufferedReader reader;
    private PrintWriter writer;

    // for internal server
    private Thread serverThread = null;

    public TicTacToe() {
        Scanner scanner;
        gameReady = true;
        while (gameReady) {
            System.out.println(printGame());
            scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String read = scanner.nextLine();
                if (!read.isEmpty()) {
                    if (read.equals("start")) {
                        startServer();
                    }
                    if (read.equals("connect")) {
                        // connect to server
                        connect();
                    }
                    if (read.matches(".*\\d.*")) {
                        sendPlay(Integer.parseInt(read));
                    }
                    if (read.equals("done")) {
                        serverThread.interrupt();
                        break;
                        //gameReady = false;
                    }
                }
            }
        }
    }

    private void connect() {
        connect("127.0.0.1", 5000);
    }

    private void connect(String addr, int port) {
        if (socket == null) {
            try {
                socket = new Socket(addr, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        determineStatus();
    }
    private void sendPlay(int n) {
        if (n < 1 || n > 9) {
            System.out.println("Invalid play from " + status + " of " + n);
            return;
        }
        writer.println("played: " + n);
        System.out.println("Received play from " + status + " as " + n);
    }

    public char getStatus() {
        return status;
    }

    @Override
    public String toString() {
        String game = printGame();
        return game;
    }
    private void determineStatus() {
//        status = possibleStatus[(int) Math.round(Math.random())];
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                System.out.println("Trying");
                if (!reader.ready()) break;
                System.out.println("Trying succeeded");
                String ans = reader.readLine();
                char myStatus = ans.charAt(0);
                if (myStatus != 'X' && myStatus != 'O') {
                    continue;
                }
                System.out.println("Found status " + ans);
                status = myStatus;
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        gameReady = true;
    }
    private String printGame() {
        String game = "";
        for (int i = 0; i < gameStatus.length; i++) {
            game += gameStatus[i];
            if ((i+1) % 3 == 0) {
                game += "\n";
            } else {
                game += ", ";
            }
        }
        if (status != 'X' && status != 'O') {
            return "Choose a game to join: [connect ADDR PORT]";
        }
        return "[player " + status + "] Choose a position to play at:\n" + game;
    }
    private void startServer() {
        serverThread = new Thread(new TicTacToeServer());
        serverThread.start();
        System.out.println("Starting server");
    }

    public static void main(String[] args) {
        new TicTacToe();
    }
}
