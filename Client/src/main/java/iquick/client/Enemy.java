package iquick.client;

import java.awt.*;

public class Enemy {
    private static final double ENEMY_SPEED = 1.5;
    
    private double x, y;
    private int gridX, gridY;
    private double velX, velY;
    private EnemyType type;
    private boolean active;
    private int direction;
    private int size;
    private double speed;
    
    private double verticalDirection;
    private int movementTimer;
    private boolean onGround;
    private double fallSpeed;
    private boolean hasLanded;
    private boolean initialized = false;
    
    public Enemy(int x, int y, EnemyType type) {
        this.gridX = x;
        this.gridY = y;
        this.x = x * Client.getCellSize();
        this.y = y * Client.getCellSize();
        this.type = type;
        this.active = false;
        this.direction = 1;
        this.size = Client.getCellSize() - 8;
        this.speed = ENEMY_SPEED;
        this.verticalDirection = 1;
        this.movementTimer = 0;
        this.onGround = false;
        this.fallSpeed = 0;
        this.hasLanded = false;
    }
    
    public void spawn(double x, double y) {
        // Solo hacer spawn si no ha sido inicializado previamente
        if (!initialized) {
            this.x = x;
            this.y = y;
            this.hasLanded = false;
            this.onGround = false;
            this.fallSpeed = 0;
            this.movementTimer = 0;
            this.initialized = true; // Marcar como inicializado

            // Solo cambiar la dirección basada en la posición X si no es un enemigo FOCA
            if (type != EnemyType.FOCA) {
                if (x < Client.getMatrixWidth() * Client.getCellSize() / 2) {
                    direction = 1;
                } else {
                    direction = -1;
                }
            }

            switch (type) {
                case FOCA:
                    speed = ENEMY_SPEED;
                    // Para FOCA, establecer dirección hacia el centro de la pantalla
                    double centerX = Client.getMatrixWidth() * Client.getCellSize() / 2.0;
                    if (x < centerX) {
                        direction = 1; // Moverse hacia la derecha
                    } else {
                        direction = -1; // Moverse hacia la izquierda
                    }
                    break;
                case BIRD:
                    speed = ENEMY_SPEED * 1.5;
                    verticalDirection = Math.random() < 0.5 ? 1 : -1;
                    break;
                case ICE:
                    speed = ENEMY_SPEED * 0.8;
                    fallSpeed = 2.0;
                    this.y = 0;
                    break;
            }
        }
    }

    public void copyFrom(Enemy other) {
        if (other != null) {
            this.gridX = other.gridX;
            this.gridY = other.gridY;

            boolean wasActive = this.active;
            EnemyType previousType = this.type;

            this.type = other.type;
            this.active = other.active;

            // Solo hacer spawn si:
            // 1. El enemigo se acaba de activar (no estaba activo antes)
            // 2. O si cambió de tipo (es un enemigo diferente)
            if (other.active && (!wasActive || previousType != other.type)) {
                // Resetear el estado de inicialización para permitir nuevo spawn
                this.initialized = false;

                // Si es FOCA, el EnemyManager manejará la posición especial
                if (this.type != EnemyType.FOCA) {
                    spawn(other.gridX * Client.getCellSize(), other.gridY * Client.getCellSize());
                } else {
                    // Para FOCA, usar las coordenadas del grid pero dejar que EnemyManager ajuste
                    spawn(other.gridX * Client.getCellSize(), other.gridY * Client.getCellSize());
                }
            }

            // Si se desactiva, resetear inicialización para futuros spawns
            if (!other.active) {
                this.initialized = false;
            }
        }
    }

    public void setActive(boolean active) {
        // Si se desactiva, resetear inicialización
        if (!active && this.active) {
            this.initialized = false;
        }
        this.active = active;
    }

    // Método para verificar si está inicializado
    public boolean isInitialized() {
        return initialized;
    }

    // Método para forzar un respawn (usado por EnemyManager para casos especiales)
    public void forceRespawn(double x, double y) {
        this.initialized = false; // Resetear estado
        spawn(x, y); // Hacer spawn con nueva posición
    }
    
