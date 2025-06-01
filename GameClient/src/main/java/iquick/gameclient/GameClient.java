package iquick.gameclient;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.UIManager;

public class GameClient extends JFrame {

    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 1080;
    private static final long INPUT_DELAY = 50;
    private static final long ATTACK_DELAY = 500;

    private ServerConnection server;
    private GamePanel gamePanel;
    private Player player1;
    private Player player2;
    private boolean isPlayer2Active = false;
    private List<DestroyedTile> destroyedTiles;
    private List<Enemy> enemies;
    private Timer inputTimer;
    private Timer regenerationTimer;

    private InputHandler inputHandler;
    
    private long lastInputTime = 0;
    private long lastPlayer1AttackTime = 0;
    private long lastPlayer2AttackTime = 0;

    private int[][] gameMap;
    private int mapWidth;
    private int mapHeight;
    
    private boolean gameOverState = false;
    private JPanel gameOverPanel;
    private JButton restartButton;
    private JLayeredPane layeredPane;

    public GameClient() {
        this.player1 = new Player();
        this.player2 = new Player();
        this.destroyedTiles = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.inputHandler = new InputHandler();

        initializeUI();
        connectToServer();
        startInputTimer();
        startRegenerationTimer();
    }

    private void initializeUI() {
        setTitle("Juego de Plataformas - Cliente (2 Jugadores)");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel(player1, player2, destroyedTiles, enemies);
        add(gamePanel);

        addKeyListener(inputHandler);
        setFocusable(true);
        requestFocus();
        setVisible(true);
        
        createGameOverPanel();

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 1000));

        gamePanel.setBounds(0, 0, 800, 1000);
        gameOverPanel.setBounds(0, 0, 800, 1000);

        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(gameOverPanel, JLayeredPane.POPUP_LAYER);

        add(layeredPane);
    }

    private void connectToServer() {
        try {
            server = new ServerConnection("localhost", 8888, this);
            gamePanel.setConnectionStatus(true);
            System.out.println("Conectado al servidor.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "No se pudo conectar al servidor.",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // Métodos públicos para que ServerConnection pueda acceder a los datos del juego
    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    // Método legacy para compatibilidad (devuelve player1)
    public Player getPlayer() {
        return player1;
    }
    
    public boolean isPlayer2Active() {
        return isPlayer2Active;
    }
    
    public void setPlayer2Active(boolean active) {
        this.isPlayer2Active = active;
        gamePanel.setPlayer2Active(active);
    }
    
    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    public void updateMap(int[][] gameMap, int mapWidth, int mapHeight) {
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        gamePanel.updateMap(gameMap, mapWidth, mapHeight);
    }
    
    public void addDestroyedTile(int x, int y) {
        destroyedTiles.add(new DestroyedTile(x, y));
    }
    
    public void removeDestroyedTile(int x, int y) {
        destroyedTiles.removeIf(tile -> tile.x == x && tile.y == y);
    }
    
    public void updateMapTile(int x, int y, int value) {
        if (validTile(x, y)) {
            gameMap[y][x] = value;
        }
    }
    
    public void repaintGame() {
        gamePanel.repaint();
    }
    
    public void showGameOver() {
        gameOverState = true;
        gameOverPanel.setVisible(true);
        restartButton.requestFocus();
    }

    private boolean validTile(int x, int y) {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
    }

    private void startInputTimer() {
        inputTimer = new Timer(16, e -> handleInput());
        inputTimer.start();
    }

    private void createGameOverPanel() {
        gameOverPanel = new JPanel();
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setBackground(new Color(0, 0, 0, 150));
        gameOverPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel gameOverLabel = new JLabel("¡GAME OVER!");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("¡Ambos jugadores han perdido todas sus vidas!");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel controlsLabel = new JLabel("<html><center>Player 1: WASD + X para atacar<br>Player 2: Flechas + Espacio para atacar<br>Presiona 'I' para activar Player 2</center></html>");
        controlsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        controlsLabel.setForeground(Color.LIGHT_GRAY);
        controlsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        restartButton = new JButton("REINICIAR JUEGO");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.setMaximumSize(new Dimension(200, 50));
        restartButton.addActionListener(e -> restartGame());

        gameOverPanel.add(Box.createVerticalGlue());
        gameOverPanel.add(gameOverLabel);
        gameOverPanel.add(Box.createVerticalStrut(20));
        gameOverPanel.add(messageLabel);
        gameOverPanel.add(Box.createVerticalStrut(20));
        gameOverPanel.add(controlsLabel);
        gameOverPanel.add(Box.createVerticalStrut(30));
        gameOverPanel.add(restartButton);
        gameOverPanel.add(Box.createVerticalGlue());

        gameOverPanel.setVisible(false);
    }

    private void restartGame() {
        server.send("RESTART");
        gameOverState = false;
        SwingUtilities.invokeLater(() -> {
            gameOverPanel.setVisible(false);
            requestFocus();
        });
    }

    private void handleInput() {
    long now = System.currentTimeMillis();
    if (now - lastInputTime < INPUT_DELAY) return;

    boolean sent = false;

    // Verificar si se presionó 'I' para activar Player 2
    if (inputHandler.isActivatePlayer2Pressed() && !isPlayer2Active) {
        server.send("P");  // Usar P para activar Player 2
        inputHandler.resetActivatePlayer2();
        System.out.println("Enviando comando: P");
        sent = true;
    }

    // Manejar input del Player 1 - COMANDOS CORREGIDOS
    if (inputHandler.isPlayer1AttackPressed() && now - lastPlayer1AttackTime >= ATTACK_DELAY) {
        server.send("X");  // Comando simple para ataque Player 1
        inputHandler.resetPlayer1Attack();
        lastPlayer1AttackTime = now;
        System.out.println("Enviando comando: X");
        sent = true;
    }

    if (inputHandler.isPlayer1LeftPressed()) {
        server.send("A");  // Comando simple para izquierda Player 1
        System.out.println("Enviando comando: A");
        sent = true;
    } else if (inputHandler.isPlayer1RightPressed()) {
        server.send("D");  // Comando simple para derecha Player 1
        System.out.println("Enviando comando: D");
        sent = true;
    }

    if (inputHandler.isPlayer1JumpPressed()) {
        server.send("W");  // Comando simple para salto Player 1
        inputHandler.resetPlayer1Jump();
        System.out.println("Enviando comando: W");
        sent = true;
    }

    // Manejar input del Player 2 (solo si está activo) - COMANDOS CORREGIDOS
    if (isPlayer2Active) {
        if (inputHandler.isPlayer2AttackPressed() && now - lastPlayer2AttackTime >= ATTACK_DELAY) {
            server.send("P");  // Comando simple para ataque Player 2
            inputHandler.resetPlayer2Attack();
            lastPlayer2AttackTime = now;
            System.out.println("Enviando comando: P");
            sent = true;
        }

        if (inputHandler.isPlayer2LeftPressed()) {
            server.send("LEFT");  // Comando simple para izquierda Player 2
            System.out.println("Enviando comando: LEFT");
            sent = true;
        } else if (inputHandler.isPlayer2RightPressed()) {
            server.send("RIGHT");  // Comando simple para derecha Player 2
            System.out.println("Enviando comando: RIGHT");
            sent = true;
        }

        if (inputHandler.isPlayer2JumpPressed()) {
            server.send("JUMP");  // Comando simple para salto Player 2
            inputHandler.resetPlayer2Jump();
            System.out.println("Enviando comando: JUMP");
            sent = true;
        }
    }

    if (sent) lastInputTime = now;
}

    private void startRegenerationTimer() {
        regenerationTimer = new Timer(100, e -> {
            List<DestroyedTile> ready = new ArrayList<>();
            for (DestroyedTile tile : destroyedTiles) {
                if (tile.shouldRegenerate() && !tile.isRegenerating) {
                    tile.isRegenerating = true;
                    server.send("REGENERATE_TILE " + tile.x + " " + tile.y);
                    ready.add(tile);
                }
            }

            destroyedTiles.removeAll(ready);
            if (!ready.isEmpty()) SwingUtilities.invokeLater(() -> gamePanel.repaint());
        });
        regenerationTimer.start();
    }

    @Override
    public void dispose() {
        if (regenerationTimer != null) regenerationTimer.stop();
        if (inputTimer != null) inputTimer.stop();
        try {
            if (server != null) server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Usando L&F por defecto");
            }
            new GameClient();
        });
    }
}