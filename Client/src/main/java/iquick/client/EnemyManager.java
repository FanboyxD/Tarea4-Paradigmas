package iquick.client;
import iquick.client.factory.ConcreteGameEntityFactory;
import iquick.client.factory.Enemy;
import iquick.client.factory.EnemyType;
import iquick.client.factory.GameEntityFactory;
import java.awt.Graphics2D;

public class EnemyManager {
    private static final int MAX_ENEMIES = 10;
    private Enemy[] enemies;
    private Player playerReference;
    private GameEntityFactory entityFactory;
    
    public EnemyManager() {
        this.entityFactory = new ConcreteGameEntityFactory();
        enemies = new Enemy[MAX_ENEMIES];
        for (int i = 0; i < MAX_ENEMIES; i++) {
            enemies[i] = entityFactory.createEnemyFromGrid(0, 0, EnemyType.NONE);
            enemies[i].setActive(false); // Inicialmente inactivos
        }
    }
    
    public void setPlayerReference(Player player) {
        this.playerReference = player;
    }
    
    // En EnemyManager.java - Modificar updateEnemies
    public void updateEnemies(Enemy[] newEnemies) {
        // Si no hay nuevos enemigos, no hacer nada (mantener los existentes)
        if (newEnemies == null) {
            return;
        }

        if (newEnemies.length <= MAX_ENEMIES) {
            for (int i = 0; i < newEnemies.length && i < MAX_ENEMIES; i++) {
                if (newEnemies[i] != null && newEnemies[i].isActive()) {
                    // Solo agregar enemigos NUEVOS que vienen activos del servidor
                    // Buscar un slot vacío para el nuevo enemigo
                    boolean spawned = false;
                    for (int j = 0; j < MAX_ENEMIES; j++) {
                        if (!enemies[j].isActive()) {
                            // Slot vacío encontrado, crear nuevo enemigo aquí
                            enemies[j].copyFrom(newEnemies[i]);

                            double spawnX = newEnemies[i].getGridX() * Client.getCellSize();
                            double spawnY = newEnemies[i].getGridY() * Client.getCellSize();

                            // Ajustar posición Y para enemigos FOCA para que aparezcan en el mismo piso del jugador
                            if (newEnemies[i].getType() == EnemyType.FOCA && playerReference != null) {
                                int playerRow = (int)(playerReference.getY() / Client.getCellSize());
                                spawnY = playerRow * Client.getCellSize();
                            }

                            if (newEnemies[i].getType() == EnemyType.ICE) {
                                enemies[j].forceRespawn(spawnX, spawnY);
                            } else if (newEnemies[i].getType() == EnemyType.FOCA) {
                                enemies[j].forceRespawn(spawnX, spawnY);
                            } else if (newEnemies[i].getType() == EnemyType.BIRD) {
                                enemies[j].forceRespawn(spawnX, spawnY);
                            }

                            spawned = true;
                            break;
                        }
                    }

                    // Si no se encontró slot vacío, reemplazar el más antiguo (opcional)
                    if (!spawned) {
                        System.out.println("No hay slots disponibles para nuevo enemigo");
                    }
                }
            }
        }
    }

