import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class MainScreen extends TicTacToe {
    public static JFrame frame;
    private JPanel panel;
    private JButton server;
    private JButton play;
    private JButton quit;
    private JLabel title;
    private JLabel user;
    private JPanel header;
    private JButton donate;
    private JPanel options;
    private JPanel left_options;
    private JPanel right_options;
    private JFrame game;
    private JPanel board;
    private Border padding;
    private JPanel game_header;
    private JButton game_quit;
    private JLabel game_title;
    private JLabel current_player;
    private JPanel play_screen;
    private JButton[] play_buttons;

    public static void main(String[] args) {
        new MainScreen().runGame();
    }


    public MainScreen() {
        createMenu();
        createGame();
    }

    private void createMenu() {
        frame = new JFrame("Welcome to Tic Tac Toe - New Server/Game");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        board = new JPanel(new GridLayout(3, 3));
        play_buttons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            play_buttons[i] = new JButton();
            play_buttons[i].addActionListener(new PlayButtonActionListener());
            play_buttons[i].setName(String.valueOf(i + 1));
            play_buttons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            board.add(play_buttons[i]);
        }

        panel = new JPanel(new BorderLayout());

        server = new JButton("Create Server");
        server.setBackground(new Color(0, 150, 255));
        server.setOpaque(true);
        server.setBorderPainted(false);
        server.setForeground(new Color(0, 0, 0));
        server.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        server.setMinimumSize(new Dimension(180, 20));
        server.setMaximumSize(new Dimension(180, 20));

        play = new JButton("Join Game");
        play.setBackground(new Color(0, 255, 100));
        play.setOpaque(true);
        play.setBorderPainted(false);
        play.setForeground(new Color(0, 0, 0));
        play.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        play.setMinimumSize(new Dimension(180, 20));
        play.setMaximumSize(new Dimension(180, 20));
//        play.setEnabled(false);
        play.setEnabled(true);
        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setTitle("Waiting for 2nd player...");
                play.setEnabled(false);
                frame.repaint();
                connect();
                gameReady = true;
                frame.setVisible(false);
                frame.setTitle("Welcome to Tic Tac Toe - New Server/Game");
                game.setVisible(true);
                redrawGame();
                if (!iAmPlaying) {
                    redrawGame();
                }
            }
        });

        quit = new JButton("Quit Game");
        quit.setBackground(new Color(255, 0, 50));
        quit.setOpaque(true);
        quit.setBorderPainted(false);
        quit.setForeground(new Color(255, 255, 255));
        quit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quit.setPreferredSize(new Dimension(120, 52));
        quit.addActionListener(e -> {
            closeServer();
            System.exit(0);
        });

        title = new JLabel("TIC TAC TOE", SwingConstants.CENTER);
        title.setFont(new Font("Sans", Font.ITALIC, 28));

        user = new JLabel("You are: " + System.getProperty("user.name"), SwingConstants.CENTER);
        header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        user.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(user);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        donate = new JButton("Donate");
        donate.setBackground(new Color(255, 230, 0));
        donate.setOpaque(true);
        donate.setBorderPainted(false);
        donate.setForeground(new Color(0, 0, 0));
        donate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        donate.setMinimumSize(new Dimension(180, 20));
        donate.setMaximumSize(new Dimension(180, 20));

        options = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        left_options = new JPanel();
        right_options = new JPanel();
        right_options.setLayout(new BoxLayout(right_options, BoxLayout.Y_AXIS));
        left_options.setLayout(new BoxLayout(left_options, BoxLayout.Y_AXIS));
        play.setAlignmentX(Component.CENTER_ALIGNMENT);
        server.setAlignmentX(Component.CENTER_ALIGNMENT);
        donate.setAlignmentX(Component.CENTER_ALIGNMENT);
        left_options.add(play);
        left_options.add(Box.createVerticalStrut(5));
        left_options.add(server);
        left_options.add(Box.createVerticalStrut(5));
        left_options.add(donate);

        right_options.add(quit);

        options.add(left_options);
        options.add(right_options);

        panel.add(BorderLayout.NORTH, header);
        panel.add(BorderLayout.SOUTH, options);

        server.addActionListener(e -> {
            server.setEnabled(false);
            //play.setEnabled(true);
            try {
                startServer();
            } catch (IOException ex) {
                System.out.println("Server already exists on this port (" + TicTacToe.port + ")");
            }

            JLabel started = new JLabel("Server Started @ 127.0.0.1:" + TicTacToe.port, SwingConstants.CENTER);
            started.setForeground(Color.RED);
            panel.add(BorderLayout.CENTER, started);
            SwingUtilities.updateComponentTreeUI(frame);
        });

        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    private void createGame() {
        game = new JFrame("Tic Tac Toe");
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setSize(370, 400);

        padding = BorderFactory.createEmptyBorder(10, 20, 10, 20);
        board.setBorder(padding);
        game_header = new JPanel();

        game_quit = new JButton("Quit");
        game_quit.setBackground(new Color(255, 0, 50));
        game_quit.setOpaque(true);
        game_quit.setBorderPainted(false);
        game_quit.setForeground(new Color(255, 255, 255));
        game_quit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        game_quit.setPreferredSize(new Dimension(80, 20));
        game_quit.addActionListener(e -> {
            game.dispose();
            MainScreen.frame.setVisible(true);
        });

        game_title = new JLabel("Tic Tac Toe Online", SwingConstants.CENTER);
        current_player = new JLabel("You are player " + printStatus(), SwingConstants.CENTER);

        game_header.add(game_title);
        game_header.add(Box.createHorizontalStrut(100));
        game_header.add(game_quit);

        play_screen = new JPanel(new BorderLayout());

        play_screen.add(BorderLayout.NORTH, game_header);
        play_screen.add(BorderLayout.CENTER, board);
        play_screen.add(BorderLayout.SOUTH, current_player);

        play_screen.setBorder(padding);

        game.add(play_screen);
        game.setLocationRelativeTo(null);
    }

    @Override
    protected void runGame() {
        frame.setVisible(true);
    }

    @Override
    protected void redrawGame() {
        if (gameReady) {
            redrawView();
            notifyUser("Waiting for other player...");
            Thread.yield();
            updateGame();
            System.out.println("Received update " + iAmPlaying); // TODO remove
            if (iAmPlaying) {
                notifyUser("You are playing now");
            }
//            } else {
//                notifyUser("Waiting for other player...");
//            }
            redrawView();
        }
    }
    private void redrawView() {
        System.out.println(gameStatus); // TODO remove
        for (int i = 0; i < play_buttons.length; i++) {
            //System.out.println("Updating to " + String.valueOf(gameStatus[i])); // TODO remove
            play_buttons[i].setText(String.valueOf(gameStatus[i]));
            if (Character.isDigit(gameStatus[i])) {
                play_buttons[i].setEnabled(true);
            }
        }
        if (!iAmPlaying) {
            for (int i = 0; i < play_buttons.length; i++) {
                play_buttons[i].setEnabled(false);
            }
        }
        game.revalidate();
//        game.repaint();
        if (iAmPlaying) {
            game.setVisible(true);
        }
    }

    @Override
    protected void notifyUser(String s) {
        game.setTitle(s);
        if (s.toLowerCase(Locale.ROOT).contains("win") ||
                s.toLowerCase(Locale.ROOT).contains("tie")) {
            iAmPlaying = false;
            JOptionPane.showMessageDialog(null, s);
        }
    }

    @Override
    public void resetGame() {
        for (int i = 0; i < gameStatus.length; i++) {
            gameStatus[i] = String.valueOf(i + 1).toCharArray()[0];   // sorry bro, java is stupid
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
        game.setVisible(false);
        frame.setVisible(true);
        play.setEnabled(true);
    }

    @Override
    protected void userStatus(String s) {
        current_player.setText(s);
    }


    class PlayButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            playsHaveBeenDone = true;
            JButton b = (JButton) e.getSource();
            b.setText(printStatus());
            b.repaint();
            sendPlay(Integer.parseInt(b.getName()));
            iAmPlaying = false;
            redrawGame();
        }
    }
}
