package iquick.gameclient;

public class DestroyedTile {
    public int x;
    public int y;
    public long destructionTime;
    public boolean isRegenerating;
    public static final long REGENERATION_TIME = 3000;

    public DestroyedTile(int x, int y) {
        this.x = x;
        this.y = y;
        this.destructionTime = System.currentTimeMillis();
        this.isRegenerating = false;
    }

    public boolean shouldRegenerate() {
        return System.currentTimeMillis() - destructionTime >= REGENERATION_TIME;
    }

    public float getRegenerationProgress() {
        long elapsed = System.currentTimeMillis() - destructionTime;
        if (elapsed >= REGENERATION_TIME) return 1.0f;
        return Math.max(0.0f, (elapsed - (REGENERATION_TIME - 1000)) / 1000.0f);
    }
}
