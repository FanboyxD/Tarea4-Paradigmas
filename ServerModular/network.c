#include "network.h"
#include "ui.h"
#include <ws2tcpip.h>
#include <stdio.h>
#include <string.h>

SOCKET serverSocket;
ClientData clients[MAX_CLIENTS];
int clientCount = 0;
int nextClientId = 1;

void InitializeNetwork() {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        clients[i].socket = INVALID_SOCKET;
        clients[i].isInitialized = 0;
        clients[i].clientId = 0;
        clients[i].canSpawnFruits = 0;
        clients[i].canSpawnEnemies = 1;
    }
    
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        LogMessage("Error inicializando Winsock");
    }
}

void CleanupNetwork() {
    closesocket(serverSocket);
    WSACleanup();
}

int StartServer() {
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == INVALID_SOCKET) {
        LogMessage("Error creando socket del servidor");
        return 0;
    }

    struct sockaddr_in serverAddr;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    if (bind(serverSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        LogMessage("Error en bind");
        closesocket(serverSocket);
        return 0;
    }

    if (listen(serverSocket, MAX_CLIENTS) == SOCKET_ERROR) {
        LogMessage("Error en listen");
        closesocket(serverSocket);
        return 0;
    }

    LogMessage("Servidor iniciado en puerto 8080");
    return 1;
}

// En network.c - Modificar SendGameStateToClient
void SendGameStateToClient(int clientIndex) {
    if (clientIndex < 0 || clientIndex >= MAX_CLIENTS || !clients[clientIndex].isInitialized) {
        return;
    }
    
    char buffer[BUFFER_SIZE];
    int offset = 0;
    
    GameState* clientGameState = &clients[clientIndex].gameState;
    
    memcpy(buffer + offset, &clientGameState->matrix, sizeof(clientGameState->matrix));
    offset += sizeof(clientGameState->matrix);
    
    // Enviar solo enemigos activos y luego limpiarlos inmediatamente
    memcpy(buffer + offset, &clientGameState->enemyCount, sizeof(int));
    offset += sizeof(int);
    memcpy(buffer + offset, clientGameState->enemies, sizeof(clientGameState->enemies));
    offset += sizeof(clientGameState->enemies);
    
    // Enviar solo frutas activas y luego limpiarlas inmediatamente
    memcpy(buffer + offset, &clientGameState->fruitCount, sizeof(int));
    offset += sizeof(int);
    memcpy(buffer + offset, clientGameState->fruits, sizeof(clientGameState->fruits));
    offset += sizeof(clientGameState->fruits);
    
    memcpy(buffer + offset, &clientGameState->playerScore, sizeof(int));
    offset += sizeof(int);
    
    if (clients[clientIndex].socket != INVALID_SOCKET) {
        send(clients[clientIndex].socket, buffer, offset, 0);
        
        // LIMPIAR la lista de enemigos en el servidor después de enviar
        // Solo mantenemos la información en el cliente
        for (int i = 0; i < MAX_ENEMIES; i++) {
            clientGameState->enemies[i][3] = 0; // Desactivar
        }
        clientGameState->enemyCount = 0;
        
        // LIMPIAR la lista de frutas en el servidor después de enviar
        for (int i = 0; i < MAX_FRUITS; i++) {
            clientGameState->fruits[i][3] = 0; // Desactivar
        }
        clientGameState->fruitCount = 0;
    }
}


