package iquick.gameclient;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Clase que maneja la conexión cliente-servidor para un juego multijugador.
 * Se encarga de enviar/recibir mensajes JSON y actualizar el estado del juego.
 */
public class ServerConnection {
    // Componentes de conexión de red
    private Socket socket;           // Socket TCP para la conexión
    private PrintWriter out;         // Stream de salida para enviar mensajes
    private BufferedReader in;       // Stream de entrada para recibir mensajes
    private Gson gson;              // Parser JSON de Google
    private GameClient gameClient;   // Referencia al cliente del juego
    
    /**
     * Constructor que establece la conexión con el servidor
     * @param host Dirección IP o hostname del servidor
     * @param port Puerto del servidor
     * @param gameClient Instancia del cliente del juego
     */
    public ServerConnection(String host, int port, GameClient gameClient) throws IOException {
        this.gameClient = gameClient;
        this.gson = new Gson();
        
        // Establecer conexión TCP
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Crear hilo separado para escuchar mensajes del servidor
        Thread thread = new Thread(() -> {
            try {
                String line;
                // Bucle infinito para recibir mensajes
                while ((line = in.readLine()) != null) {
                    processServerMessage(line);
                }
            } catch (IOException e) {
                System.err.println("Error receiving from server: " + e.getMessage());
            }
        });
        thread.setDaemon(true); // Hilo daemon (termina cuando termina la aplicación)
        thread.start();
    }
    
