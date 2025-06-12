package iquick.client;

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
    private static final int MAX_LIVES = 3;
    private static final int INITIAL_LIVES = 3;
    
    private boolean invulnerable;
    private int invulnerabilityTimer;
    private static final int INVULNERABILITY_TIME = 120; // 2 segundos a 60 FPS
    
    private boolean isAttacking;
    private int attackCooldown;
    private static final int ATTACK_DURATION = 15; // Duración del ataque en frames
    private static final int ATTACK_COOLDOWN_TIME = 30; // Cooldown entre ataques
    private static final int ATTACK_RANGE = 40; // Rango de ataque en píxeles
    
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
        onGround = false;
        invulnerable = false;
        invulnerabilityTimer = 0;
        isAttacking = false;
        attackCooldown = 0;
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
}
    private void performAttack(EnemyManager enemyManager) {
        isAttacking = true;
        attackCooldown = ATTACK_DURATION;

        if (enemyManager != null) {
            // Usar las coordenadas actuales del jugador en el momento exacto del ataque
            double currentPlayerX = this.x;
            double currentPlayerY = this.y;

            int enemiesDestroyed = enemyManager.checkAttackCollision(currentPlayerX, currentPlayerY, size, ATTACK_RANGE);
            if (enemiesDestroyed > 0) {
                // Agregar puntos por cada enemigo destruido
                addScore(enemiesDestroyed * 50);
                System.out.println("¡Ataque exitoso! Enemigos destruidos: " + enemiesDestroyed); // Debug
            }
        }
        breakPlatformsInRange();
    }
    private void breakPlatformsInRange() {
    // Obtener las plataformas desde el cliente
    Platform[][] platforms = Client.getCurrentPlatforms();
    if (platforms == null) return;
    
    int centerX = (int)(x + size/2);
    int centerY = (int)(y + size/2);
    
    // Buscar plataformas en rango de ataque
    for (int i = 0; i < Client.getMatrixHeight(); i++) {
        for (int j = 0; j < Client.getMatrixWidth(); j++) {
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
                        System.out.println("¡Plataforma rota en (" + j + "," + i + ")!");
                    }
                }
            }
        }
    }
}

    // NUEVO: Método para respawn más seguro
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

    // CORREGIDO: Método para manejar colisión con enemigos
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
    this.onGround = false;
}
    
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