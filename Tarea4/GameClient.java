import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class GameClient extends JFrame implements KeyListener {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TILE_SIZE = 30;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    
    // Mapa del juego
    private int[][] gameMap;
    private int mapWidth = 0;
    private int mapHeight = 0;
    
    // Jugador
    private Player player;
    private GamePanel gamePanel;
    
    // Input handling
    private boolean[] keys = new boolean[256];
    private Timer gameTimer;
    
    public GameClient() {
        initializeWindow();
        player = new Player(100, 400); // Posición inicial
        connectToServer("localhost", 8080);
        startGameLoop();
    }
    
    private void initializeWindow() {
        setTitle("Juego de Plataformas - Cliente");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        addKeyListener(this);
        setFocusable(true);
        setLocationRelativeTo(null);
    }
    
    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            
            System.out.println("Conectado al servidor");
            
            // Hilo para recibir mensajes del servidor
            Thread receiveThread = new Thread(this::receiveMessages);
            receiveThread.start();
            
        } catch (IOException e) {
            System.err.println("Error conectando al servidor: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                processServerMessage(message);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Error recibiendo mensajes: " + e.getMessage());
            }
        }
    }
    
    private void processServerMessage(String message) {
        if (message.startsWith("MAP:")) {
            parseGameMap(message);
        } else if (message.startsWith("PLAYER:")) {
            // Procesar posición de otros jugadores (para multijugador futuro)
            System.out.println("Posición de otro jugador: " + message);
        }
    }
    
    private void parseGameMap(String mapMessage) {
        try {
            String[] parts = mapMessage.split(":");
            if (parts.length >= 3) {
                String[] dimensions = parts[1].split(",");
                mapWidth = Integer.parseInt(dimensions[0]);
                mapHeight = Integer.parseInt(dimensions[1]);
                
                gameMap = new int[mapHeight][mapWidth];
                
                String[] rows = parts[2].split(";");
                for (int y = 0; y < mapHeight && y < rows.length; y++) {
                    String[] cells = rows[y].split(",");
                    for (int x = 0; x < mapWidth && x < cells.length; x++) {
                        gameMap[y][x] = Integer.parseInt(cells[x]);
                    }
                }
                
                System.out.println("Mapa recibido: " + mapWidth + "x" + mapHeight);
                repaint();
            }
        } catch (Exception e) {
            System.err.println("Error parseando mapa: " + e.getMessage());
        }
    }
    
    private void startGameLoop() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateGame();
                repaint();
            }
        }, 0, 16); // ~60 FPS
    }
    
    private void updateGame() {
        if (player != null) {
            handleInput();
            player.update();
            checkCollisions();
            sendPlayerPosition();
        }
    }
    
    private void handleInput() {
        if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]) {
            player.moveLeft();
        }
        if (keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D]) {
            player.moveRight();
        }
        if (keys[KeyEvent.VK_SPACE] || keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W]) {
            player.jump();
        }
    }
    
    private void checkCollisions() {
        if (gameMap == null) return;
        
        int playerTileX = (int) (player.x / TILE_SIZE);
        int playerTileY = (int) ((player.y + player.height) / TILE_SIZE);
        
        // Verificar colisión con el suelo
        boolean onGround = false;
        if (playerTileY < mapHeight && playerTileX >= 0 && playerTileX < mapWidth) {
            if (gameMap[playerTileY][playerTileX] == 1) {
                player.y = playerTileY * TILE_SIZE - player.height;
                player.velocityY = 0;
                player.onGround = true;
                onGround = true;
            }
        }
        
        if (!onGround) {
            player.onGround = false;
        }
        
        // Mantener al jugador dentro de los límites de la pantalla
        if (player.x < 0) player.x = 0;
        if (player.x + player.width > WINDOW_WIDTH) player.x = WINDOW_WIDTH - player.width;
        if (player.y > WINDOW_HEIGHT) {
            // Respawn del jugador
            player.x = 100;
            player.y = 400;
            player.velocityY = 0;
        }
    }
    
    private void sendPlayerPosition() {
        if (connected && out != null) {
            String positionMessage = String.format("PLAYER:%.2f,%.2f", player.x, player.y);
            out.println(positionMessage);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // Panel personalizado para dibujar el juego
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Fondo
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // Dibujar mapa
            if (gameMap != null) {
                g.setColor(Color.GRAY);
                for (int y = 0; y < mapHeight; y++) {
                    for (int x = 0; x < mapWidth; x++) {
                        if (gameMap[y][x] == 1) {
                            g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            g.setColor(Color.WHITE);
                            g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            g.setColor(Color.GRAY);
                        }
                    }
                }
            }
            
            // Dibujar jugador
            if (player != null) {
                g.setColor(Color.RED);
                g.fillRect((int) player.x, (int) player.y, player.width, player.height);
            }
            
            // Información de estado
            g.setColor(Color.WHITE);
            g.drawString("Usa WASD o flechas para mover, ESPACIO para saltar", 10, 20);
            g.drawString("Estado: " + (connected ? "Conectado" : "Desconectado"), 10, 40);
            if (player != null) {
                g.drawString(String.format("Posición: (%.0f, %.0f)", player.x, player.y), 10, 60);
                g.drawString("En suelo: " + player.onGround, 10, 80);
            }
        }
    }
    
    @Override
    public void dispose() {
        connected = false;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error cerrando socket: " + e.getMessage());
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameClient().setVisible(true);
        });
    }
}

// Clase del jugador
class Player {
    public float x, y;
    public float velocityX = 0;
    public float velocityY = 0;
    public int width = 25;
    public int height = 25;
    public boolean onGround = false;
    
    private static final float GRAVITY = 0.5f;
    private static final float JUMP_STRENGTH = -12f;
    private static final float MOVE_SPEED = 3f;
    private static final float AIR_RESISTANCE = 0.8f;
    
    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }
    
    public void update() {
        // Aplicar gravedad
        if (!onGround) {
            velocityY += GRAVITY;
        }
        
        // Aplicar resistencia del aire al movimiento horizontal
        velocityX *= AIR_RESISTANCE;
        
        // Actualizar posición
        x += velocityX;
        y += velocityY;
        
        // Limitar velocidad de caída
        if (velocityY > 15) {
            velocityY = 15;
        }
    }
    
    public void moveLeft() {
        velocityX -= MOVE_SPEED;
        if (velocityX < -MOVE_SPEED * 2) {
            velocityX = -MOVE_SPEED * 2;
        }
    }
    
    public void moveRight() {
        velocityX += MOVE_SPEED;
        if (velocityX > MOVE_SPEED * 2) {
            velocityX = MOVE_SPEED * 2;
        }
    }
    
    public void jump() {
        if (onGround) {
            velocityY = JUMP_STRENGTH;
            onGround = false;
        }
    }
}