import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TicTacToeServer implements Runnable {

    private final char[] possibleStatus = {'x', 'o'};
    private Character p1status = null;
    private Character p2status = null;

    private final char[] gameStatus = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static ServerSocket serverSocket;
    private Socket socket1;
    private Socket socket2;
    private BufferedReader reader1;
    private BufferedReader reader2;
    private PrintWriter writer1;
    private PrintWriter writer2;

    public TicTacToeServer() throws IOException {
        serverSocket = new ServerSocket(TicTacToe.port);
    }

    @Override
    public void run() {
        boolean serverUp = true;
        while (serverUp) { // lifetime of server
            while (true) {
                try {
                    if (socket1 == null) {
                        socket1 = serverSocket.accept();
                        System.err.println("server> Player 1 accepts " + socket1);
                    }
                    if (socket2 == null) {
                        socket2 = serverSocket.accept();
                        System.err.println("server> Player 2 accepts " + socket2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket1 != null && socket2 != null) {
                    System.err.println("server> Found 2 players");
                    break;
                }
            }

            try {
                reader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                reader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
                writer1 = new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()));
                writer2 = new PrintWriter(new OutputStreamWriter(socket2.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            p1status = possibleStatus[(int) Math.round(Math.random())];
            p2status = (p1status == 'x' ? 'o' : 'x');
            writer1.println(p1status);
            writer1.flush();
            writer2.println(p2status);
            writer2.flush();

            writer1.println(p1status);
            writer1.flush();
            writer2.println(p1status);
            writer2.flush();

            while (true) { // each individual game
                try {
                    String p1 = reader1.readLine();
                    if (p1 == null) {
                        playerLeft();
                        break;
                    }
                    int p1play = Integer.parseInt(p1);
                    System.out.println("server> P1 played: " + p1play);
                    gameStatus[p1play - 1] = p1status;
                    if (checkGame()) {
                        break;
                    }
                    writer2.println(p1play);
                    writer2.flush();

                    String p2 = reader2.readLine();
                    if (p2 == null) {
                        playerLeft();
                        break;
                    }
                    int p2play = Integer.parseInt(p2);
                    System.out.println("server> P2 played: " + p2play);
                    gameStatus[p2play - 1] = p2status;
                    if (checkGame()) {
                        break;
                    }
                    writer1.println(p2play);
                    writer1.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("server> New Game");
            writer1 = null;
            writer2 = null;
            reader1 = null;
            reader2 = null;
            try {
                socket1.close();
                socket2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket1 = null;
            socket2 = null;
            for (int i = 0; i < gameStatus.length; i++) {
                gameStatus[i] = String.valueOf(i + 1).toCharArray()[0];   // sorry bro, java is stupid
            }
            p1status = null;
            p2status = null;
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkGame() {
        Character winner = null;
        for (int i = 0; i < 3; i++) {
            // vertical
            if ((gameStatus[i] == gameStatus[i + 3]) && (gameStatus[i] == gameStatus[i + 6])) {
                winner = gameStatus[i];
            }
            // horizontal
            if ((gameStatus[3 * i] == gameStatus[(3 * i) + 1]) && (gameStatus[3 * i] == gameStatus[(3 * i) + 2])) {
                winner = gameStatus[3 * i];
            }
        }
        // negative slope diagonal
        if ((gameStatus[0] == gameStatus[4]) && (gameStatus[0] == gameStatus[8])) {
            winner = gameStatus[0];
        }
        // positive slope diagonal
        if ((gameStatus[2] == gameStatus[4]) && (gameStatus[2] == gameStatus[6])) {
            winner = gameStatus[2];
        }

        if (winner != null) {
            writer1.println(winner);
            writer1.flush();
            writer2.println(winner);
            writer2.flush();
            return true;
        }

        boolean foundTie = true;
        for (char status : gameStatus) {
            if (Character.isDigit(status)) {
                foundTie = false;
            }
        }
        if (foundTie) {
            writer1.println('T');
            writer1.flush();
            writer2.println('T');
            writer2.flush();
            return true;
        }

        return false;
    }

    private void playerLeft() {
        writer1.println('L');
        writer1.flush();
        writer2.println('L');
        writer2.flush();
    }
}