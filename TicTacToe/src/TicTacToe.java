import java.io.*;
import java.net.Socket;
import java.util.Locale;
import java.util.Scanner;


public class TicTacToe {
    protected Character status = null;
    protected char gameStatus[] = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    protected boolean gameReady = false;
    protected boolean playsHaveBeenDone = false;  // for win condition
    protected boolean iAmPlaying = true;
    protected Socket socket = null;
    protected BufferedReader reader = null;
    protected PrintWriter writer = null;

    // for internal server
    protected Thread serverThread = null;

    public static void main(String[] args) {
        new TicTacToe();
    }

    public TicTacToe() {
        gameReady = false;
        runGame();
    }

    public void startServer() {
        serverThread = new Thread(new TicTacToeServer());
        serverThread.start();
        System.err.println("client> Starting server");
    }

    @Override
    public String toString() {
        return printGame();
    }

    protected void runGame() {
        go();
    }

    protected void go() {
        Scanner scanner;
        while (true) {
            redrawGame();
            if (gameReady) {
                System.out.print("player " + printStatus() + "> ");
            }
            scanner = new Scanner(System.in);
            if (iAmPlaying && scanner.hasNextLine()) {
                String read = scanner.nextLine();
                if (!read.isEmpty()) {
                    if (read.equals("start")) {
                        startServer();
                    }
                    if (read.equals("connect")) {
                        connect();
                        gameReady = true;
                    }
                    if (read.matches(".*\\d.*")) {
                        sendPlay(Integer.parseInt(read));
                        playsHaveBeenDone = true;
                    }
                    if (read.equals("done")) {
                        closeServer();
                        System.exit(0);
                        break;
                    }
                }
            }
        }
    }

    protected void redrawGame() {
        if (gameReady) {
            notifyUser("Waiting for other player...");
            updateGame();
        }
        System.out.println(printGame());
    }

    protected void updateGame() {
        String ans = null;
        try {
            ans = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ans == null) {
            return;
        }
        if (ans.charAt(0) == 'L') {
            notifyUser("Your opponent left the game :(");
            resetGame();
        }
        iAmPlaying = true;

        if (ans.matches(".*\\d.*")) {
            gameStatus[Integer.parseInt(ans) - 1] = (status == 'x' ? 'o' : 'x');
        }
        if (ans.charAt(0) == 'T') {
            notifyUser("TIE!!!!");
            resetGame();
        }
        if (ans.charAt(0) =='x' || ans.charAt(0) == 'o') {
            if (playsHaveBeenDone) {
                notifyUser("WINNER: " + printStatus(ans.charAt(0)) + "!!!!");
                resetGame();
            } else {
                if (ans.charAt(0) != status) {
                    iAmPlaying = false;
                }
            }
        }
    }

    protected void sendPlay(int n) {
        if (status == null) {
            return;
        }
        if (n < 1 || n > 9 ||
                gameStatus[n-1] == 'x' || gameStatus[n-1] == 'o') {
            System.err.println("Invalid play from " + printStatus() + " of " + n);
            return;
        }
        writer.println(n);
        writer.flush();
        gameStatus[n-1] = status;
    }

    protected void determineStatus() {
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
                status = myStatus;
                userStatus("You are player " + printStatus());
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // You are [X/O]
    protected void userStatus(String s) {
        System.out.println(s);
    }
    protected String printStatus() {
        return printStatus(status);
    }
    protected String printStatus(Character s) {
        return String.valueOf(s); //.toUpperCase(Locale.ROOT);
    }

    // State information while playing
    protected void notifyUser(String s) {
        System.out.println(s);
    }

    protected String printGame() {
        String game = "";
        for (int i = 0; i < gameStatus.length; i++) {
            game += gameStatus[i];
            if ((i+1) % 3 == 0) {
                game += "\n";
            } else {
                game += ", ";
            }
        }
        if (status == null) {
            return "Choose a game to join: [connect ADDR PORT]";
        }
        return "[player " + printStatus() + "] Choose a position to play at:\n" + game;
    }

    protected void connect() {
        connect("127.0.0.1", 5000);
    }

    protected void connect(String addr, int port) {
        if (socket == null) {
            try {
                socket = new Socket(addr, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        determineStatus();
    }

    public void resetGame() {
        for (int i=0; i<3; i++) {
            System.out.println();
        }
        for (int i=0; i<gameStatus.length; i++) {
            gameStatus[i] = String.valueOf(i+1).toCharArray()[0];   // sorry bro, java is stupid
        }
        status = null;
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

    protected void closeServer() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverThread != null) {
           // serverThread.interrupt();
        }
    }
}