// Función para procesar mensajes recibidos del cliente
void ProcessClientMessage(int clientIndex, const char* buffer, int bufferSize) {
    // Crear buffer para el mensaje como string
    char tempBuffer[512];
    int copySize = bufferSize < 511 ? bufferSize : 511;
    memcpy(tempBuffer, buffer, copySize);
    tempBuffer[copySize] = '\0';
    
    // VERIFICAR si es mensaje "BONUSOFF" - FIN de fase bonus
    if (strstr(tempBuffer, "BONUSOFF") != NULL) {
        clients[clientIndex].canSpawnFruits = 0; // Deshabilitar creación de frutas
        clients[clientIndex].canSpawnEnemies = 1; // NUEVO: Habilitar creación de enemigos
        char logMsg[256];
        sprintf(logMsg, "Cliente %d - Fin fase bonus: frutas OFF, enemigos ON", 
                clients[clientIndex].clientId);
        LogMessage(logMsg);
        printf("SERVIDOR: %s\n", logMsg);
        
        // Debug: Confirmar que los flags cambiaron
        printf("DEBUG: Cliente %d - canSpawnFruits = %d, canSpawnEnemies = %d\n", 
               clients[clientIndex].clientId, clients[clientIndex].canSpawnFruits, clients[clientIndex].canSpawnEnemies);
        return;
    }
    
    // VERIFICAR si es mensaje "BONUS" - INICIO de fase bonus
    if (strstr(tempBuffer, "BONUS") != NULL) {
        clients[clientIndex].canSpawnFruits = 1; // Habilitar creación de frutas
        clients[clientIndex].canSpawnEnemies = 0; // NUEVO: Deshabilitar creación de enemigos
        char logMsg[256];
        sprintf(logMsg, "Cliente %d - Inicio fase bonus: frutas ON, enemigos OFF", 
                clients[clientIndex].clientId);
        LogMessage(logMsg);
        printf("SERVIDOR: %s\n", logMsg);
        
        // Debug: Confirmar que los flags cambiaron
        printf("DEBUG: Cliente %d - canSpawnFruits = %d, canSpawnEnemies = %d\n", 
               clients[clientIndex].clientId, clients[clientIndex].canSpawnFruits, clients[clientIndex].canSpawnEnemies);
        return;
    }
    
    // Procesar mensaje estructurado original
    ClientMessage* message = (ClientMessage*)buffer;
    
    if (message->messageType == MESSAGE_TYPE_CLIENT_MESSAGE) {
        // VERIFICAR también en mensaje estructurado si contiene "BONUSOFF"
        if (strstr(message->messageContent, "BONUSOFF") != NULL) {
            clients[clientIndex].canSpawnFruits = 0;
            clients[clientIndex].canSpawnEnemies = 1; // NUEVO: Habilitar enemigos
            char logMsg[256];
            sprintf(logMsg, "Cliente %d - Fin fase bonus (msg estructurado): frutas OFF, enemigos ON", 
                    clients[clientIndex].clientId);
            LogMessage(logMsg);
            printf("SERVIDOR: %s\n", logMsg);
            printf("DEBUG: Cliente %d - canSpawnFruits = %d, canSpawnEnemies = %d\n", 
                   clients[clientIndex].clientId, clients[clientIndex].canSpawnFruits, clients[clientIndex].canSpawnEnemies);
            return;
        }
        
        // VERIFICAR también en mensaje estructurado si contiene "BONUS"
        if (strstr(message->messageContent, "BONUS") != NULL) {
            clients[clientIndex].canSpawnFruits = 1;
            clients[clientIndex].canSpawnEnemies = 0; // NUEVO: Deshabilitar enemigos
            char logMsg[256];
            sprintf(logMsg, "Cliente %d - Inicio fase bonus (msg estructurado): frutas ON, enemigos OFF", 
                    clients[clientIndex].clientId);
            LogMessage(logMsg);
            printf("SERVIDOR: %s\n", logMsg);
            printf("DEBUG: Cliente %d - canSpawnFruits = %d, canSpawnEnemies = %d\n", 
                   clients[clientIndex].clientId, clients[clientIndex].canSpawnFruits, clients[clientIndex].canSpawnEnemies);
            return;
        }
        
        char logMsg[512];
        sprintf(logMsg, "Mensaje del Cliente %d: %s", 
                clients[clientIndex].clientId, 
                message->messageContent);
        LogMessage(logMsg);
        printf("SERVIDOR RECIBIO: Cliente %d dice: '%s'\n", 
               clients[clientIndex].clientId, 
               message->messageContent);
    }
}

void SpawnEnemyForAllClients(EnemyType type, int iceSpawnRow, int iceSpawnCol) {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
            // MODIFICADO: Pasar el flag canSpawnEnemies a la función
            SpawnEnemyForClient(&clients[i].gameState, type, iceSpawnRow, iceSpawnCol, clients[i].canSpawnEnemies);
            UpdateGameForClient(&clients[i].gameState);
            SendGameStateToClient(i);
        }
    }
}

void SpawnFruitForAllClients(FruitType type) {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
            // MODIFICADO: Pasar el flag canSpawnFruits a la función
            SpawnFruitForClient(&clients[i].gameState, type, clients[i].canSpawnFruits);
            UpdateGameForClient(&clients[i].gameState);
            SendGameStateToClient(i);
        }
    }
}

