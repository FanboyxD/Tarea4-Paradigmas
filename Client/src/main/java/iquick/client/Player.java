package iquick.client;

import iquick.client.facade.NetworkManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;

public class Player {
    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -12.0;
    private static final double MOVE_SPEED = 3.0;
    
    private double x, y;
    private double velX, velY;
    private boolean onGround;
    private int size;
    private Color playerColor;
    
    // Nuevas propiedades para vidas y score
    private int lives;
    private int score;
    private static final int MAX_LIVES = 10;
    private static final int INITIAL_LIVES = 3;
    
    private boolean invulnerable;
    private int invulnerabilityTimer;
    private static final int INVULNERABILITY_TIME = 120; // 2 segundos a 60 FPS
    
    private boolean isAttacking;
    private int attackCooldown;
    private static final int ATTACK_DURATION = 15; // Duración del ataque en frames
    private static final int ATTACK_COOLDOWN_TIME = 30; // Cooldown entre ataques
    private static final int ATTACK_RANGE = 40; // Rango de ataque en píxeles
    
    private static final int ICE_ENEMY_POINTS = 10;
    private static final int FOCA_ENEMY_POINTS = 400;
    private static final int BIRD_ENEMY_POINTS = 800;
    
    private NetworkManager networkManager;
    private Client clientReference;
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.velX = 0;
        this.velY = 0;
        this.onGround = false;
        this.size = Client.getCellSize() - 4;
        this.playerColor = Color.BLUE; // Color por defecto para player 1

        // Inicializar vidas y score
        this.lives = INITIAL_LIVES;
        this.score = 0;

        // Inicializar invulnerabilidad
        this.invulnerable = false;
        this.invulnerabilityTimer = 0;

