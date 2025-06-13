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

    // Limpiar enemigos que han salido de los límites
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
    
    public void updateAll(Platform[][] platforms) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.update(platforms);
            }
        }
    }
    
    public void drawAll(Graphics2D g) {
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                enemy.draw(g);
            }
        }
    }
    
    public Enemy getEnemy(int index) {
        if (index >= 0 && index < MAX_ENEMIES) {
            return enemies[index];
        }
        return null;
    }
    
    public Enemy[] getAllEnemies() {
        return enemies;
    }
    
    public int getActiveEnemyCount() {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    public void clearAll() {
        for (Enemy enemy : enemies) {
            enemy.setActive(false);
        }
    }
    
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
    
    public void removeEnemy(int index) {
        if (index >= 0 && index < MAX_ENEMIES) {
            enemies[index].setActive(false);
        }
    }
    
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
    
    public void increaseAllEnemySpeed(double speedMultiplier) {
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.setSpeedMultiplier(speedMultiplier);
            }
        }
    }
    
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