void ClearAllEnemies() {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
            for (int j = 0; j < MAX_ENEMIES; j++) {
                clients[i].gameState.enemies[j][3] = 0;
            }
            clients[i].gameState.enemyCount = 0;
            UpdateGameForClient(&clients[i].gameState);
            SendGameStateToClient(i);
        }
    }
}

void ClearAllFruits() {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
            for (int j = 0; j < MAX_FRUITS; j++) {
                clients[i].gameState.fruits[j][3] = 0;
            }
            clients[i].gameState.fruitCount = 0;
            UpdateGameForClient(&clients[i].gameState);
            SendGameStateToClient(i);
        }
    }
}

void InitializeNewClientConnection(int clientIndex) {
    InitializeNewClient(&clients[clientIndex].gameState);
    
    clients[clientIndex].isInitialized = 1;
    clients[clientIndex].clientId = nextClientId++;
    clients[clientIndex].canSpawnFruits = 0;
    clients[clientIndex].canSpawnEnemies = 1;
    
    UpdateGameForClient(&clients[clientIndex].gameState);
}

// Función modificada para manejar la recepción de datos del cliente
DWORD WINAPI HandleClientData(LPVOID lpParam) {
    int clientIndex = *(int*)lpParam;
    char receiveBuffer[BUFFER_SIZE];
    int bytesReceived;
    
    while (clients[clientIndex].socket != INVALID_SOCKET && 
           clients[clientIndex].isInitialized) {
        
        bytesReceived = recv(clients[clientIndex].socket, receiveBuffer, sizeof(receiveBuffer), 0);
        
        if (bytesReceived > 0) {
            // Procesar el mensaje recibido
            ProcessClientMessage(clientIndex, receiveBuffer, bytesReceived);
        } else if (bytesReceived == 0) {
            // Cliente desconectado
            printf("Cliente %d desconectado\n", clients[clientIndex].clientId);
            closesocket(clients[clientIndex].socket);
            clients[clientIndex].socket = INVALID_SOCKET;
            clients[clientIndex].isInitialized = 0;
            clientCount--;
            break;
        } else {
            // Error en recv
            int error = WSAGetLastError();
            if (error != WSAEWOULDBLOCK) {
                printf("Error recibiendo datos del cliente %d: %d\n", 
                       clients[clientIndex].clientId, error);
                closesocket(clients[clientIndex].socket);
                clients[clientIndex].socket = INVALID_SOCKET;
                clients[clientIndex].isInitialized = 0;
                clientCount--;
                break;
            }
        }
        
        Sleep(10); // Pequeña pausa para no saturar la CPU
    }
    
    return 0;
}

// Modificar la función ClientThread para crear hilos de manejo de datos
DWORD WINAPI ClientThread(LPVOID lpParam) {
    (void)lpParam;
    
    while (1) {
        struct sockaddr_in clientAddr;
        int clientAddrLen = sizeof(clientAddr);
        SOCKET clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &clientAddrLen);
        
        if (clientSocket != INVALID_SOCKET && clientCount < MAX_CLIENTS) {
            int clientIndex = -1;
            for (int i = 0; i < MAX_CLIENTS; i++) {
                if (clients[i].socket == INVALID_SOCKET || !clients[i].isInitialized) {
                    clientIndex = i;
                    break;
                }
            }
            
            if (clientIndex != -1) {
                clients[clientIndex].socket = clientSocket;
                clientCount++;
                
                InitializeNewClientConnection(clientIndex);
                
                char logMsg[256];
                sprintf(logMsg, "Cliente %d conectado. Total: %d", clients[clientIndex].clientId, clientCount);
                LogMessage(logMsg);
                
                SendGameStateToClient(clientIndex);
                
                // Crear hilo para manejar los datos de este cliente
                HANDLE clientDataThread = CreateThread(NULL, 0, HandleClientData, &clientIndex, 0, NULL);
                if (clientDataThread == NULL) {
                    printf("Error creando hilo para cliente %d\n", clients[clientIndex].clientId);
                } else {
                    CloseHandle(clientDataThread); // No necesitamos mantener el handle
                }
            }
        }
    }
    return 0;
}