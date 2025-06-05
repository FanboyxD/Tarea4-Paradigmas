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

/**
 * Clase principal del cliente del juego de plataformas para 2 jugadores.
 * Esta clase maneja la interfaz gráfica, la comunicación con el servidor,
 * y la lógica de entrada del usuario.
 */
public class GameClient extends JFrame {

    // Constantes de configuración de la ventana y del juego
    private static final int WINDOW_WIDTH = 1100;      // Ancho de la ventana
    private static final int WINDOW_HEIGHT = 400;      // Alto de la ventana
    private static final long INPUT_DELAY = 50;        // Delay entre inputs en milisegundos
    private static final long ATTACK_DELAY = 500;      // Delay entre ataques en milisegundos

    // Componentes principales del juego
    private ServerConnection server;        // Conexión con el servidor
    private GamePanel gamePanel;           // Panel donde se dibuja el juego
    private Player player1;               // Primer jugador
    private Player player2;               // Segundo jugador
    private Integer playerAbove = null;    // Indica qué jugador está arriba del otro
    private boolean isPlayer2Active = false;  // Indica si el jugador 2 está activo
    
    // Listas de elementos del juego
    private List<DestroyedTile> destroyedTiles;  // Tiles destruidos que se regenerarán
    private List<Enemy> enemies;                 // Lista de enemigos
    private List<Fruit> fruits;                  // Lista de frutas (power-ups)
    
    // Timers para control de eventos
    private Timer inputTimer;           // Timer para procesar inputs
    private Timer regenerationTimer;    // Timer para regenerar tiles

    // Manejador de entrada del usuario
    private InputHandler inputHandler;
    
    // Control de timing para evitar spam de inputs
    private long lastInputTime = 0;
    private long lastPlayer1AttackTime = 0;
    private long lastPlayer2AttackTime = 0;

    // Datos del mapa del juego
    private int[][] gameMap;    // Matriz que representa el mapa
    private int mapWidth;       // Ancho del mapa
    private int mapHeight;      // Alto del mapa
    
    // Componentes para la pantalla de Game Over
    private boolean gameOverState = false;
    private JPanel gameOverPanel;
    private JButton restartButton;
    private JLayeredPane layeredPane;  // Panel en capas para superponer Game Over
    
    // Variables para la fase bonus del juego
    private boolean isBonusPhase = false;    // Indica si estamos en fase bonus
    private int bonusPlayerId = -1;          // ID del jugador que tiene el bonus
    private int bonusTimeRemaining = 0;      // Tiempo restante de la fase bonus
    
    // Sistema de observadores para espectadores
    private final List<GameObserver> observers = new ArrayList<>();
    private static int spectatorCount = 0;
    private static final int MAX_SPECTATORS_PER_CLIENT = 2;

    /**
     * Constructor principal de GameClient.
     * Inicializa todos los componentes del juego y establece la conexión con el servidor.
     */
    public GameClient() {
        // Inicializar objetos del juego
        this.player1 = new Player();
        this.player2 = new Player();
        this.destroyedTiles = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.fruits = new ArrayList<>();
        this.inputHandler = new InputHandler();

        // Configurar la interfaz de usuario
        initializeUI();
        
        // Conectar al servidor
        connectToServer();
        
        // Iniciar los timers del juego
        startInputTimer();
        startRegenerationTimer();
        
        // Configurar el atajo para espectadores
        setupSpectatorShortcut();
    }

    /**
     * Inicializa la interfaz de usuario del juego.
     * Configura la ventana principal, el panel de juego y el panel de Game Over.
     */
    private void initializeUI() {
        // Configuración básica de la ventana
        setTitle("Juego de Plataformas - Cliente (2 Jugadores)");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);  // Centrar la ventana

        // Crear el panel principal del juego
        gamePanel = new GamePanel(player1, player2, destroyedTiles, enemies, fruits);

        // Crear el panel de Game Over
        createGameOverPanel();

