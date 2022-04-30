import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest implements Runnable {
    private ServerSocket serverSocket;
    private Socket socket1;
    private Socket socket2;
    private BufferedReader reader1;
    private BufferedReader reader2;
    private PrintWriter writer1;
    private PrintWriter writer2;

    public ServerTest() {
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
                    break;
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
            System.out.println("server> looking");
        }
        try {
            writer1 = new PrintWriter(socket1.getOutputStream());
            // writer2 = new PrintWriter(socket2.getOutputStream());
            reader1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
            //reader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        running = true; //put this somewhere else
        System.out.println("Server started: " + running);
        System.out.println(writer1 + " " + reader1);
        writer1.println("test 1");
        while (running) {
            try {
                //while (!reader1.ready());
                String a = reader1.readLine();
                System.out.println(a);
                writer1.println(a);

//                if (reader1.ready()) {
//                    writer2.println(reader1.readLine());
//                }
//                if (reader2.ready()) {
//                    writer1.println(reader2.readLine());
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
