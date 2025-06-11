package iquick.client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NetworkManager {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int MAX_ENEMIES = 10;
    
    private Socket socket;
    private DataInputStream inputStream;
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
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
    
    private void receiveGameData() {
        byte[] buffer = new byte[2048];
        
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
            
            if (length < (Client.getMatrixHeight() * Client.getMatrixWidth() + 1) * 4) {
                System.err.println("Datos insuficientes recibidos");
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
            
            Enemy[] enemies = new Enemy[MAX_ENEMIES];
            for (int i = 0; i < MAX_ENEMIES; i++) {
                enemies[i] = new Enemy(0, 0, EnemyType.NONE);
            }
            
            for (int i = 0; i < MAX_ENEMIES && buffer.remaining() >= 16; i++) {
                int x = buffer.getInt();
                int y = buffer.getInt();
                int type = buffer.getInt();
                int active = buffer.getInt();
                
                if (active == 1 && !enemies[i].isActive()) {
                    enemies[i].spawn(x * Client.getCellSize(), y * Client.getCellSize());
                }
                
                enemies[i].setGridPosition(x, y);
                enemies[i].setType(EnemyType.values()[Math.min(Math.max(type, 0), EnemyType.values().length - 1)]);
                enemies[i].setActive(active == 1);
            }
            
            // Actualizar datos del cliente
            client.updateGameData(gameMatrix, enemies);
            
        } catch (Exception e) {
            System.err.println("Error parseando datos del juego: " + e.getMessage());
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
