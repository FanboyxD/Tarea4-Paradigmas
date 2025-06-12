package iquick.client;
import java.awt.Graphics2D;
import java.awt.*;

public class Fruit {
    public enum Type {
        NONE, NARANJA, BANANO, BERENJENA, LECHUGA
    }

    private int x;
    private int y;
    private Type type;
    private boolean collected;
    private boolean active; // Indica si el fruto está activo o no

    public Fruit(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.collected = false;
        this.active = false; // Inicializar como inactivo
    }

    public void spawn(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
        this.collected = false;
        this.active = true; // Activar el fruto al hacer spawn
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setGridPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    private boolean isActive() {
        return active;
    }

    public void draw(Graphics2D g) {
        if (!active) {
            return; // No dibujar si el fruto no está activo
        }
        
        Color fruitColor;

        switch (type) {
            case NARANJA:
                fruitColor = java.awt.Color.ORANGE;
                break;
            case BANANO:
                fruitColor = java.awt.Color.YELLOW;
                break;
            case BERENJENA:
                fruitColor = new java.awt.Color(128, 0, 128); // Color berenjena
                break;
            case LECHUGA:
                fruitColor = java.awt.Color.GREEN;
                break;
            default:
                fruitColor = java.awt.Color.GRAY; // Color por defecto
        }
        if (!collected) {
            switch (type) {
                case NONE:
                    g.setColor(java.awt.Color.GRAY); // Color por defecto para NONE
                    break;
                case NARANJA:
                    g.setColor(java.awt.Color.ORANGE);
                    break;
                case BANANO:
                    g.setColor(java.awt.Color.YELLOW);
                    break;
                case BERENJENA:
                    g.setColor(new java.awt.Color(128, 0, 128)); // Color berenjena
                    break;
                case LECHUGA:
                    g.setColor(java.awt.Color.GREEN);
                    break;
            }
            g.fillOval(x, y, 20, 20); // Dibujar el fruto como un círculo
            g.setColor(fruitColor);
        }
    }

    public void forceRespawn(double x, double y) {
        this.collected = false;
        spawn(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Type getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
    }

    public int getPoints() {
        switch (type) {
            case NONE:
                return 0;
            case NARANJA:
                return 100;
            case BANANO:
                return 200;
            case BERENJENA:
                return 300;
            case LECHUGA:
                return 400;
            default:
                return 0;
        }
    }
}
