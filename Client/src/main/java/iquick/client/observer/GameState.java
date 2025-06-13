// GameState.java - Clase que encapsula el estado del juego
package iquick.client.observer;

import iquick.client.EnemyManager;
import iquick.client.FruitManager;
import iquick.client.Platform;
import iquick.client.Player;

public class GameState {
    private Player player1;
    private Player player2;
    private boolean player2Active;
    private Platform[][] platforms;
    private EnemyManager enemyManager;
    private FruitManager fruitManager;
    private int[][] gameMatrix;
    private String statusMessage;
    
    public GameState(Player player1, Player player2, boolean player2Active, 
                    Platform[][] platforms, EnemyManager enemyManager, 
                    FruitManager fruitManager, int[][] gameMatrix, String statusMessage) {
        this.player1 = player1;
        this.player2 = player2;
        this.player2Active = player2Active;
        this.platforms = platforms;
        this.enemyManager = enemyManager;
        this.fruitManager = fruitManager;
        this.gameMatrix = gameMatrix;
        this.statusMessage = statusMessage;
    }
    
    // Getters
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public boolean isPlayer2Active() { return player2Active; }
    public Platform[][] getPlatforms() { return platforms; }
    public EnemyManager getEnemyManager() { return enemyManager; }
    public FruitManager getFruitManager() { return fruitManager; }
    public int[][] getGameMatrix() { return gameMatrix; }
    public String getStatusMessage() { return statusMessage; }
}
