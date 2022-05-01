import java.io.*;
import java.net.Socket;
import java.util.Locale;
import java.util.Scanner;


public class TicTacToe {
    private char status = '\0';
    private char gameStatus[] = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private boolean gameReady = false;
    private boolean playsHaveBeenDone = false;  // for win condition
    private boolean iAmPlaying = true;
    private Socket socket = null;
    private BufferedReader reader;
    private PrintWriter writer;

    // for internal server
    private Thread serverThread = null;

    public TicTacToe() {
        Scanner scanner;
        gameReady = false;
        while (true) {
            if (gameReady) {
                System.out.println("client> Waiting for other player...");
                updateGame();
            }
            System.out.println(printGame());
            scanner = new Scanner(System.in);
            if (playsHaveBeenDone) {
                System.out.print("player " + printStatus() + "> ");
            }
            if (iAmPlaying && scanner.hasNextLine()) {
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
//                        gameReady = true;
                        playsHaveBeenDone = true;
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
        }
    }

    private void updateGame() {
        String ans = null;
        try {
            ans = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("client> from server: " + ans);
        iAmPlaying = true;

        if (ans.matches(".*\\d.*")) {
            gameStatus[Integer.parseInt(ans) - 1] = (status == 'x' ? 'o' : 'x');
        }
        if (ans.charAt(0) == 'T') {
            System.out.println("TIE!!!!");
            resetGame();
        }
        if (ans.charAt(0) =='x' || ans.charAt(0) == 'o') {
            //System.out.println("Checking for win condiiton");
            if (playsHaveBeenDone) {
                System.out.println("WINNER: " + ans + "!!!!");
                resetGame();
            } else {
                if (ans.charAt(0) != status) {
                    iAmPlaying = false;
                    //System.out.println("I'm not playing yet");
                    return;
                }
            }
        }

    }

    private void sendPlay(int n) {
        if (status == '\0') {
            return;
        }
        if (n < 1 || n > 9 ||
                gameStatus[n-1] == 'x' || gameStatus[n-1] == 'o') {
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
    private String printStatus() {
        return String.valueOf(status); //.toUpperCase(Locale.ROOT);
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
                if (!(myStatus == 'x' || myStatus == 'o')) {
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
        if (status != 'x' && status != 'o') {
            return "Choose a game to join: [connect ADDR PORT]";
        }
        return "[player " + status + "] Choose a position to play at:\n" + game;
    }
    private void resetGame() {
        for (int i=0; i<4; i++) {
            System.out.println();
        }
        for (int i=0; i<gameStatus.length; i++) {
            gameStatus[i] = String.valueOf(i+1).toCharArray()[0];   // sorry bro, java is stupid
        }
        status = '\0';
        playsHaveBeenDone = false;
        gameReady = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        reader = null;
        writer = null;
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
        gameReady = true;
    }

    public static void main(String[] args) {
        new TicTacToe();
    }
}
