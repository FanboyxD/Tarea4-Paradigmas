package iquick.gameclient;

public class Player {
    public float x;
    public float y;
    public boolean isOnGround;
    public boolean isJumping;
    public boolean isAttacking;
    public int lives;
    public int maxLives;
    public boolean isDamaged;
    public long damageTime;

    public Player() {
        this.x = 2.0f;
        this.y = 29.0f;
        this.isOnGround = true;
        this.isJumping = false;
        this.isAttacking = false;
        this.lives = 3;
        this.maxLives = 3;
        this.isDamaged = false;
        this.damageTime = 0;
    }
}
