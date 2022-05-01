import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TicTacToeServer implements Runnable {

    private char possibleStatus[] = {'X', 'o'};
    private char p1status = ' ';
    private char p2status = ' ';

    private ServerSocket serverSocket;
    private Socket socket1;
    private Socket socket2;
    private BufferedReader reader1;
    private BufferedReader reader2;
    private PrintWriter writer1;
    private PrintWriter writer2;

    public TicTacToeServer() {
        try {
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean running = false;
        while (!running) {
            try {
                if (socket1 == null) {
                    socket1 = serverSocket.accept();
                    System.out.println("Player 1 accepts " + socket1);

                }
                if (socket2 == null) {
                    socket2 = serverSocket.accept();
                    System.out.println("Player 2 accepts " + socket2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socket1 != null && socket2 != null) {
                running = true;
                System.out.println("server> Found 2 players");
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
        p2status = (p1status == 'X' ? 'O' : 'X');
        writer1.println(p1status);
        writer1.flush();
        writer2.println(p2status);
        writer2.flush();
        //gameplay updates
        while (running) {
            try {
                writer2.println(reader1.readLine());
                writer2.flush();

                writer1.println(reader2.readLine());
                writer1.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void printoutW1(String in) {
        writer1.println(in);
        writer1.flush();
    }
    private void printoutW2(String in) {
        writer2.println(in);
        writer2.flush();
    }
}
