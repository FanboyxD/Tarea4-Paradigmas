package iquick.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class Client extends JFrame implements KeyListener {
    private static final int MATRIX_WIDTH = 20;
    private static final int MATRIX_HEIGHT = 5;
    private static final int CELL_SIZE = 30;
    private static final int FPS = 60;
    private static final int FRAME_TIME = 1000 / FPS;
    
    private Player player;
    private EnemyManager enemyManager;
    private Platform[][] platforms;
    private int[][] gameMatrix;
    
    private Set<Integer> pressedKeys = new HashSet<>();
    
    private NetworkManager networkManager;
    
    private JPanel gamePanel;
    private JLabel statusLabel;
    private Timer gameTimer;
    
    public Client() {
        initializeGame();
        setupUI();
        connectToServer();
        startGameLoop();
    }
    
    private void initializeGame() {
        player = new Player(2 * CELL_SIZE, (MATRIX_HEIGHT - 2) * CELL_SIZE);
        enemyManager = new EnemyManager();
        enemyManager.setPlayerReference(player);
        platforms = new Platform[MATRIX_HEIGHT][MATRIX_WIDTH];
        gameMatrix = new int[MATRIX_HEIGHT][MATRIX_WIDTH];
        networkManager = new NetworkManager(this);
    }
    
    private void setupUI() {
        setTitle("Cliente del Juego de Plataformas - WASD para mover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        setLayout(new BorderLayout());
        
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);
            }
        };
        gamePanel.setPreferredSize(new Dimension(MATRIX_WIDTH * CELL_SIZE, MATRIX_HEIGHT * CELL_SIZE));
        gamePanel.setBackground(Color.LIGHT_GRAY);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);
        
        add(gamePanel, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel();
        statusLabel = new JLabel("Desconectado del servidor - Usa A/D para mover, W para saltar");
        infoPanel.add(statusLabel);
        add(infoPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        gamePanel.requestFocus();
    }
    
    private void startGameLoop() {
        gameTimer = new Timer(FRAME_TIME, e -> {
            updateGame();
            gamePanel.repaint();
        });
        gameTimer.start();
    }
    
    private void updateGame() {
        player.update(pressedKeys, platforms);
        enemyManager.updateAll(platforms);
    }
    
    private void connectToServer() {
        networkManager.connect();
    }
    
    public void updateGameData(int[][] matrix, Enemy[] enemies) {
        this.gameMatrix = matrix;
        enemyManager.updateEnemies(enemies);
        updateGameObjects();
    }
    
    private void updateGameObjects() {
        for (int i = 0; i < MATRIX_HEIGHT; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                if (gameMatrix[i][j] == 1) {
                    if (platforms[i][j] == null) {
                        platforms[i][j] = new Platform(j, i);
                    }
                } else {
                    platforms[i][j] = null;
                }
            }
        }
    }
    
    private void drawGame(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Plataformas
        for (int i = 0; i < MATRIX_HEIGHT; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                if (platforms[i][j] != null) {
                    platforms[i][j].draw(g2d);
                }
            }
        }
        
        // Jugador
        player.draw(g2d);
        
        // Enemigos
        enemyManager.drawAll(g2d);
        
        // Grid
        g2d.setColor(new Color(255, 255, 255, 50));
        for (int i = 0; i <= MATRIX_WIDTH; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, MATRIX_HEIGHT * CELL_SIZE);
        }
        for (int i = 0; i <= MATRIX_HEIGHT; i++) {
            g2d.drawLine(0, i * CELL_SIZE, MATRIX_WIDTH * CELL_SIZE, i * CELL_SIZE);
        }
    }
    
    public void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
    
    public static int getMatrixWidth() { return MATRIX_WIDTH; }
    public static int getMatrixHeight() { return MATRIX_HEIGHT; }
    public static int getCellSize() { return CELL_SIZE; }
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Ignorar errores de Look and Feel
            }
            new Client();
        });
    }
}