    /**
     * Procesa los mensajes JSON recibidos del servidor
     * @param message Mensaje JSON como string
     */
    private void processServerMessage(String message) {
        try {
            // Parsear JSON
            JsonElement jsonElement = gson.fromJson(message, JsonElement.class);
            if (!jsonElement.isJsonObject()) {
                System.out.println("⚠️ Mensaje inesperado del servidor (no es JSON válido): " + jsonElement.toString());
                return;
            }

            JsonObject json = jsonElement.getAsJsonObject();
            String type = json.get("type").getAsString();

            // Switch para manejar diferentes tipos de mensajes
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
    
    /**
     * Procesa la información del mapa del juego
     * @param json Objeto JSON con datos del mapa
     */
    private void processMap(JsonObject json) {
        int mapWidth = json.get("width").getAsInt();
        int mapHeight = json.get("height").getAsInt();
        JsonArray mapArray = json.getAsJsonArray("map");

        // Crear matriz 2D del mapa
        int[][] gameMap = new int[mapHeight][mapWidth];
        for (int i = 0; i < mapHeight; i++) {
            JsonArray row = mapArray.get(i).getAsJsonArray();
            for (int j = 0; j < mapWidth; j++) {
                gameMap[i][j] = row.get(j).getAsInt();
            }
        }

        // Actualizar en el hilo de la UI (thread-safe)
        SwingUtilities.invokeLater(() -> 
            gameClient.updateMap(gameMap, mapWidth, mapHeight));
    }
    
    /**
     * Procesa actualizaciones de estado de los jugadores
     * @param json Objeto JSON con datos de los jugadores
     */
    private void processPlayerUpdate(JsonObject json) {
        // Actualizar Player 1
        if (json.has("player1")) {
            JsonObject player1Data = json.getAsJsonObject("player1");
            Player player1 = gameClient.getPlayer1();
            updatePlayerData(player1, player1Data);
        }

        // Verificar si Player 2 está activo
        boolean isPlayer2Active = json.has("isPlayer2Active") && json.get("isPlayer2Active").getAsBoolean();
        gameClient.setPlayer2Active(isPlayer2Active);

        // Actualizar Player 2 si está activo
        if (isPlayer2Active && json.has("player2") && !json.get("player2").isJsonNull()) {
            JsonObject player2Data = json.getAsJsonObject("player2");
            Player player2 = gameClient.getPlayer2();
            updatePlayerData(player2, player2Data);
        }

        // Determinar qué jugador está más arriba (para renderizado en capas)
        final Integer playerAbove = json.has("playerAbove") && !json.get("playerAbove").isJsonNull() 
            ? json.get("playerAbove").getAsInt() 
            : null;
        gameClient.setPlayerAbove(playerAbove);

        // Manejar fase bonus del juego
        if (json.has("isBonusPhase")) {
            boolean isBonusPhase = json.get("isBonusPhase").getAsBoolean();
            gameClient.setBonusPhase(isBonusPhase);

            if (isBonusPhase) {
                // Actualizar ID del jugador en fase bonus
                if (json.has("bonusPlayerId") && !json.get("bonusPlayerId").isJsonNull()) {
                    int bonusPlayerId = json.get("bonusPlayerId").getAsInt();
                    gameClient.setBonusPlayerId(bonusPlayerId);
                }

                // Actualizar tiempo restante de la fase bonus
                if (json.has("bonusTimeRemaining")) {
                    int bonusTimeRemaining = json.get("bonusTimeRemaining").getAsInt();
                    gameClient.setBonusTimeRemaining(bonusTimeRemaining);
                }
            } else {
                // Resetear valores cuando no hay fase bonus
                gameClient.setBonusPlayerId(-1);
                gameClient.setBonusTimeRemaining(0);
            }
        }

        // Actualizar listas de enemigos y frutas
        if (json.has("enemies")) {
            updateEnemies(json.getAsJsonArray("enemies"));
        }
        if (json.has("fruits")) {
            updateFruits(json.getAsJsonArray("fruits"));
        }
        
        // Actualizar UI en el hilo apropiado
        SwingUtilities.invokeLater(() -> {
            gameClient.getGamePanel().setPlayerAbove(playerAbove);
            gameClient.repaintGame();
        });
    }
    
    /**
     * Procesa eventos de inicio/fin de fase bonus
     * @param json Objeto JSON con información de la fase bonus
     */
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
    
    /**
     * Actualiza los datos de un jugador específico
     * @param player Objeto Player a actualizar
     * @param data JSON con los nuevos datos del jugador
     */
    private void updatePlayerData(Player player, JsonObject data) {
        // Datos básicos de posición y estado
        player.x = data.get("x").getAsFloat();
        player.y = data.get("y").getAsFloat();
        player.isOnGround = data.get("isOnGround").getAsBoolean();
        player.isJumping = data.get("isJumping").getAsBoolean();
        player.isAttacking = data.get("isAttacking").getAsBoolean();

        // Datos opcionales (verificar existencia antes de leer)
        if (data.has("lives")) {
            player.lives = data.get("lives").getAsInt();
        }
        if (data.has("score")) {
            player.score = data.get("score").getAsInt();
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
            // Registrar tiempo del daño para efectos visuales
            if (player.isDamaged && !wasDamaged) {
                player.damageTime = System.currentTimeMillis();
            }
        }
    }
    
    /**
     * Actualiza la lista de enemigos desde el servidor
     * @param array Array JSON con datos de enemigos
     */
    private void updateEnemies(JsonArray array) {
        List<Enemy> enemies = gameClient.getEnemies();
        enemies.clear(); // Limpiar lista existente

        // Reconstruir lista con datos del servidor
        for (JsonElement e : array) {
            JsonObject obj = e.getAsJsonObject();
            Enemy enemy = new Enemy();
            enemy.id = obj.get("id").getAsString();
            enemy.x = obj.get("x").getAsFloat();
            enemy.y = obj.get("y").getAsFloat();
            enemy.isActive = obj.get("isActive").getAsBoolean();

            // Obtener tipo de enemigo (para diferentes sprites/comportamientos)
            if (obj.has("enemyType")) {
                enemy.enemyType = obj.get("enemyType").getAsString();
            }
            System.out.println("ENEMY TYPE: " + enemy.enemyType);
            enemies.add(enemy);
        }
    }
    
    /**
     * Actualiza la lista de frutas (power-ups/puntos) desde el servidor
     * @param array Array JSON con datos de frutas
     */
    private void updateFruits(JsonArray array) {
        List<Fruit> fruits = gameClient.getFruits();
        fruits.clear(); // Limpiar lista existente

        // Reconstruir lista con datos del servidor
        for (JsonElement e : array) {
            JsonObject obj = e.getAsJsonObject();
            Fruit fruit = new Fruit();
            fruit.id = obj.get("id").getAsString();
            fruit.x = obj.get("x").getAsFloat();
            fruit.y = obj.get("y").getAsFloat();
            fruit.isActive = obj.get("isActive").getAsBoolean();

            // Datos específicos de frutas
            if (obj.has("fruitType")) {
                fruit.fruitType = obj.get("fruitType").getAsString();
            }
            if (obj.has("points")) {
                fruit.points = obj.get("points").getAsInt();
            }

            fruits.add(fruit);
        }
    }

    /**
     * Procesa la destrucción de un tile del mapa
     * @param json JSON con coordenadas del tile destruido
     */
    private void processTileDestroyed(JsonObject json) {
        int x = json.get("x").getAsInt();
        int y = json.get("y").getAsInt();
        
        // Registrar tile destruido y actualizar mapa
        gameClient.addDestroyedTile(x, y);
        gameClient.updateMapTile(x, y, 0); // 0 = espacio vacío
        
        SwingUtilities.invokeLater(() -> gameClient.repaintGame());
    }
    
    /**
     * Procesa la regeneración de un tile del mapa
     * @param json JSON con coordenadas del tile regenerado
     */
    private void processTileRegenerated(JsonObject json) {
        int x = json.get("x").getAsInt();
        int y = json.get("y").getAsInt();
        
        // Quitar de lista de destruidos y restaurar en mapa
        gameClient.removeDestroyedTile(x, y);
        gameClient.updateMapTile(x, y, 1); // 1 = tile sólido
        
        SwingUtilities.invokeLater(() -> gameClient.repaintGame());
    }
    
    /**
     * Procesa el resultado de un ataque
     * @param json JSON con información del resultado
     */
    private void processAttackResult(JsonObject json) {
        boolean hit = json.get("hit").getAsBoolean();
        if (hit) {
            System.out.println("¡Ataque exitoso!");
        }
    }
    
    /**
     * Procesa el fin del juego
     * @param json JSON con mensaje de game over
     */
    private void processGameOver(JsonObject json) {
        String message = json.get("message").getAsString();
        System.out.println("Game Over: " + message);
        
        SwingUtilities.invokeLater(() -> gameClient.showGameOver());
    }
    
    /**
     * Procesa la activación del segundo jugador
     * @param json JSON con resultado de la activación
     */
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
    
    /**
     * Envía un mensaje al servidor
     * @param msg Mensaje a enviar (normalmente JSON)
     */
    public void send(String msg) {
        if (socket != null && !socket.isClosed()) {
            out.println(msg);
            out.flush(); // Asegurar que se envíe inmediatamente
        }
    }
    
    /**
     * Cierra la conexión con el servidor
     */
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    /**
     * Verifica si la conexión está activa
     * @return true si está conectado, false si no
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}