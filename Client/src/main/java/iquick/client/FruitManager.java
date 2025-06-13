package iquick.client;
import iquick.client.factory.ConcreteGameEntityFactory;
import iquick.client.factory.Fruit;
import iquick.client.factory.FruitType;
import iquick.client.factory.GameEntityFactory;
import java.awt.Graphics2D;

public class FruitManager {
    private static final int MAX_FRUITS = 4;
    private Fruit[] fruits;
    private GameEntityFactory entityFactory;
    private int[][] currentGameMatrix;
    
    public FruitManager() {
        this.entityFactory = new ConcreteGameEntityFactory();
        fruits = new Fruit[MAX_FRUITS];
        for (int i = 0; i < MAX_FRUITS; i++) {
            fruits[i] = entityFactory.createFruitFromGrid(0, 0, FruitType.NONE);
            fruits[i].setActive(false); // Inicialmente inactivas
        }
    }
    public void setGameMatrix(int[][] gameMatrix) {
        this.currentGameMatrix = gameMatrix;
    }
    public void updateFruits(Fruit[] newFruits) {
    // Si no hay nuevas frutas, no hacer nada (mantener las existentes)
    if (newFruits == null) {
        return;
    }
    
    if (newFruits.length <= MAX_FRUITS) {
        for (int i = 0; i < newFruits.length && i < MAX_FRUITS; i++) {
            if (newFruits[i] != null && newFruits[i].isActive()) {
                // Verificar si la posición está libre antes de spawnear
                int gridX = newFruits[i].getGridX();
                int gridY = newFruits[i].getGridY();
                
                if (isValidSpawnPosition(gridX, gridY)) {
                    // Solo agregar frutas NUEVAS que vienen activas del servidor
                    // Buscar un slot vacío para la nueva fruta
                    boolean spawned = false;
                    for (int j = 0; j < MAX_FRUITS; j++) {
                        if (!fruits[j].isActive()) {
                            // Slot vacío encontrado, crear nueva fruta aquí
                            fruits[j].copyFrom(newFruits[i]);
                            
                            double spawnX = gridX * Client.getCellSize();
                            double spawnY = gridY * Client.getCellSize();
                            fruits[j].spawn(spawnX, spawnY);
                            
                            spawned = true;
                            break;
                        }
                    }   
                }
            }
        }
    }
}
    private boolean isValidSpawnPosition(int gridX, int gridY) {
    // Verificar límites de la matriz
    if (gridX < 0 || gridX >= Client.getMatrixWidth() || 
        gridY < 0 || gridY >= Client.getMatrixHeight()) {
        return false;
    }
    
    // Si no tenemos referencia a la matriz, permitir spawn (comportamiento por defecto)
    if (currentGameMatrix == null) {
        return true;
    }
    
    // Verificar que la posición no tenga una plataforma (valor 1 o 22)
    int cellValue = currentGameMatrix[gridY][gridX];
    return cellValue == 0; // Solo permitir spawn en celdas vacías
}
    // Método para limpiar frutas recolectadas u obsoletas
public void cleanupCollectedFruits(double playerX, double playerY, int playerSize) {
    for (Fruit fruit : fruits) {
        if (fruit.isActive() && fruit.intersects(playerX, playerY, playerSize)) {
            fruit.setActive(false);
            // Aquí podrías agregar lógica para sumar puntos, etc.
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
        if (isValidSpawnPosition(gridX, gridY)) {
            fruits[index] = entityFactory.createFruitFromGrid(gridX, gridY, type);
        } else {
            System.out.println("No se puede spawnear fruta en posición ocupada (" + gridX + ", " + gridY + ")");
        }
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