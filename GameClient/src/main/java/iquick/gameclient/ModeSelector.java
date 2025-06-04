package iquick.gameclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ModeSelector extends JFrame {
    private JButton playerButton;
    private JButton spectatorButton;
    private JLabel titleLabel;
    private JLabel descriptionLabel;
    
    public ModeSelector() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Selector de Modo - Juego de Plataformas");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(45, 45, 45));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Título
        titleLabel = new JLabel("Selecciona el Modo de Juego");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Descripción
        descriptionLabel = new JLabel("<html><center>Elige si quieres jugar activamente<br>o solo observar el juego</center></html>");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionLabel.setForeground(Color.LIGHT_GRAY);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Botón Jugador
        playerButton = new JButton("MODO JUGADOR");
        playerButton.setFont(new Font("Arial", Font.BOLD, 16));
        playerButton.setPreferredSize(new Dimension(200, 50));
        playerButton.setMaximumSize(new Dimension(200, 50));
        playerButton.setBackground(new Color(0, 120, 215));
        playerButton.setForeground(Color.WHITE);
        playerButton.setFocusPainted(false);
        playerButton.setBorderPainted(false);
        playerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Botón Espectador
        spectatorButton = new JButton("MODO ESPECTADOR");
        spectatorButton.setFont(new Font("Arial", Font.BOLD, 16));
        spectatorButton.setPreferredSize(new Dimension(200, 50));
        spectatorButton.setMaximumSize(new Dimension(200, 50));
        spectatorButton.setBackground(new Color(46, 125, 50));
        spectatorButton.setForeground(Color.WHITE);
        spectatorButton.setFocusPainted(false);
        spectatorButton.setBorderPainted(false);
        spectatorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Agregar listeners
        playerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new GameClient());
            }
        });
        
        spectatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> new GameClient());
            }
        });
        
        // Agregar componentes al panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(descriptionLabel);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(playerButton);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(spectatorButton);
        
        add(mainPanel);
        setVisible(true);
    }
}