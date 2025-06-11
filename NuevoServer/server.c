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

// Variables globales
SOCKET serverSocket;
SOCKET clientSockets[MAX_CLIENTS];
int clientCount = 0;
GameState gameState;
HWND hwnd;

// Prototipos de funciones
LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
DWORD WINAPI ClientThread(LPVOID lpParam);
void InitializeGame();
void SendGameState();
void SpawnEnemy(EnemyType type);
void SpawnFruit(FruitType type);
void UpdateGame();
void LogMessage(const char* message);
int GetFruitScore(FruitType type);
const char* GetFruitName(FruitType type);

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    // Suprimir warnings de parámetros no utilizados
    (void)hPrevInstance;
    (void)lpCmdLine;
    
    // Inicializar semilla para números aleatorios
    srand((unsigned int)time(NULL));
    
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

    // Inicializar el juego
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
            
        CreateWindow("BUTTON", "Generar Hielo",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 90, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY3, NULL, NULL);
            
        CreateWindow("BUTTON", "Limpiar Enemigos",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 130, 150, 30, hwnd, (HMENU)ID_BTN_CLEAR, NULL, NULL);

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
            SpawnEnemy(ENEMY_FOCA);
            LogMessage("Enemigo Foca generado");
            break;
        case ID_BTN_ENEMY2:
            SpawnEnemy(ENEMY_BIRD);
            LogMessage("Enemigo Ave generado");
            break;
        case ID_BTN_ENEMY3:
            SpawnEnemy(ENEMY_ICE);
            LogMessage("Enemigo Hielo generado");
            break;
        case ID_BTN_CLEAR:
            for (int i = 0; i < MAX_ENEMIES; i++) {
                gameState.enemies[i][3] = 0; // Desactivar
            }
            gameState.enemyCount = 0;
            LogMessage("Enemigos eliminados");
            UpdateGame();
            SendGameState();
            break;
        case ID_BTN_FRUIT1:
            SpawnFruit(FRUIT_NARANJA);
            LogMessage("Fruta Naranja generada (100 puntos)");
            break;
        case ID_BTN_FRUIT2:
            SpawnFruit(FRUIT_BANANO);
            LogMessage("Fruta Banano generada (200 puntos)");
            break;
        case ID_BTN_FRUIT3:
            SpawnFruit(FRUIT_BERENJENA);
            LogMessage("Fruta Berenjena generada (300 puntos)");
            break;
        case ID_BTN_FRUIT4:
            SpawnFruit(FRUIT_LECHUGA);
            LogMessage("Fruta Lechuga generada (400 puntos)");
            break;
        case ID_BTN_CLEAR_FRUITS:
            for (int i = 0; i < MAX_FRUITS; i++) {
                gameState.fruits[i][3] = 0; // Desactivar
            }
            gameState.fruitCount = 0;
            LogMessage("Frutas eliminadas");
            UpdateGame();
            SendGameState();
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
    // Definir la matriz del mapa
    int mapMatrix[MATRIX_HEIGHT][MATRIX_WIDTH] = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };
    
    // Aplicar la matriz del mapa al gameState
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
    gameState.matrix[gameState.playerY][gameState.playerX] = CELL_PLAYER;
    
    // Inicializar enemigos y frutas
    gameState.enemyCount = 0;
    gameState.fruitCount = 0;
    gameState.playerScore = 0;
    memset(gameState.enemies, 0, sizeof(gameState.enemies));
    memset(gameState.fruits, 0, sizeof(gameState.fruits));
}

void SpawnEnemy(EnemyType type) {
    if (gameState.enemyCount >= MAX_ENEMIES) return;
    
    int x, y;
    
    if (type == ENEMY_FOCA) {
        // Para FOCA, buscar posiciones vacías (0) en los bordes
        x = (rand() % 2 == 0) ? 1 : MATRIX_WIDTH - 2; // Borde izquierdo o derecho
        
        // Buscar una posición Y que sea espacio vacío (0) y que tenga plataforma debajo
        int attempts = 0;
        do {
            y = rand() % (MATRIX_HEIGHT - 3) + 1; // Entre 1 y MATRIX_HEIGHT-3 para dejar espacio para el suelo
            attempts++;
        } while (attempts < 20 && (gameState.matrix[y][x] != CELL_EMPTY || 
                 gameState.matrix[y + 1][x] != CELL_PLATFORM)); // Verificar que hay plataforma debajo
        
        // Si no encuentra espacio válido, usar posición segura sobre el suelo
        if (attempts >= 20) {
            y = MATRIX_HEIGHT - 2; // Una fila arriba del suelo
        }
    } else {
        // Para otros enemigos, mantener el comportamiento original
        x = rand() % (MATRIX_WIDTH - 2) + 1;
        y = MATRIX_HEIGHT - 2;
    }
    
    // Buscar el primer slot libre
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (gameState.enemies[i][3] == 0) { // No activo
            gameState.enemies[i][0] = x;
            gameState.enemies[i][1] = y;
            gameState.enemies[i][2] = type;
            gameState.enemies[i][3] = 1; // Activo
            gameState.enemyCount++;
            break;
        }
    }
    
    UpdateGame();
    SendGameState();
}

