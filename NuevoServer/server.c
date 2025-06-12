#include <winsock2.h>
#include <windows.h>
#include <ws2tcpip.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "constants.h"

// Tipos de enemigos
typedef enum {
    ENEMY_NONE = 0,
    ENEMY_FOCA = 1,
    ENEMY_BIRD = 2,
    ENEMY_ICE = 3
} EnemyType;

// Tipos de frutas
typedef enum {
    FRUIT_NONE = 0,
    FRUIT_NARANJA = 1,
    FRUIT_BANANO = 2,
    FRUIT_BERENJENA = 3,
    FRUIT_LECHUGA = 4
} FruitType;

// Estructura del juego
typedef struct {
    int matrix[MATRIX_HEIGHT][MATRIX_WIDTH];
    int playerX, playerY;
    int enemies[MAX_ENEMIES][4]; // x, y, tipo, activo
    int fruits[MAX_FRUITS][4];   // x, y, tipo, activo
    int enemyCount;
    int fruitCount;
    int playerScore;
} GameState;

// Estructura para manejar estados individuales por cliente
typedef struct {
    SOCKET socket;
    GameState gameState;
    int isInitialized;
    int clientId;
} ClientData;

// Variables globales modificadas
SOCKET serverSocket;
ClientData clients[MAX_CLIENTS];
int clientCount = 0;
int nextClientId = 1;
GameState gameState; // Estado base para inicialización
HWND hwnd;
// Variables globales para almacenar las coordenadas de spawn del ICE
int iceSpawnRow = 0;    // Fila por defecto
int iceSpawnCol = 10;   // Columna por defecto (centro del mapa)

