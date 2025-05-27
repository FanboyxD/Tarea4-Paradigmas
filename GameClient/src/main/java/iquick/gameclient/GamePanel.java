package iquick.gameclient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 30;

    private Player player;
    private int[][] gameMap;
    private int mapWidth, mapHeight;
    private List<DestroyedTile> destroyedTiles;
    private List<Enemy> enemies;
    private boolean isConnected;

    public GamePanel(Player player, List<DestroyedTile> destroyedTiles, List<Enemy> enemies) {
        this.player = player;
        this.destroyedTiles = destroyedTiles;
        this.enemies = enemies;
        this.setPreferredSize(new Dimension(800, 1000));
    }

    public void updateMap(int[][] gameMap, int width, int height) {
        this.gameMap = gameMap;
        this.mapWidth = width;
        this.mapHeight = height;
        repaint();
    }

    public void setConnectionStatus(boolean connected) {
        this.isConnected = connected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (gameMap != null) {
            drawMap(g2d);
            drawRegeneratingTiles(g2d);
            drawPlayer(g2d);
            drawEnemies(g2d);
        } else {
            drawLoadingMessage(g2d);
        }

        drawHUD(g2d);
    }

    private void drawMap(Graphics2D g2d) {
        for (int i = 0; i < mapHeight; i++) {
            for (int j = 0; j < mapWidth; j++) {
                if (gameMap[i][j] == 1) {
                    int x = j * TILE_SIZE;
                    int y = i * TILE_SIZE;
                    GradientPaint gradient = new GradientPaint(x, y, new Color(34, 139, 34), x, y + TILE_SIZE, new Color(0, 100, 0));
                    g2d.setPaint(gradient);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g2d.setColor(new Color(0, 80, 0));
                    g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawPlayer(Graphics2D g2d) {
        int x = (int) (player.x * TILE_SIZE);
        int y = (int) (player.y * TILE_SIZE);
        int size = TILE_SIZE - 4;

        // Sombra
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRect(x + 4, y + 4, size, size);

        // Color base del jugador con efecto de daño
        Color baseColor = new Color(255, 100, 100);
        Color topColor = new Color(255, 150, 0);

        // Animación de daño - parpadeo rojo
        if (player.isDamaged) {
            long timeSinceDamage = System.currentTimeMillis() - player.damageTime;
            if (timeSinceDamage < 1000) { // 1 segundo de animación
                // Parpadeo cada 100ms
                if ((timeSinceDamage / 100) % 2 == 0) {
                    baseColor = new Color(255, 50, 50);
                    topColor = new Color(255, 0, 0);
                }
            } else {
                player.isDamaged = false; // Terminar animación
            }
        }

        // Cambiar color si está atacando
        if (player.isAttacking) {
            topColor = new Color(255, 150, 0);
            baseColor = new Color(255, 100, 0);
        }

        GradientPaint gradient = new GradientPaint(x + 2, y + 2, topColor, x + 2, y + 2 + size, baseColor);
        g2d.setPaint(gradient);
        g2d.fillRect(x + 2, y + 2, size, size);

        g2d.setColor(player.isAttacking ? new Color(200, 100, 0) : new Color(139, 0, 0));
        g2d.drawRect(x + 2, y + 2, size, size);

        // Indicador de salto
        if (player.isJumping) {
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(x + TILE_SIZE / 2 - 3, y - 8, 6, 6);
            g2d.setColor(Color.ORANGE);
            g2d.drawOval(x + TILE_SIZE / 2 - 3, y - 8, 6, 6);
        }

        // Efecto de ataque
        if (player.isAttacking) {
            g2d.setColor(new Color(255, 255, 0, 150));
            g2d.fillOval(x - 10, y - 10, size + 20, size + 20);
        }
    }

    private void drawEnemies(Graphics2D g2d) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive) {
                int x = (int) (enemy.x * TILE_SIZE);
                int y = (int) (enemy.y * TILE_SIZE);
                int size = TILE_SIZE - 4;

                g2d.setColor(new Color(0, 255, 0, 180));
                g2d.fillRect(x + 2, y + 2, size, size);
                g2d.setColor(new Color(0, 200, 0));
                g2d.drawRect(x + 2, y + 2, size, size);
            }
        }
    }

    private void drawRegeneratingTiles(Graphics2D g2d) {
        for (DestroyedTile tile : destroyedTiles) {
            float progress = tile.getRegenerationProgress();
            if (progress > 0) {
                int x = tile.x * TILE_SIZE;
                int y = tile.y * TILE_SIZE;
                int alpha = (int) (progress * 255);
                Color color = new Color(34, 139, 34, alpha);
                g2d.setColor(color);
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                g2d.setColor(new Color(255, 255, 0, alpha));
                for (int i = 0; i < 5; i++) {
                    int px = x + (int) (Math.random() * TILE_SIZE);
                    int py = y + (int) (Math.random() * TILE_SIZE);
                    g2d.fillOval(px, py, 2, 2);
                }
            }
        }
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), 80); // Aumentar altura para las vidas

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Controles: A/D-Mover, W/Espacio-Saltar, X-Atacar", 10, 15);

        drawLives(g2d, 10, 45); // Nueva función para mostrar vidas

        String status = String.format("Pos: (%.1f, %.1f)", player.x, player.y);
        status += player.isOnGround ? " - En el suelo" : (player.isJumping ? " - Saltando" : " - En el aire");
        if (player.isAttacking) status += " - Atacando";
        if (player.isDamaged) status += " - ¡DAÑADO!";
        g2d.drawString(status, 10, 65); // Mover hacia abajo

        g2d.setColor(isConnected ? Color.GREEN : Color.RED);
        g2d.drawString(isConnected ? "Conectado" : "Desconectado", getWidth() - 100, 15);

        g2d.setColor(Color.YELLOW);
        g2d.drawString("Tiles regenerándose: " + destroyedTiles.size(), getWidth() - 200, 35);
        // Agregar al final del método drawHUD():
        if (player.lives <= 0) {
            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String gameOverMsg = "PRESIONA ENTER PARA REINICIAR";
            FontMetrics fm = g2d.getFontMetrics();
            int msgX = (getWidth() - fm.stringWidth(gameOverMsg)) / 2;
            g2d.drawString(gameOverMsg, msgX, getHeight() - 50);
        }
    }

    private void drawLives(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Vidas: ", x, y + 12);

        int startX = x + 50;
        int heartSize = 16;
        int spacing = 20;

        for (int i = 0; i < player.maxLives; i++) {
            int heartX = startX + (i * spacing);
            int heartY = y;

            if (i < player.lives) {
                // Corazón lleno (vida disponible)
                g2d.setColor(Color.RED);
                g2d.fillOval(heartX, heartY, heartSize, heartSize);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawOval(heartX, heartY, heartSize, heartSize);
            } else {
                // Corazón vacío (vida perdida)
                g2d.setColor(Color.GRAY);
                g2d.drawOval(heartX, heartY, heartSize, heartSize);
            }
        }
    }

    private void drawLoadingMessage(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String msg = "Cargando mapa...";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(msg, x, y);
    }
}