void SpawnFruit(FruitType type) {
    if (gameState.fruitCount >= MAX_FRUITS) return;
    
    // Encontrar posición libre en plataformas
    int x, y;
    int attempts = 0;
    do {
        x = rand() % MATRIX_WIDTH;
        y = rand() % (MATRIX_HEIGHT - 1);
        attempts++;
    } while (attempts < 50 && (gameState.matrix[y][x] != CELL_EMPTY || 
             (y + 1 < MATRIX_HEIGHT && gameState.matrix[y + 1][x] != CELL_PLATFORM)));
    
    if (attempts >= 50) {
        // Si no encuentra posición válida, colocar en una posición predeterminada
        x = rand() % MATRIX_WIDTH;
        y = MATRIX_HEIGHT - 3;
    }
    
    // Buscar el primer slot libre
    for (int i = 0; i < MAX_FRUITS; i++) {
        if (gameState.fruits[i][3] == 0) { // No activo
            gameState.fruits[i][0] = x;
            gameState.fruits[i][1] = y;
            gameState.fruits[i][2] = type;
            gameState.fruits[i][3] = 1; // Activo
            gameState.fruitCount++;
            break;
        }
    }
    
    UpdateGame();
    SendGameState();
}

void UpdateGame() {
    // Matriz base del mapa
    int mapMatrix[MATRIX_HEIGHT][MATRIX_WIDTH] = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };
    
    // Restaurar mapa base
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
        for (int j = 0; j < MATRIX_WIDTH; j++) {
            if (mapMatrix[i][j] == 1) {
                gameState.matrix[i][j] = CELL_PLATFORM;
            } else {
                gameState.matrix[i][j] = CELL_EMPTY;
            }
        }
    }
    
    // Colocar jugador
    gameState.matrix[gameState.playerY][gameState.playerX] = CELL_PLAYER;
    
    // Colocar enemigos activos
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (gameState.enemies[i][3] == 1) { // Activo
            int x = gameState.enemies[i][0];
            int y = gameState.enemies[i][1];
            int type = gameState.enemies[i][2];
            if (x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                gameState.matrix[y][x] = CELL_ENEMY_FOCA + type - 1;
            }
        }
    }
    
    // Colocar frutas activas
    for (int i = 0; i < MAX_FRUITS; i++) {
        if (gameState.fruits[i][3] == 1) { // Activo
            int x = gameState.fruits[i][0];
            int y = gameState.fruits[i][1];
            int type = gameState.fruits[i][2];
            if (x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                // Solo colocar si la celda está vacía
                if (gameState.matrix[y][x] == CELL_EMPTY) {
                    gameState.matrix[y][x] = CELL_FRUIT_NARANJA + type - 1;
                }
            }
        }
    }
}

void SendGameState() {
    char buffer[BUFFER_SIZE];
    int offset = 0;
    
    // Enviar matriz
    memcpy(buffer + offset, &gameState.matrix, sizeof(gameState.matrix));
    offset += sizeof(gameState.matrix);
    
    // Enviar datos de enemigos
    memcpy(buffer + offset, &gameState.enemyCount, sizeof(int));
    offset += sizeof(int);
    memcpy(buffer + offset, gameState.enemies, sizeof(gameState.enemies));
    offset += sizeof(gameState.enemies);
    
    // Enviar datos de frutas
    memcpy(buffer + offset, &gameState.fruitCount, sizeof(int));
    offset += sizeof(int);
    memcpy(buffer + offset, gameState.fruits, sizeof(gameState.fruits));
    offset += sizeof(gameState.fruits);
    
    // Enviar puntuación del jugador
    memcpy(buffer + offset, &gameState.playerScore, sizeof(int));
    offset += sizeof(int);
    
    // Enviar a todos los clientes conectados
    for (int i = 0; i < clientCount; i++) {
        if (clientSockets[i] != INVALID_SOCKET) {
            send(clientSockets[i], buffer, offset, 0);
        }
    }
}

DWORD WINAPI ClientThread(LPVOID lpParam) {
    // Suprimir warning de parámetro no utilizado
    (void)lpParam;
    
    while (1) {
        struct sockaddr_in clientAddr;
        int clientAddrLen = sizeof(clientAddr);
        SOCKET clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &clientAddrLen);
        
        if (clientSocket != INVALID_SOCKET && clientCount < MAX_CLIENTS) {
            clientSockets[clientCount] = clientSocket;
            clientCount++;
            
            char logMsg[256];
            sprintf(logMsg, "Cliente conectado. Total: %d", clientCount);
            LogMessage(logMsg);
            
            // Enviar estado inicial
            SendGameState();
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
