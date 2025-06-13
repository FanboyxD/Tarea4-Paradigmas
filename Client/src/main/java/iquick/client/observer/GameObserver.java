// GameObserver.java - Interfaz Observer
package iquick.client.observer;

public interface GameObserver {
    void updateGameState(GameState gameState);
    void updateConnectionStatus(String status);
}