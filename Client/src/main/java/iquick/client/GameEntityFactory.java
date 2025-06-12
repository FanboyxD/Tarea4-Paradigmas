// Abstract Factory para crear entidades del juego
package iquick.client;

public abstract class GameEntityFactory {
    public abstract Enemy createEnemy(double x, double y, EnemyType type);
    public abstract Fruit createFruit(double x, double y, FruitType type);
    
    // Métodos sobrecargados para crear desde coordenadas de grid
    public abstract Enemy createEnemyFromGrid(int gridX, int gridY, EnemyType type);
    public abstract Fruit createFruitFromGrid(int gridX, int gridY, FruitType type);
}