    public void setGridPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }
    
    public void setType(EnemyType type) {
        this.type = type;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public EnemyType getType() {
        return type;
    }
    
    public int getGridX() {
        return gridX;
    }
    
    public int getGridY() {
        return gridY;
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
    
    public boolean intersects(double otherX, double otherY, int otherSize) {
        return x < otherX + otherSize &&
               x + size > otherX &&
               y < otherY + otherSize &&
               y + size > otherY;
    }
    
    public void update(Platform[][] platforms) {
        if (!active) return;

        switch (type) {
            case FOCA:
                updateBasic(platforms);
                break;
            case BIRD:
                updateFast(platforms);
                break;
            case ICE:
                updateStrong(platforms);
                break;
        }

        // Actualizar posición del grid después del movimiento (solo para tipos que lo necesiten)
        if (type == EnemyType.ICE) {
            updateGridPosition();
        }
    }
    
    public void updateGridPosition() {
        this.gridX = (int)(x / Client.getCellSize());
        this.gridY = (int)(y / Client.getCellSize());
    }
    
    private void updateBasic(Platform[][] platforms) {
        velX = direction * speed;
        double newX = x + velX;
        
        if (newX < 0 || newX > (Client.getMatrixWidth() * Client.getCellSize() - size)) {
            direction *= -1;
            return;
        }
        
        if (checkCollision(newX, y, platforms)) {
            direction *= -1;
            return;
        }
        
        double futureX = newX + (direction * Client.getCellSize());
        if (!checkCollision(futureX, y + Client.getCellSize(), platforms) && 
            y + Client.getCellSize() < Client.getMatrixHeight() * Client.getCellSize()) {
            direction *= -1;
            return;
        }
        
        x = newX;
    }
    
    private void updateFast(Platform[][] platforms) {
        movementTimer++;

        if (movementTimer > 60 + (int)(Math.random() * 60)) {
            if (Math.random() < 0.3) {
                direction *= -1;
            }
            if (Math.random() < 0.3) {
                verticalDirection *= -1;
            }
            movementTimer = 0;
        }

        // Movimiento horizontal
        velX = direction * speed;
        double newX = x + velX;

        // Solo verificar límites de pantalla, no plataformas
        if (newX < 0 || newX > (Client.getMatrixWidth() * Client.getCellSize() - size)) {
            direction *= -1;
            newX = x;
        }

        // El BIRD puede atravesar plataformas, así que no verificamos colisiones
        x = newX;

        // Movimiento vertical
        velY = verticalDirection * speed * 0.7;
        double newY = y + velY;

        // Solo verificar límites de pantalla, no plataformas
        if (newY < 0 || newY > (Client.getMatrixHeight() * Client.getCellSize() - size)) {
            verticalDirection *= -1;
            newY = y;
        }

        // El BIRD puede atravesar plataformas, así que no verificamos colisiones
        y = newY;
    }
    
    private void updateStrong(Platform[][] platforms) {
        // El enemigo ICE solo cae en vertical atravesando todas las plataformas
        fallSpeed += 0.8;
        if (fallSpeed > 8) fallSpeed = 8;

        // Movimiento solo vertical
        double newY = y + fallSpeed;
        y = newY;

        // Actualizar también la posición del grid para que el servidor pueda rastrearlo
        gridY = (int)(y / Client.getCellSize());

        // Verificar si ha salido del mapa por abajo
        if (y > Client.getMatrixHeight() * Client.getCellSize()) {
            // El servidor detectará que está fuera de los límites y lo eliminará
            // No desactivamos aquí para evitar desincronización
        }
    }
    
    private boolean checkCollision(double newX, double newY, Platform[][] platforms) {
        int left = (int) (newX / Client.getCellSize());
        int right = (int) ((newX + size) / Client.getCellSize());
        int top = (int) (newY / Client.getCellSize());
        int bottom = (int) ((newY + size) / Client.getCellSize());
        
        for (int i = top; i <= bottom && i < Client.getMatrixHeight(); i++) {
            for (int j = left; j <= right && j < Client.getMatrixWidth(); j++) {
                if (i >= 0 && j >= 0 && i < Client.getMatrixHeight() && j < Client.getMatrixWidth() && platforms[i][j] != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g) {
        if (!active) return;

        Color enemyColor;
        String typeIndicator = "";

        switch (type) {
            case FOCA:
                enemyColor = Color.RED;
                typeIndicator = "B";
                break;
            case BIRD:
                enemyColor = Color.ORANGE;
                typeIndicator = "F";
                g.setColor(new Color(255, 200, 0, 100));
                g.fillOval((int)x - 2, (int)y - 2, size + 4, size + 4);
                break;
            case ICE:
                enemyColor = Color.MAGENTA;
                typeIndicator = "S";
                // Efecto visual especial para ICE
                g.setColor(new Color(255, 0, 255, 150));
                for (int i = 0; i < 3; i++) {
                    g.drawOval((int)x - i*2, (int)y - i*2, size + i*4, size + i*4);
                }
                break;
            default:
                enemyColor = Color.GRAY;
                typeIndicator = "?";
        }

        g.setColor(enemyColor);
        g.fillOval((int)x, (int)y, size, size);
        g.setColor(Color.BLACK);
        g.drawOval((int)x, (int)y, size, size);

        // Dibujar indicador de tipo
        g.setColor(Color.WHITE);
        g.drawString(typeIndicator, (int)x + size/4, (int)y + size/2);
    }
}