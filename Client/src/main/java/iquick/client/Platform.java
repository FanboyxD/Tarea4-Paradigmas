package iquick.client;
import java.awt.*;

public class Platform {
    private int x, y;
    private boolean isBroken = false;
    private long brokenTime = 0;
    private static final long REGENERATION_TIME = 3000; // 3 segundos en millisegundos
    private PlatformType type; // NUEVO: Tipo de plataforma
    
    // Constructor modificado para aceptar tipo
    public Platform(int x, int y, PlatformType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    
    // Constructor por compatibilidad (por defecto NORMAL)
    public Platform(int x, int y) {
        this(x, y, PlatformType.NORMAL);
    }
    
    // NUEVO: Getter para el tipo
    public PlatformType getType() {
        return type;
    }
    
    // NUEVO: Método para verificar si es especial
    public boolean isSpecial() {
        return type == PlatformType.SPECIAL;
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
    
    public boolean intersects(double otherX, double otherY, int otherSize) {
        int pixelX = getPixelX();
        int pixelY = getPixelY();
        int cellSize = Client.getCellSize();
        
        return otherX < pixelX + cellSize &&
               otherX + otherSize > pixelX &&
               otherY < pixelY + cellSize &&
               otherY + otherSize > pixelY;
    }
    
    // Método draw modificado para manejar diferentes tipos
    public void draw(Graphics2D g) {
        if (!isBroken) {
            switch (type) {
                case NORMAL:
                    // Plataforma normal (marrón)
                    g.setColor(new Color(101, 67, 33));
                    g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                               Client.getCellSize(), Client.getCellSize());
                    break;
                    
                case SPECIAL:
                    // Plataforma especial (dorada con brillo)
                    g.setColor(new Color(255, 215, 0)); // Dorado
                    g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                               Client.getCellSize(), Client.getCellSize());
                    
                    // Efecto de brillo
                    g.setColor(new Color(255, 255, 255, 100));
                    g.fillRect(x * Client.getCellSize() + 2, y * Client.getCellSize() + 2, 
                               Client.getCellSize() - 4, Client.getCellSize() - 4);
                    
                    // Borde especial
                    g.setColor(new Color(255, 140, 0));
                    g.setStroke(new BasicStroke(2));
                    g.drawRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                               Client.getCellSize(), Client.getCellSize());
                    break;
            }
        } else {
            // Dibujar plataforma rota con efecto visual
            Color brokenColor = (type == PlatformType.SPECIAL) ? 
                new Color(255, 215, 0, 100) : new Color(101, 67, 33, 100);
            
            g.setColor(brokenColor);
            g.drawRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                       Client.getCellSize(), Client.getCellSize());
            g.setColor(new Color(255, 0, 0, 50));
            g.fillRect(x * Client.getCellSize(), y * Client.getCellSize(), 
                       Client.getCellSize(), Client.getCellSize());
        }
    }
}