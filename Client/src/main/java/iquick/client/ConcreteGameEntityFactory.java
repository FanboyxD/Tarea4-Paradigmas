// Factory concreta para entidades del juego
package iquick.client;

public class ConcreteGameEntityFactory extends GameEntityFactory {
    @Override
    public Enemy createEnemy(double x, double y, EnemyType type) {
        // Convertir coordenadas de píxeles a grid
        int gridX = (int)(x / Client.getCellSize());
        int gridY = (int)(y / Client.getCellSize());
        Enemy enemy = new Enemy(gridX, gridY, type);
        enemy.setActive(true);
        return enemy;
    }
    
    @Override
    public Fruit createFruit(double x, double y, FruitType type) {
        // Convertir coordenadas de píxeles a grid
        int gridX = (int)(x / Client.getCellSize());
        int gridY = (int)(y / Client.getCellSize());
        // El constructor de Fruit espera coordenadas de grid (int), no píxeles (double)
        Fruit fruit = new Fruit(gridX, gridY, type);
        fruit.setActive(true);
        return fruit;
    }
    
    @Override
    public Enemy createEnemyFromGrid(int gridX, int gridY, EnemyType type) {
        Enemy enemy = new Enemy(gridX, gridY, type);
        enemy.setActive(true);
        return enemy;
    }
    
    @Override
    public Fruit createFruitFromGrid(int gridX, int gridY, FruitType type) {
        Fruit fruit = new Fruit(gridX, gridY, type);
        fruit.setActive(true);
        return fruit;
    }
}