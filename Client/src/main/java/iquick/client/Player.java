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
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.velX = 0;
        this.velY = 0;
        this.onGround = false;
        this.size = Client.getCellSize() - 4;
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
    
    public void update(Set<Integer> keys, Platform[][] platforms) {
        final double GROUND_SPEED = MOVE_SPEED;
        final double AIR_SPEED = MOVE_SPEED * 0.6;
        
        double currentMoveSpeed = onGround ? GROUND_SPEED : AIR_SPEED;
        
        velX = 0;
        
        boolean movingLeft = keys.contains(KeyEvent.VK_A) || keys.contains(KeyEvent.VK_LEFT);
        boolean movingRight = keys.contains(KeyEvent.VK_D) || keys.contains(KeyEvent.VK_RIGHT);
        
        if (movingLeft && !movingRight) {
            velX = -currentMoveSpeed;
        } else if (movingRight && !movingLeft) {
            velX = currentMoveSpeed;
        }
        
        if ((keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_UP)) && onGround) {
            velY = JUMP_STRENGTH;
            onGround = false;
        }
        
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
        
        // Verificar si sigue en el suelo
        if (onGround) {
            double checkY = y + 1;
            if (!checkVerticalCollision(x, checkY, platforms) && 
                checkY < (Client.getMatrixHeight() * Client.getCellSize() - size)) {
                onGround = false;
            }
        }
    }
    
    private boolean checkHorizontalCollision(double newX, double currentY, Platform[][] platforms) {
        int left = (int) (newX / Client.getCellSize());
        int right = (int) ((newX + size - 1) / Client.getCellSize());
        int top = (int) (currentY / Client.getCellSize());
        int bottom = (int) ((currentY + size - 1) / Client.getCellSize());
        
        for (int i = top; i <= bottom && i < Client.getMatrixHeight(); i++) {
            for (int j = left; j <= right && j < Client.getMatrixWidth(); j++) {
                if (i >= 0 && j >= 0 && platforms[i][j] != null) {
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
                if (i >= 0 && j >= 0 && platforms[i][j] != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.fillRect((int)x, (int)y, size, size);
        g.setColor(Color.DARK_GRAY);
        g.drawRect((int)x, (int)y, size, size);
    }
}