// Prototipos de funciones
LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
DWORD WINAPI ClientThread(LPVOID lpParam);
void InitializeGame();
void InitializeNewClient(int clientIndex);
void SendGameStateToClient(int clientIndex);
void SpawnEnemyForAllClients(EnemyType type);
void SpawnEnemyForClient(int clientIndex, EnemyType type);
void ReadIceSpawnCoordinates();
void SpawnFruitForAllClients(FruitType type);
void SpawnFruitForClient(int clientIndex, FruitType type);
void UpdateGameForClient(int clientIndex);
void CleanupOutOfBoundsEnemiesForClient(int clientIndex);
void LogMessage(const char* message);
int GetFruitScore(FruitType type);
const char* GetFruitName(FruitType type);

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    (void)hPrevInstance;
    (void)lpCmdLine;
    
    // Inicializar semilla para números aleatorios
    srand((unsigned int)time(NULL));
    
    // Inicializar array de clientes
    for (int i = 0; i < MAX_CLIENTS; i++) {
        clients[i].socket = INVALID_SOCKET;
        clients[i].isInitialized = 0;
        clients[i].clientId = 0;
    }
    
    // Inicializar Winsock
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        MessageBox(NULL, "Error inicializando Winsock", "Error", MB_OK);
        return 1;
    }

    // Crear ventana
    const char CLASS_NAME[] = "GameServerWindow";
    
    WNDCLASS wc = {};
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = CLASS_NAME;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);

    RegisterClass(&wc);

    hwnd = CreateWindowEx(
        0,
        CLASS_NAME,
        "Servidor del Juego de Plataformas",
        WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, CW_USEDEFAULT, 700, 500,
        NULL, NULL, hInstance, NULL
    );

    if (hwnd == NULL) {
        return 0;
    }

    ShowWindow(hwnd, nCmdShow);
    UpdateWindow(hwnd);

    // Inicializar el juego base
    InitializeGame();

    // Crear socket del servidor
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == INVALID_SOCKET) {
        LogMessage("Error creando socket del servidor");
        WSACleanup();
        return 1;
    }

    struct sockaddr_in serverAddr;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    if (bind(serverSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        LogMessage("Error en bind");
        closesocket(serverSocket);
        WSACleanup();
        return 1;
    }

    if (listen(serverSocket, MAX_CLIENTS) == SOCKET_ERROR) {
        LogMessage("Error en listen");
        closesocket(serverSocket);
        WSACleanup();
        return 1;
    }

    LogMessage("Servidor iniciado en puerto 8080");

    // Crear hilo para aceptar conexiones
    CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)ClientThread, NULL, 0, NULL);

    // Loop principal de mensajes
    MSG msg = {};
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    // Limpiar
    closesocket(serverSocket);
    WSACleanup();
    return 0;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_CREATE:
        // Crear controles de enemigos
        CreateWindow("BUTTON", "Generar Foca",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 10, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY1, NULL, NULL);
            
        CreateWindow("BUTTON", "Generar Ave",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 50, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY2, NULL, NULL);
            
        // Controles para el enemigo ICE
        CreateWindow("STATIC", "Fila ICE:",
            WS_VISIBLE | WS_CHILD,
            10, 170, 70, 20, hwnd, (HMENU)ID_STATIC_ICE_ROW, NULL, NULL);
            
        CreateWindow("EDIT", "0",
            WS_VISIBLE | WS_CHILD | WS_BORDER | ES_NUMBER,
            85, 170, 40, 20, hwnd, (HMENU)ID_EDIT_ICE_ROW, NULL, NULL);
            
        CreateWindow("STATIC", "Col ICE:",
            WS_VISIBLE | WS_CHILD,
            10, 195, 70, 20, hwnd, (HMENU)ID_STATIC_ICE_COL, NULL, NULL);
            
        CreateWindow("EDIT", "10",
            WS_VISIBLE | WS_CHILD | WS_BORDER | ES_NUMBER,
            85, 195, 40, 20, hwnd, (HMENU)ID_EDIT_ICE_COL, NULL, NULL);
        
        // Mover el botón "Generar Hielo" más abajo
        CreateWindow("BUTTON", "Generar Hielo",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 220, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY3, NULL, NULL);
        
        // También mover "Limpiar Enemigos" más abajo
        CreateWindow("BUTTON", "Limpiar Enemigos",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 260, 150, 30, hwnd, (HMENU)ID_BTN_CLEAR, NULL, NULL);

        // Crear controles de frutas
        CreateWindow("BUTTON", "Naranja (100)",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            170, 10, 150, 30, hwnd, (HMENU)ID_BTN_FRUIT1, NULL, NULL);
            
        CreateWindow("BUTTON", "Banano (200)",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            170, 50, 150, 30, hwnd, (HMENU)ID_BTN_FRUIT2, NULL, NULL);
            
        CreateWindow("BUTTON", "Berenjena (300)",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            170, 90, 150, 30, hwnd, (HMENU)ID_BTN_FRUIT3, NULL, NULL);
            
        CreateWindow("BUTTON", "Lechuga (400)",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            170, 130, 150, 30, hwnd, (HMENU)ID_BTN_FRUIT4, NULL, NULL);
            
        CreateWindow("BUTTON", "Limpiar Frutas",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            170, 170, 150, 30, hwnd, (HMENU)ID_BTN_CLEAR_FRUITS, NULL, NULL);
            
        // Crear listbox para mensajes
        CreateWindow("LISTBOX", NULL,
            WS_CHILD | WS_VISIBLE | WS_VSCROLL | LBS_NOTIFY,
            340, 10, 320, 400, hwnd, (HMENU)ID_LISTBOX, NULL, NULL);
        break;

    case WM_COMMAND:
        switch (LOWORD(wParam)) {
        case ID_BTN_ENEMY1:
            SpawnEnemyForAllClients(ENEMY_FOCA);
            LogMessage("Enemigo Foca generado para todos los clientes");
            break;
        case ID_BTN_ENEMY2:
            SpawnEnemyForAllClients(ENEMY_BIRD);
            LogMessage("Enemigo Ave generado para todos los clientes");
            break;
        case ID_BTN_ENEMY3:
            ReadIceSpawnCoordinates(); // Leer coordenadas antes de generar
            SpawnEnemyForAllClients(ENEMY_ICE);
            {
                char logMsg[256];
                sprintf(logMsg, "Enemigo Hielo generado en posición (%d, %d) para todos los clientes", 
                        iceSpawnCol, iceSpawnRow);
                LogMessage(logMsg);
            }
    break;
        case ID_BTN_CLEAR:
            for (int i = 0; i < MAX_CLIENTS; i++) {
                if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
                    for (int j = 0; j < MAX_ENEMIES; j++) {
                        clients[i].gameState.enemies[j][3] = 0; // Desactivar
                    }
                    clients[i].gameState.enemyCount = 0;
                    UpdateGameForClient(i);
                    SendGameStateToClient(i);
                }
            }
            LogMessage("Enemigos eliminados para todos los clientes");
            break;
        case ID_BTN_FRUIT1:
            SpawnFruitForAllClients(FRUIT_NARANJA);
            LogMessage("Fruta Naranja generada para todos los clientes (100 puntos)");
            break;
        case ID_BTN_FRUIT2:
            SpawnFruitForAllClients(FRUIT_BANANO);
            LogMessage("Fruta Banano generada para todos los clientes (200 puntos)");
            break;
        case ID_BTN_FRUIT3:
            SpawnFruitForAllClients(FRUIT_BERENJENA);
            LogMessage("Fruta Berenjena generada para todos los clientes (300 puntos)");
            break;
        case ID_BTN_FRUIT4:
            SpawnFruitForAllClients(FRUIT_LECHUGA);
            LogMessage("Fruta Lechuga generada para todos los clientes (400 puntos)");
            break;
        case ID_BTN_CLEAR_FRUITS:
            for (int i = 0; i < MAX_CLIENTS; i++) {
                if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
                    for (int j = 0; j < MAX_FRUITS; j++) {
                        clients[i].gameState.fruits[j][3] = 0; // Desactivar
                    }
                    clients[i].gameState.fruitCount = 0;
                    UpdateGameForClient(i);
                    SendGameStateToClient(i);
                }
            }
            LogMessage("Frutas eliminadas para todos los clientes");
            break;
        }
        break;

    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void InitializeGame() {
    // Definir la matriz del mapa base
    int mapMatrix[MATRIX_HEIGHT][MATRIX_WIDTH] = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };
    
    // Aplicar la matriz del mapa al gameState base
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
        for (int j = 0; j < MATRIX_WIDTH; j++) {
            if (mapMatrix[i][j] == 1) {
                gameState.matrix[i][j] = CELL_PLATFORM;
            } else {
                gameState.matrix[i][j] = CELL_EMPTY;
            }
        }
    }
    
    // Posición inicial del jugador
    gameState.playerX = 2;
    gameState.playerY = MATRIX_HEIGHT - 2;
    
    // Inicializar enemigos y frutas
    gameState.enemyCount = 0;
    gameState.fruitCount = 0;
    gameState.playerScore = 0;
    memset(gameState.enemies, 0, sizeof(gameState.enemies));
    memset(gameState.fruits, 0, sizeof(gameState.fruits));
}

