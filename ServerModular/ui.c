#include "ui.h"
#include "network.h"
#include "constants.h"
#include <stdio.h>
#include <stdlib.h>

HWND hwnd;
int iceSpawnRow = 0;
int iceSpawnCol = 10;

int InitializeUI(HINSTANCE hInstance, int nCmdShow) {
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
    
    return 1;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
    case WM_CREATE:
        CreateWindow("BUTTON", "Generar Foca",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 10, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY1, NULL, NULL);
            
        CreateWindow("BUTTON", "Generar Ave",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 50, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY2, NULL, NULL);
            
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
        
        CreateWindow("BUTTON", "Generar Hielo",
            WS_TABSTOP | WS_VISIBLE | WS_CHILD | BS_DEFPUSHBUTTON,
            10, 220, 150, 30, hwnd, (HMENU)ID_BTN_ENEMY3, NULL, NULL);

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
            
        CreateWindow("LISTBOX", NULL,
            WS_CHILD | WS_VISIBLE | WS_VSCROLL | LBS_NOTIFY,
            340, 10, 320, 400, hwnd, (HMENU)ID_LISTBOX, NULL, NULL);
        break;

    case WM_COMMAND:
        switch (LOWORD(wParam)) {
        case ID_BTN_ENEMY1:
            SpawnEnemyForAllClients(ENEMY_FOCA, 0, 0);
            LogMessage("Enemigo Foca generado para todos los clientes");
            break;
        case ID_BTN_ENEMY2:
            SpawnEnemyForAllClients(ENEMY_BIRD, 0, 0);
            LogMessage("Enemigo Ave generado para todos los clientes");
            break;
        case ID_BTN_ENEMY3:
            ReadIceSpawnCoordinates();
            SpawnEnemyForAllClients(ENEMY_ICE, iceSpawnRow, iceSpawnCol);
            {
                char logMsg[256];
                sprintf(logMsg, "Enemigo Hielo generado en posición (%d, %d) para todos los clientes", 
                        iceSpawnCol, iceSpawnRow);
                LogMessage(logMsg);
            }
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
        }
        break;

    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void LogMessage(const char* message) {
    HWND hListBox = GetDlgItem(hwnd, ID_LISTBOX);
    if (hListBox) {
        SendMessage(hListBox, LB_ADDSTRING, 0, (LPARAM)message);
        SendMessage(hListBox, LB_SETTOPINDEX, SendMessage(hListBox, LB_GETCOUNT, 0, 0) - 1, 0);
    }
}

void ReadIceSpawnCoordinates() {
    char buffer[10];
    
    GetWindowText(GetDlgItem(hwnd, ID_EDIT_ICE_ROW), buffer, sizeof(buffer));
    iceSpawnRow = atoi(buffer);
    
    if (iceSpawnRow < 0) iceSpawnRow = 0;
    if (iceSpawnRow >= MATRIX_HEIGHT) iceSpawnRow = MATRIX_HEIGHT - 1;
    
    GetWindowText(GetDlgItem(hwnd, ID_EDIT_ICE_COL), buffer, sizeof(buffer));
    iceSpawnCol = atoi(buffer);
    
    if (iceSpawnCol < 0) iceSpawnCol = 0;
    if (iceSpawnCol >= MATRIX_WIDTH) iceSpawnCol = MATRIX_WIDTH - 1;
}