        // Inicializar ataque
        this.isAttacking = false;
        this.attackCooldown = 0;
    }
    
    public void setPosition(int gridX, int gridY) {
        this.x = gridX * Client.getCellSize();
        this.y = gridY * Client.getCellSize();
    }
    
    public void setPosition(double x, double y) {
    this.x = x;
    this.y = y;

    // Verificar que la posición esté dentro de los límites del juego
    if (y > (Client.getMatrixHeight() - 1) * Client.getCellSize()) {
        this.y = (Client.getMatrixHeight() - 2) * Client.getCellSize();
    }
    
    if (x < 0 || x > (Client.getMatrixWidth() - 1) * Client.getCellSize()) {
        this.x = Math.max(0, Math.min(x, (Client.getMatrixWidth() - 2) * Client.getCellSize()));
    }
}
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean isOnGround() {
        return onGround;
    }
    
    // Métodos para manejar vidas
    public int getLives() {
        return lives;
    }
    
    public boolean isAttacking() {
        return isAttacking;
    }

    public int getAttackRange() {
        return ATTACK_RANGE;
    }
    
    public void loseLive() {
        if (lives > 0) {
            lives--;
        }
    }
    
    // Agregar método para verificar si el jugador es invulnerable
    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    
    public void addLive() {
        if (lives < MAX_LIVES) {
            lives++;
        }
    }
    
    public boolean isAlive() {
        return lives > 0;
    }
    
    // Métodos para manejar score
    public int getScore() {
        return score;
    }
    
    public void addScore(int points) {
        score += points;
    }
    
    public void resetScore() {
        score = 0;
    }
    
    // Método para reiniciar el jugador
    public void reset() {
    lives = INITIAL_LIVES;
    score = 0;
    velX = 0;
    velY = 0;
    onGround = true; // Asegurar que esté en el suelo
    invulnerable = false;
    invulnerabilityTimer = 0;
    isAttacking = false;
    attackCooldown = 0;
    
    // Debug
    System.out.println("Player reset - Lives: " + lives + ", Position will be set externally");
}
    
    public void update(Set<Integer> keys, Platform[][] platforms, EnemyManager enemyManager, boolean isPlayer2) {
    if (lives <= 0) {
        return;
    }

    // Actualizar timer de invulnerabilidad
    if (invulnerable) {
        invulnerabilityTimer--;
        if (invulnerabilityTimer <= 0) {
            invulnerable = false;
        }
    }

    // Actualizar ataque
    if (isAttacking) {
        attackCooldown--;
        if (attackCooldown <= 0) {
            isAttacking = false;
        }
    } else if (attackCooldown > 0) {
        attackCooldown--;
    }

    // Verificar teclas de ataque según el jugador
    boolean attackPressed = false;
    if (isPlayer2) {
        attackPressed = keys.contains(KeyEvent.VK_P);
    } else {
        attackPressed = keys.contains(KeyEvent.VK_X);
    }
    
    if (attackPressed && !isAttacking && attackCooldown <= 0) {
        performAttack(enemyManager);
    }

    final double GROUND_SPEED = MOVE_SPEED;
    final double AIR_SPEED = MOVE_SPEED * 0.6;
    double currentMoveSpeed = onGround ? GROUND_SPEED : AIR_SPEED;

    velX = 0;

    // Verificar teclas de movimiento según el jugador
    boolean movingLeft, movingRight, jumping;
    
    if (isPlayer2) {
        movingLeft = keys.contains(KeyEvent.VK_LEFT);
        movingRight = keys.contains(KeyEvent.VK_RIGHT);
        jumping = keys.contains(KeyEvent.VK_UP);
    } else {
        movingLeft = keys.contains(KeyEvent.VK_A);
        movingRight = keys.contains(KeyEvent.VK_D);
        jumping = keys.contains(KeyEvent.VK_W);
    }

    if (movingLeft && !movingRight) {
        velX = -currentMoveSpeed;
    } else if (movingRight && !movingLeft) {
        velX = currentMoveSpeed;
    }

    if (jumping && onGround) {
        velY = JUMP_STRENGTH;
        onGround = false;
    }

    // Resto del método update permanece igual...
    velY += GRAVITY;
    if (velY > 15) velY = 15;

    // Movimiento horizontal
    if (velX != 0) {
        double newX = x + velX;
        if (newX >= 0 && newX <= (Client.getMatrixWidth() * Client.getCellSize() - size)) {
            if (!checkHorizontalCollision(newX, y, platforms)) {
                x = newX;
            }
        }
    }

    // Movimiento vertical
    if (velY != 0) {
        double newY = y + velY;
        if (newY >= (Client.getMatrixHeight() * Client.getCellSize() - size)) {
            y = Client.getMatrixHeight() * Client.getCellSize() - size;
            velY = 0;
            onGround = true;
            return;
        }
        if (newY < 0) {
            newY = 0;
            velY = 0;
        }
        if (checkVerticalCollision(x, newY, platforms)) {
            if (velY > 0) { 
                int gridY = (int)((newY + size) / Client.getCellSize());
                y = gridY * Client.getCellSize() - size;
                velY = 0;
                onGround = true;
            } else { 
                int gridY = (int)(newY / Client.getCellSize()) + 1;
                y = gridY * Client.getCellSize();
                velY = 0;
            }
        } else {
            y = newY;
            if (velY > 0) {
                onGround = false;
            }
        }
    }

    if (onGround) {
        double checkY = y + 1;
        if (!checkVerticalCollision(x, checkY, platforms) && 
            checkY < (Client.getMatrixHeight() * Client.getCellSize() - size)) {
            onGround = false;
        }
    }

    if (!invulnerable && enemyManager != null && enemyManager.checkPlayerCollision(x, y, size)) {
        handleEnemyCollision();
    }

    if (y > (Client.getMatrixHeight() * Client.getCellSize())) {
        if (!invulnerable) {
            loseLive();
            makeInvulnerable();
        }
        respawnPlayer();
    }
    checkSpecialBlockCollision(platforms);
}
    private void performAttack(EnemyManager enemyManager) {
        isAttacking = true;
        attackCooldown = ATTACK_DURATION;

        if (enemyManager != null) {
            // Usar las coordenadas actuales del jugador en el momento exacto del ataque
            double currentPlayerX = this.x;
            double currentPlayerY = this.y;

            AttackResult attackResult = enemyManager.checkAttackCollision(currentPlayerX, currentPlayerY, size, ATTACK_RANGE);

            if (attackResult.getTotalEnemiesDestroyed() > 0) {
                // Calcular puntos según el tipo de enemigo destruido
                int totalPoints = 0;
                totalPoints += attackResult.getIceEnemiesDestroyed() * ICE_ENEMY_POINTS;
                totalPoints += attackResult.getFocaEnemiesDestroyed() * FOCA_ENEMY_POINTS;
                totalPoints += attackResult.getBirdEnemiesDestroyed() * BIRD_ENEMY_POINTS;

                addScore(totalPoints);

                // Debug mejorado
                System.out.println("¡Ataque exitoso! Enemigos destruidos: " + 
                                 attackResult.getTotalEnemiesDestroyed() + 
                                 " (ICE: " + attackResult.getIceEnemiesDestroyed() + 
                                 ", FOCA: " + attackResult.getFocaEnemiesDestroyed() + 
                                 ", BIRD: " + attackResult.getBirdEnemiesDestroyed() + 
                                 ") - Puntos obtenidos: " + totalPoints);
            }
        }
        breakPlatformsInRange();
    }
    /**
     * Rompe las plataformas dentro del rango de ataque del jugador que se encuentran por encima de su posición actual.
     * 
     * Este método obtiene la matriz de plataformas desde el cliente y verifica, para cada plataforma no rota,
     * si se encuentra dentro del rango de ataque definido por {@code ATTACK_RANGE} y si está ubicada arriba del jugador.
     * Si ambas condiciones se cumplen, la plataforma es rota y se otorgan puntos al jugador.
     * 
     * No se consideran las plataformas de la primera y última fila o columna.
     */
    private void breakPlatformsInRange() {
        // Obtener las plataformas desde el cliente
        Platform[][] platforms = Client.getCurrentPlatforms();
        if (platforms == null) return;
        
        int centerX = (int)(x + size/2);
        int centerY = (int)(y + size/2);
        
        // Buscar plataformas en rango de ataque
        for (int i = 1; i < Client.getMatrixHeight() - 1; i++) { // Evitar primera y última fila
            for (int j = 1; j < Client.getMatrixWidth() - 1; j++) { // Evitar primera y última columna
                if (platforms[i][j] != null && !platforms[i][j].isBroken()) {
                    int platformCenterX = platforms[i][j].getPixelX() + Client.getCellSize()/2;
                    int platformCenterY = platforms[i][j].getPixelY() + Client.getCellSize()/2;
                    
                    double distance = Math.sqrt(Math.pow(centerX - platformCenterX, 2) + 
                                            Math.pow(centerY - platformCenterY, 2));
                    
                    if (distance <= ATTACK_RANGE) {
                        // Solo romper plataformas que están ARRIBA del jugador
                        if (platformCenterY < centerY) {
                            platforms[i][j].breakPlatform();
                            addScore(10); // Puntos por romper plataforma
                        }
                    }
                }
            }
        }
    }

    /**
     * Restaura la posición del jugador a una ubicación segura de respawn dentro de los límites del mapa.
     * Calcula una posición inicial predeterminada y ajusta la coordenada Y para asegurar que esté dentro de los límites válidos.
     * Reinicia la velocidad del jugador y permite que la física determine si el jugador está en el suelo.
     */
    private void respawnPlayer() {
        // Posición de respawn más segura: encontrar una posición válida
        double spawnX = 2 * Client.getCellSize();
        double spawnY = (Client.getMatrixHeight() - 2) * Client.getCellSize();
        
        // Asegurar que la posición de spawn esté dentro de los límites
        if (spawnY < 0) {
            spawnY = 0;
        }
        if (spawnY > (Client.getMatrixHeight() - 1) * Client.getCellSize()) {
            spawnY = (Client.getMatrixHeight() - 1) * Client.getCellSize();
        }
        
        setPosition(spawnX, spawnY);
        velX = 0;
        velY = 0;
        onGround = false; // Permitir que la física determine si está en el suelo
    }

    private void handleEnemyCollision() {
        loseLive();
        makeInvulnerable(); // Activar invulnerabilidad después de perder vida

        // Respawn más seguro
        respawnPlayer();
    }
    
    
    
    public void makeInvulnerable() {
        invulnerable = true;
        invulnerabilityTimer = INVULNERABILITY_TIME;
    }

    public void resetVelocity() {
        this.velX = 0;
        this.velY = 0;
        this.onGround = true;
    }
    
    /**
     * Verifica si existe una colisión horizontal entre el jugador y las plataformas.
     *
     * @param newX      La nueva posición X del jugador.
     * @param currentY  La posición Y actual del jugador.
     * @param platforms La matriz de plataformas del juego.
     * @return true si hay colisión horizontal con alguna plataforma no rota, false en caso contrario.
     */
    private boolean checkHorizontalCollision(double newX, double currentY, Platform[][] platforms) {
        int left = (int) (newX / Client.getCellSize());
        int right = (int) ((newX + size - 1) / Client.getCellSize());
        int top = (int) (currentY / Client.getCellSize());
        int bottom = (int) ((currentY + size - 1) / Client.getCellSize());
        
        for (int i = top; i <= bottom && i < Client.getMatrixHeight(); i++) {
            for (int j = left; j <= right && j < Client.getMatrixWidth(); j++) {
                if (i >= 0 && j >= 0 && platforms[i][j] != null && !platforms[i][j].isBroken()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkVerticalCollision(double currentX, double newY, Platform[][] platforms) {
        int left = (int) (currentX / Client.getCellSize());
        int right = (int) ((currentX + size - 1) / Client.getCellSize());
        int top = (int) (newY / Client.getCellSize());
        int bottom = (int) ((newY + size - 1) / Client.getCellSize());
        
        for (int i = top; i <= bottom && i < Client.getMatrixHeight(); i++) {
            for (int j = left; j <= right && j < Client.getMatrixWidth(); j++) {
                if (i >= 0 && j >= 0 && platforms[i][j] != null && !platforms[i][j].isBroken()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Agregar este método a la clase Player
    public void checkSpecialBlockCollision(Platform[][] platforms) {
        int gridX = (int) (x / Client.getCellSize());
        int gridY = (int) (y / Client.getCellSize());
        
        // Verificar la plataforma actual y las adyacentes
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int checkX = gridX + dx;
                int checkY = gridY + dy;
                
                if (checkX >= 0 && checkX < Client.getMatrixWidth() && 
                    checkY >= 0 && checkY < Client.getMatrixHeight()) {
                    
                    Platform platform = platforms[checkY][checkX];
                    if (platform != null && platform.isSpecial() && 
                        platform.intersects(x, y, size)) {
                        
                        // Enviar mensaje al servidor sobre la colisión
                        sendSpecialBlockCollisionMessage(checkX, checkY);
                        
                        // Aquí puedes agregar efectos locales
                        addLive();
                        System.out.println("¡Colisión con bloque especial!");
                    }
                }
            }
        }
    }


    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

// Método para establecer la referencia del cliente
    public void setClientReference(Client client) {
        this.clientReference = client;
    }

    // Modificar el método en la clase Player para activar el bonus
    private void sendSpecialBlockCollisionMessage(int blockX, int blockY) {
        if (networkManager != null && networkManager.isConnected()) {
            networkManager.sendMessage("BONUS");
            System.out.println("Enviado mensaje BONUS al servidor por colisión en bloque especial (" + blockX + "," + blockY + ")");
            
            // NUEVO: Activar el modo bonus localmente
            if (clientReference != null) {
                clientReference.activateBonusMode();
            }
        } else {
            System.out.println("NetworkManager no disponible o no conectado");
        }
    }
        
    public void draw(Graphics2D g) {
        // Siempre dibujar el jugador mientras tenga al menos 1 vida
        if (lives <= 0) {
            return;
        }
        
        // Efecto de parpadeo cuando es invulnerable
        if (invulnerable && (invulnerabilityTimer / 10) % 2 == 0) {
            // No dibujar en algunos frames para crear efecto de parpadeo
            return;
        }
        
        // Cambiar color si es invulnerable
        if (invulnerable) {
            g.setColor(new Color(0, 0, 255, 150)); // Azul semi-transparente
        } else {
            g.setColor(Color.BLUE);
        }
        
        g.fillRect((int)x, (int)y, size, size);
        g.setColor(Color.DARK_GRAY);
        g.drawRect((int)x, (int)y, size, size);
        
        // Dibujar área de ataque si está atacando
        if (isAttacking) {
            g.setColor(new Color(255, 0, 0, 100)); // Rojo semi-transparente
            int attackRadius = ATTACK_RANGE;
            g.fillOval((int)(x + size/2 - attackRadius/2), (int)(y + size/2 - attackRadius/2), 
                    attackRadius, attackRadius);
            g.setColor(Color.RED);
            g.drawOval((int)(x + size/2 - attackRadius/2), (int)(y + size/2 - attackRadius/2), 
                    attackRadius, attackRadius);
        }
    }
}