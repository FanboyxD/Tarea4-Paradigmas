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
    private Integer playerAbove = null;
    private boolean isPlayer2Active = false;
    private List<DestroyedTile> destroyedTiles;
    private List<Enemy> enemies;
    private List<Fruit> fruits;
    private int[][] gameMap;
    private int mapWidth;
    private int mapHeight;
    private int cameraX = 0;
    private int cameraY = 0;
    private boolean isConnected = false;
    private boolean isBonusPhase = false;
    private int bonusPlayerId = -1;
    private int bonusTimeRemaining = 0;
    
    public GamePanel(Player player1, Player player2, List<DestroyedTile> destroyedTiles, List<Enemy> enemies, List<Fruit> fruits) {
        this.player1 = player1;
        this.player2 = player2;
        this.destroyedTiles = destroyedTiles;
        this.enemies = enemies;
        this.fruits = fruits;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1100, 400));
    }
    
    public void updateMap(int[][] gameMap, int mapWidth, int mapHeight) {
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        repaint();
    }
    
    public void setBonusPhase(boolean bonusPhase) {
        this.isBonusPhase = bonusPhase;
        repaint();
    }

    public void setBonusPlayerId(int bonusPlayerId) {
        this.bonusPlayerId = bonusPlayerId;
        repaint();
    }

    public void setBonusTimeRemaining(int bonusTimeRemaining) {
        this.bonusTimeRemaining = bonusTimeRemaining;
        repaint();
    }
    private void updateCamera() {
        Player targetPlayer = null;

        if (isBonusPhase) {
            // En fase bonus, seguir solo al jugador bonus
            if (bonusPlayerId == 1 && player1.isAlive) {
                targetPlayer = player1;
            } else if (bonusPlayerId == 2 && isPlayer2Active && player2.isAlive) {
                targetPlayer = player2;
            }
        } else {
            // Lógica normal de cámara (código existente)
            if (playerAbove != null && playerAbove == 2 && isPlayer2Active && player2.isAlive) {
                targetPlayer = player2;
            } else if (playerAbove != null && playerAbove == 1 && player1.isAlive) {
                targetPlayer = player1;
            } else if (player1.isAlive) {
                targetPlayer = player1;
            } else if (isPlayer2Active && player2.isAlive) {
                targetPlayer = player2;
            }
        }

        if (targetPlayer != null) {
            int targetX = (int)(targetPlayer.x * TILE_SIZE) - getWidth() / 2;
            int targetY = (int)(targetPlayer.y * TILE_SIZE) - (getHeight() / 3);

            int maxCameraX = (mapWidth * TILE_SIZE) - getWidth();
            int maxCameraY = (mapHeight * TILE_SIZE) - getHeight() / 2;

            cameraX = Math.max(0, Math.min(targetX, maxCameraX));
            cameraY = Math.max(0, Math.min(targetY, maxCameraY));
        }
    }
    
    public void setConnectionStatus(boolean connected) {
        this.isConnected = connected;
        repaint();
    }
    
    public void setPlayer2Active(boolean active) {
        this.isPlayer2Active = active;
        repaint();
    }
    
    public void setPlayerAbove(Integer playerAbove) {
        this.playerAbove = playerAbove;
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

        // Actualizar la posición de la cámara
        updateCamera();

        // Aplicar transformación de cámara
        g2d.translate(-cameraX, -cameraY);

        // Dibujar el mapa
        drawMap(g2d);

        // Dibujar tiles destruidos
        drawDestroyedTiles(g2d);

        // Dibujar enemigos
        drawEnemies(g2d);
        
        // Dibujar frutas
        drawFruits(g2d);

        // Dibujar Player 1 (siempre activo)
        drawPlayer(g2d, player1, Color.BLUE, "P1");

        // Dibujar Player 2 solo si está activo
        if (isPlayer2Active && player2 != null) {
            drawPlayer(g2d, player2, Color.RED, "P2");
        }

        // Resetear transformación para UI fija
        g2d.translate(cameraX, cameraY);

        // Dibujar UI (siempre fija en pantalla)
        drawUI(g2d);
    }
    
    private void drawMap(Graphics2D g2d) {
        int startX = Math.max(0, cameraX / TILE_SIZE - 1);
        int endX = Math.min(mapWidth, (cameraX + getWidth()) / TILE_SIZE + 2);
        int startY = Math.max(0, cameraY / TILE_SIZE - 1);
        int endY = Math.min(mapHeight, (cameraY + getHeight()) / TILE_SIZE + 2);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
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
                    case 2: // Bloque bonus (zona especial)
                        if (isBonusPhase) {
                            // En fase bonus, mostrar como bloque normal
                            g2d.setColor(Color.ORANGE);
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(Color.YELLOW);
                            g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        } else {
                            // Fuera de fase bonus, mostrar con efecto especial
                            g2d.setColor(Color.MAGENTA);
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(Color.CYAN);
                            g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                            // Efecto de brillo
                            if ((System.currentTimeMillis() / 300) % 2 == 0) {
                                g2d.setColor(new Color(255, 255, 255, 100));
                                g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            }
                        }
                        break;
                }
            }
        }
    }
    
    private void drawDestroyedTiles(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 0, 100));
        for (DestroyedTile tile : destroyedTiles) {
            // Solo dibujar si está en el área visible
            int drawX = tile.x * TILE_SIZE;
            int drawY = tile.y * TILE_SIZE;

            if (drawX + TILE_SIZE >= cameraX && drawX <= cameraX + getWidth() &&
                drawY + TILE_SIZE >= cameraY && drawY <= cameraY + getHeight()) {
                g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
            }
        }
    }
    
    private void drawEnemies(Graphics2D g2d) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive) {
                int drawX = (int)(enemy.x * TILE_SIZE) + (TILE_SIZE - ENEMY_SIZE) / 2;
                int drawY = (int)(enemy.y * TILE_SIZE) + (TILE_SIZE - ENEMY_SIZE) / 2;

                // Solo dibujar si está en el área visible
                if (drawX + ENEMY_SIZE >= cameraX && drawX <= cameraX + getWidth() &&
                    drawY + ENEMY_SIZE >= cameraY && drawY <= cameraY + getHeight()) {

                    // MODIFICADO - Dibujar según el tipo de enemigo
                    switch (enemy.enemyType) {
                        case "NORMAL":
                            // Enemigo normal (verde)
                            g2d.setColor(Color.GREEN);
                            g2d.fillOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            g2d.setColor(Color.DARK_GRAY);
                            g2d.drawOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            break;

                        case "FAST":
                            // Enemigo rápido (amarillo con efecto de velocidad)
                            g2d.setColor(Color.YELLOW);
                            g2d.fillOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            g2d.setColor(Color.ORANGE);
                            g2d.drawOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);

                            // Efecto de "rastro de velocidad"
                            g2d.setColor(new Color(255, 255, 0, 100));
                            g2d.fillOval(drawX - 3, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            g2d.fillOval(drawX + 3, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            break;

                        case "HEAVY":
                            // Enemigo pesado (más grande y rojo)
                            int heavySize = ENEMY_SIZE + 8;
                            int heavyDrawX = drawX - 4;
                            int heavyDrawY = drawY - 4;

                            g2d.setColor(Color.RED);
                            g2d.fillOval(heavyDrawX, heavyDrawY, heavySize, heavySize);
                            g2d.setColor(Color.RED);
                            g2d.drawOval(heavyDrawX, heavyDrawY, heavySize, heavySize);

                            // Líneas para mostrar que es "pesado"
                            g2d.setColor(Color.WHITE);
                            g2d.drawLine(heavyDrawX + 3, heavyDrawY + heavySize/2, 
                                       heavyDrawX + heavySize - 3, heavyDrawY + heavySize/2);
                            g2d.drawLine(heavyDrawX + heavySize/2, heavyDrawY + 3, 
                                       heavyDrawX + heavySize/2, heavyDrawY + heavySize - 3);
                            break;

                        case "FLYING":
                            // Enemigo volador (azul con alas)
                            g2d.setColor(Color.CYAN);
                            g2d.fillOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            g2d.setColor(Color.BLUE);
                            g2d.drawOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);

                            // "Alas" - líneas que simulan movimiento de alas
                            g2d.setColor(Color.WHITE);
                            int wingOffset = (int)((System.currentTimeMillis() / 100) % 6) - 3;
                            g2d.drawLine(drawX - 5, drawY + ENEMY_SIZE/2 + wingOffset, 
                                       drawX + 5, drawY + ENEMY_SIZE/2 - wingOffset);
                            g2d.drawLine(drawX + ENEMY_SIZE - 5, drawY + ENEMY_SIZE/2 + wingOffset, 
                                       drawX + ENEMY_SIZE + 5, drawY + ENEMY_SIZE/2 - wingOffset);
                            break;

                        default:
                            // Tipo desconocido - usar el diseño normal pero con color púrpura
                            g2d.setColor(Color.MAGENTA);
                            g2d.fillOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);
                            g2d.setColor(Color.BLACK);
                            g2d.drawOval(drawX, drawY, ENEMY_SIZE, ENEMY_SIZE);

                            // Signo de interrogación
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(new Font("Arial", Font.BOLD, 12));
                            g2d.drawString("?", drawX + ENEMY_SIZE/2 - 3, drawY + ENEMY_SIZE/2 + 4);
                            break;
                    }
                }
            }
        }
    }
    private void drawFruits(Graphics2D g2d) {
        for (Fruit fruit : fruits) {
            if (fruit.isActive) {
                int drawX = (int)(fruit.x * TILE_SIZE) + (TILE_SIZE - 25) / 2;
                int drawY = (int)(fruit.y * TILE_SIZE) + (TILE_SIZE - 25) / 2;

                // Solo dibujar si está en el área visible
                if (drawX + 25 >= cameraX && drawX <= cameraX + getWidth() &&
                    drawY + 25 >= cameraY && drawY <= cameraY + getHeight()) {

                    // Dibujar según el tipo de fruta
                    switch (fruit.fruitType.toUpperCase()) {
                        case "ORANGE":
                            // Naranja - círculo naranja
                            g2d.setColor(Color.ORANGE);
                            g2d.fillOval(drawX, drawY, 25, 25);
                            g2d.setColor(new Color(255, 140, 0));
                            g2d.drawOval(drawX, drawY, 25, 25);

                            // Pequeña "hoja" verde en la parte superior
                            g2d.setColor(Color.GREEN);
                            g2d.fillOval(drawX + 18, drawY + 2, 6, 8);
                            break;

                        case "BANANA":
                            // Plátano - forma curva amarilla
                            g2d.setColor(Color.YELLOW);
                            g2d.fillArc(drawX, drawY, 25, 20, 30, 120);
                            g2d.setColor(new Color(255, 215, 0));
                            g2d.drawArc(drawX, drawY, 25, 20, 30, 120);

                            // Pequeño tallo
                            g2d.setColor(new Color(139, 69, 19));
                            g2d.fillRect(drawX + 2, drawY + 2, 3, 6);
                            break;

                        case "EGGPLANT":
                            // Berenjena - óvalo púrpura
                            g2d.setColor(new Color(102, 0, 153));
                            g2d.fillOval(drawX, drawY + 3, 20, 22);
                            g2d.setColor(new Color(75, 0, 130));
                            g2d.drawOval(drawX, drawY + 3, 20, 22);

                            // Tallo verde
                            g2d.setColor(Color.GREEN);
                            g2d.fillRect(drawX + 8, drawY, 4, 8);
                            break;

                        case "LETTUCE":
                            // Lechuga - verde con textura
                            g2d.setColor(new Color(50, 205, 50));
                            g2d.fillOval(drawX, drawY, 25, 22);
                            g2d.setColor(new Color(34, 139, 34));
                            g2d.drawOval(drawX, drawY, 25, 22);

                            // Líneas para simular hojas
                            g2d.setColor(new Color(124, 252, 0));
                            g2d.drawLine(drawX + 5, drawY + 8, drawX + 20, drawY + 8);
                            g2d.drawLine(drawX + 3, drawY + 12, drawX + 22, drawY + 12);
                            g2d.drawLine(drawX + 6, drawY + 16, drawX + 19, drawY + 16);
                            break;

                        default:
                            // Fruta desconocida - círculo multicolor
                            g2d.setColor(Color.PINK);
                            g2d.fillOval(drawX, drawY, 25, 25);
                            g2d.setColor(Color.BLACK);
                            g2d.drawOval(drawX, drawY, 25, 25);
                            g2d.setFont(new Font("Arial", Font.BOLD, 10));
                            g2d.drawString("?", drawX + 10, drawY + 15);
                            break;
                    }

                    // Efecto de brillo para indicar que es coleccionable
                    if ((System.currentTimeMillis() / 500) % 2 == 0) {
                        g2d.setColor(new Color(255, 255, 255, 80));
                        g2d.fillOval(drawX - 2, drawY - 2, 29, 29);
                    }

                    // Mostrar puntos
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 8));
                    String pointsText = "+" + fruit.points;
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(pointsText);
                    g2d.drawString(pointsText, drawX + (25 - textWidth) / 2, drawY + 35);
                }
            }
        }
    }
    
    private void drawPlayer(Graphics2D g2d, Player player, Color playerColor, String playerLabel) {
        if (!player.isAlive) return;
        
        // Durante la fase bonus, solo dibujar el jugador que tiene el control
        if (isBonusPhase) {
            if (playerLabel.equals("P1") && bonusPlayerId != 1) {
                return; // No dibujar Player 1 si no es el jugador bonus
            }
            if (playerLabel.equals("P2") && bonusPlayerId != 2) {
                return; // No dibujar Player 2 si no es el jugador bonus
            }
        }
        
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
        g2d.drawString("POPO (WASD + X)", 800, 30);
        g2d.drawString("Vidas: " + player1.lives, 800, 50);
        g2d.drawString("Score: " + player1.score, 800, 70);
        g2d.drawString("Estado: " + (player1.isAlive ? "Vivo" : "Muerto"), 800, 90);

        // Información del Player 2 si está activo
        if (isPlayer2Active && player2 != null) {
            g2d.setColor(Color.RED);
            g2d.drawString("NANA (Flechas + P)", 800, 130);
            g2d.drawString("Vidas: " + player2.lives, 800, 150);
            g2d.drawString("Score: " + player2.score, 800, 170);
            g2d.drawString("Estado: " + (player2.isAlive ? "Vivo" : "Muerto"), 800, 190);
        }

        // Información de la fase bonus
        if (isBonusPhase) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("¡FASE BONUS!", getWidth() - 200, 290);

            String playerName = bonusPlayerId == 1 ? "POPO" : "NANA";
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Jugador: " + playerName, getWidth() - 200, 320);

            // Tiempo restante
            int seconds = bonusTimeRemaining / 1000;
            g2d.setColor(seconds <= 10 ? Color.RED : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Tiempo: " + seconds + "s", getWidth() -200, 350);

            float progress = (float) bonusTimeRemaining / 30000f; // 30 segundos total

            if (progress > 0.5f) {
                g2d.setColor(Color.GREEN);
            } else if (progress > 0.25f) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.RED);
            }
            g2d.setColor(Color.WHITE);
        }

        // Información de conexión
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Conectado al servidor", getWidth() - 150, 20);

        // Controles (solo mostrar si no estamos en fase bonus)
        if (!isBonusPhase) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            int yOffset = getHeight() - 80;

            if (!isPlayer2Active) {
                g2d.setColor(Color.ORANGE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("NANA: INACTIVO", 800, yOffset - 20);
                g2d.setColor(Color.YELLOW);
                g2d.drawString("Presiona 'I' para activar a NANA", 800, yOffset + 15);
            }

            // Información sobre bloques bonus
            g2d.setColor(Color.MAGENTA);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Toca bloques brillantes para fase BONUS!", 10, getHeight() - 20);
        }
    }
}