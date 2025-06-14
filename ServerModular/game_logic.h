#ifndef GAME_LOGIC_H
#define GAME_LOGIC_H

#include "constants.h"

typedef enum {
    ENEMY_NONE = 0,
    ENEMY_FOCA = 1,
    ENEMY_BIRD = 2,
    ENEMY_ICE = 3
} EnemyType;

typedef enum {
    FRUIT_NARANJA = 0,
    FRUIT_BANANO = 1,
    FRUIT_BERENJENA = 2,
    FRUIT_LECHUGA = 3
} FruitType;

typedef struct {
    int matrix[MATRIX_HEIGHT][MATRIX_WIDTH];
    int playerX, playerY;
    int enemies[MAX_ENEMIES][4];
    int fruits[MAX_FRUITS][4];
    int enemyCount;
    int fruitCount;
    int playerScore;
} GameState;

void InitializeGame(GameState* gameState);
void InitializeNewClient(GameState* gameState);
void SpawnEnemyForClient(GameState* gameState, EnemyType type, int iceSpawnRow, int iceSpawnCol, int canSpawnEnemies);
void SpawnFruitForClient(GameState* gameState, FruitType type, int canSpawnFruits);
void CleanupOutOfBoundsEnemiesForClient(GameState* gameState);
void UpdateGameForClient(GameState* gameState);
int GetFruitScore(FruitType type);
const char* GetFruitName(FruitType type);

#endif
