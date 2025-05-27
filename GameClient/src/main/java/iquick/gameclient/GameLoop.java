// Archivo: GameLoop.java
package iquick.gameclient;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class GameLoop {
    private static final long INPUT_DELAY = 50;
    private static final long ATTACK_DELAY = 500;
    private static final long REGENERATION_TIME = 5000;

    private long lastInputTime = 0;
    private long lastAttackTime = 0;

    private final ServerConnection server;
    private final InputHandler inputHandler;
    private final Player player;
    private final List<DestroyedTile> destroyedTiles;
    private final GamePanel gamePanel;
    private final GameMap gameMap;

    public GameLoop(ServerConnection server, InputHandler inputHandler, Player player,
                    List<DestroyedTile> destroyedTiles, GamePanel gamePanel, GameMap gameMap) {
        this.server = server;
        this.inputHandler = inputHandler;
        this.player = player;
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
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastInputTime < INPUT_DELAY) return;

        boolean commandSent = false;

        if (inputHandler.isAttackPressed() && (currentTime - lastAttackTime >= ATTACK_DELAY)) {
            server.send("ATTACK");
            inputHandler.resetAttack();
            lastAttackTime = currentTime;
            commandSent = true;
        }

        if (inputHandler.isLeftPressed()) {
            server.send("LEFT");
            commandSent = true;
        } else if (inputHandler.isRightPressed()) {
            server.send("RIGHT");
            commandSent = true;
        }

        if (inputHandler.isJumpPressed()) {
            server.send("JUMP");
            inputHandler.resetJump();
            commandSent = true;
        }

        if (commandSent) {
            lastInputTime = currentTime;
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
