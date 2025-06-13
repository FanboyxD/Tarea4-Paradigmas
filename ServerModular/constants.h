#ifndef CONSTANTS_H
#define CONSTANTS_H

// Configuración del servidor
#define PORT 8888
#define MAX_CLIENTS 2

#define MSG_PLAYER_MOVEMENT 1
#define MSG_PLAYER_ACTION 2
#define MSG_FRUIT_COLLECTED 3
#define MSG_PLAYER_DEATH 4

// Configuración del juego
#define MATRIX_WIDTH 26
#define MATRIX_HEIGHT 24
#define MAX_ENEMIES 10
#define MAX_FRUITS 4

// Tipos de celdas en la matriz
#define CELL_EMPTY 0
#define CELL_PLATFORM 1
#define CELL_PLAYER 2
#define CELL_ENEMY_FOCA 3
#define CELL_ENEMY_BIRD 4
#define CELL_ENEMY_ICE 5
#define CELL_FRUIT_NARANJA 6
#define CELL_FRUIT_BANANO 7
#define CELL_FRUIT_BERENJENA 8
#define CELL_FRUIT_LECHUGA 9
#define CELL_BONUS_ZONE 22

// Puntuaciones de las frutas
#define SCORE_NARANJA 100
#define SCORE_BANANO 200
#define SCORE_BERENJENA 300
#define SCORE_LECHUGA 400

// IDs de los controles de la ventana
#define ID_BTN_ENEMY1 1001
#define ID_BTN_ENEMY2 1002
#define ID_BTN_ENEMY3 1003
#define ID_BTN_FRUIT1 1005
#define ID_BTN_FRUIT2 1006
#define ID_BTN_FRUIT3 1007
#define ID_BTN_FRUIT4 1008
#define ID_LISTBOX 1010

// Configuración de red
#define BUFFER_SIZE 4096

#define ID_EDIT_ICE_ROW     1010
#define ID_EDIT_ICE_COL     1011
#define ID_STATIC_ICE_ROW   1012
#define ID_STATIC_ICE_COL   1013

#endif // CONSTANTS_H