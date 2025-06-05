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

    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 400;
    private static final long INPUT_DELAY = 50;
    private static final long ATTACK_DELAY = 500;

    private ServerConnection server;
    private GamePanel gamePanel;
    private Player player1;
    private Player player2;
    private Integer playerAbove = null;
    private boolean isPlayer2Active = false;
    private List<DestroyedTile> destroyedTiles;
    private List<Enemy> enemies;
    private List<Fruit> fruits;
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
    
    private boolean isBonusPhase = false;
    private int bonusPlayerId = -1;
    private int bonusTimeRemaining = 0;
    
    private final List<GameObserver> observers = new ArrayList<>();
    private static int spectatorCount = 0;
    private static final int MAX_SPECTATORS_PER_CLIENT = 2;


    public GameClient() {
        this.player1 = new Player();
        this.player2 = new Player();
        this.destroyedTiles = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.fruits = new ArrayList<>();
        this.inputHandler = new InputHandler();

        initializeUI();
        connectToServer();
        startInputTimer();
        startRegenerationTimer();
        setupSpectatorShortcut();
    }


    private void initializeUI() {
        setTitle("Juego de Plataformas - Cliente (2 Jugadores)");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel(player1, player2, destroyedTiles, enemies, fruits);

        createGameOverPanel();

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        gamePanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        gameOverPanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(gameOverPanel, JLayeredPane.POPUP_LAYER);

        add(layeredPane);

        addKeyListener(inputHandler);
        setFocusable(true);
        requestFocus();
        setVisible(true);
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
    
    public Integer getPlayerAbove() {
        return playerAbove;
    }
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    // Agregar este método setter en GameClient.java  
    public void setPlayerAbove(Integer playerAbove) {
        this.playerAbove = playerAbove;
    }
    
    public boolean isPlayer2Active() {
        return isPlayer2Active;
    }
    
    public void setPlayer2Active(boolean active) {
        this.isPlayer2Active = active;
        gamePanel.setPlayer2Active(active);
    }
    
    public boolean isBonusPhase() {
    return isBonusPhase;
}

    public void setBonusPhase(boolean bonusPhase) {
        this.isBonusPhase = bonusPhase;
        gamePanel.setBonusPhase(bonusPhase);
    }

    public int getBonusPlayerId() {
        return bonusPlayerId;
    }

    public void setBonusPlayerId(int bonusPlayerId) {
        this.bonusPlayerId = bonusPlayerId;
        gamePanel.setBonusPlayerId(bonusPlayerId);
    }

    public int getBonusTimeRemaining() {
        return bonusTimeRemaining;
    }

    public void setBonusTimeRemaining(int bonusTimeRemaining) {
        this.bonusTimeRemaining = bonusTimeRemaining;
        gamePanel.setBonusTimeRemaining(bonusTimeRemaining);
    }
    
    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    public List<Fruit> getFruits() {
        return fruits;
    }
    
    public void updateMap(int[][] gameMap, int mapWidth, int mapHeight) {
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        gamePanel.updateMap(gameMap, mapWidth, mapHeight);
        notifyObservers();
    }
    
    public int[][] getGameMap() {
        return gameMap;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public List<DestroyedTile> getDestroyedTiles() {
        return destroyedTiles;
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
    
    public void registerObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateUpdate();
        }
    }
    
    
    public void repaintGame() {
        gamePanel.repaint();
        notifyObservers();  // Notificar a los espectadores
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

        // Si estamos en fase bonus, solo permitir input del jugador bonus
        if (isBonusPhase) {
            if (bonusPlayerId == 1) {
                // Solo Player 1 puede controlar en fase bonus
                if (inputHandler.isPlayer1AttackPressed() && now - lastPlayer1AttackTime >= ATTACK_DELAY) {
                    server.send("X");
                    inputHandler.resetPlayer1Attack();
                    lastPlayer1AttackTime = now;
                    sent = true;
                }

                if (inputHandler.isPlayer1LeftPressed()) {
                    server.send("A");
                    sent = true;
                } else if (inputHandler.isPlayer1RightPressed()) {
                    server.send("D");
                    sent = true;
                }

                if (inputHandler.isPlayer1JumpPressed()) {
                    server.send("W");
                    inputHandler.resetPlayer1Jump();
                    sent = true;
                }
            } else if (bonusPlayerId == 2 && isPlayer2Active) {
                // Solo Player 2 puede controlar en fase bonus
                if (inputHandler.isPlayer2AttackPressed() && now - lastPlayer2AttackTime >= ATTACK_DELAY) {
                    server.send("SPACE");
                    inputHandler.resetPlayer2Attack();
                    lastPlayer2AttackTime = now;
                    sent = true;
                }

                if (inputHandler.isPlayer2LeftPressed()) {
                    server.send("LEFT");
                    sent = true;
                } else if (inputHandler.isPlayer2RightPressed()) {
                    server.send("RIGHT");
                    sent = true;
                }

                if (inputHandler.isPlayer2JumpPressed()) {
                    server.send("JUMP");
                    inputHandler.resetPlayer2Jump();
                    sent = true;
                }
            }
        } else {
            // Lógica normal del juego (código existente)
            // Verificar si se presionó 'I' para activar Player 2
            if (inputHandler.isActivatePlayer2Pressed() && !isPlayer2Active) {
                server.send("P");
                inputHandler.resetActivatePlayer2();
                sent = true;
            }

            // Manejar input del Player 1
            if (inputHandler.isPlayer1AttackPressed() && now - lastPlayer1AttackTime >= ATTACK_DELAY) {
                server.send("X");
                inputHandler.resetPlayer1Attack();
                lastPlayer1AttackTime = now;
                sent = true;
            }

            if (inputHandler.isPlayer1LeftPressed()) {
                server.send("A");
                sent = true;
            } else if (inputHandler.isPlayer1RightPressed()) {
                server.send("D");
                sent = true;
            }

            if (inputHandler.isPlayer1JumpPressed()) {
                server.send("W");
                inputHandler.resetPlayer1Jump();
                sent = true;
            }

            // Manejar input del Player 2 (solo si está activo)
            if (isPlayer2Active) {
                if (inputHandler.isPlayer2AttackPressed() && now - lastPlayer2AttackTime >= ATTACK_DELAY) {
                    server.send("P");
                    inputHandler.resetPlayer2Attack();
                    lastPlayer2AttackTime = now;
                    sent = true;
                }

                if (inputHandler.isPlayer2LeftPressed()) {
                    server.send("LEFT");
                    sent = true;
                } else if (inputHandler.isPlayer2RightPressed()) {
                    server.send("RIGHT");
                    sent = true;
                }

                if (inputHandler.isPlayer2JumpPressed()) {
                    server.send("JUMP");
                    inputHandler.resetPlayer2Jump();
                    sent = true;
                }
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
    
    // Dentro de GameClient.java
    private void setupSpectatorShortcut() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_O) {
                    if (spectatorCount >= MAX_SPECTATORS_PER_CLIENT) {
                        JOptionPane.showMessageDialog(null, "Máximo de espectadores alcanzado para este cliente.", "Límite alcanzado", JOptionPane.WARNING_MESSAGE);
                    } else {
                        new SpectatorClient(GameClient.this);
                        spectatorCount++;
                    }
                }
            }
        });
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

            if (args.length > 0 && args[0].equals("spectator")) {
                JOptionPane.showMessageDialog(null, "El cliente espectador debe iniciarse desde el cliente jugador.", "Modo inválido", JOptionPane.WARNING_MESSAGE);
            } else {
                GameClient jugador = new GameClient();
            }
        });
    }
}