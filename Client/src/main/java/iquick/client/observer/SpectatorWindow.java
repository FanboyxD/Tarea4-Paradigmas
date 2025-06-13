// SpectatorWindow.java - Ventana del espectador (CORREGIDO)
package iquick.client.observer;

import iquick.client.Platform;
import iquick.client.Player;
import iquick.client.observer.GameState;
import iquick.client.observer.GameObserver;
import javax.swing.*;
import java.awt.*;

public class SpectatorWindow extends JFrame implements GameObserver {
    private static final int MATRIX_WIDTH = 26;
    private static final int MATRIX_HEIGHT = 16;
    private static final int CELL_SIZE = 30;
    
    private JPanel gamePanel;
    private JLabel statusLabel;
    private JLabel spectatorLabel;
    private GameState currentGameState;
    private int spectatorId;
    
    public SpectatorWindow(int spectatorId) {
        this.spectatorId = spectatorId;
        setupUI();
    }
    
    private void setupUI() {
        setTitle("Espectador #" + spectatorId + " - Modo Observación");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);
        
        setLayout(new BorderLayout());
        
        // Panel del juego (solo visualización)
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentGameState != null) {
                    drawGame(g);
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(MATRIX_WIDTH * CELL_SIZE, MATRIX_HEIGHT * CELL_SIZE));
        gamePanel.setBackground(Color.LIGHT_GRAY);
        
        add(gamePanel, BorderLayout.CENTER);
        
        // Panel de información
        JPanel infoPanel = new JPanel(new BorderLayout());
        
        spectatorLabel = new JLabel("MODO ESPECTADOR - Solo visualización", JLabel.CENTER);
        spectatorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        spectatorLabel.setForeground(Color.RED);
        infoPanel.add(spectatorLabel, BorderLayout.NORTH);
        
        statusLabel = new JLabel("Esperando datos del juego...", JLabel.CENTER);
        infoPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(infoPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    @Override
    public void updateGameState(GameState gameState) {
        SwingUtilities.invokeLater(() -> {
            this.currentGameState = gameState;
            gamePanel.repaint();
        });
    }
    
    @Override
    public void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Espectador #" + spectatorId + " - " + status);
        });
    }
    
    private void drawGame(Graphics g) {
        if (currentGameState == null) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Plataformas
        Platform[][] platforms = currentGameState.getPlatforms();
        if (platforms != null) {
            for (int i = 0; i < MATRIX_HEIGHT; i++) {
                for (int j = 0; j < MATRIX_WIDTH; j++) {
                    if (platforms[i][j] != null) {
                        platforms[i][j].draw(g2d);
                    }
                }
            }
        }

        // Frutas
        if (currentGameState.getFruitManager() != null) {
            currentGameState.getFruitManager().drawAll(g2d);
        }

        // Jugador 1
        if (currentGameState.getPlayer1() != null) {
            currentGameState.getPlayer1().draw(g2d);
        }
        
        // Jugador 2 (si está activo)
        if (currentGameState.isPlayer2Active() && currentGameState.getPlayer2() != null) {
            currentGameState.getPlayer2().draw(g2d);
        }

        // Enemigos
        if (currentGameState.getEnemyManager() != null) {
            currentGameState.getEnemyManager().drawAll(g2d);
        }

        // Grid
        g2d.setColor(new Color(255, 255, 255, 50));
        for (int i = 0; i <= MATRIX_WIDTH; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, MATRIX_HEIGHT * CELL_SIZE);
        }
        for (int i = 0; i <= MATRIX_HEIGHT; i++) {
            g2d.drawLine(0, i * CELL_SIZE, MATRIX_WIDTH * CELL_SIZE, i * CELL_SIZE);
        }

        // Overlay de espectador
        g2d.setColor(new Color(255, 0, 0, 100));
        g2d.fillRect(0, 0, MATRIX_WIDTH * CELL_SIZE, 30);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("ESPECTADOR #" + spectatorId, 10, 20);

        // Mostrar mensaje de Game Over
        Player player1 = currentGameState.getPlayer1();
        Player player2 = currentGameState.getPlayer2();
        boolean player2Active = currentGameState.isPlayer2Active();
        
        if (player1 != null && !player1.isAlive() && 
            (!player2Active || (player2 != null && !player2.isAlive()))) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, MATRIX_WIDTH * CELL_SIZE, MATRIX_HEIGHT * CELL_SIZE);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String gameOverText = "GAME OVER";
            int textWidth = fm.stringWidth(gameOverText);
            int textX = (MATRIX_WIDTH * CELL_SIZE - textWidth) / 2;
            int textY = (MATRIX_HEIGHT * CELL_SIZE) / 2;
            g2d.drawString(gameOverText, textX, textY);
        }
    }
}