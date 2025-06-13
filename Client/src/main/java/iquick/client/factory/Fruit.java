package iquick.client.factory;

import iquick.client.Client;
import java.awt.*;

/**
 * Clase que representa una fruta en el juego.
 */
public class Fruit {
    // Posición en píxeles
    private double x, y;
    // Posición en la cuadrícula
    private int gridX, gridY;
    // Tipo de fruta
    private FruitType type;
    // Indica si la fruta está activa en el juego
    private boolean active;
    // Tamaño de la fruta en píxeles
    private int size;
    
    /**
     * Constructor de la fruta.
     * @param x posición X en la cuadrícula
     * @param y posición Y en la cuadrícula
     * @param type tipo de fruta
     */
    public Fruit(int x, int y, FruitType type) {
        this.gridX = x;
        this.gridY = y;
        this.x = x * Client.getCellSize();
        this.y = y * Client.getCellSize();
        this.type = type;
        this.active = false;
        this.size = Client.getCellSize() - 8;
    }
    
    /**
     * Establece la posición de la fruta en píxeles.
     * @param x coordenada X en píxeles
     * @param y coordenada Y en píxeles
     */
    public void spawn(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Copia los atributos de otra fruta.
     * @param other otra instancia de Fruit
     */
    public void copyFrom(Fruit other) {
        if (other != null) {
            this.gridX = other.gridX;
            this.gridY = other.gridY;
            this.type = other.type;
            
            boolean wasActive = this.active;
            this.active = other.active;
            
            // Si la fruta se activa, actualiza su posición
            if (other.active && !wasActive) {
                spawn(other.gridX * Client.getCellSize(), other.gridY * Client.getCellSize());
            }
        }
    }
    
    /**
     * Activa o desactiva la fruta.
     * @param active true para activar, false para desactivar
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Cambia el tipo de fruta.
     * @param type nuevo tipo de fruta
     */
    public void setType(FruitType type) {
        this.type = type;
    }
    
    /**
     * Cambia la posición de la fruta en la cuadrícula.
     * @param x nueva posición X en la cuadrícula
     * @param y nueva posición Y en la cuadrícula
     */
    public void setGridPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }
    
    // Métodos getter
    public boolean isActive() { return active; }
    public FruitType getType() { return type; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    
    /**
     * Verifica si esta fruta colisiona con otra.
     * @param otherX posición X de la otra fruta
     * @param otherY posición Y de la otra fruta
     * @param otherSize tamaño de la otra fruta
     * @return true si hay intersección, false en caso contrario
     */
    public boolean intersects(double otherX, double otherY, int otherSize) {
        return x < otherX + otherSize &&
               x + size > otherX &&
               y < otherY + otherSize &&
               y + size > otherY;
    }
    
    /**
     * Devuelve el puntaje asociado al tipo de fruta.
     * @return puntaje de la fruta
     */
    public int getScore() {
        switch (type) {
            case NARANJA: return 100;
            case BANANO: return 200;
            case BERENJENA: return 300;
            case LECHUGA: return 400;
            default: return 0;
        }
    }
    
    /**
     * Dibuja la fruta en el contexto gráfico.
     * @param g contexto gráfico
     */
    public void draw(Graphics2D g) {
        if (!active) return;

        Color fruitColor;
        String typeIndicator = "";

        // Selecciona el color y el indicador según el tipo de fruta
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
        
        // Dibuja la fruta
        g.setColor(fruitColor);
        g.fillOval((int)x, (int)y, size, size);
        g.setColor(Color.BLACK);
        g.drawOval((int)x, (int)y, size, size);

        // Dibuja el indicador de tipo de fruta
        g.setColor(Color.WHITE);
        g.drawString(typeIndicator, (int)x + size/4, (int)y + size/2);
    }
}