package iquick.client;
import java.awt.Graphics2D;

public class EnemyManager {
    private static final int MAX_ENEMIES = 10;
    private Enemy[] enemies;
    private Player playerReference; // Referencia al jugador para obtener su posición
    
    public EnemyManager() {
        enemies = new Enemy[MAX_ENEMIES];
        for (int i = 0; i < MAX_ENEMIES; i++) {
            enemies[i] = new Enemy(0, 0, EnemyType.NONE);
        }
    }
    
    // Método para establecer la referencia al jugador
    public void setPlayerReference(Player player) {
        this.playerReference = player;
    }
    
    public void updateEnemies(Enemy[] newEnemies) {
    if (newEnemies != null && newEnemies.length <= MAX_ENEMIES) {
        for (int i = 0; i < newEnemies.length && i < MAX_ENEMIES; i++) {
            if (newEnemies[i] != null) {
                // Verificar si es un enemigo completamente nuevo o reactivado
                boolean isNewOrReactivated = !enemies[i].isActive() && newEnemies[i].isActive();
                boolean typeChanged = enemies[i].getType() != newEnemies[i].getType();

                // Primero copiar los datos básicos del enemigo
                enemies[i].copyFrom(newEnemies[i]);

                // Manejo especial para enemigos FOCA que se acaban de activar o cambiar de tipo
                if (newEnemies[i].getType() == EnemyType.FOCA && 
                    newEnemies[i].isActive() && 
                    (isNewOrReactivated || typeChanged)) {

                    // Usar directamente las coordenadas del servidor (que ahora serán espacios vacíos)
                    double spawnX = newEnemies[i].getGridX() * Client.getCellSize();
                    double spawnY = newEnemies[i].getGridY() * Client.getCellSize();

                    // Forzar reinicialización y aplicar posición de spawn del servidor
                    enemies[i].forceRespawn(spawnX, spawnY);
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
            // Si es un enemigo FOCA y tenemos referencia al jugador, ajustar la posición Y
            if (type == EnemyType.FOCA && playerReference != null) {
                int playerRow = (int)(playerReference.getY() / Client.getCellSize());
                y = playerRow * Client.getCellSize();
            }
            
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