// Función para inicializar un nuevo cliente
void InitializeNewClient(int clientIndex) {
    // Copiar el mapa base pero sin enemigos
    InitializeGame(); // Esto inicializa gameState global
    
    // Copiar solo el mapa y jugador, no los enemigos
    memcpy(clients[clientIndex].gameState.matrix, gameState.matrix, sizeof(gameState.matrix));
    clients[clientIndex].gameState.playerX = gameState.playerX;
    clients[clientIndex].gameState.playerY = gameState.playerY;
    clients[clientIndex].gameState.playerScore = 0;
    
    // Limpiar enemigos y frutas para este cliente
    clients[clientIndex].gameState.enemyCount = 0;
    clients[clientIndex].gameState.fruitCount = 0;
    memset(clients[clientIndex].gameState.enemies, 0, sizeof(clients[clientIndex].gameState.enemies));
    memset(clients[clientIndex].gameState.fruits, 0, sizeof(clients[clientIndex].gameState.fruits));
    
    clients[clientIndex].isInitialized = 1;
    clients[clientIndex].clientId = nextClientId++;
    
    // Actualizar el juego para este cliente
    UpdateGameForClient(clientIndex);
}

// Función modificada para enviar estado a un cliente específico
void SendGameStateToClient(int clientIndex) {
    if (clientIndex < 0 || clientIndex >= MAX_CLIENTS || !clients[clientIndex].isInitialized) {
        return;
    }
    
    char buffer[BUFFER_SIZE];
    int offset = 0;
    
    GameState* clientGameState = &clients[clientIndex].gameState;
    
    // Enviar matriz
    memcpy(buffer + offset, &clientGameState->matrix, sizeof(clientGameState->matrix));
    offset += sizeof(clientGameState->matrix);
    
    // Enviar datos de enemigos
    memcpy(buffer + offset, &clientGameState->enemyCount, sizeof(int));
    offset += sizeof(int);
    memcpy(buffer + offset, clientGameState->enemies, sizeof(clientGameState->enemies));
    offset += sizeof(clientGameState->enemies);
    
    // Enviar datos de frutas
    memcpy(buffer + offset, &clientGameState->fruitCount, sizeof(int));
    offset += sizeof(int);
    memcpy(buffer + offset, clientGameState->fruits, sizeof(clientGameState->fruits));
    offset += sizeof(clientGameState->fruits);
    
    // Enviar puntuación del jugador
    memcpy(buffer + offset, &clientGameState->playerScore, sizeof(int));
    offset += sizeof(int);
    
    // Enviar solo a este cliente
    if (clients[clientIndex].socket != INVALID_SOCKET) {
        send(clients[clientIndex].socket, buffer, offset, 0);
    }
}

