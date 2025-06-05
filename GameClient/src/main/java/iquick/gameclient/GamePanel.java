package iquick.gameclient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel principal del juego que maneja la renderización de todos los elementos visuales
 * Incluye jugadores, enemigos, frutas, mapa y efectos especiales
 */
public class GamePanel extends JPanel {
    // Constantes para definir el tamaño de los elementos del juego
    private static final int TILE_SIZE = 30;     // Tamaño de cada casilla del mapa
    private static final int PLAYER_SIZE = 30;   // Tamaño de los jugadores
    private static final int ENEMY_SIZE = 25;    // Tamaño base de los enemigos
    
    // Referencias a los elementos del juego
    private Player player1;                      // Jugador 1 (POPO - controlado con WASD)
    private Player player2;                      // Jugador 2 (NANA - controlado con flechas)
    private Integer playerAbove = null;          // Indica qué jugador está "encima" (prioridad de cámara)
    private boolean isPlayer2Active = false;     // Si el jugador 2 está activo en la partida
    
    // Listas de elementos del juego
    private List<DestroyedTile> destroyedTiles;  // Tiles que han sido destruidos
    private List<Enemy> enemies;                 // Lista de enemigos activos
    private List<Fruit> fruits;                  // Lista de frutas coleccionables
    
    // Datos del mapa
    private int[][] gameMap;                     // Matriz que representa el mapa del juego
    private int mapWidth;                        // Ancho del mapa en tiles
    private int mapHeight;                       // Alto del mapa en tiles
    
    // Sistema de cámara
    private int cameraX = 0;                     // Posición X de la cámara
    private int cameraY = 0;                     // Posición Y de la cámara
    
    // Estados del juego
    private boolean isConnected = false;         // Estado de conexión al servidor
    private boolean isBonusPhase = false;        // Si está en fase bonus
    private int bonusPlayerId = -1;              // ID del jugador que activó la fase bonus
    private int bonusTimeRemaining = 0;          // Tiempo restante de la fase bonus
    
    private Image tileSolidImg;
    private Image player1Img;
    private Image player2Img;
    private Image naranjaImg;
    private Image bananoImg;
    private Image berenjenaImg;
    private Image lechugaImg;
    private Image focaImg;
    private Image tempanoImg;
    private Image birdImg;
    
    /**
     * Constructor del panel de juego
     * Inicializa todas las referencias y configura el panel
     */
    public GamePanel(Player player1, Player player2, List<DestroyedTile> destroyedTiles, 
                     List<Enemy> enemies, List<Fruit> fruits) {
        this.player1 = player1;
        this.player2 = player2;
        this.destroyedTiles = destroyedTiles;
        this.enemies = enemies;
        this.fruits = fruits;
        setBackground(Color.WHITE);              // Fondo blanco para el juego
        setPreferredSize(new Dimension(1100, 400)); // Tamaño fijo del panel
        tileSolidImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/tileSolidImg.png")).getImage();
        player1Img = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/POPO.jpg")).getImage();
        player2Img = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/NANA.png")).getImage();
        naranjaImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/naranja.png")).getImage();
        bananoImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/banano.png")).getImage();
        berenjenaImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/berenjena.png")).getImage();
        lechugaImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/lechuga.jpg")).getImage();
        focaImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/foca.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/bird.png")).getImage();
        tempanoImg = new ImageIcon(getClass().getResource("/iquick/gameclient/assets/tempano.png")).getImage();
    }
    
    /**
     * Actualiza los datos del mapa y dispara un repaint
     */
    public void updateMap(int[][] gameMap, int mapWidth, int mapHeight) {
        this.gameMap = gameMap;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        repaint(); // Redibuja el panel
    }
    
    // Métodos para controlar la fase bonus
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
    
