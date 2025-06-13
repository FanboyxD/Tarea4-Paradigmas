package iquick.client;

import iquick.client.facade.NetworkManager;
import iquick.client.observer.SpectatorWindow;
import iquick.client.observer.GameState;
import iquick.client.observer.GameObserver;
import iquick.client.factory.Fruit;
import iquick.client.factory.Enemy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class Client extends JFrame implements KeyListener {
    private static final int MATRIX_WIDTH = 26;
    private static final int MATRIX_HEIGHT = 24;
    private static final int CELL_SIZE = 30;
    private static final int FPS = 60;
    private static final int FRAME_TIME = 1000 / FPS;
    private static final int STATS_PANEL_WIDTH = 200;
    private static final int MAX_OBSERVERS = 2;
    
    private Player player;
    private Player player2;
    private boolean player2Active = false;
    private EnemyManager enemyManager;
    private Platform[][] platforms;
    private static Platform[][] currentPlatforms;
    private int[][] gameMatrix;
    private boolean bonusModeActive = false;
    private Timer bonusTimer;
    private int[][] originalMatrix;
    private int[][] bonusMatrix;
    private static final int BONUS_DURATION = 30000; // 30 segundos en millisegundos
    private static final double SPEED_INCREMENT_FACTOR = 1.2;
    private int bonusCompletedCount = 0; // Contador de bonus completados
    
    private Set<Integer> pressedKeys = new HashSet<>();
    
    // Observer pattern fields
    private List<GameObserver> observers = new ArrayList<>();
    private List<SpectatorWindow> spectatorWindows = new ArrayList<>();
    private int nextSpectatorId = 1;
    
    private NetworkManager networkManager;
    private FruitManager fruitManager;
    
    private JPanel gamePanel;
    private JPanel statsPanel;
    private JLabel statusLabel;
    private JLabel livesLabel;
    private JLabel scoreLabel;
    private JLabel scoreLabel2;
    private JLabel livesLabel2;
    private JProgressBar livesBar2;
    private JProgressBar livesBar;
    private Timer gameTimer;
    
    public Client() {
        initializeGame();
        setupUI();
        connectToServer();
        startGameLoop();
    }
    
    // Método para agregar un observer al cliente
    /**
     * Agrega un observador a la lista de observadores si no se ha alcanzado el número máximo permitido.
     * 
     * @param observer El observador que se desea agregar.
     * @throws NullPointerException si el observador proporcionado es null.
     * 
     * Imprime en consola un mensaje indicando si el observador fue agregado exitosamente o si se alcanzó el límite máximo de observadores.
     */
    public void addObserver(GameObserver observer) {
        if (observers.size() < MAX_OBSERVERS) {
            observers.add(observer);
            System.out.println("Observer agregado. Total: " + observers.size() + "/" + MAX_OBSERVERS);
        } else {
            System.out.println("Máximo de observadores alcanzado (" + MAX_OBSERVERS + ")");
        }
    }
    
    /**
     * Elimina un observador de la lista de observadores registrados.
     * 
     * @param observer El observador que se desea eliminar.
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
        System.out.println("Observer removido. Total: " + observers.size() + "/" + MAX_OBSERVERS);
    }
    
    /**
     * Notifica a todos los observadores registrados sobre el cambio en el estado del juego.
     * Si la lista de observadores no está vacía, crea una nueva instancia de GameState
     * con la información actual del juego y llama al método updateGameState de cada observador,
     * pasando el nuevo estado del juego como parámetro.
     */
    private void notifyObservers() {
        if (!observers.isEmpty()) {
            GameState gameState = new GameState(
                player, player2, player2Active, platforms, 
                enemyManager, fruitManager, gameMatrix, statusLabel.getText()
            );
            
            for (GameObserver observer : observers) {
                observer.updateGameState(gameState);
            }
        }
    }
    
    /**
     * Notifica a todos los observadores registrados sobre un cambio en el estado de la conexión.
     *
     * @param status El nuevo estado de la conexión que se notificará a los observadores.
     */
    private void notifyObserversStatus(String status) {
        for (GameObserver observer : observers) {
            observer.updateConnectionStatus(status);
        }
    }
    
    /**
     * Crea una nueva ventana de espectador si no se ha alcanzado el número máximo permitido.
     * Si el límite de espectadores ha sido alcanzado, muestra un mensaje de advertencia al usuario.
     * Al crear una nueva ventana de espectador, se le asigna un identificador único, se añade a la lista de ventanas de espectadores,
     * se registra como observador y se muestra la ventana. Finalmente, actualiza el título de la ventana principal del cliente.
     */
    private void createSpectatorWindow() {
    if (observers.size() >= MAX_OBSERVERS) {
        JOptionPane.showMessageDialog(this, 
            "Máximo de espectadores alcanzado (" + MAX_OBSERVERS + ")", 
            "Límite de Espectadores", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    SpectatorWindow spectator = new SpectatorWindow(nextSpectatorId++);
    spectatorWindows.add(spectator);
    addObserver(spectator);

    spectator.setVisible(true);
    
    // Actualizar el título del cliente principal
    updateMainWindowTitle();
    
    System.out.println("Ventana de espectador creada. ID: " + (nextSpectatorId - 1));
}
    
    /**
     * Actualiza el título de la ventana principal del cliente.
     * El título incluye una descripción básica de los controles y, si hay observadores conectados,
     * muestra la cantidad actual de espectadores junto con el máximo permitido.
     */
    private void updateMainWindowTitle() {
        String title = "Cliente del Juego de Plataformas - WASD para mover";
        if (!observers.isEmpty()) {
            title += " [Espectadores: " + observers.size() + "/" + MAX_OBSERVERS + "]";
        }
        setTitle(title);
    }
    
    private void initializeGame() {
        player = new Player(2 * CELL_SIZE, (MATRIX_HEIGHT - 2) * CELL_SIZE);
        player2 = new Player(17 * CELL_SIZE, (MATRIX_HEIGHT - 2) * CELL_SIZE);
        enemyManager = new EnemyManager();
        fruitManager = new FruitManager();
        enemyManager.setPlayerReference(player);
        platforms = new Platform[MATRIX_HEIGHT][MATRIX_WIDTH];
        gameMatrix = new int[MATRIX_HEIGHT][MATRIX_WIDTH];
        networkManager = new NetworkManager(this);
        
        // NUEVO: Inicializar matriz bonus
        initializeBonusMatrix();
        
        player.setNetworkManager(networkManager);
        player2.setNetworkManager(networkManager);
        
        // NUEVO: Establecer referencias del cliente en los jugadores
        player.setClientReference(this);
        player2.setClientReference(this);
    }
    
    // Método para inicializar la matriz bonus (agregar en initializeGame())
private void initializeBonusMatrix() {
    bonusMatrix = new int[MATRIX_HEIGHT][MATRIX_WIDTH];
    
    // Ejemplo de matriz con plataformas especiales y normales
    int[][] customMatrix = {
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
        {0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
        {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1},
        {0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    
    // Copiar la matriz personalizada
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
        for (int j = 0; j < MATRIX_WIDTH; j++) {
            bonusMatrix[i][j] = customMatrix[i][j];
        }
    }
}

// Método para activar el modo bonus
public void activateBonusMode() {
    if (bonusModeActive) {
        return; // Ya está activo
    }
    enemyManager.clearAll();
    bonusModeActive = true;
    fruitManager.setGameMatrix(bonusMatrix);
    
    // Respaldar la matriz original
    if (originalMatrix == null) {
        originalMatrix = new int[MATRIX_HEIGHT][MATRIX_WIDTH];
    }
    
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
        for (int j = 0; j < MATRIX_WIDTH; j++) {
            originalMatrix[i][j] = gameMatrix[i][j];
        }
    }
    
    // Cambiar a la matriz bonus
    gameMatrix = bonusMatrix;
    updateGameObjects();
    
    // CAMBIO: Guardar el tiempo de inicio del modo bonus
    final long bonusStartTime = System.currentTimeMillis();
    
    // Actualizar el estado visual
    updateConnectionStatus("¡MODO BONUS ACTIVADO! - Tiempo restante: 30 segundos");
    
    // Iniciar el timer principal
    bonusTimer = new Timer(BONUS_DURATION, e -> deactivateBonusMode());
    bonusTimer.setRepeats(false);
    bonusTimer.start();
    
    // CAMBIO: Timer corregido para actualizar el contador visual
    Timer countdownTimer = new Timer(1000, null);
    countdownTimer.addActionListener(e -> {
        if (bonusModeActive && bonusTimer != null) {
            // CORRECCION: Calcular tiempo transcurrido correctamente
            long elapsed = System.currentTimeMillis() - bonusStartTime;
            long remaining = BONUS_DURATION - elapsed;
            int secondsLeft = (int) Math.max(0, remaining / 1000);
            
            updateConnectionStatus("¡MODO BONUS ACTIVADO! - Tiempo restante: " + secondsLeft + " segundos");
            
            if (secondsLeft <= 0) {
                countdownTimer.stop();
            }
        } else {
            countdownTimer.stop();
        }
    });
    countdownTimer.start();
    
    System.out.println("¡Modo bonus activado por 30 segundos!");
}

// Método para desactivar el modo bonus
private void deactivateBonusMode() {
    // Calcular posiciones seguras
    double player1X = 2 * CELL_SIZE;
    double player1Y = (MATRIX_HEIGHT - 2) * CELL_SIZE;
    double player2X = 17 * CELL_SIZE;
    double player2Y = (MATRIX_HEIGHT - 2) * CELL_SIZE;
    if (!bonusModeActive) {
        return;
    }
    
    bonusModeActive = false;
    fruitManager.clearAll();
    player.setPosition(player1X, player1Y);
    player.resetVelocity();
    
    if (player2Active) {
        // Resetear el jugador 2
        player2.setPosition(player2X, player2Y);
        player2.resetVelocity();
    }
    
    // Incrementar contador de bonus completados
    bonusCompletedCount++;
    // Calcular nuevo multiplicador de velocidad
    double newSpeedMultiplier = Math.pow(SPEED_INCREMENT_FACTOR, bonusCompletedCount);
    
    // Aplicar nueva velocidad a todos los enemigos
    enemyManager.increaseAllEnemySpeed(newSpeedMultiplier);
    networkManager.sendMessage("BONUSOFF");
    // Restaurar la matriz original
    if (originalMatrix != null) {
        gameMatrix = originalMatrix;
        updateGameObjects();
    }
    
    // Limpiar el timer
    if (bonusTimer != null) {
        bonusTimer.stop();
        bonusTimer = null;
    }
    
    // Actualizar el estado visual
    updateConnectionStatus("Modo bonus terminado - Regresando al juego normal");
    
    System.out.println("Modo bonus desactivado, regresando a la matriz original");
}
     
    public static Platform[][] getCurrentPlatforms() {
        return currentPlatforms;
    }
    
    private void setupUI() {
        setTitle("Cliente del Juego de Plataformas - WASD para mover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Agregar WindowListener para manejar el cierre de ventanas espectadoras
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAllSpectatorWindows();
            }
        });
        
        setLayout(new BorderLayout());
        
        // Panel principal que contiene el juego y las estadísticas
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel del juego
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
        
        // Panel de estadísticas
        setupStatsPanel();
        
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel de información inferior
        JPanel infoPanel = new JPanel();
        statusLabel = new JLabel("Desconectado del servidor - Usa A/D para mover, W para saltar, X para atacar");
        infoPanel.add(statusLabel);
        add(infoPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        gamePanel.requestFocus();
    }
    // Getter para saber si el modo bonus está activo (útil para los observadores)
public boolean isBonusModeActive() {
    return bonusModeActive;
}
    
    private void closeAllSpectatorWindows() {
        for (SpectatorWindow spectator : spectatorWindows) {
            spectator.dispose();
        }
        spectatorWindows.clear();
        observers.clear();
    }
    
    private void setupStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setPreferredSize(new Dimension(STATS_PANEL_WIDTH, MATRIX_HEIGHT * CELL_SIZE));
        statsPanel.setBackground(new Color(240, 240, 240));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        
        // JUGADOR 1
        JLabel titleLabel1 = new JLabel("JUGADOR 1");
        titleLabel1.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel1.setForeground(Color.BLUE);
        titleLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(titleLabel1);
        
        JLabel scoreTitle1 = new JLabel("PUNTUACIÓN:");
        scoreTitle1.setFont(new Font("Arial", Font.BOLD, 10));
        scoreTitle1.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(scoreTitle1);
        
        scoreLabel = new JLabel("0");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLabel.setForeground(new Color(0, 128, 0));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(scoreLabel);
        
        JLabel livesTitle1 = new JLabel("VIDAS:");
        livesTitle1.setFont(new Font("Arial", Font.BOLD, 10));
        livesTitle1.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(livesTitle1);
        
        livesLabel = new JLabel("3");
        livesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        livesLabel.setForeground(new Color(200, 0, 0));
        livesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(livesLabel);
        
        livesBar = new JProgressBar(0, 3);
        livesBar.setValue(3);
        livesBar.setStringPainted(true);
        livesBar.setString("❤❤❤");
        livesBar.setForeground(new Color(220, 50, 50));
        livesBar.setMaximumSize(new Dimension(150, 20));
        livesBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(livesBar);
        
        statsPanel.add(Box.createVerticalStrut(10));
        
        // JUGADOR 2
        JLabel titleLabel2 = new JLabel("JUGADOR 2");
        titleLabel2.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel2.setForeground(Color.RED);
        titleLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(titleLabel2);
        
        JLabel scoreTitle2 = new JLabel("PUNTUACIÓN:");
        scoreTitle2.setFont(new Font("Arial", Font.BOLD, 10));
        scoreTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(scoreTitle2);
        
        scoreLabel2 = new JLabel("0");
        scoreLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLabel2.setForeground(new Color(0, 128, 0));
        scoreLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(scoreLabel2);
        
        JLabel livesTitle2 = new JLabel("VIDAS:");
        livesTitle2.setFont(new Font("Arial", Font.BOLD, 10));
        livesTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(livesTitle2);
        
        livesLabel2 = new JLabel("3");
        livesLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
        livesLabel2.setForeground(new Color(200, 0, 0));
        livesLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(livesLabel2);
        
        livesBar2 = new JProgressBar(0, 3);
        livesBar2.setValue(3);
        livesBar2.setStringPainted(true);
        livesBar2.setString("❤❤❤");
        livesBar2.setForeground(new Color(220, 50, 50));
        livesBar2.setMaximumSize(new Dimension(150, 20));
        livesBar2.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(livesBar2);
        
        statsPanel.add(Box.createVerticalStrut(15));
        
        // Botones de control
        JButton resetButton = new JButton("Reiniciar Juego");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> resetGame());
        statsPanel.add(resetButton);
        
        statsPanel.add(Box.createVerticalStrut(5));
        
        JButton addScoreButton = new JButton("+ 100 P1");
        addScoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addScoreButton.addActionListener(e -> {
            player.addScore(100);
            updateStatsDisplay();
        });
        statsPanel.add(addScoreButton);
        
        JButton addScoreButton2 = new JButton("+ 100 P2");
        addScoreButton2.setAlignmentX(Component.CENTER_ALIGNMENT);
        addScoreButton2.addActionListener(e -> {
            if (player2Active) {
                player2.addScore(100);
                updateStatsDisplay();
            }
        });
        statsPanel.add(addScoreButton2);
        
        // Botón para crear espectador
        statsPanel.add(Box.createVerticalStrut(10));
        JButton spectatorButton = new JButton("Crear Espectador");
        spectatorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        spectatorButton.addActionListener(e -> createSpectatorWindow());
        statsPanel.add(spectatorButton);
        
        statsPanel.add(Box.createVerticalGlue());
        
        // Información de controles actualizada
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Controles"));
        
        String[] controls = {
            "P1: WASD + X", 
            "P2: ↑↓←→ + P",
            "I - Activar P2",
            "O - Crear Espectador"
        };
        for (String control : controls) {
            JLabel controlLabel = new JLabel(control);
            controlLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            controlLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            controlsPanel.add(controlLabel);
        }
        
        statsPanel.add(controlsPanel);
    }
    
    private void updateStatsDisplay() {
        SwingUtilities.invokeLater(() -> {
            // Player 1
            scoreLabel.setText(String.valueOf(player.getScore()));
            livesLabel.setText(String.valueOf(player.getLives()));
            livesBar.setValue(player.getLives());
            
            String heartsString = "";
            for (int i = 0; i < player.getLives(); i++) {
                heartsString += "❤";
            }
            for (int i = player.getLives(); i < 3; i++) {
                heartsString += "♡";
            }
            livesBar.setString(heartsString);
            
            // Player 2 (si está activo)
            if (player2Active) {
                scoreLabel2.setText(String.valueOf(player2.getScore()));
                livesLabel2.setText(String.valueOf(player2.getLives()));
                livesBar2.setValue(player2.getLives());
                
                String heartsString2 = "";
                for (int i = 0; i < player2.getLives(); i++) {
                    heartsString2 += "❤";
                }
                for (int i = player2.getLives(); i < 3; i++) {
                    heartsString2 += "♡";
                }
                livesBar2.setString(heartsString2);
            }
            
            // Game Over solo si ambos jugadores están muertos
            if (!player.isAlive() && (!player2Active || !player2.isAlive())) {
                statusLabel.setText("¡GAME OVER! - Presiona Reiniciar Juego para continuar");
            }
        });
    }
    
    private void resetGame() {
    // Calcular posiciones seguras
    double player1X = 2 * CELL_SIZE;
    double player1Y = (MATRIX_HEIGHT - 2) * CELL_SIZE;
    double player2X = 17 * CELL_SIZE;
    double player2Y = (MATRIX_HEIGHT - 2) * CELL_SIZE;
    
    // Resetear el jugador 1
    player.reset();
    player.setPosition(player1X, player1Y);
    player.resetVelocity();
    
    if (player2Active) {
        // Resetear el jugador 2
        player2.reset();
        player2.setPosition(player2X, player2Y);
        player2.resetVelocity();
    }
    
    // Limpiar frutas
    fruitManager.clearAll();
    
    enemyManager.clearAll();
    
    // Actualizar display
    updateStatsDisplay();
    
    // Actualizar status
    statusLabel.setText("Juego reiniciado - Usa A/D para mover, W para saltar" + 
                       (player2Active ? " | Jugador 2: Flechas para mover" : ""));
    
    // Forzar repaint inmediato
    gamePanel.repaint();
    gamePanel.requestFocus();
}
    
    private void startGameLoop() {
        gameTimer = new Timer(FRAME_TIME, e -> {
            updateGame();
            updateStatsDisplay();
            gamePanel.repaint();
            // Notificar a los observadores
            notifyObservers();
        });
        gameTimer.start();
    }
    
    private void updateGame() {
        // Actualizar plataformas (regeneración)
        for (int i = 0; i < MATRIX_HEIGHT; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                if (platforms[i][j] != null) {
                    platforms[i][j].update();
                }
            }
        }
        
        // Hacer las plataformas accesibles estáticamente
        currentPlatforms = platforms;
        
        if (player.isAlive()) {
            player.update(pressedKeys, platforms, enemyManager, false);
            
            int fruitScore = fruitManager.checkPlayerCollision(
                player.getX(), player.getY(), player.getSize()
            );
            if (fruitScore > 0) {
                player.addScore(fruitScore);
            }
        }
        
        if (player2Active && player2.isAlive()) {
            player2.update(pressedKeys, platforms, enemyManager, true);
            
            int fruitScore2 = fruitManager.checkPlayerCollision(
                player2.getX(), player2.getY(), player2.getSize()
            );
            if (fruitScore2 > 0) {
                player2.addScore(fruitScore2);
            }
        }
        
        // Verificar distancia entre jugadores
        checkPlayerDistance();
        
        enemyManager.updateAll(platforms);

        if (gameTimer.getDelay() % 60 == 0) {
            player.addScore(1);
            if (player2Active) {
                player2.addScore(1);
            }
        }
    }
    
    private void connectToServer() {
        networkManager.connect();
    }
    
    public void updateGameData(int[][] matrix, Enemy[] enemies, Fruit[] fruits) {
    // Solo actualizar la matriz si no estamos en modo bonus
    if (!bonusModeActive) {
        this.gameMatrix = matrix;
        updateGameObjects();
    } else {
        // Actualizar la matriz original para cuando termine el bonus
        if (originalMatrix == null) {
            originalMatrix = new int[MATRIX_HEIGHT][MATRIX_WIDTH];
        }
        
        for (int i = 0; i < MATRIX_HEIGHT; i++) {
            for (int j = 0; j < MATRIX_WIDTH; j++) {
                originalMatrix[i][j] = matrix[i][j];
            }
        }
        fruitManager.setGameMatrix(bonusMatrix);
    }
    
    // Siempre actualizar enemigos y frutas
    enemyManager.updateEnemies(enemies);
    fruitManager.updateFruits(fruits);
}
    
    private void checkPlayerDistance() {
        if (!player2Active || !player.isAlive() || !player2.isAlive()) {
            return;
        }
        
        // Calcular filas de diferencia
        int player1Row = (int)(player.getY() / CELL_SIZE);
        int player2Row = (int)(player2.getY() / CELL_SIZE);
        int rowDifference = Math.abs(player1Row - player2Row);
        
        if (rowDifference > 10) {
            // Determinar quién está más abajo
            Player lowerPlayer;
            Player upperPlayer;
            
            if (player1Row > player2Row) {
                lowerPlayer = player;
                upperPlayer = player2;
            } else {
                lowerPlayer = player2;
                upperPlayer = player;
            }
            
            // El jugador más abajo pierde una vida
            if (!lowerPlayer.isInvulnerable()) {
                lowerPlayer.loseLive();
                lowerPlayer.makeInvulnerable();
                
                // Hacer spawn en el mismo piso del jugador de arriba
                int upperPlayerRow = (int)(upperPlayer.getY() / CELL_SIZE);
                double newX = lowerPlayer.getX();
                double newY = upperPlayerRow * CELL_SIZE;
                
                lowerPlayer.setPosition(newX, newY);
                lowerPlayer.resetVelocity();
                
                System.out.println("¡Jugador reposicionado por distancia excesiva!");
            }
        }
    }
    
    private void updateGameObjects() {
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
        for (int j = 0; j < MATRIX_WIDTH; j++) {
            int cellValue = gameMatrix[i][j];
            
            if (cellValue == 1 || cellValue == 22) { // Manejar ambos tipos
                if (platforms[i][j] == null) {
                    PlatformType platformType = PlatformType.fromValue(cellValue);
                    platforms[i][j] = new Platform(j, i, platformType);
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

        // Frutas
        fruitManager.drawAll(g2d);

        // Jugador 1
        player.draw(g2d);
        
        // Jugador 2 (si está activo)
        if (player2Active) {
            player2.draw(g2d);
        }

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

        // Mostrar mensaje de Game Over
        if (!player.isAlive() && (!player2Active || !player2.isAlive())) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, MATRIX_WIDTH * CELL_SIZE, MATRIX_HEIGHT * CELL_SIZE);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String gameOverText = "GAME OVER";
            int textWidth = fm.stringWidth(gameOverText);
            int textX = (MATRIX_WIDTH * CELL_SIZE - textWidth) / 2;
            int textY = (MATRIX_HEIGHT * CELL_SIZE) / 2;
            g2d.drawString(gameOverText, textX, textY);
        }
    }
    
    public void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            // Notificar a los observadores sobre el cambio de estado
            notifyObserversStatus(status);
        });
    }
    
    public static int getMatrixWidth() { return MATRIX_WIDTH; }
    public static int getMatrixHeight() { return MATRIX_HEIGHT; }
    public static int getCellSize() { return CELL_SIZE; }
    
    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        
        // Activar player 2 con la tecla I
        if (e.getKeyCode() == KeyEvent.VK_I && !player2Active) {
            player2Active = true;
            statusLabel.setText("¡Jugador 2 activado! - Usa flechas para mover, P para atacar");
        }
        
        // Crear ventana espectador con la tecla O
        if (e.getKeyCode() == KeyEvent.VK_O) {
            createSpectatorWindow();
        }
        if (e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
        }
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