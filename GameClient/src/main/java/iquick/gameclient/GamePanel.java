package iquick.gameclient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 30;
    private static final int PLAYER_SIZE = 30;
    private static final int ENEMY_SIZE = 25;
    
    private Player player1;
    private Player player2;
    private boolean isPlayer2Active = false;
    private List<DestroyedTile> destroyedTiles;
    private List<Enemy> enemies;
    private int[][] gameMap;
    private int mapWidth;
    private int mapHeight;
    private boolean isConnected = false;
    
    public GamePanel(Player player1, Player player2, List<DestroyedTile> destroyedTiles, List<Enemy> enemies) {
        this.player1 = player1;
        this.player2 = player2;
        this.destroyedTiles = destroyedTiles;
        this.enemies = enemies;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1000, 1080));
    }
    
    public void updateMap(int[][] gameMap, int mapWidth, int mapHeight) {
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        repaint();
    }
    
    public void setConnectionStatus(boolean connected) {
        this.isConnected = connected;
        repaint();
    }
    
    public void setPlayer2Active(boolean active) {
        this.isPlayer2Active = active;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (!isConnected) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Conectando al servidor...", getWidth()/2 - 150, getHeight()/2);
            return;
        }
        
        if (gameMap == null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Esperando mapa del servidor...", getWidth()/2 - 180, getHeight()/2);
            return;
        }
        
        // Dibujar el mapa
        drawMap(g2d);
        
        // Dibujar tiles destruidos
        drawDestroyedTiles(g2d);
        
        // Dibujar enemigos
        drawEnemies(g2d);
        
        // Dibujar Player 1 (siempre activo)
        drawPlayer(g2d, player1, Color.BLUE, "P1");
        
        // Dibujar Player 2 solo si está activo
        if (isPlayer2Active && player2 != null) {
            drawPlayer(g2d, player2, Color.RED, "P2");
        }
        
        // Dibujar UI
        drawUI(g2d);
    }
    
    private void drawMap(Graphics2D g2d) {
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int tileType = gameMap[y][x];
                int drawX = x * TILE_SIZE;
                int drawY = y * TILE_SIZE;
                
                switch (tileType) {
                    case 1: // Bloque sólido
                        g2d.setColor(Color.GRAY);
                        g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        g2d.setColor(Color.WHITE);
                        g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                    case 2: // Bloque destructible
                        g2d.setColor(Color.ORANGE);
                        g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        g2d.setColor(Color.YELLOW);
                        g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                }
            }
        }
    }
    
    private void drawDestroyedTiles(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 0, 100));
        for (DestroyedTile tile : destroyedTiles) {
            int drawX = tile.x * TILE_SIZE;
            int drawY = tile.y * TILE_SIZE;
            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
        }
    }
    
    private void drawEnemies(Graphics2D g2d) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive) {
                g2d.setColor(Color.GREEN);
                int drawX = (int)(enemy.x * TILE_SIZE) + (TILE_SIZE - ENEMY_SIZE) / 2;
                int drawY = (int)(enemy.y * TILE_SIZE) + (TILE_SIZE - ENEMY_SIZE) / 2;
                g2d.fillOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
                g2d.setColor(Color.GREEN);
                g2d.drawOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
            }
        }
    }
    
    private void drawPlayer(Graphics2D g2d, Player player, Color playerColor, String playerLabel) {
        if (!player.isAlive) return;
        
        int drawX = (int)(player.x * TILE_SIZE) + (TILE_SIZE - PLAYER_SIZE) / 2;
        int drawY = (int)(player.y * TILE_SIZE) + (TILE_SIZE - PLAYER_SIZE) / 2;
        
        // Efecto de parpadeo si está dañado
        if (player.isDamaged && (System.currentTimeMillis() - player.damageTime) % 200 < 100) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(playerColor);
        }
        
        // Dibujar el jugador
        g2d.fillRect(drawX, drawY, PLAYER_SIZE, PLAYER_SIZE);
        
        // Borde del jugador
        g2d.setColor(Color.WHITE);
        g2d.drawRect(drawX, drawY, PLAYER_SIZE, PLAYER_SIZE);
        
        // Indicador de ataque
        if (player.isAttacking) {
            g2d.setColor(Color.YELLOW);
            g2d.drawRect(drawX - 2, drawY - 2, PLAYER_SIZE + 4, PLAYER_SIZE + 4);
        }
        
        // Etiqueta del jugador
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(playerLabel);
        g2d.drawString(playerLabel, drawX + (PLAYER_SIZE - labelWidth) / 2, drawY - 5);
        
        // Indicador de invulnerabilidad
        if (player.isInvulnerable) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRect(drawX - 5, drawY - 5, PLAYER_SIZE + 10, PLAYER_SIZE + 10);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
        // Información del Player 1
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("POPO (WASD + X)", 10, 30);
        g2d.drawString("Vidas: " + player1.lives, 10, 50);
        g2d.drawString("Estado: " + (player1.isAlive ? "Vivo" : "Muerto"), 10, 70);
        
        // Información del Player 2 si está activo
        if (isPlayer2Active && player2 != null) {
            g2d.setColor(Color.RED);
            g2d.drawString("NANA (Flechas + P)", 10, 110);
            g2d.drawString("Vidas: " + player2.lives, 10, 130);
            g2d.drawString("Estado: " + (player2.isAlive ? "Vivo" : "Muerto"), 10, 150);
        }
        
        // Información de conexión
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Conectado al servidor", getWidth() - 150, 20);
        
        // Controles
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int yOffset = getHeight() - 80;
        g2d.drawString("Controles:", 10, yOffset);
        g2d.drawString("POPO: WASD para mover, X para atacar", 10, yOffset + 15);
        
        if (isPlayer2Active) {
            g2d.drawString("NANA: Flechas para mover, P para atacar", 10, yOffset + 30);
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Presiona 'I' para activar a NANA", 10, yOffset + 30);
        }
        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("R para reiniciar cuando sea Game Over", 10, yOffset + 45);
        
        // Mostrar estado de Player 2
        if (!isPlayer2Active) {
            g2d.setColor(Color.ORANGE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("NANA: INACTIVO", 10, yOffset + 65);
        }
    }
}