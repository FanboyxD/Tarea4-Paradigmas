package iquick.client;
public class AttackResult {    
    private int iceEnemiesDestroyed = 0;
    private int focaEnemiesDestroyed = 0;
    private int birdEnemiesDestroyed = 0;
    
    public void addIceEnemy() { iceEnemiesDestroyed++; }
    public void addFocaEnemy() { focaEnemiesDestroyed++; }
    public void addBirdEnemy() { birdEnemiesDestroyed++; }
    
    public int getIceEnemiesDestroyed() { return iceEnemiesDestroyed; }
    public int getFocaEnemiesDestroyed() { return focaEnemiesDestroyed; }
    public int getBirdEnemiesDestroyed() { return birdEnemiesDestroyed; }
    
    public int getTotalEnemiesDestroyed() {
        return iceEnemiesDestroyed + focaEnemiesDestroyed + birdEnemiesDestroyed;
    }
}