        // Configurar el sistema de capas para superponer paneles
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // Posicionar los paneles en las capas correspondientes
        gamePanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        gameOverPanel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);    // Capa base
        layeredPane.add(gameOverPanel, JLayeredPane.POPUP_LAYER);  // Capa superior

        add(layeredPane);

        // Configurar el manejo de input
        addKeyListener(inputHandler);
        setFocusable(true);
        requestFocus();
        setVisible(true);
    }
    
    /**
     * Establece la conexión con el servidor del juego.
     * Si no puede conectar, muestra un mensaje de error y cierra la aplicación.
     */
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

    // ===== MÉTODOS GETTER Y SETTER PARA ACCESO DESDE SERVERCONNECTION =====
    
    /**
     * Obtiene la referencia del jugador 1.
     * @return Objeto Player del jugador 1
     */
    public Player getPlayer1() {
        return player1;
    }
    
    /**
     * Obtiene la referencia del jugador 2.
     * @return Objeto Player del jugador 2
     */
    public Player getPlayer2() {
        return player2;
    }
    
    /**
     * Obtiene el ID del jugador que está encima del otro (para colisiones).
     * @return ID del jugador superior o null si no hay superposición
     */
    public Integer getPlayerAbove() {
        return playerAbove;
    }
    
    /**
     * Obtiene la referencia del panel de juego.
     * @return Objeto GamePanel
     */
    public GamePanel getGamePanel() {
        return gamePanel;
    }
 
    /**
     * Establece qué jugador está encima del otro.
     * @param playerAbove ID del jugador superior
     */
    public void setPlayerAbove(Integer playerAbove) {
        this.playerAbove = playerAbove;
    }
    
    /**
     * Verifica si el jugador 2 está activo en el juego.
     * @return true si el jugador 2 está activo
     */
    public boolean isPlayer2Active() {
        return isPlayer2Active;
    }
    
    /**
     * Activa o desactiva el jugador 2.
     * @param active true para activar el jugador 2
     */
    public void setPlayer2Active(boolean active) {
        this.isPlayer2Active = active;
        gamePanel.setPlayer2Active(active);
    }
    
    /**
     * Verifica si el juego está en fase bonus.
     * @return true si está en fase bonus
     */
    public boolean isBonusPhase() {
        return isBonusPhase;
    }

    /**
     * Establece si el juego está en fase bonus.
     * @param bonusPhase true para activar la fase bonus
     */
    public void setBonusPhase(boolean bonusPhase) {
        this.isBonusPhase = bonusPhase;
        gamePanel.setBonusPhase(bonusPhase);
    }

    /**
     * Obtiene el ID del jugador que tiene el bonus activo.
     * @return ID del jugador con bonus (-1 si no hay)
     */
    public int getBonusPlayerId() {
        return bonusPlayerId;
    }

    /**
     * Establece el ID del jugador que tiene el bonus.
     * @param bonusPlayerId ID del jugador con bonus
     */
    public void setBonusPlayerId(int bonusPlayerId) {
        this.bonusPlayerId = bonusPlayerId;
        gamePanel.setBonusPlayerId(bonusPlayerId);
    }

    /**
     * Obtiene el tiempo restante de la fase bonus.
     * @return Tiempo restante en segundos
     */
    public int getBonusTimeRemaining() {
        return bonusTimeRemaining;
    }

    /**
     * Establece el tiempo restante de la fase bonus.
     * @param bonusTimeRemaining Tiempo restante en segundos
     */
    public void setBonusTimeRemaining(int bonusTimeRemaining) {
        this.bonusTimeRemaining = bonusTimeRemaining;
        gamePanel.setBonusTimeRemaining(bonusTimeRemaining);
    }
    
    /**
     * Obtiene la lista de enemigos del juego.
     * @return Lista de enemigos
     */
    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    /**
     * Obtiene la lista de frutas del juego.
     * @return Lista de frutas
     */
    public List<Fruit> getFruits() {
        return fruits;
    }
    
    /**
     * Actualiza el mapa del juego con nuevos datos del servidor.
     * @param gameMap Matriz del mapa
     * @param mapWidth Ancho del mapa
     * @param mapHeight Alto del mapa
     */
    public void updateMap(int[][] gameMap, int mapWidth, int mapHeight) {
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        gamePanel.updateMap(gameMap, mapWidth, mapHeight);
        notifyObservers();  // Notificar a los espectadores
    }
    
    /**
     * Obtiene la matriz del mapa del juego.
     * @return Matriz bidimensional del mapa
     */
    public int[][] getGameMap() {
        return gameMap;
    }

    /**
     * Obtiene el ancho del mapa.
     * @return Ancho del mapa en tiles
     */
    public int getMapWidth() {
        return mapWidth;
    }

    /**
     * Obtiene el alto del mapa.
     * @return Alto del mapa en tiles
     */
    public int getMapHeight() {
        return mapHeight;
    }

    /**
     * Obtiene la lista de tiles destruidos.
     * @return Lista de tiles destruidos
     */
    public List<DestroyedTile> getDestroyedTiles() {
        return destroyedTiles;
    }

    /**
     * Añade un tile destruido a la lista para su posterior regeneración.
     * @param x Coordenada X del tile
     * @param y Coordenada Y del tile
     */
    public void addDestroyedTile(int x, int y) {
        destroyedTiles.add(new DestroyedTile(x, y));
    }
    
    /**
     * Remueve un tile destruido de la lista (cuando se regenera).
     * @param x Coordenada X del tile
     * @param y Coordenada Y del tile
     */
    public void removeDestroyedTile(int x, int y) {
        destroyedTiles.removeIf(tile -> tile.x == x && tile.y == y);
    }
    
    /**
     * Actualiza un tile específico del mapa.
     * @param x Coordenada X del tile
     * @param y Coordenada Y del tile
     * @param value Nuevo valor del tile
     */
    public void updateMapTile(int x, int y, int value) {
        if (validTile(x, y)) {
            gameMap[y][x] = value;
        }
    }
    
    // ===== SISTEMA DE OBSERVADORES PARA ESPECTADORES =====
    
    /**
     * Registra un observador para notificaciones de cambios de estado.
     * @param observer Observador a registrar
     */
    public void registerObserver(GameObserver observer) {
        observers.add(observer);
    }

    /**
     * Remueve un observador de la lista.
     * @param observer Observador a remover
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifica a todos los observadores sobre cambios en el estado del juego.
     */
    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateUpdate();
        }
    }
    
    /**
     * Redibuja el panel del juego y notifica a los observadores.
     */
    public void repaintGame() {
        gamePanel.repaint();
        notifyObservers();  // Notificar a los espectadores
    }
    
    /**
     * Muestra la pantalla de Game Over.
     */
    public void showGameOver() {
        gameOverState = true;
        gameOverPanel.setVisible(true);
        restartButton.requestFocus();
    }

    /**
     * Verifica si las coordenadas de un tile son válidas.
     * @param x Coordenada X
     * @param y Coordenada Y
     * @return true si las coordenadas están dentro del mapa
     */
    private boolean validTile(int x, int y) {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
    }

    /**
     * Inicia el timer que procesa los inputs del usuario.
     * Se ejecuta cada 16ms (aproximadamente 60 FPS).
     */
    private void startInputTimer() {
        inputTimer = new Timer(16, e -> handleInput());
        inputTimer.start();
    }

    /**
     * Crea el panel de Game Over con todos sus componentes.
     * Incluye el mensaje, controles y botón de reinicio.
     */
    private void createGameOverPanel() {
        gameOverPanel = new JPanel();
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setBackground(new Color(0, 0, 0, 150));  // Fondo semi-transparente
        gameOverPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Título "GAME OVER"
        JLabel gameOverLabel = new JLabel("¡GAME OVER!");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 36));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mensaje explicativo
        JLabel messageLabel = new JLabel("¡Ambos jugadores han perdido todas sus vidas!");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Información de controles
        JLabel controlsLabel = new JLabel("<html><center>Player 1: WASD + X para atacar<br>Player 2: Flechas + Espacio para atacar<br>Presiona 'I' para activar Player 2</center></html>");
        controlsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        controlsLabel.setForeground(Color.LIGHT_GRAY);
        controlsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botón de reinicio
        restartButton = new JButton("REINICIAR JUEGO");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.setMaximumSize(new Dimension(200, 50));
        restartButton.addActionListener(e -> restartGame());

        // Agregar todos los componentes con espaciado
        gameOverPanel.add(Box.createVerticalGlue());
        gameOverPanel.add(gameOverLabel);
        gameOverPanel.add(Box.createVerticalStrut(20));
        gameOverPanel.add(messageLabel);
        gameOverPanel.add(Box.createVerticalStrut(20));
        gameOverPanel.add(controlsLabel);
        gameOverPanel.add(Box.createVerticalStrut(30));
        gameOverPanel.add(restartButton);
        gameOverPanel.add(Box.createVerticalGlue());

        gameOverPanel.setVisible(false);  // Inicialmente oculto
    }

    /**
     * Reinicia el juego enviando un comando al servidor.
     */
    private void restartGame() {
        server.send("RESTART");
        gameOverState = false;
        SwingUtilities.invokeLater(() -> {
            gameOverPanel.setVisible(false);
            requestFocus();  // Devolver el foco para recibir inputs
        });
    }

    /**
     * Procesa los inputs del usuario y los envía al servidor.
     * Maneja diferentes lógicas para fase normal y fase bonus.
     */
    private void handleInput() {
        long now = System.currentTimeMillis();
        // Control de delay entre inputs para evitar spam
        if (now - lastInputTime < INPUT_DELAY) return;

        boolean sent = false;

        // LÓGICA PARA FASE BONUS
        if (isBonusPhase) {
            // Solo el jugador con bonus puede controlar en esta fase
            if (bonusPlayerId == 1) {
                // Player 1 controla en fase bonus
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
                // Player 2 controla en fase bonus (solo si está activo)
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
            // LÓGICA NORMAL DEL JUEGO
            
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
                    server.send("P");  // Nota: Parece ser un error, debería ser "SPACE"
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

        // Actualizar timestamp del último input enviado
        if (sent) lastInputTime = now;
    }

    /**
     * Inicia el timer que maneja la regeneración de tiles destruidos.
     * Se ejecuta cada 100ms para verificar qué tiles están listos para regenerarse.
     */
    private void startRegenerationTimer() {
        regenerationTimer = new Timer(100, e -> {
            List<DestroyedTile> ready = new ArrayList<>();
            
            // Verificar qué tiles están listos para regenerarse
            for (DestroyedTile tile : destroyedTiles) {
                if (tile.shouldRegenerate() && !tile.isRegenerating) {
                    tile.isRegenerating = true;
                    server.send("REGENERATE_TILE " + tile.x + " " + tile.y);
                    ready.add(tile);
                }
            }

            // Remover tiles que están siendo regenerados
            destroyedTiles.removeAll(ready);
            
            // Redibujar si hubo cambios
            if (!ready.isEmpty()) {
                SwingUtilities.invokeLater(() -> gamePanel.repaint());
            }
        });
        regenerationTimer.start();
    }
    
    /**
     * Configura el atajo de teclado para abrir ventanas de espectador.
     * Presionar 'O' abre una nueva ventana de espectador (máximo 2 por cliente).
     */
    private void setupSpectatorShortcut() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_O) {
                    // Verificar límite de espectadores
                    if (spectatorCount >= MAX_SPECTATORS_PER_CLIENT) {
                        JOptionPane.showMessageDialog(null, 
                            "Máximo de espectadores alcanzado para este cliente.", 
                            "Límite alcanzado", 
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        // Crear nueva ventana de espectador
                        new SpectatorClient(GameClient.this);
                        spectatorCount++;
                    }
                }
            }
        });
    }

    /**
     * Limpia recursos al cerrar la aplicación.
     * Detiene los timers y cierra la conexión con el servidor.
     */
    @Override
    public void dispose() {
        // Detener timers
        if (regenerationTimer != null) regenerationTimer.stop();
        if (inputTimer != null) inputTimer.stop();
        
        // Cerrar conexión con el servidor
        try {
            if (server != null) server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        super.dispose();
    }

    /**
     * Método principal de la aplicación.
     * Configura el Look & Feel y crea la instancia del cliente.
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Usar el Look & Feel del sistema operativo
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Usando L&F por defecto");
            }

            // Verificar si se intenta ejecutar en modo espectador directamente
            if (args.length > 0 && args[0].equals("spectator")) {
                JOptionPane.showMessageDialog(null, 
                    "El cliente espectador debe iniciarse desde el cliente jugador.", 
                    "Modo inválido", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                // Crear el cliente principal del juego
                GameClient jugador = new GameClient();
            }
        });
    }
}