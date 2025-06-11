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
        this.x = x;
        this.y = y;
        this.hasLanded = false;
        this.onGround = false;
        this.fallSpeed = 0;
        this.movementTimer = 0;
        
        if (x < Client.getMatrixWidth() * Client.getCellSize() / 2) {
            direction = 1;
        } else {
            direction = -1;
        }
        
        switch (type) {
            case FOCA:
                speed = ENEMY_SPEED;
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
    
    public void copyFrom(Enemy other) {
        if (other != null) {
            this.gridX = other.gridX;
            this.gridY = other.gridY;
            this.type = other.type;
            this.active = other.active;
            
            if (other.active && !this.active) {
                // Enemigo que se acaba de activar
                spawn(other.x, other.y);
            }
            
            this.active = other.active;
        }
    }
    
    public void setGridPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }
    
    public void setType(EnemyType type) {
        this.type = type;
    }
    
    public void setActive(boolean active) {
        this.active = active;
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
        
        velX = direction * speed;
        double newX = x + velX;
        
        if (newX < 0 || newX > (Client.getMatrixWidth() * Client.getCellSize() - size)) {
            direction *= -1;
            newX = x;
        }
        
        if (!checkCollision(newX, y, platforms)) {
            x = newX;
        } else {
            direction *= -1;
        }
        
        velY = verticalDirection * speed * 0.7;
        double newY = y + velY;
        
        if (newY < 0 || newY > (Client.getMatrixHeight() * Client.getCellSize() - size)) {
            verticalDirection *= -1;
            newY = y;
        }
        
        if (!checkCollision(x, newY, platforms)) {
            y = newY;
        } else {
            verticalDirection *= -1;
        }
    }
    
    private void updateStrong(Platform[][] platforms) {
        if (!hasLanded) {
            fallSpeed += 0.8;
            if (fallSpeed > 8) fallSpeed = 8;
            
            double newY = y + fallSpeed;
            
            if (checkCollision(x, newY, platforms) || newY >= (Client.getMatrixHeight() * Client.getCellSize() - size)) {
                hasLanded = true;
                onGround = true;
                fallSpeed = 0;
                
                if (newY >= (Client.getMatrixHeight() * Client.getCellSize() - size)) {
                    y = Client.getMatrixHeight() * Client.getCellSize() - size;
                } else {
                    int gridY = (int)((y + size + fallSpeed) / Client.getCellSize());
                    y = gridY * Client.getCellSize() - size;
                }
            } else {
                y = newY;
            }
        } else {
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
                if (!hasLanded) {
                    g.setColor(new Color(255, 0, 255, 150));
                    for (int i = 0; i < 3; i++) {
                        g.drawOval((int)x - i*2, (int)y - i*2, size + i*4, size + i*4);
                    }
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
    }
}