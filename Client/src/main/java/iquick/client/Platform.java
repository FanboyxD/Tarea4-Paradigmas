package iquick.client;

import java.awt.*;

public class Platform {
    private int x, y;
    private boolean isBroken = false;
    private long brokenTime = 0;
    private static final long REGENERATION_TIME = 3000; // 3 segundos en millisegundos
    
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
    // NUEVOS MÉTODOS:
    public void breakPlatform() {
        isBroken = true;
        brokenTime = System.currentTimeMillis();
    }
    
    public boolean isBroken() {
        return isBroken;
    }
    
    public void update() {
        if (isBroken && System.currentTimeMillis() - brokenTime >= REGENERATION_TIME) {
            isBroken = false;
        }
    }
    public enum PlatformType {
        NORMAL,
        BONUS_TRIGGER
    }

    private PlatformType type = PlatformType.NORMAL;

    // AGREGAR constructor con tipo
    public Platform(int x, int y, PlatformType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    // AGREGAR getter para tipo
    public PlatformType getType() {
        return type;
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
    if (!isBroken) {
        if (type == PlatformType.BONUS_TRIGGER) {
            // Plataforma especial dorada con brillo
            g.setColor(new Color(255, 215, 0));
            g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                       Client.getCellSize(), Client.getCellSize());
            
            // Efecto de brillo
            g.setColor(new Color(255, 255, 0, 100));
            g.fillRect(x * Client.getCellSize() + 2, y * Client.getCellSize() + 2, 
                       Client.getCellSize() - 4, Client.getCellSize() - 4);
        } else {
            // Plataforma normal
            g.setColor(new Color(101, 67, 33));
            g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                       Client.getCellSize(), Client.getCellSize());
        }
    } else {
        // Dibujar plataforma rota con efecto visual
        g.setColor(new Color(101, 67, 33, 100));
        g.drawRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                   Client.getCellSize(), Client.getCellSize());
        g.setColor(new Color(255, 0, 0, 50));
        g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                   Client.getCellSize(), Client.getCellSize());
    }
}

}