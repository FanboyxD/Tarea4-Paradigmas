// SpectatorClient.java
package iquick.gameclient;

import javax.swing.*;
import java.util.List;

/**
 * Clase SpectatorClient que implementa una ventana de espectador para observar el juego
 * Hereda de JFrame para crear una interfaz gráfica e implementa GameObserver 
 * para recibir notificaciones de cambios en el estado del juego
 */
public class SpectatorClient extends JFrame implements GameObserver {
    // Panel que mostrará el estado visual del juego
    private GamePanel spectatorPanel;
    
    // Variables para almacenar información del mapa del juego
    private int[][] gameMap;
    private int mapWidth;
    private int mapHeight;
    
    // Referencia al cliente principal del juego para acceder a su estado
    private final GameClient gameClient;

    /**
     * Constructor que inicializa la ventana del espectador
     * @param gameClient Referencia al cliente principal del juego
     */
    public SpectatorClient(GameClient gameClient) {
        // Configuración básica de la ventana
        setTitle("Espectador del Juego");
        setSize(1100, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana al cerrar
        setLocationRelativeTo(null); // Centra la ventana en pantalla
        
        // Crear GamePanel espejo, compartiendo las mismas referencias de datos del juego principal
        // Esto permite que el espectador vea exactamente lo mismo que el juego original
        this.spectatorPanel = new GamePanel(
            gameClient.getPlayer1(),      // Referencia al jugador 1
            gameClient.getPlayer2(),      // Referencia al jugador 2
            gameClient.getDestroyedTiles(), // Lista de tiles destruidos
            gameClient.getEnemies(),      // Lista de enemigos
            gameClient.getFruits()        // Lista de frutas
        );
        
        // Guardar referencia al cliente del juego
        this.gameClient = gameClient;
        
        // Configurar el estado inicial del panel espectador
        spectatorPanel.setConnectionStatus(true); // Marcar como conectado
        
        // Sincronizar todos los estados relevantes del juego principal
        spectatorPanel.setBonusPhase(gameClient.isBonusPhase());
        spectatorPanel.setBonusPlayerId(gameClient.getBonusPlayerId());
        spectatorPanel.setBonusTimeRemaining(gameClient.getBonusTimeRemaining());
        spectatorPanel.setPlayer2Active(gameClient.isPlayer2Active());
        spectatorPanel.setPlayerAbove(gameClient.getPlayerAbove());
        
        // Actualizar el mapa del juego en el panel espectador
        spectatorPanel.updateMap(gameClient.getGameMap(), gameClient.getMapWidth(), gameClient.getMapHeight());
        
        // Agregar el panel a la ventana
        add(spectatorPanel);
        
        // Hacer visible la ventana
        setVisible(true);
        
        // Registrar este espectador como observador del juego principal
        // Esto permitirá recibir notificaciones cuando el estado del juego cambie
        gameClient.registerObserver(this);
    }
    
    /**
     * Método implementado de la interfaz GameObserver
     * Se ejecuta cada vez que hay una actualización en el estado del juego
     * Utiliza SwingUtilities.invokeLater para asegurar que las actualizaciones
     * de la interfaz gráfica se ejecuten en el hilo de eventos de Swing
     */
    @Override
    public void onGameStateUpdate() {
        SwingUtilities.invokeLater(() -> {
            // Sincronizar todos los estados del juego con el panel espectador
            spectatorPanel.setBonusPhase(gameClient.isBonusPhase());
            spectatorPanel.setBonusPlayerId(gameClient.getBonusPlayerId());
            spectatorPanel.setBonusTimeRemaining(gameClient.getBonusTimeRemaining());
            spectatorPanel.setPlayer2Active(gameClient.isPlayer2Active());
            spectatorPanel.setPlayerAbove(gameClient.getPlayerAbove());
            
            // Actualizar el mapa del juego
            spectatorPanel.updateMap(gameClient.getGameMap(), gameClient.getMapWidth(), gameClient.getMapHeight());
            
            // Forzar el redibujado del panel para mostrar los cambios
            spectatorPanel.repaint();
        });
    }
}