    /**
     * Sistema de cámara que sigue al jugador apropiado
     * En fase bonus sigue solo al jugador que la activó
     * En juego normal sigue al jugador con prioridad o al que esté vivo
     */
    private void updateCamera() {
        Player targetPlayer = null;

        if (isBonusPhase) {
            // En fase bonus, seguir solo al jugador que la activó
            if (bonusPlayerId == 1 && player1.isAlive) {
                targetPlayer = player1;
            } else if (bonusPlayerId == 2 && isPlayer2Active && player2.isAlive) {
                targetPlayer = player2;
            }
        } else {
            // Lógica normal de cámara basada en prioridad y estado de jugadores
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

        // Calcular posición de cámara centrada en el jugador objetivo
        if (targetPlayer != null) {
            int targetX = (int)(targetPlayer.x * TILE_SIZE) - getWidth() / 2;
            int targetY = (int)(targetPlayer.y * TILE_SIZE) - (getHeight() / 3);

            // Límites para que la cámara no se salga del mapa
            int maxCameraX = (mapWidth * TILE_SIZE) - getWidth();
            int maxCameraY = (mapHeight * TILE_SIZE) - getHeight() / 2;

            // Aplicar los límites
            cameraX = Math.max(0, Math.min(targetX, maxCameraX));
            cameraY = Math.max(0, Math.min(targetY, maxCameraY));
        }
    }
    
    // Métodos para actualizar el estado del juego
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
    
    /**
     * Método principal de renderización
     * Se llama automáticamente cuando el panel necesita redibujarse
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Activar antialiasing para gráficos más suaves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Pantalla de conexión
        if (!isConnected) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Conectando al servidor...", getWidth()/2 - 150, getHeight()/2);
            return;
        }

        // Pantalla de error (servidor lleno)
        if (gameMap == null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Numero maximo de juegos alcanzado, cierre esta ventana", getWidth()/2 - 300, getHeight()/2);
            return;
        }

        // Actualizar la posición de la cámara
        updateCamera();

        // Aplicar transformación de cámara para elementos del mundo
        g2d.translate(-cameraX, -cameraY);

        // Dibujar elementos del mundo (afectados por la cámara)
        drawMap(g2d);
        drawDestroyedTiles(g2d);
        drawEnemies(g2d);
        drawFruits(g2d);

        // Dibujar jugadores
        drawPlayer(g2d, player1, Color.BLUE, "P1");
        if (isPlayer2Active && player2 != null) {
            drawPlayer(g2d, player2, Color.RED, "P2");
        }

        // Resetear transformación para UI (no afectada por cámara)
        g2d.translate(cameraX, cameraY);

        // Dibujar interfaz de usuario (fija en pantalla)
        drawUI(g2d);
    }
    
    /**
     * Dibuja el mapa del juego con optimización de culling
     * Solo dibuja los tiles visibles en pantalla
     */
    private void drawMap(Graphics2D g2d) {
        // Calcular qué tiles están visibles (culling)
        int startX = Math.max(0, cameraX / TILE_SIZE - 1);
        int endX = Math.min(mapWidth, (cameraX + getWidth()) / TILE_SIZE + 2);
        int startY = Math.max(0, cameraY / TILE_SIZE - 1);
        int endY = Math.min(mapHeight, (cameraY + getHeight()) / TILE_SIZE + 2);

        // Dibujar solo los tiles visibles
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tileType = gameMap[y][x];
                int drawX = x * TILE_SIZE;
                int drawY = y * TILE_SIZE;

                switch (tileType) {
                    case 1: // Bloque sólido normal
                        g2d.drawImage(tileSolidImg, drawX, drawY, TILE_SIZE, TILE_SIZE, null);

                        g2d.setColor(Color.WHITE);
                        g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        break;
                        
                    case 2: // Bloque bonus (activa fase especial)
                        if (isBonusPhase) {
                            // Durante fase bonus: apariencia normal
                            g2d.setColor(Color.ORANGE);
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(Color.YELLOW);
                            g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        } else {
                            // Fuera de fase bonus: efecto llamativo
                            g2d.setColor(Color.MAGENTA);
                            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                            g2d.setColor(Color.CYAN);
                            g2d.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);

                            // Efecto de parpadeo para llamar la atención
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
    
    /**
     * Dibuja los tiles destruidos con efecto semi-transparente
     */
    private void drawDestroyedTiles(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 0, 100)); // Amarillo semi-transparente
        for (DestroyedTile tile : destroyedTiles) {
            int drawX = tile.x * TILE_SIZE;
            int drawY = tile.y * TILE_SIZE;

            // Solo dibujar si está visible (culling)
            if (drawX + TILE_SIZE >= cameraX && drawX <= cameraX + getWidth() &&
                drawY + TILE_SIZE >= cameraY && drawY <= cameraY + getHeight()) {
                g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
            }
        }
    }
    
    /**
     * Dibuja enemigos con diferentes tipos y efectos visuales
     * Cada tipo de enemigo tiene su propia apariencia distintiva
     */
    private void drawEnemies(Graphics2D g2d) {
        Image foca = focaImg;
        Image bird = birdImg;
        Image tempano = tempanoImg;
        for (Enemy enemy : enemies) {
            if (enemy.isActive) {
                int drawX = (int)(enemy.x * TILE_SIZE) + (TILE_SIZE - ENEMY_SIZE) / 2;
                int drawY = (int)(enemy.y * TILE_SIZE) + (TILE_SIZE - ENEMY_SIZE) / 2;

                // Culling: solo dibujar si está visible
                if (drawX + ENEMY_SIZE >= cameraX && drawX <= cameraX + getWidth() &&
                    drawY + ENEMY_SIZE >= cameraY && drawY <= cameraY + getHeight()) {

                    // Dibujar según el tipo de enemigo
                    switch (enemy.enemyType.toUpperCase()) {
                        case "GROUND":
                            // Enemigo básico verde
                            g2d.drawImage(focaImg, drawX, drawY, ENEMY_SIZE, ENEMY_SIZE, null);
                            break;

                        case "ICE":
                            // Enemigo rápido con efecto de velocidad
                            g2d.drawImage(tempanoImg, drawX, drawY, ENEMY_SIZE, ENEMY_SIZE, null);
                            break;

                        case "BIRD":
                            // Enemigo volador
                            g2d.drawImage(birdImg, drawX, drawY, ENEMY_SIZE, ENEMY_SIZE, null);
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * Dibuja las frutas coleccionables con diferentes tipos y efectos
     */
    private void drawFruits(Graphics2D g2d) {
        Image fruit1 = naranjaImg;
        Image fruit2 = bananoImg;
        Image fruit3 = berenjenaImg;
        Image fruit4 = lechugaImg;  
        for (Fruit fruit : fruits) {
            if (fruit.isActive) {
                int drawX = (int)(fruit.x * TILE_SIZE) + (TILE_SIZE - 25) / 2;
                int drawY = (int)(fruit.y * TILE_SIZE) + (TILE_SIZE - 25) / 2;

                // Culling
                if (drawX + 25 >= cameraX && drawX <= cameraX + getWidth() &&
                    drawY + 25 >= cameraY && drawY <= cameraY + getHeight()) {

                    // Dibujar según el tipo de fruta
                    switch (fruit.fruitType.toUpperCase()) {
                        case "ORANGE":
                            // Naranja con hoja
                            g2d.drawImage(fruit1, drawX, drawY, 25, 25, null);
                            break;

                        case "BANANA":
                            // Plátano curvo
                            g2d.drawImage(fruit2, drawX, drawY, 25, 25, null);
                            break;

                        case "EGGPLANT":
                            // Berenjena púrpura
                            g2d.drawImage(fruit3, drawX, drawY, 25, 25, null);
                            break;

                        case "LETTUCE":
                            // Lechuga con textura de hojas
                            g2d.drawImage(fruit4, drawX, drawY, 25, 25, null);
                            break;                    
                    }

                    // Efecto de brillo para indicar que es coleccionable
                    if ((System.currentTimeMillis() / 500) % 2 == 0) {
                        g2d.setColor(new Color(255, 255, 255, 80));
                        g2d.fillOval(drawX - 2, drawY - 2, 29, 29);
                    }

                    // Mostrar puntos que otorga
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 8));
                    String pointsText = "+" + fruit.points;
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(pointsText);
                    g2d.drawString(pointsText, drawX + (25 - textWidth) / 2, drawY + 35);
                }
            }
        }
    }
    
    /**
     * Dibuja un jugador con todos sus efectos visuales
     */
    private void drawPlayer(Graphics2D g2d, Player player, Color playerColor, String playerLabel) {
        Image img = playerLabel.equals("P1") ? player1Img : player2Img;
        if (!player.isAlive) return;
        
        // Durante la fase bonus, solo mostrar el jugador activo
        if (isBonusPhase) {
            if (playerLabel.equals("P1") && bonusPlayerId != 1) {
                return;
            }
            if (playerLabel.equals("P2") && bonusPlayerId != 2) {
                return;
            }
        }
        
        int drawX = (int)(player.x * TILE_SIZE) + (TILE_SIZE - PLAYER_SIZE) / 2;
        int drawY = (int)(player.y * TILE_SIZE) + (TILE_SIZE - PLAYER_SIZE) / 2;
        
        // Efecto de parpadeo cuando está dañado
        if (player.isDamaged && (System.currentTimeMillis() - player.damageTime) % 200 < 100) {
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(playerColor);
        }
        
        // Cuerpo del jugador
        g2d.drawImage(img, drawX, drawY, PLAYER_SIZE, PLAYER_SIZE, null);
        
        // Borde
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
        
        // Efecto de invulnerabilidad
        if (player.isInvulnerable) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRect(drawX - 5, drawY - 5, PLAYER_SIZE + 10, PLAYER_SIZE + 10);
        }
    }
    
    /**
     * Dibuja la interfaz de usuario (HUD)
     * Información de estadísticas, controles e instrucciones
     */
    private void drawUI(Graphics2D g2d) {
        // Información del Player 1 (POPO)
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("POPO (WASD + X)", 800, 30);
        g2d.drawString("Vidas: " + player1.lives, 800, 50);
        g2d.drawString("Score: " + player1.score, 800, 70);
        g2d.drawString("Estado: " + (player1.isAlive ? "Vivo" : "Muerto"), 800, 90);
        
        // Información sobre observadores
        g2d.setColor(Color.MAGENTA);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Pulse la tecla O para agregar observers", 800, getHeight() - 170);

        // Información del Player 2 (NANA) si está activo
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

            // Contador de tiempo con colores según urgencia
            int seconds = bonusTimeRemaining / 1000;
            g2d.setColor(seconds <= 10 ? Color.RED : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Tiempo: " + seconds + "s", getWidth() -200, 350);

            // Barra de progreso (código incompleto)
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
        
        // Indicador de conexión
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Conectado al servidor", getWidth() - 150, 20);
        
        // Información de controles (solo si no está en fase bonus)
        if (!isBonusPhase) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            int yOffset = getHeight() - 80;

            // Mensaje si Player 2 no está activo
            if (!isPlayer2Active) {
                g2d.setColor(Color.ORANGE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("NANA: INACTIVO", 800, yOffset - 20);
                g2d.setColor(Color.YELLOW);
                g2d.drawString("Presiona 'I' para activar a NANA", 800, yOffset + 15);
            }

            // Instrucciones sobre bloques bonus
            g2d.setColor(Color.MAGENTA);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Toca bloques brillantes para fase BONUS!", 10, getHeight() - 20);
        }
    }
}