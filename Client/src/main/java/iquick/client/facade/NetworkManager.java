package iquick.client.facade;

import iquick.client.Client;
import iquick.client.Client;
import iquick.client.Client;
import iquick.client.factory.Fruit;
import iquick.client.factory.Enemy;
import iquick.client.factory.FruitType;
import iquick.client.factory.EnemyType;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetworkManager {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int MAX_ENEMIES = 10;
    private static final int MAX_FRUITS = 4;
    
    // Constantes para tipos de mensajes
    private static final int MESSAGE_TYPE_GAME_STATE = 1;
    private static final int MESSAGE_TYPE_CLIENT_MESSAGE = 2;
    private static final int MESSAGE_BUFFER_SIZE = 256;
    
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean connected = false;
    private Client client;
    private Thread receiveThread;
    
    public NetworkManager(Client client) {
        this.client = client;
    }
    
    public void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            connected = true;
            client.updateConnectionStatus("Conectado al servidor - Usa A/D para mover, W para saltar");
            System.out.println("Conectado al servidor en " + SERVER_HOST + ":" + SERVER_PORT);
            
            // Iniciar hilo de recepción
            receiveThread = new Thread(this::receiveGameData);
            receiveThread.start();
            
        } catch (IOException e) {
            client.updateConnectionStatus("Error conectando al servidor: " + e.getMessage() + " - Usa A/D para mover, W para saltar");
            System.err.println("Error conectando al servidor: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
    
    // Función para enviar mensajes de texto al servidor (mejorada para envío inmediato)
    public void sendMessage(String message) {
        if (!connected || outputStream == null) {
            System.err.println("No conectado al servidor");
            return;
        }
        
        try {
            // Usar synchronized para asegurar envío inmediato y evitar conflictos
            synchronized (outputStream) {
                ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + MESSAGE_BUFFER_SIZE);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                
                // Tipo de mensaje
                buffer.putInt(MESSAGE_TYPE_CLIENT_MESSAGE);
                
                // ID del cliente (0 por ahora, el servidor asignará el real)
                buffer.putInt(0);
                
                // Contenido del mensaje (limitado a MESSAGE_BUFFER_SIZE-1 para null terminator)
                byte[] messageBytes = message.getBytes("UTF-8");
                int maxLength = Math.min(messageBytes.length, MESSAGE_BUFFER_SIZE - 1);
                buffer.put(messageBytes, 0, maxLength);
                
                // Rellenar el resto con ceros
                for (int i = maxLength; i < MESSAGE_BUFFER_SIZE; i++) {
                    buffer.put((byte) 0);
                }
                
                outputStream.write(buffer.array());
                outputStream.flush();
                System.out.println("Mensaje enviado al servidor: " + message);
            }
            
        } catch (IOException e) {
            System.err.println("Error enviando mensaje: " + e.getMessage());
            connected = false;
        }
    }
    
    private void receiveGameData() {
        byte[] buffer = new byte[4096];
        
        while (connected) {
            try {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead > 0) {
                    parseGameData(buffer, bytesRead);
                }
            } catch (IOException e) {
                connected = false;
                client.updateConnectionStatus("Conexión perdida con el servidor - Usa A/D para mover, W para saltar");
                System.err.println("Error recibiendo datos: " + e.getMessage());
                break;
            }
        }
    }
    
    private void parseGameData(byte[] data, int length) {
    try {
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Calcular el tamaño mínimo esperado correctamente
        int matrixSize = Client.getMatrixHeight() * Client.getMatrixWidth() * 4;
        int enemyCountSize = 4;
        int enemiesSize = MAX_ENEMIES * 4 * 4; // MAX_ENEMIES * 4 campos * 4 bytes
        int fruitCountSize = 4;
        int fruitsSize = MAX_FRUITS * 4 * 4;   // MAX_FRUITS * 4 campos * 4 bytes
        int playerScoreSize = 4;
        
        int expectedMinSize = matrixSize + enemyCountSize + enemiesSize + 
                             fruitCountSize + fruitsSize + playerScoreSize;
        
        if (length < expectedMinSize) {
            System.err.println("Datos insuficientes recibidos. Esperado: " + 
                             expectedMinSize + ", Recibido: " + length);
            return;
        }
        
        // Leer matriz del juego
        int[][] gameMatrix = new int[Client.getMatrixHeight()][Client.getMatrixWidth()];
        for (int i = 0; i < Client.getMatrixHeight(); i++) {
            for (int j = 0; j < Client.getMatrixWidth(); j++) {
                if (buffer.remaining() >= 4) {
                    gameMatrix[i][j] = buffer.getInt();
                }
            }
        }
        
        // Leer enemigos
        int enemyCount = 0;
        if (buffer.remaining() >= 4) {
            enemyCount = buffer.getInt();
        }
        
        Enemy[] newEnemies = new Enemy[MAX_ENEMIES];
        boolean hasNewEnemies = false;
        
        for (int i = 0; i < MAX_ENEMIES && buffer.remaining() >= 16; i++) {
            int x = buffer.getInt();
            int y = buffer.getInt();
            int type = buffer.getInt();
            int active = buffer.getInt();
            
            if (active == 1) {
                newEnemies[i] = new Enemy(x, y, EnemyType.values()[Math.min(Math.max(type, 0), EnemyType.values().length - 1)]);
                newEnemies[i].setGridPosition(x, y);
                newEnemies[i].setActive(true);
                hasNewEnemies = true;
            } else {
                newEnemies[i] = new Enemy(0, 0, EnemyType.NONE);
                newEnemies[i].setActive(false);
            }
        }
        
        // Leer frutas
        int fruitCount = 0;
        if (buffer.remaining() >= 4) {
            fruitCount = buffer.getInt();
        }
        
        Fruit[] newFruits = new Fruit[MAX_FRUITS];
        boolean hasNewFruits = false;
        
        for (int i = 0; i < MAX_FRUITS && buffer.remaining() >= 16; i++) {
            int x = buffer.getInt();
            int y = buffer.getInt();
            int type = buffer.getInt();
            int active = buffer.getInt();
            
            if (active == 1) {
                newFruits[i] = new Fruit(x, y, FruitType.values()[Math.min(Math.max(type, 0), FruitType.values().length - 1)]);
                newFruits[i].setGridPosition(x, y);
                newFruits[i].setActive(true);
                hasNewFruits = true;
            } else {
                newFruits[i] = new Fruit(0, 0, FruitType.NONE);
                newFruits[i].setActive(false);
            }
        }
        
        // Leer puntuación del jugador (si hay datos suficientes)
        int playerScore = 0;
        if (buffer.remaining() >= 4) {
            playerScore = buffer.getInt();
            // Opcional: actualizar puntuación en el cliente
            // client.updatePlayerScore(playerScore);
        }
        
        // Actualizar cliente
        if (hasNewEnemies || hasNewFruits) {
            client.updateGameData(gameMatrix, newEnemies, newFruits);
        } else {
            client.updateGameData(gameMatrix, null, null);
        }
        
    } catch (Exception e) {
        System.err.println("Error parseando datos del juego: " + e.getMessage());
        e.printStackTrace(); // Para debug adicional
    }
}
    
    public boolean isConnected() {
        return connected;
    }
    
    public void sendData(byte[] data) {
        if (connected && socket != null) {
            try {
                socket.getOutputStream().write(data);
                socket.getOutputStream().flush();
            } catch (IOException e) {
                System.err.println("Error enviando datos: " + e.getMessage());
                connected = false;
                client.updateConnectionStatus("Error de comunicación con el servidor");
            }
        }
    }
}