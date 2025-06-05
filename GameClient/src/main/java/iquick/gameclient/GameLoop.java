// Archivo: GameLoop.java
package iquick.gameclient;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Clase principal que maneja el bucle de juego para un cliente multijugador.
 * Controla la entrada de los jugadores, comunicación con el servidor y regeneración de tiles.
 */
public class GameLoop {
    // Constantes de tiempo para controlar la frecuencia de acciones
    private static final long INPUT_DELAY = 50;        // 50ms entre inputs (20 inputs/segundo máximo)
    private static final long ATTACK_DELAY = 500;      // 500ms entre ataques (2 ataques/segundo máximo)
    private static final long REGENERATION_TIME = 3000; // 3 segundos para regenerar tiles
    
    // Control de tiempo independiente para cada jugador
    // Esto permite que ambos jugadores actúen simultáneamente sin interferirse
    private long player1LastInputTime = 0;   // Último input general del jugador 1
    private long player1LastAttackTime = 0;  // Último ataque del jugador 1
    private long player2LastInputTime = 0;   // Último input general del jugador 2
    private long player2LastAttackTime = 0;  // Último ataque del jugador 2
    
    // Referencias a los componentes principales del juego
    private final ServerConnection server;      // Conexión al servidor
    private final InputHandler inputHandler;    // Manejo de entrada de teclado/controles
    private final Player player1;              // Objeto del jugador 1
    private final Player player2;              // Objeto del jugador 2
    private final List<DestroyedTile> destroyedTiles; // Lista de tiles destruidos pendientes de regenerar
    private final GamePanel gamePanel;         // Panel de renderizado del juego
    private final GameMap gameMap;            // Mapa del juego
    
    /**
     * Constructor que inicializa el GameLoop con todas las dependencias necesarias
     */
    public GameLoop(ServerConnection server, InputHandler inputHandler, 
                    Player player1, Player player2,
                    List<DestroyedTile> destroyedTiles, GamePanel gamePanel, GameMap gameMap) {
        this.server = server;
        this.inputHandler = inputHandler;
        this.player1 = player1;
        this.player2 = player2;
        this.destroyedTiles = destroyedTiles;
        this.gamePanel = gamePanel;
        this.gameMap = gameMap;
    }
    
    /**
     * Inicia los timers principales del juego
     */
    public void start() {
        // Timer principal: ejecuta cada 16ms (~60 FPS) para manejo de inputs
        Timer timer = new Timer(16, e -> handleInput());
        timer.start();
        
        // Timer de regeneración: ejecuta cada 100ms para verificar regeneración de tiles
        Timer regenerationTimer = new Timer(100, e -> checkTileRegeneration());
        regenerationTimer.start();
    }
    
    /**
     * Maneja la entrada de ambos jugadores en cada frame
     */
    private void handleInput() {
        handlePlayer1Input();
        handlePlayer2Input();
    }
    
    /**
     * Procesa la entrada del Jugador 1
     * Controla movimiento (A/D), salto (W) y ataque (X)
     */
    private void handlePlayer1Input() {
        long currentTime = System.currentTimeMillis();
        
        // Throttling: evita spam de comandos limitando la frecuencia
        if (currentTime - player1LastInputTime < INPUT_DELAY) return;
        
        boolean commandSent = false;
        
        // Manejo del ataque para Player 1
        // El ataque tiene su propio delay más largo para balanceo del juego
        if (inputHandler.isPlayer1AttackPressed() && 
            (currentTime - player1LastAttackTime >= ATTACK_DELAY)) {
            server.send("X");                    // Envía comando de ataque al servidor
            inputHandler.resetPlayer1Attack();   // Resetea el estado del botón de ataque
            player1LastAttackTime = currentTime; // Actualiza el tiempo del último ataque
            commandSent = true;
        }
        
        // Manejo del movimiento para Player 1
        // Movimiento izquierda/derecha (mutuamente excluyente)
        if (inputHandler.isPlayer1LeftPressed()) {
            server.send("A");
            commandSent = true;
        } else if (inputHandler.isPlayer1RightPressed()) {
            server.send("D");
            commandSent = true;
        }
        
        // Manejo del salto para Player 1
        if (inputHandler.isPlayer1JumpPressed()) {
            server.send("W");
            inputHandler.resetPlayer1Jump();     // Resetea para evitar saltos continuos
            commandSent = true;
        }
        
        // Solo actualiza el tiempo si se envió algún comando
        if (commandSent) {
            player1LastInputTime = currentTime;
        }
    }
    
    /**
     * Procesa la entrada del Jugador 2
     * Similar al Player 1 pero con diferentes comandos y teclas
     */
    private void handlePlayer2Input() {
        long currentTime = System.currentTimeMillis();
        
        // Mismo throttling que Player 1
        if (currentTime - player2LastInputTime < INPUT_DELAY) return;
        
        boolean commandSent = false;
        
        // Manejo del ataque para Player 2
        if (inputHandler.isPlayer2AttackPressed() && 
            (currentTime - player2LastAttackTime >= ATTACK_DELAY)) {
            server.send("P");                    // Comando de ataque diferente para Player 2
            inputHandler.resetPlayer2Attack();
            player2LastAttackTime = currentTime;
            commandSent = true;
        }
        
        // Manejo del movimiento para Player 2
        // Usa comandos más descriptivos ("LEFT"/"RIGHT" vs "A"/"D")
        if (inputHandler.isPlayer2LeftPressed()) {
            server.send("LEFT");
            commandSent = true;
        } else if (inputHandler.isPlayer2RightPressed()) {
            server.send("RIGHT");
            commandSent = true;
        }
        
        // Manejo del salto para Player 2
        if (inputHandler.isPlayer2JumpPressed()) {
            server.send("JUMP");                 // Comando más descriptivo que "W"
            inputHandler.resetPlayer2Jump();
            commandSent = true;
        }
        
        if (commandSent) {
            player2LastInputTime = currentTime;
        }
    }
    
    /**
     * Verifica y maneja la regeneración de tiles destruidos
     * Se ejecuta cada 100ms para verificar si algún tile debe regenerarse
     */
    private void checkTileRegeneration() {
        List<DestroyedTile> toRemove = new ArrayList<>();
        
        // Recorre todos los tiles destruidos
        for (DestroyedTile tile : destroyedTiles) {
            // Verifica si el tile debe regenerarse y no está ya en proceso
            if (tile.shouldRegenerate() && !tile.isRegenerating) {
                tile.isRegenerating = true;  // Marca como en proceso de regeneración
                
                // Envía comando al servidor con las coordenadas del tile
                server.send("REGENERATE_TILE " + tile.x + " " + tile.y);
                
                // Marca para remover de la lista local
                toRemove.add(tile);
            }
        }
        
        // Remueve todos los tiles que iniciaron regeneración
        destroyedTiles.removeAll(toRemove);
        
        // Si hubo cambios, actualiza la pantalla
        if (!toRemove.isEmpty()) {
            SwingUtilities.invokeLater(gamePanel::repaint);  // Thread-safe repaint
        }
    }
}