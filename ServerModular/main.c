#include <windows.h>
#include <stdio.h>
#include "ui.h"
#include "network.h"
#include "game_logic.h"

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    (void)hPrevInstance;
    (void)lpCmdLine;
    
    // Inicializar red
    InitializeNetwork();
    
    // Inicializar interfaz de usuario
    if (!InitializeUI(hInstance, nCmdShow)) {
        LogMessage("Error inicializando interfaz de usuario");
        CleanupNetwork();
        return -1;
    }
    
    // Iniciar servidor en hilo separado
    if (!StartServer()) {
        LogMessage("Error iniciando servidor");
        CleanupNetwork();
        return -1;
    }
    
    // Crear hilo para manejar clientes
    HANDLE hClientThread = CreateThread(NULL, 0, ClientThread, NULL, 0, NULL);
    if (hClientThread == NULL) {
        LogMessage("Error creando hilo de clientes");
        CleanupNetwork();
        return -1;
    }
    
    // Bucle principal de mensajes
    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    
    // Limpiar recursos
    CloseHandle(hClientThread);
    CleanupNetwork();
    
    return (int)msg.wParam;
}