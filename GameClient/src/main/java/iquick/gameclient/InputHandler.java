package iquick.gameclient;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
    
    // Player 1 controls (WASD + X)
    private boolean player1Left = false;
    private boolean player1Right = false;
    private boolean player1Jump = false;
    private boolean player1Attack = false;
    
    // Player 2 controls (Arrow keys + Space)
    private boolean player2Left = false;
    private boolean player2Right = false;
    private boolean player2Jump = false;
    private boolean player2Attack = false;
    
    // Special controls
    private boolean activatePlayer2 = false;
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Player 1 controls (WASD + X)
        switch (keyCode) {
            case KeyEvent.VK_W:
                player1Jump = true;
                break;
            case KeyEvent.VK_A:
                player1Left = true;
                break;
            case KeyEvent.VK_D:
                player1Right = true;
                break;
            case KeyEvent.VK_X:
                player1Attack = true;
                break;
        }
        
        // Player 2 controls (Arrow keys + Space)
        switch (keyCode) {
            case KeyEvent.VK_UP:
                player2Jump = true;
                break;
            case KeyEvent.VK_LEFT:
                player2Left = true;
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = true;
                break;
            case KeyEvent.VK_P:
                player2Attack = true;
                break;
        }
        
        // Special controls
        switch (keyCode) {
            case KeyEvent.VK_I:
                activatePlayer2 = true;
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Player 1 controls (WASD + X)
        switch (keyCode) {
            case KeyEvent.VK_A:
                player1Left = false;
                break;
            case KeyEvent.VK_D:
                player1Right = false;
                break;
        }
        
        // Player 2 controls (Arrow keys + Space)
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                player2Left = false;
                break;
            case KeyEvent.VK_RIGHT:
                player2Right = false;
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // No se usa en este caso
    }
    
    // Player 1 getters
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
    
    // Player 2 getters
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
    
    // Special controls getters
    public boolean isActivatePlayer2Pressed() {
        return activatePlayer2;
    }
    
    // Reset methods para acciones que se ejecutan una sola vez
    public void resetPlayer1Jump() {
        player1Jump = false;
    }
    
    public void resetPlayer1Attack() {
        player1Attack = false;
    }
    
    public void resetPlayer2Jump() {
        player2Jump = false;
    }
    
    public void resetPlayer2Attack() {
        player2Attack = false;
    }
    
    public void resetActivatePlayer2() {
        activatePlayer2 = false;
    }
}