#ifndef NETWORK_H
#define NETWORK_H

#include <winsock2.h>
#include "game_logic.h"

#define MESSAGE_TYPE_GAME_STATE 1
#define MESSAGE_TYPE_CLIENT_MESSAGE 2
#define MESSAGE_BUFFER_SIZE 256

typedef struct {
    int messageType;
    int clientId;
    char messageContent[MESSAGE_BUFFER_SIZE];
} ClientMessage;

typedef struct {
    SOCKET socket;
    GameState gameState;
    int isInitialized;
    int clientId;
    int canSpawnFruits;
    int canSpawnEnemies;
} ClientData;

extern SOCKET serverSocket;
extern ClientData clients[MAX_CLIENTS];
extern int clientCount;
extern int nextClientId;

void InitializeNetwork();
void CleanupNetwork();
int StartServer();
DWORD WINAPI ClientThread(LPVOID lpParam);
void SendGameStateToClient(int clientIndex);
void SpawnEnemyForAllClients(EnemyType type, int iceSpawnRow, int iceSpawnCol);
void SpawnFruitForAllClients(FruitType type);
void ClearAllEnemies();
void ClearAllFruits();
void InitializeNewClientConnection(int clientIndex);
void ProcessClientMessage(int clientIndex, const char* buffer, int bufferSize);
DWORD WINAPI HandleClientData(LPVOID lpParam);

#endif