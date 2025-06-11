package iquick.client;

import java.awt.Graphics2D;

public class EnemyManager {
    private static final int MAX_ENEMIES = 10;
    private Enemy[] enemies;
    
    public EnemyManager() {
        enemies = new Enemy[MAX_ENEMIES];
        for (int i = 0; i < MAX_ENEMIES; i++) {
            enemies[i] = new Enemy(0, 0, EnemyType.NONE);
        }
    }
    
    public void updateEnemies(Enemy[] newEnemies) {
        if (newEnemies != null && newEnemies.length <= MAX_ENEMIES) {
            for (int i = 0; i < newEnemies.length && i < MAX_ENEMIES; i++) {
                if (newEnemies[i] != null) {
                    // Actualizar enemigo existente
                    enemies[i].copyFrom(newEnemies[i]);
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
            enemies[index].spawn(x, y);
            enemies[index].setType(type);
            enemies[index].setActive(true);
        }
    }
    
    public void removeEnemy(int index) {
        if (index >= 0 && index < MAX_ENEMIES) {
            enemies[index].setActive(false);
        }
    }
    
    // Método para detectar colisiones con el jugador
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
    
    // Método para obtener información de depuración
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