// Función para aplicar enemigos a todos los clientes
void SpawnEnemyForAllClients(EnemyType type) {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
            SpawnEnemyForClient(i, type);
            SendGameStateToClient(i);
        }
    }
}

// Función para leer las coordenadas de los controles de texto
void ReadIceSpawnCoordinates() {
    char buffer[10];
    
    // Leer fila
    GetWindowText(GetDlgItem(hwnd, ID_EDIT_ICE_ROW), buffer, sizeof(buffer));
    iceSpawnRow = atoi(buffer);
    
    // Validar fila (debe estar entre 0 y MATRIX_HEIGHT-1)
    if (iceSpawnRow < 0) iceSpawnRow = 0;
    if (iceSpawnRow >= MATRIX_HEIGHT) iceSpawnRow = MATRIX_HEIGHT - 1;
    
    // Leer columna
    GetWindowText(GetDlgItem(hwnd, ID_EDIT_ICE_COL), buffer, sizeof(buffer));
    iceSpawnCol = atoi(buffer);
    
    // Validar columna (debe estar entre 0 y MATRIX_WIDTH-1)
    if (iceSpawnCol < 0) iceSpawnCol = 0;
    if (iceSpawnCol >= MATRIX_WIDTH) iceSpawnCol = MATRIX_WIDTH - 1;
}

