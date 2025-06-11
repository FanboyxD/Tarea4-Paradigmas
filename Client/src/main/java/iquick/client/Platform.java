package iquick.client;

import java.awt.*;

public class Platform {
    private int x, y;
    
    public Platform(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getGridX() {
        return x;
    }
    
    public int getGridY() {
        return y;
    }
    
    public int getPixelX() {
        return x * Client.getCellSize();
    }
    
    public int getPixelY() {
        return y * Client.getCellSize();
    }
    
    public boolean intersects(double otherX, double otherY, int otherSize) {
        int pixelX = getPixelX();
        int pixelY = getPixelY();
        int cellSize = Client.getCellSize();
        
        return otherX < pixelX + cellSize &&
               otherX + otherSize > pixelX &&
               otherY < pixelY + cellSize &&
               otherY + otherSize > pixelY;
    }
    
    public void draw(Graphics2D g) {
        g.setColor(new Color(101, 67, 33));
        g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                   Client.getCellSize(), Client.getCellSize());
    }
}