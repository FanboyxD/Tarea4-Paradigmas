package iquick.client;
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
    
    public void updateEnemies(Enemy[] newEnemies) {
        if (newEnemies != null && newEnemies.length <= MAX_ENEMIES) {
            for (int i = 0; i < newEnemies.length && i < MAX_ENEMIES; i++) {
                if (newEnemies[i] != null) {
                    boolean isNewOrReactivated = !enemies[i].isActive() && newEnemies[i].isActive();
                    boolean typeChanged = enemies[i].getType() != newEnemies[i].getType();

                    if (!newEnemies[i].isActive() && enemies[i].isActive()) {
                        enemies[i].setActive(false);
                        continue;
                    }

                    if (newEnemies[i].getType() == EnemyType.ICE && newEnemies[i].isActive()) {
                        boolean isSameICE = enemies[i].isActive() && 
                                           enemies[i].getType() == EnemyType.ICE &&
                                           enemies[i].isInitialized() &&
                                           enemies[i].getGridX() == newEnemies[i].getGridX() &&
                                           Math.abs(enemies[i].getGridY() - newEnemies[i].getGridY()) <= 1;

                        if (isSameICE) {
                            enemies[i].setGridPosition(newEnemies[i].getGridX(), newEnemies[i].getGridY());
                        } else {
                            enemies[i].copyFrom(newEnemies[i]);
                            double spawnX = newEnemies[i].getGridX() * Client.getCellSize();
                            double spawnY = 0;
                            enemies[i].forceRespawn(spawnX, spawnY);
                        }
                    } else {
                        enemies[i].copyFrom(newEnemies[i]);

                        if (newEnemies[i].isActive() && (isNewOrReactivated || typeChanged)) {
                            if (newEnemies[i].getType() == EnemyType.FOCA) {
                                double spawnX = newEnemies[i].getGridX() * Client.getCellSize();
                                double spawnY = newEnemies[i].getGridY() * Client.getCellSize();
                                enemies[i].forceRespawn(spawnX, spawnY);
                            }
                        }
                    }
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
    
    public int checkAttackCollision(double playerX, double playerY, int playerSize, int attackRange) {
        int enemiesDestroyed = 0;
        double playerCenterX = playerX + playerSize / 2.0;
        double playerCenterY = playerY + playerSize / 2.0;
        
        for (Enemy enemy : enemies) {
            if (enemy.isActive()) {
                double enemyCenterX = enemy.getX() + enemy.getSize();
                double enemyCenterY = enemy.getY() + enemy.getSize();
                
                double distance = Math.sqrt(Math.pow(playerCenterX - enemyCenterX, 2) + 
                                          Math.pow(playerCenterY - enemyCenterY, 2));
                
                if (distance <= attackRange) {
                    enemy.setActive(false);
                    enemiesDestroyed++;
                }
            }
        }
        
        return enemiesDestroyed;
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