// Nueva función para generar enemigo para un cliente específico
void SpawnEnemyForClient(int clientIndex, EnemyType type) {
    if (clientIndex < 0 || clientIndex >= MAX_CLIENTS || !clients[clientIndex].isInitialized) {
        return;
    }
    
    GameState* clientGameState = &clients[clientIndex].gameState;
    
    // Lógica similar a SpawnEnemy pero para el estado específico del cliente
    if (type == ENEMY_ICE) {
        for (int i = 0; i < MAX_ENEMIES; i++) {
            if (clientGameState->enemies[i][3] == 1 && clientGameState->enemies[i][2] == ENEMY_ICE) {
                clientGameState->enemies[i][3] = 0;
                clientGameState->enemyCount--;
            }
        }
    }
    
    if (clientGameState->enemyCount >= MAX_ENEMIES) return;
    
    int x, y;
    
    if (type == ENEMY_FOCA) {
        x = (rand() % 2 == 0) ? 1 : MATRIX_WIDTH - 2;
        int attempts = 0;
        do {
            y = rand() % (MATRIX_HEIGHT - 3) + 1;
            attempts++;
        } while (attempts < 20 && (clientGameState->matrix[y][x] != CELL_EMPTY || 
                 clientGameState->matrix[y + 1][x] != CELL_PLATFORM));
        
        if (attempts >= 20) {
            y = MATRIX_HEIGHT - 2;
        }
    } else if (type == ENEMY_ICE) {
        // Usar las coordenadas personalizadas para ICE
        x = iceSpawnCol;
        y = iceSpawnRow;
    } else {
        x = rand() % (MATRIX_WIDTH - 2) + 1;
        y = MATRIX_HEIGHT - 2;
    }
    
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (clientGameState->enemies[i][3] == 0) {
            clientGameState->enemies[i][0] = x;
            clientGameState->enemies[i][1] = y;
            clientGameState->enemies[i][2] = type;
            clientGameState->enemies[i][3] = 1;
            clientGameState->enemyCount++;
            break;
        }
    }
    
    UpdateGameForClient(clientIndex);
}

// Función para aplicar frutas a todos los clientes
void SpawnFruitForAllClients(FruitType type) {
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (clients[i].isInitialized && clients[i].socket != INVALID_SOCKET) {
            SpawnFruitForClient(i, type);
            SendGameStateToClient(i);
        }
    }
}

// Nueva función para generar fruta para un cliente específico
void SpawnFruitForClient(int clientIndex, FruitType type) {
    if (clientIndex < 0 || clientIndex >= MAX_CLIENTS || !clients[clientIndex].isInitialized) {
        return;
    }
    
    GameState* clientGameState = &clients[clientIndex].gameState;
    
    if (clientGameState->fruitCount >= MAX_FRUITS) return;
    
    // Encontrar posición libre en plataformas
    int x, y;
    int attempts = 0;
    do {
        x = rand() % MATRIX_WIDTH;
        y = rand() % (MATRIX_HEIGHT - 1);
        attempts++;
    } while (attempts < 50 && (clientGameState->matrix[y][x] != CELL_EMPTY || 
             (y + 1 < MATRIX_HEIGHT && clientGameState->matrix[y + 1][x] != CELL_PLATFORM)));
    
    if (attempts >= 50) {
        x = rand() % MATRIX_WIDTH;
        y = MATRIX_HEIGHT - 3;
    }
    
    for (int i = 0; i < MAX_FRUITS; i++) {
        if (clientGameState->fruits[i][3] == 0) {
            clientGameState->fruits[i][0] = x;
            clientGameState->fruits[i][1] = y;
            clientGameState->fruits[i][2] = type;
            clientGameState->fruits[i][3] = 1;
            clientGameState->fruitCount++;
            break;
        }
    }
    
    UpdateGameForClient(clientIndex);
}

void CleanupOutOfBoundsEnemiesForClient(int clientIndex) {
    if (clientIndex < 0 || clientIndex >= MAX_CLIENTS || !clients[clientIndex].isInitialized) {
        return;
    }
    
    GameState* clientGameState = &clients[clientIndex].gameState;
    
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (clientGameState->enemies[i][3] == 1) {
            int x = clientGameState->enemies[i][0];
            int y = clientGameState->enemies[i][1];
            int type = clientGameState->enemies[i][2];
            
            if (type == ENEMY_ICE && y >= MATRIX_HEIGHT) {
                clientGameState->enemies[i][3] = 0;
                clientGameState->enemyCount--;
            }
        }
    }
}

