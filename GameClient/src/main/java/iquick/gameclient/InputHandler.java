package iquick.gameclient;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Clase que maneja todas las entradas del teclado para un juego de dos jugadores.
 * Implementa KeyListener para capturar eventos de teclado.
 */
public class InputHandler implements KeyListener {
    
    // ===== VARIABLES DE ESTADO PARA JUGADOR 1 =====
    // Controles: WASD para movimiento + X para atacar
    private boolean player1Left = false;    // Tecla A - mover izquierda
    private boolean player1Right = false;   // Tecla D - mover derecha
    private boolean player1Jump = false;    // Tecla W - saltar
    private boolean player1Attack = false;  // Tecla X - atacar
    
    // ===== VARIABLES DE ESTADO PARA JUGADOR 2 =====
    // Controles: Flechas para movimiento + P para atacar
    private boolean player2Left = false;    // Flecha izquierda - mover izquierda
    private boolean player2Right = false;   // Flecha derecha - mover derecha
    private boolean player2Jump = false;    // Flecha arriba - saltar
    private boolean player2Attack = false;  // Tecla P - atacar
    
    // ===== CONTROLES ESPECIALES =====
    private boolean activatePlayer2 = false; // Tecla I - activar jugador 2
    
    /**
     * Método llamado cuando se presiona una tecla.
     * Actualiza el estado de las variables booleanas correspondientes.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // ===== PROCESAMIENTO DE CONTROLES DEL JUGADOR 1 =====
        switch (keyCode) {
            case KeyEvent.VK_W:
                player1Jump = true;     // W - Salto del jugador 1
                break;
            case KeyEvent.VK_A:
                player1Left = true;     // A - Movimiento izquierda jugador 1
                break;
            case KeyEvent.VK_D:
                player1Right = true;    // D - Movimiento derecha jugador 1
                break;
            case KeyEvent.VK_X:
                player1Attack = true;   // X - Ataque del jugador 1
                break;
        }
        
        // ===== PROCESAMIENTO DE CONTROLES DEL JUGADOR 2 =====
        switch (keyCode) {
            case KeyEvent.VK_UP:
                player2Jump = true;     // Flecha arriba - Salto del jugador 2
                break;
            case KeyEvent.VK_LEFT:
                player2Left = true;     // Flecha izquierda - Movimiento izquierda jugador 2
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = true;    // Flecha derecha - Movimiento derecha jugador 2
                break;
            case KeyEvent.VK_P:
                player2Attack = true;   // P - Ataque del jugador 2
                break;
        }
        
        // ===== PROCESAMIENTO DE CONTROLES ESPECIALES =====
        switch (keyCode) {
            case KeyEvent.VK_I:
                activatePlayer2 = true; // I - Activar/mostrar jugador 2 en el juego
                break;
        }
    }
    
    /**
     * Método llamado cuando se suelta una tecla.
     * Solo se procesan las teclas de movimiento continuo (izquierda/derecha)
     * ya que salto y ataque son acciones instantáneas.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // ===== LIBERACIÓN DE CONTROLES DE MOVIMIENTO JUGADOR 1 =====
        switch (keyCode) {
            case KeyEvent.VK_A:
                player1Left = false;    // Dejar de mover izquierda jugador 1
                break;
            case KeyEvent.VK_D:
                player1Right = false;   // Dejar de mover derecha jugador 1
                break;
        }
        
        // ===== LIBERACIÓN DE CONTROLES DE MOVIMIENTO JUGADOR 2 =====
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                player2Left = false;    // Dejar de mover izquierda jugador 2
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = false;   // Dejar de mover derecha jugador 2
                break;
        }
    }
    
    /**
     * Método requerido por KeyListener pero no utilizado en esta implementación.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // No se usa en este caso - solo necesitamos keyPressed y keyReleased
    }
    
    // ===== MÉTODOS GETTER PARA JUGADOR 1 =====
    // Permiten consultar el estado actual de las teclas del jugador 1
    
    public boolean isPlayer1LeftPressed() {
        return player1Left;
    }
    
    public boolean isPlayer1RightPressed() {
        return player1Right;
    }
    
    public boolean isPlayer1JumpPressed() {
        return player1Jump;
    }
    
    public boolean isPlayer1AttackPressed() {
        return player1Attack;
    }
    
    // ===== MÉTODOS GETTER PARA JUGADOR 2 =====
    // Permiten consultar el estado actual de las teclas del jugador 2
    
    public boolean isPlayer2LeftPressed() {
        return player2Left;
    }
    
    public boolean isPlayer2RightPressed() {
        return player2Right;
    }
    
    public boolean isPlayer2JumpPressed() {
        return player2Jump;
    }
    
    public boolean isPlayer2AttackPressed() {
        return player2Attack;
    }
    
    // ===== MÉTODOS GETTER PARA CONTROLES ESPECIALES =====
    
    public boolean isActivatePlayer2Pressed() {
        return activatePlayer2;
    }
    
    // ===== MÉTODOS DE RESET =====
    // Estos métodos son cruciales para acciones que deben ejecutarse solo una vez
    // por pulsación (como saltar o atacar), evitando que se repitan continuamente
    
    public void resetPlayer1Jump() {
        player1Jump = false;        // Reset salto jugador 1 después de procesarlo
    }
    
    public void resetPlayer1Attack() {
        player1Attack = false;      // Reset ataque jugador 1 después de procesarlo
    }
    
    public void resetPlayer2Jump() {
        player2Jump = false;        // Reset salto jugador 2 después de procesarlo
    }
    
    public void resetPlayer2Attack() {
        player2Attack = false;      // Reset ataque jugador 2 después de procesarlo
    }
    
    public void resetActivatePlayer2() {
        activatePlayer2 = false;    // Reset activación jugador 2 después de procesarlo
    }
}