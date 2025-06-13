#ifndef UI_H
#define UI_H

#include <windows.h>
#include "game_logic.h"

extern HWND hwnd;
extern int iceSpawnRow;
extern int iceSpawnCol;

int InitializeUI(HINSTANCE hInstance, int nCmdShow);
LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
void LogMessage(const char* message);
void ReadIceSpawnCoordinates();

#endif