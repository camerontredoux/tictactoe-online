import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToe {
    private char status = '\0';
    private char gameStatus[] = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private boolean gameReady = false;
    private Socket socket = null;
    private BufferedReader reader;
    private PrintWriter writer;

    // for internal server
    private Thread serverThread = null;

    public TicTacToe() {
        Scanner scanner;
        gameReady = false;
        while (true) {
            System.out.println(printGame());
            scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String read = scanner.nextLine();
                if (!read.isEmpty()) {
                    if (read.equals("start")) {
                        startServer();
                    }
                    if (read.equals("connect")) {
                        connect();
                    }
                    if (read.matches(".*\\d.*")) {
                        sendPlay(Integer.parseInt(read));
                        gameReady = true;
                    }
                    if (read.equals("done")) {
                        serverThread.interrupt();
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                        //gameReady = false;
                    }
                }
            }

            if (gameReady) {
                updateGame();
            }
        }
    }

    private void updateGame() {
        String ans = null;
        try {
            ans = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[Client " + status + "] received " + ans);
    }

    private void sendPlay(int n) {
        if (n < 1 || n > 9) {
            System.err.println("Invalid play from " + status + " of " + n);
            return;
        }
        writer.println(n);
        writer.flush();
        gameStatus[n-1] = status;
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
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String ans = reader.readLine();
                char myStatus = ans.charAt(0);
                if (!(myStatus == 'X' || myStatus == 'O')) {
                    continue;
                }
                System.out.println("Found status " + ans);
                status = myStatus;
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        gameReady = true;
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

    public static void main(String[] args) {
        new TicTacToe();
    }
}
