// Factory concreta para entidades del juego
package iquick.client.factory;

import iquick.client.Client;
import iquick.client.factory.Fruit;
import iquick.client.factory.Enemy;
import iquick.client.factory.FruitType;
import iquick.client.factory.EnemyType;
import iquick.client.factory.GameEntityFactory;

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
    
    /**
     * Crea una nueva instancia de Enemy en las coordenadas de la cuadrícula especificadas y del tipo indicado.
     * 
     * @param gridX la posición X en la cuadrícula donde se creará el enemigo.
     * @param gridY la posición Y en la cuadrícula donde se creará el enemigo.
     * @param type el tipo de enemigo a crear.
     * @return una nueva instancia de Enemy activa en la posición y tipo especificados.
     */
    @Override
    public Enemy createEnemyFromGrid(int gridX, int gridY, EnemyType type) {
        Enemy enemy = new Enemy(gridX, gridY, type);
        enemy.setActive(true);
        return enemy;
    }
    
    /**
     * Crea una nueva instancia de {@link Fruit} en las coordenadas de la cuadrícula especificadas
     * y del tipo indicado. El fruto creado se marca como activo.
     *
     * @param gridX la posición X en la cuadrícula donde se creará el fruto
     * @param gridY la posición Y en la cuadrícula donde se creará el fruto
     * @param type el tipo de fruto a crear
     * @return una nueva instancia de {@link Fruit} activa en la posición y tipo especificados
     */
    @Override
    public Fruit createFruitFromGrid(int gridX, int gridY, FruitType type) {
        Fruit fruit = new Fruit(gridX, gridY, type);
        fruit.setActive(true);
        return fruit;
    }
}