#include "game_logic.h"
#include <string.h>
#include <stdlib.h>

void InitializeGame(GameState* gameState) {
    int mapMatrix[MATRIX_HEIGHT][MATRIX_WIDTH] = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
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
    
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
    for (int j = 0; j < MATRIX_WIDTH; j++) {
        if (mapMatrix[i][j] == 1) {
            gameState->matrix[i][j] = CELL_PLATFORM;
        } else if (mapMatrix[i][j] == 22) {
            gameState->matrix[i][j] = CELL_BONUS_ZONE;  // Preservar el bloque especial
        } else {
            gameState->matrix[i][j] = CELL_EMPTY;
        }
    }
}
    
    gameState->playerX = 2;
    gameState->playerY = MATRIX_HEIGHT - 2;
    
    gameState->enemyCount = 0;
    gameState->fruitCount = 0;
    gameState->playerScore = 0;
    memset(gameState->enemies, 0, sizeof(gameState->enemies));
    memset(gameState->fruits, 0, sizeof(gameState->fruits));
}

void InitializeNewClient(GameState* gameState) {
    InitializeGame(gameState);
}

void SpawnEnemyForClient(GameState* gameState, EnemyType type, int iceSpawnRow, int iceSpawnCol, int canSpawnEnemies) {
    // NUEVO: Verificar si se pueden crear enemigos (NO durante fase bonus)
    if (!canSpawnEnemies) {
        return; // No crear enemigo si está en fase bonus
    }
    
    if (type == ENEMY_ICE) {
        for (int i = 0; i < MAX_ENEMIES; i++) {
            if (gameState->enemies[i][3] == 1 && gameState->enemies[i][2] == ENEMY_ICE) {
                gameState->enemies[i][3] = 0;
                gameState->enemyCount--;
            }
        }
    }
    
    if (gameState->enemyCount >= MAX_ENEMIES) return;
    
    int x, y;
    
    if (type == ENEMY_FOCA) {
        x = (rand() % 2 == 0) ? 1 : MATRIX_WIDTH - 2;
        int attempts = 0;
        do {
            y = rand() % (MATRIX_HEIGHT - 3) + 1;
            attempts++;
        } while (attempts < 20 && (gameState->matrix[y][x] != CELL_EMPTY || 
                 gameState->matrix[y + 1][x] != CELL_PLATFORM));
        
        if (attempts >= 20) {
            y = MATRIX_HEIGHT - 2;
        }
    } else if (type == ENEMY_ICE) {
        x = iceSpawnCol;
        y = iceSpawnRow;
    } else {
        x = rand() % (MATRIX_WIDTH - 2) + 1;
        y = MATRIX_HEIGHT - 2;
    }
    
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (gameState->enemies[i][3] == 0) {
            gameState->enemies[i][0] = x;
            gameState->enemies[i][1] = y;
            gameState->enemies[i][2] = type;
            gameState->enemies[i][3] = 1;
            gameState->enemyCount++;
            break;
        }
    }
}

void SpawnFruitForClient(GameState* gameState, FruitType type, int canSpawnFruits) {
    // NUEVO: Verificar si se puede crear frutas
    if (!canSpawnFruits) {
        return; // No crear fruta si no está habilitado
    }
    
    if (gameState->fruitCount >= MAX_FRUITS) return;
    
    int x, y;
    int attempts = 0;
    do {
        x = rand() % MATRIX_WIDTH;
        y = rand() % (MATRIX_HEIGHT - 1);
        attempts++;
    } while (attempts < 50 && (gameState->matrix[y][x] != CELL_EMPTY || 
             (y + 1 < MATRIX_HEIGHT && gameState->matrix[y + 1][x] != CELL_PLATFORM)));
    
    if (attempts >= 50) {
        x = rand() % MATRIX_WIDTH;
        y = MATRIX_HEIGHT - 3;
    }
    
    for (int i = 0; i < MAX_FRUITS; i++) {
        if (gameState->fruits[i][3] == 0) {
            gameState->fruits[i][0] = x;
            gameState->fruits[i][1] = y;
            gameState->fruits[i][2] = type;
            gameState->fruits[i][3] = 1;
            gameState->fruitCount++;
            break;
        }
    }
}

void CleanupOutOfBoundsEnemiesForClient(GameState* gameState) {
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (gameState->enemies[i][3] == 1) {
            int x = gameState->enemies[i][0];
            int y = gameState->enemies[i][1];
            int type = gameState->enemies[i][2];
            
            if (type == ENEMY_ICE && y >= MATRIX_HEIGHT) {
                gameState->enemies[i][3] = 0;
                gameState->enemyCount--;
            }
        }
    }
}

void UpdateGameForClient(GameState* gameState) {
    CleanupOutOfBoundsEnemiesForClient(gameState);
    
    int mapMatrix[MATRIX_HEIGHT][MATRIX_WIDTH] = {
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, 22, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
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
    
    for (int i = 0; i < MATRIX_HEIGHT; i++) {
    for (int j = 0; j < MATRIX_WIDTH; j++) {
        if (mapMatrix[i][j] == 1) {
            gameState->matrix[i][j] = CELL_PLATFORM;
        } else if (mapMatrix[i][j] == 22) {
            gameState->matrix[i][j] = CELL_BONUS_ZONE;  // Preservar el bloque especial
        } else {
            gameState->matrix[i][j] = CELL_EMPTY;
        }
    }
}
    
    gameState->matrix[gameState->playerY][gameState->playerX] = CELL_PLAYER;
    
    for (int i = 0; i < MAX_ENEMIES; i++) {
        if (gameState->enemies[i][3] == 1) {
            int x = gameState->enemies[i][0];
            int y = gameState->enemies[i][1];
            int type = gameState->enemies[i][2];
            
            if (type != ENEMY_ICE && x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                if (gameState->matrix[y][x] == CELL_EMPTY) {
                    gameState->matrix[y][x] = CELL_ENEMY_FOCA + type - 1;
                }
            }
        }
    }
    
    for (int i = 0; i < MAX_FRUITS; i++) {
        if (gameState->fruits[i][3] == 1) {
            int x = gameState->fruits[i][0];
            int y = gameState->fruits[i][1];
            int type = gameState->fruits[i][2];
            if (x >= 0 && x < MATRIX_WIDTH && y >= 0 && y < MATRIX_HEIGHT) {
                if (gameState->matrix[y][x] == CELL_EMPTY) {
                    gameState->matrix[y][x] = CELL_FRUIT_NARANJA + type - 1;
                }
            }
        }
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