    /**
     * Desactiva los enemigos que han salido fuera de los límites del mapa.
     * 
     * Recorre la lista de enemigos y verifica si cada enemigo activo se encuentra
     * fuera de los límites definidos por el tamaño de la celda y las dimensiones de la matriz.
     * Si un enemigo está fuera de estos límites, se marca como inactivo.
     */
    public void cleanupOutOfBounds() {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                // Limpiar enemigos que han salido de los límites del mapa
                if (enemy.getX() < -Client.getCellSize() || 
                    enemy.getX() > (Client.getMatrixWidth() + 1) * Client.getCellSize() ||
                    enemy.getY() > (Client.getMatrixHeight() + 1) * Client.getCellSize()) {
                    enemy.setActive(false);
                }
            }
        }
    }
    
    /**
     * Actualiza todos los enemigos activos en la lista.
     * 
     * Recorre la colección de enemigos y llama al método {@code update} de cada enemigo
     * que esté activo, pasando la matriz de plataformas como parámetro.
     *
     * @param platforms Matriz bidimensional de plataformas sobre las que los enemigos interactúan.
     */
    public void updateAll(Platform[][] platforms) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.update(platforms);
            }
        }
    }
    
    /**
     * Dibuja todos los enemigos activos en el contexto gráfico proporcionado.
     *
     * @param g El objeto Graphics2D donde se dibujarán los enemigos.
     */
    public void drawAll(Graphics2D g) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.draw(g);
            }
        }
    }
    
    /**
     * Devuelve el enemigo en la posición especificada del arreglo de enemigos.
     *
     * @param index el índice del enemigo a obtener (debe estar entre 0 y MAX_ENEMIES - 1)
     * @return el objeto Enemy en la posición dada, o null si el índice está fuera de rango
     */
    public Enemy getEnemy(int index) {
        if (index >= 0 && index < MAX_ENEMIES) {
            return enemies[index];
        }
        return null;
    }
    
    public Enemy[] getAllEnemies() {
        return enemies;
    }
    
    /**
     * Devuelve la cantidad de enemigos que están actualmente activos.
     *
     * Recorre la lista de enemigos y cuenta cuántos de ellos tienen el estado activo.
     *
     * @return el número de enemigos activos.
     */
    public int getActiveEnemyCount() {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Desactiva todos los enemigos en la lista estableciendo su estado como inactivo.
     * Este método recorre la colección de enemigos y llama al método setActive(false)
     * en cada uno de ellos, asegurando que ninguno permanezca activo.
     */
    public void clearAll() {
        for (Enemy enemy : enemies) {
            enemy.setActive(false);
        }
    }
    
    /**
     * Genera un nuevo enemigo en la posición especificada y del tipo indicado.
     * Si el tipo de enemigo es FOCA y existe una referencia al jugador, el enemigo
     * aparecerá alineado en la misma fila que el jugador.
     *
     * @param index Índice en el arreglo de enemigos donde se almacenará el nuevo enemigo.
     * @param x Coordenada X donde se generará el enemigo.
     * @param y Coordenada Y donde se generará el enemigo.
     * @param type Tipo de enemigo a generar.
     */
    public void spawnEnemy(int index, double x, double y, EnemyType type) {
        if (index >= 0 && index < MAX_ENEMIES) {
            if (type == EnemyType.FOCA && playerReference != null) {
                int playerRow = (int)(playerReference.getY() / Client.getCellSize());
                y = playerRow * Client.getCellSize();
            }
            
            // Usar factory para crear nuevo enemigo
            enemies[index] = entityFactory.createEnemy(x, y, type);
            enemies[index].spawn(x, y);
        }
    }
    
    /**
     * Desactiva al enemigo en la posición especificada del arreglo de enemigos.
     * 
     * @param index el índice del enemigo que se desea desactivar.
     *              Debe estar en el rango de 0 (inclusive) a MAX_ENEMIES (exclusive).
     */
    public void removeEnemy(int index) {
        if (index >= 0 && index < MAX_ENEMIES) {
            enemies[index].setActive(false);
        }
    }
    
    /**
     * Verifica si el jugador colisiona con algún enemigo activo.
     *
     * @param playerX     La posición X del jugador.
     * @param playerY     La posición Y del jugador.
     * @param playerSize  El tamaño del jugador.
     * @return true si el jugador colisiona con algún enemigo activo, false en caso contrario.
     */
    public boolean checkPlayerCollision(double playerX, double playerY, int playerSize) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                if (enemy.intersects(playerX, playerY, playerSize)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Verifica la colisión de un ataque del jugador con los enemigos activos.
     * Calcula la distancia entre el centro del jugador y el centro de cada enemigo activo.
     * Si la distancia es menor o igual al rango de ataque, considera que el enemigo ha sido atacado,
     * lo desactiva y actualiza el resultado según el tipo de enemigo.
     *
     * @param playerX      La coordenada X de la esquina superior izquierda del jugador.
     * @param playerY      La coordenada Y de la esquina superior izquierda del jugador.
     * @param playerSize   El tamaño (ancho/alto) del jugador.
     * @param attackRange  El rango de alcance del ataque del jugador.
     * @return             Un objeto AttackResult que contiene la cantidad de enemigos impactados por tipo.
     */
    public AttackResult checkAttackCollision(double playerX, double playerY, int playerSize, int attackRange) {
        AttackResult result = new AttackResult();
        double playerCenterX = playerX + playerSize / 2.0;
        double playerCenterY = playerY + playerSize / 2.0;
        
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                double enemyCenterX = enemy.getX() + enemy.getSize();
                double enemyCenterY = enemy.getY() + enemy.getSize();
                
                double distance = Math.sqrt(Math.pow(playerCenterX - enemyCenterX, 2) + 
                                        Math.pow(playerCenterY - enemyCenterY, 2));
                
                if (distance <= attackRange) {
                    // Contar por tipo de enemigo
                    switch (enemy.getType()) {
                        case ICE:
                            result.addIceEnemy();
                            break;
                        case FOCA:
                            result.addFocaEnemy();
                            break;
                        case BIRD:
                            result.addBirdEnemy();
                            break;
                    }
                    enemy.setActive(false);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Incrementa la velocidad de todos los enemigos aplicando un multiplicador de velocidad.
     *
     * @param speedMultiplier El factor por el cual se multiplicará la velocidad de cada enemigo.
     */
    public void increaseAllEnemySpeed(double speedMultiplier) {
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.setSpeedMultiplier(speedMultiplier);
            }
        }
    }
    
    /**
     * Genera una cadena de texto con información de depuración sobre los enemigos activos.
     * Incluye la cantidad de enemigos activos y, para cada enemigo activo, su tipo y posición en la cuadrícula.
     *
     * @return Una cadena con información detallada de los enemigos activos.
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Enemigos activos: ").append(getActiveEnemyCount()).append("/").append(MAX_ENEMIES);
        
        for (int i = 0; i < MAX_ENEMIES; i++) {
            if (enemies[i].isActive()) {
                info.append("\n  Enemy ").append(i).append(": ")
                    .append(enemies[i].getType()).append(" en (")
                    .append(enemies[i].getGridX()).append(", ")
                    .append(enemies[i].getGridY()).append(")");
            }
        }
        
        return info.toString();
    }
}