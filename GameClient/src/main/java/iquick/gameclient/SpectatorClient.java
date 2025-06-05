// SpectatorClient.java
package iquick.gameclient;

import javax.swing.*;
import java.util.List;

public class SpectatorClient extends JFrame implements GameObserver {

    private GamePanel spectatorPanel;
    private int[][] gameMap;
    private int mapWidth;
    private int mapHeight;
    private final GameClient gameClient;


    public SpectatorClient(GameClient gameClient) {
        setTitle("Espectador del Juego");
        setSize(1100, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear GamePanel espejo, pero compartiendo las mismas referencias de datos
        this.spectatorPanel = new GamePanel(
            gameClient.getPlayer1(),
            gameClient.getPlayer2(),
            gameClient.getDestroyedTiles(),
            gameClient.getEnemies(),
            gameClient.getFruits()
        );
        this.gameClient = gameClient;
        spectatorPanel.setConnectionStatus(true);

        // También reflejar estados relevantes
        spectatorPanel.setBonusPhase(gameClient.isBonusPhase());
        spectatorPanel.setBonusPlayerId(gameClient.getBonusPlayerId());
        spectatorPanel.setBonusTimeRemaining(gameClient.getBonusTimeRemaining());
        spectatorPanel.setPlayer2Active(gameClient.isPlayer2Active());
        spectatorPanel.setPlayerAbove(gameClient.getPlayerAbove());
        spectatorPanel.updateMap(gameClient.getGameMap(), gameClient.getMapWidth(), gameClient.getMapHeight());

        add(spectatorPanel);
        setVisible(true);

        // Registrar como observador
        gameClient.registerObserver(this);
    }
    

    @Override
    public void onGameStateUpdate() {
        SwingUtilities.invokeLater(() -> {
            spectatorPanel.setBonusPhase(gameClient.isBonusPhase());
            spectatorPanel.setBonusPlayerId(gameClient.getBonusPlayerId());
            spectatorPanel.setBonusTimeRemaining(gameClient.getBonusTimeRemaining());
            spectatorPanel.setPlayer2Active(gameClient.isPlayer2Active());
            spectatorPanel.setPlayerAbove(gameClient.getPlayerAbove());
            spectatorPanel.updateMap(gameClient.getGameMap(), gameClient.getMapWidth(), gameClient.getMapHeight());
            spectatorPanel.repaint();
        });
    }
}
