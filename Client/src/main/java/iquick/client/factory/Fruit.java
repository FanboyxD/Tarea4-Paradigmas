package iquick.client.factory;

import iquick.client.Client;
import java.awt.*;

public class Fruit {
    private double x, y;
    private int gridX, gridY;
    private FruitType type;
    private boolean active;
    private int size;
    
    public Fruit(int x, int y, FruitType type) {
        this.gridX = x;
        this.gridY = y;
        this.x = x * Client.getCellSize();
        this.y = y * Client.getCellSize();
        this.type = type;
        this.active = false;
        this.size = Client.getCellSize() - 8;
    }
    
    public void spawn(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void copyFrom(Fruit other) {
        if (other != null) {
            this.gridX = other.gridX;
            this.gridY = other.gridY;
            this.type = other.type;
            
            boolean wasActive = this.active;
            this.active = other.active;
            
            if (other.active && !wasActive) {
                spawn(other.gridX * Client.getCellSize(), other.gridY * Client.getCellSize());
            }
        }
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setType(FruitType type) {
        this.type = type;
    }
    
    public void setGridPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }
    
    public boolean isActive() { return active; }
    public FruitType getType() { return type; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    
    public boolean intersects(double otherX, double otherY, int otherSize) {
        return x < otherX + otherSize &&
               x + size > otherX &&
               y < otherY + otherSize &&
               y + size > otherY;
    }
    
    public int getScore() {
        switch (type) {
            case NARANJA: return 100;
            case BANANO: return 200;
            case BERENJENA: return 300;
            case LECHUGA: return 400;
            default: return 0;
        }
    }
    
    public void draw(Graphics2D g) {
        if (!active) return;

        Color fruitColor;
        String typeIndicator = "";

        switch (type) {
            case NARANJA:
                fruitColor = new Color(255, 165, 0); // Naranja
                typeIndicator = "O";
                break;
            case BANANO:
                fruitColor = Color.YELLOW;
                typeIndicator = "B";
                break;
            case BERENJENA:
                fruitColor = new Color(128, 0, 128); // Púrpura
                typeIndicator = "E";
                break;
            case LECHUGA:
                fruitColor = Color.GREEN;
                typeIndicator = "L";
                break;
            default:
                fruitColor = Color.GRAY;
                typeIndicator = "?";
        }

        // Efecto brillante para las frutas
        g.setColor(new Color(fruitColor.getRed(), fruitColor.getGreen(), fruitColor.getBlue(), 100));
        g.fillOval((int)x - 2, (int)y - 2, size + 4, size + 4);
        
        g.setColor(fruitColor);
        g.fillOval((int)x, (int)y, size, size);
        g.setColor(Color.BLACK);
        g.drawOval((int)x, (int)y, size, size);

        // Dibujar indicador de tipo
        g.setColor(Color.WHITE);
        g.drawString(typeIndicator, (int)x + size/4, (int)y + size/2);
    }
}