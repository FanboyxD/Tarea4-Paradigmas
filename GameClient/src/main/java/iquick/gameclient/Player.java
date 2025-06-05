package iquick.gameclient;

public class Player {
    public float x;
    public float y;
    public boolean isOnGround;
    public boolean isJumping;
    public boolean isAttacking;
    public int lives;
    public boolean isDamaged;
    public long damageTime;
    public boolean isInvulnerable;
    public boolean isAlive;
    public int score;
    
    public Player() {
        this.x = 0;
        this.y = 0;
        this.isOnGround = false;
        this.isJumping = false;
        this.isAttacking = false;
        this.lives = 3;
        this.isDamaged = false;
        this.damageTime = 0;
        this.isInvulnerable = false;
        this.isAlive = true;
        this.score = 0;
    }
}