package iquick.client;
import java.awt.Graphics2D;

public class FruitManager {
    private static final int MAX_FRUITS = 10;
    private Fruit[] fruits;
    private GameEntityFactory entityFactory;
    
    public FruitManager() {
        this.entityFactory = new ConcreteGameEntityFactory();
        fruits = new Fruit[MAX_FRUITS];
        for (int i = 0; i < MAX_FRUITS; i++) {
            fruits[i] = entityFactory.createFruitFromGrid(0, 0, FruitType.NONE);
            fruits[i].setActive(false); // Inicialmente inactivas
        }
    }
    
    public void updateFruits(Fruit[] newFruits) {
        if (newFruits != null && newFruits.length <= MAX_FRUITS) {
            for (int i = 0; i < newFruits.length && i < MAX_FRUITS; i++) {
                if (newFruits[i] != null) {
                    if (!newFruits[i].isActive()) {
                        fruits[i].setActive(false);
                        continue;
                    }
                    
                    boolean isNewFruit = !fruits[i].isActive() && newFruits[i].isActive();
                    boolean typeChanged = fruits[i].getType() != newFruits[i].getType();
                    
                    if (isNewFruit || typeChanged || (newFruits[i].isActive() && !fruits[i].isActive())) {
                        fruits[i].copyFrom(newFruits[i]);
                    }
                }
            }
        }
    }
    
    public void drawAll(Graphics2D g) {
        for (Fruit fruit : fruits) {
            if (fruit.isActive()) {
                fruit.draw(g);
            }
        }
    }
    
    public Fruit getFruit(int index) {
        if (index >= 0 && index < MAX_FRUITS) {
            return fruits[index];
        }
        return null;
    }
    
    public Fruit[] getAllFruits() {
        return fruits;
    }
    
    public int getActiveFruitCount() {
        int count = 0;
        for (Fruit fruit : fruits) {
            if (fruit.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    public void clearAll() {
        for (Fruit fruit : fruits) {
            fruit.setActive(false);
        }
    }
    
    // Método para spawn usando factory
    public void spawnFruit(int index, double x, double y, FruitType type) {
        if (index >= 0 && index < MAX_FRUITS) {
            fruits[index] = entityFactory.createFruit(x, y, type);
        }
    }
    
    // Método alternativo para spawn desde coordenadas de grid
    public void spawnFruitFromGrid(int index, int gridX, int gridY, FruitType type) {
        if (index >= 0 && index < MAX_FRUITS) {
            fruits[index] = entityFactory.createFruitFromGrid(gridX, gridY, type);
        }
    }
    
    public int checkPlayerCollision(double playerX, double playerY, int playerSize) {
        int totalScore = 0;
        for (Fruit fruit : fruits) {
            if (fruit.isActive()) {
                if (fruit.intersects(playerX, playerY, playerSize)) {
                    totalScore += fruit.getScore();
                    fruit.setActive(false);
                }
            }
        }
        return totalScore;
    }
    
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Frutas activas: ").append(getActiveFruitCount()).append("/").append(MAX_FRUITS);
        
        for (int i = 0; i < MAX_FRUITS; i++) {
            if (fruits[i].isActive()) {
                info.append("\n  Fruit ").append(i).append(": ")
                    .append(fruits[i].getType()).append(" en (")
                    .append(fruits[i].getGridX()).append(", ")
                    .append(fruits[i].getGridY()).append(") - ")
                    .append(fruits[i].getScore()).append(" pts");
            }
        }
        
        return info.toString();
    }
}