void UpdateGameForClient(int clientIndex) {
    if (clientIndex < 0 || clientIndex >= MAX_CLIENTS || !clients[clientIndex].isInitialized) {
        return;
    }
    
    GameState* clientGameState = &clients[clientIndex].gameState;
    
    // Limpiar enemigos que salen del mapa
    CleanupOutOfBoundsEnemiesForClient(clientIndex);
    
    // Matriz base del mapa
    int mapMatrix[MATRIX_HEIGHT][MATRIX_WIDTH] = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };
    
    // Restaurar mapa base
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
        for (int j = 0; j < MATRIX_WIDTH; j++) {
            if (mapMatrix[i][j] == 1) {
                clientGameState->matrix[i][j] = CELL_PLATFORM;
            } else {
                clientGameState->matrix[i][j] = CELL_EMPTY;
            }
        }
    }
    
    // Colocar jugador
    clientGameState->matrix[clientGameState->playerY][clientGameState->playerX] = CELL_PLAYER;
    
    // Colocar enemigos activos
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (clientGameState->enemies[i][3] == 1) {
            int x = clientGameState->enemies[i][0];
            int y = clientGameState->enemies[i][1];
            int type = clientGameState->enemies[i][2];
            
            if (type != ENEMY_ICE && x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                if (clientGameState->matrix[y][x] == CELL_EMPTY) {
                    clientGameState->matrix[y][x] = CELL_ENEMY_FOCA + type - 1;
                }
            }
        }
    }
    
    // Colocar frutas activas
    for (int i = 0; i < MAX_FRUITS; i++) {
        if (clientGameState->fruits[i][3] == 1) {
            int x = clientGameState->fruits[i][0];
            int y = clientGameState->fruits[i][1];
            int type = clientGameState->fruits[i][2];
            if (x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                if (clientGameState->matrix[y][x] == CELL_EMPTY) {
                    clientGameState->matrix[y][x] = CELL_FRUIT_NARANJA + type - 1;
                }
            }
        }
    }
}

// Hilo de cliente modificado
DWORD WINAPI ClientThread(LPVOID lpParam) {
    (void)lpParam;
    
    while (1) {
        struct sockaddr_in clientAddr;
        int clientAddrLen = sizeof(clientAddr);
        SOCKET clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &clientAddrLen);
        
        if (clientSocket != INVALID_SOCKET && clientCount < MAX_CLIENTS) {
            // Encontrar slot libre
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
                
                // Inicializar cliente con estado limpio
                InitializeNewClient(clientIndex);
                
                char logMsg[256];
                sprintf(logMsg, "Cliente %d conectado. Total: %d", clients[clientIndex].clientId, clientCount);
                LogMessage(logMsg);
                
                // Enviar estado inicial limpio
                SendGameStateToClient(clientIndex);
            }
        }
    }
    return 0;
}

void LogMessage(const char* message) {
    HWND hListBox = GetDlgItem(hwnd, ID_LISTBOX);
    if (hListBox) {
        SendMessage(hListBox, LB_ADDSTRING, 0, (LPARAM)message);
        SendMessage(hListBox, LB_SETTOPINDEX, SendMessage(hListBox, LB_GETCOUNT, 0, 0) - 1, 0);
    }
}

int GetFruitScore(FruitType type) {
    switch (type) {
        case FRUIT_NARANJA: return SCORE_NARANJA;
        case FRUIT_BANANO: return SCORE_BANANO;
        case FRUIT_BERENJENA: return SCORE_BERENJENA;
        case FRUIT_LECHUGA: return SCORE_LECHUGA;
        default: return 0;
    }
}

const char* GetFruitName(FruitType type) {
    switch (type) {
        case FRUIT_NARANJA: return "Naranja";
        case FRUIT_BANANO: return "Banano";
        case FRUIT_BERENJENA: return "Berenjena";
        case FRUIT_LECHUGA: return "Lechuga";
        default: return "Desconocida";
    }
}