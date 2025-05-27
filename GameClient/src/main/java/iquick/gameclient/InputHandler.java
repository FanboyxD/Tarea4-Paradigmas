package iquick.gameclient;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean attackPressed = false;

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    public boolean isAttackPressed() {
        return attackPressed;
    }

    public void resetJump() {
        this.jumpPressed = false;
    }

    public void resetAttack() {
        this.attackPressed = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                jumpPressed = true;
                break;
            case KeyEvent.VK_X:
                attackPressed = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                rightPressed = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No utilizado
    }
}
