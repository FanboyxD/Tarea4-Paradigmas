package iquick.gameclient;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class ServerConnection {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;
    private GameClient gameClient;
    
    public ServerConnection(String host, int port, GameClient gameClient) throws IOException {
        this.gameClient = gameClient;
        this.gson = new Gson();
        
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        Thread thread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    processServerMessage(line);
                }
            } catch (IOException e) {
                System.err.println("Error receiving from server: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    private void processServerMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            switch (type) {
                case "MAP": 
                    processMap(json); 
                    break;
                case "PLAYER_UPDATE": 
                    processPlayerUpdate(json); 
                    break;
                case "TILE_DESTROYED": 
                    processTileDestroyed(json); 
                    break;
                case "TILE_REGENERATED": 
                    processTileRegenerated(json); 
                    break;
                case "ATTACK_RESULT": 
                    processAttackResult(json); 
                    break;
                case "GAME_OVER": 
                    processGameOver(json); 
                    break;
                case "PLAYER2_ACTIVATED":
                    processPlayer2Activated(json);
                    break;
                case "BONUS_PHASE":
                    processBonusPhase(json);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processMap(JsonObject json) {
        int mapWidth = json.get("width").getAsInt();
        int mapHeight = json.get("height").getAsInt();
        JsonArray mapArray = json.getAsJsonArray("map");

        int[][] gameMap = new int[mapHeight][mapWidth];
        for (int i = 0; i < mapHeight; i++) {
            JsonArray row = mapArray.get(i).getAsJsonArray();
            for (int j = 0; j < mapWidth; j++) {
                gameMap[i][j] = row.get(j).getAsInt();
            }
        }

        SwingUtilities.invokeLater(() -> 
            gameClient.updateMap(gameMap, mapWidth, mapHeight));
    }
    
    private void processPlayerUpdate(JsonObject json) {
        // Actualizar Player 1
        if (json.has("player1")) {
            JsonObject player1Data = json.getAsJsonObject("player1");
            Player player1 = gameClient.getPlayer1();
            updatePlayerData(player1, player1Data);
        }

        // Actualizar Player 2 si está activo
        boolean isPlayer2Active = json.has("isPlayer2Active") && json.get("isPlayer2Active").getAsBoolean();
        gameClient.setPlayer2Active(isPlayer2Active);

        if (isPlayer2Active && json.has("player2") && !json.get("player2").isJsonNull()) {
            JsonObject player2Data = json.getAsJsonObject("player2");
            Player player2 = gameClient.getPlayer2();
            updatePlayerData(player2, player2Data);
        }

        // Actualizar información de cuál jugador está más arriba
        final Integer playerAbove = json.has("playerAbove") && !json.get("playerAbove").isJsonNull() 
            ? json.get("playerAbove").getAsInt() 
            : null;
        gameClient.setPlayerAbove(playerAbove);

        // Actualizar información de fase bonus
        if (json.has("isBonusPhase")) {
            boolean isBonusPhase = json.get("isBonusPhase").getAsBoolean();
            gameClient.setBonusPhase(isBonusPhase);

            if (isBonusPhase) {
                // Actualizar ID del jugador bonus
                if (json.has("bonusPlayerId") && !json.get("bonusPlayerId").isJsonNull()) {
                    int bonusPlayerId = json.get("bonusPlayerId").getAsInt();
                    gameClient.setBonusPlayerId(bonusPlayerId);
                }

                // Actualizar tiempo restante
                if (json.has("bonusTimeRemaining")) {
                    int bonusTimeRemaining = json.get("bonusTimeRemaining").getAsInt();
                    gameClient.setBonusTimeRemaining(bonusTimeRemaining);
                }
            } else {
                // Reset valores cuando no estamos en fase bonus
                gameClient.setBonusPlayerId(-1);
                gameClient.setBonusTimeRemaining(0);
            }
        }

        // Actualizar enemigos
        if (json.has("enemies")) {
            updateEnemies(json.getAsJsonArray("enemies"));
        }
        if (json.has("fruits")) {
            updateFruits(json.getAsJsonArray("fruits"));
        }
        

        SwingUtilities.invokeLater(() -> {
            // Pasar la información de playerAbove al GamePanel
            gameClient.getGamePanel().setPlayerAbove(playerAbove);
            gameClient.repaintGame();
        });
    }
    private void processBonusPhase(JsonObject json) {
        String action = json.get("action").getAsString();

        switch (action) {
            case "START":
                System.out.println("¡Fase bonus iniciada!");
                break;
            case "END":
                System.out.println("¡Fase bonus terminada!");
                break;
        }

        SwingUtilities.invokeLater(() -> gameClient.repaintGame());
    }
    
    private void updatePlayerData(Player player, JsonObject data) {
        player.x = data.get("x").getAsFloat();
        player.y = data.get("y").getAsFloat();
        player.isOnGround = data.get("isOnGround").getAsBoolean();
        player.isJumping = data.get("isJumping").getAsBoolean();
        player.isAttacking = data.get("isAttacking").getAsBoolean();

        if (data.has("lives")) {
            player.lives = data.get("lives").getAsInt();
        }
        if (data.has("score")) {
            player.score = data.get("score").getAsInt(); // Actualizar score
        }
        if (data.has("isInvulnerable")) {
            player.isInvulnerable = data.get("isInvulnerable").getAsBoolean();
        }
        if (data.has("isAlive")) {
            player.isAlive = data.get("isAlive").getAsBoolean();
        }
        if (data.has("isDamaged")) {
            boolean wasDamaged = player.isDamaged;
            player.isDamaged = data.get("isDamaged").getAsBoolean();
            if (player.isDamaged && !wasDamaged) {
                player.damageTime = System.currentTimeMillis();
            }
        }
    }
    
    private void updateEnemies(JsonArray array) {
        List<Enemy> enemies = gameClient.getEnemies();
        enemies.clear();

        for (JsonElement e : array) {
            JsonObject obj = e.getAsJsonObject();
            Enemy enemy = new Enemy();
            enemy.id = obj.get("id").getAsString();
            enemy.x = obj.get("x").getAsFloat();
            enemy.y = obj.get("y").getAsFloat();
            enemy.isActive = obj.get("isActive").getAsBoolean();

            // NUEVO - Obtener el tipo de enemigo del JSON
            if (obj.has("enemyType")) {
                enemy.enemyType = obj.get("enemyType").getAsString();
            }

            enemies.add(enemy);
        }
    }
    
    private void updateFruits(JsonArray array) {
        List<Fruit> fruits = gameClient.getFruits();
        fruits.clear();

        for (JsonElement e : array) {
            JsonObject obj = e.getAsJsonObject();
            Fruit fruit = new Fruit();
            fruit.id = obj.get("id").getAsString();
            fruit.x = obj.get("x").getAsFloat();
            fruit.y = obj.get("y").getAsFloat();
            fruit.isActive = obj.get("isActive").getAsBoolean();

            if (obj.has("fruitType")) {
                fruit.fruitType = obj.get("fruitType").getAsString();
            }
            if (obj.has("points")) {
                fruit.points = obj.get("points").getAsInt();
            }

            fruits.add(fruit);
        }
    }

    
    private void processTileDestroyed(JsonObject json) {
        int x = json.get("x").getAsInt();
        int y = json.get("y").getAsInt();
        
        gameClient.addDestroyedTile(x, y);
        gameClient.updateMapTile(x, y, 0);
        
        SwingUtilities.invokeLater(() -> gameClient.repaintGame());
    }
    
    private void processTileRegenerated(JsonObject json) {
        int x = json.get("x").getAsInt();
        int y = json.get("y").getAsInt();
        
        gameClient.removeDestroyedTile(x, y);
        gameClient.updateMapTile(x, y, 1);
        
        SwingUtilities.invokeLater(() -> gameClient.repaintGame());
    }
    
    private void processAttackResult(JsonObject json) {
        boolean hit = json.get("hit").getAsBoolean();
        if (hit) {
            System.out.println("¡Ataque exitoso!");
        }
    }
    
    private void processGameOver(JsonObject json) {
        String message = json.get("message").getAsString();
        System.out.println("Game Over: " + message);
        
        SwingUtilities.invokeLater(() -> gameClient.showGameOver());
    }
    
    private void processPlayer2Activated(JsonObject json) {
        boolean success = json.has("success") && json.get("success").getAsBoolean();
        if (success) {
            System.out.println("Player 2 activado exitosamente!");
            SwingUtilities.invokeLater(() -> {
                gameClient.setPlayer2Active(true);
                gameClient.repaintGame();
            });
        } else {
            String reason = json.has("reason") ? json.get("reason").getAsString() : "Razón desconocida";
            System.out.println("No se pudo activar Player 2: " + reason);
        }
    }
    
    public void send(String msg) {
        if (socket != null && !socket.isClosed()) {
            out.println(msg);
            out.flush();
        }
    }
    
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}