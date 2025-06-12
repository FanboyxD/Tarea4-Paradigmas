// GameObserver.java - Interfaz Observer
package iquick.client;

public interface GameObserver {
    void updateGameState(GameState gameState);
    void updateConnectionStatus(String status);
}