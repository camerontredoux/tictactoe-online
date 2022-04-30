import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientTest {

    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintWriter writer = null;
    private Thread serverThread = null;

    private String addr = "127.0.0.1";
    private int port = 5000;

    public ClientTest() {
        serverThread = new Thread(new ServerTest());
        serverThread.start();
        if (socket == null) {
            try {
                socket = new Socket(addr, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("prompt>");
            if (scanner.hasNextLine()) {
                String a = scanner.nextLine();
                System.out.println("Scanner found " + a);
                writer.println(a);
            } else {
                System.out.println("scanner no work");
            }
            try {
                System.out.println(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ClientTest();
    }
}
