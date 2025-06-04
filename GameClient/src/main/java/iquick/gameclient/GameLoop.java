// Archivo: GameLoop.java
package iquick.gameclient;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class GameLoop {
    private static final long INPUT_DELAY = 50;
    private static final long ATTACK_DELAY = 500;
    private static final long REGENERATION_TIME = 5000;
    
    // Tiempos separados para cada jugador
    private long player1LastInputTime = 0;
    private long player1LastAttackTime = 0;
    private long player2LastInputTime = 0;
    private long player2LastAttackTime = 0;
    
    private final ServerConnection server;
    private final InputHandler inputHandler;
    private final Player player1;
    private final Player player2;
    private final List<DestroyedTile> destroyedTiles;
    private final GamePanel gamePanel;
    private final GameMap gameMap;
    
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
    
    public void start() {
        Timer timer = new Timer(16, e -> handleInput());
        timer.start();
        
        Timer regenerationTimer = new Timer(100, e -> checkTileRegeneration());
        regenerationTimer.start();
    }
    
    private void handleInput() {
        handlePlayer1Input();
        handlePlayer2Input();
    }
    
    private void handlePlayer1Input() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - player1LastInputTime < INPUT_DELAY) return;
        
        boolean commandSent = false;
        
        // Manejo del ataque para Player 1
        if (inputHandler.isPlayer1AttackPressed() && 
            (currentTime - player1LastAttackTime >= ATTACK_DELAY)) {
            server.send("X");
            inputHandler.resetPlayer1Attack();
            player1LastAttackTime = currentTime;
            commandSent = true;
        }
        
        // Manejo del movimiento para Player 1
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
            inputHandler.resetPlayer1Jump();
            commandSent = true;
        }
        
        if (commandSent) {
            player1LastInputTime = currentTime;
        }
    }
    
    private void handlePlayer2Input() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - player2LastInputTime < INPUT_DELAY) return;
        
        boolean commandSent = false;
        
        // Manejo del ataque para Player 2
        if (inputHandler.isPlayer2AttackPressed() && 
            (currentTime - player2LastAttackTime >= ATTACK_DELAY)) {
            server.send("P");
            inputHandler.resetPlayer2Attack();
            player2LastAttackTime = currentTime;
            commandSent = true;
        }
        
        // Manejo del movimiento para Player 2
        if (inputHandler.isPlayer2LeftPressed()) {
            server.send("LEFT");
            commandSent = true;
        } else if (inputHandler.isPlayer2RightPressed()) {
            server.send("RIGHT");
            commandSent = true;
        }
        
        // Manejo del salto para Player 2
        if (inputHandler.isPlayer2JumpPressed()) {
            server.send("JUMP");
            inputHandler.resetPlayer2Jump();
            commandSent = true;
        }
        
        if (commandSent) {
            player2LastInputTime = currentTime;
        }
    }
    
    private void checkTileRegeneration() {
        List<DestroyedTile> toRemove = new ArrayList<>();
        
        for (DestroyedTile tile : destroyedTiles) {
            if (tile.shouldRegenerate() && !tile.isRegenerating) {
                tile.isRegenerating = true;
                server.send("REGENERATE_TILE " + tile.x + " " + tile.y);
                toRemove.add(tile);
            }
        }
        
        destroyedTiles.removeAll(toRemove);
        
        if (!toRemove.isEmpty()) {
            SwingUtilities.invokeLater(gamePanel::repaint);
        }
    }
}