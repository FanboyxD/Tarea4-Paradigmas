package iquick.gameclient;

import com.google.gson.JsonArray;

public class GameMap {
    private int[][] tiles;
    private int width;
    private int height;

    public void loadFromJson(JsonObjectWrapper json) {
        this.width = json.getInt("width");
        this.height = json.getInt("height");

        JsonArray mapArray = json.getArray("map");
        tiles = new int[height][width];

        for (int i = 0; i < height; i++) {
            JsonArray row = mapArray.get(i).getAsJsonArray();
            for (int j = 0; j < width; j++) {
                tiles[i][j] = row.get(j).getAsInt();
            }
        }
    }

    public int[][] getTiles() {
        return tiles;
    }

    public int getTile(int x, int y) {
        if (isValid(x, y)) {
            return tiles[y][x];
        }
        return -1;
    }

    public void setTile(int x, int y, int value) {
        if (isValid(x, y)) {
            tiles[y][x] = value;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
