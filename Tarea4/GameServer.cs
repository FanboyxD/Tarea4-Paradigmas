using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

// Singleton para el servidor del juego
public sealed class GameServer
{
    private static GameServer instance = null;
    private static readonly object padlock = new object();
    
    private Socket serverSocket;
    private List<ClientHandler> clients;
    private int[,] gameMap;
    private const int MAP_WIDTH = 20;
    private const int MAP_HEIGHT = 15;
    private bool isRunning;
    
    private GameServer()
    {
        clients = new List<ClientHandler>();
        InitializeGameMap();
    }
    
    public static GameServer Instance
    {
        get
        {
            lock (padlock)
            {
                if (instance == null)
                    instance = new GameServer();
                return instance;
            }
        }
    }
    
    // Inicializar la matriz del mapa (0 = vacío, 1 = plataforma)
    private void InitializeGameMap()
    {
        gameMap = new int[MAP_HEIGHT, MAP_WIDTH];
        
        // Crear un mapa de ejemplo con plataformas
        // Suelo principal
        for (int x = 0; x < MAP_WIDTH; x++)
        {
            gameMap[MAP_HEIGHT - 1, x] = 1;
        }
        
        // Plataforma izquierda
        for (int x = 2; x <= 5; x++)
        {
            gameMap[MAP_HEIGHT - 4, x] = 1;
        }
        
        // Plataforma centro
        for (int x = 8; x <= 11; x++)
        {
            gameMap[MAP_HEIGHT - 6, x] = 1;
        }
        
        // Plataforma derecha
        for (int x = 14; x <= 17; x++)
        {
            gameMap[MAP_HEIGHT - 4, x] = 1;
        }
        
        // Plataforma superior centro
        for (int x = 9; x <= 10; x++)
        {
            gameMap[MAP_HEIGHT - 9, x] = 1;
        }
        
        // Plataformas laterales superiores
        gameMap[MAP_HEIGHT - 7, 3] = 1;
        gameMap[MAP_HEIGHT - 7, 4] = 1;
        gameMap[MAP_HEIGHT - 7, 15] = 1;
        gameMap[MAP_HEIGHT - 7, 16] = 1;
    }
    
    public void StartServer(int port = 8080)
    {
        try
        {
            serverSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            IPEndPoint endPoint = new IPEndPoint(IPAddress.Any, port);
            serverSocket.Bind(endPoint);
            serverSocket.Listen(10);
            
            isRunning = true;
            Console.WriteLine($"Servidor iniciado en puerto {port}");
            Console.WriteLine("Mapa del juego:");
            PrintGameMap();
            Console.WriteLine("Esperando conexiones...");
            
            // Hilo para aceptar clientes
            Thread acceptThread = new Thread(AcceptClients);
            acceptThread.Start();
            
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error al iniciar servidor: {ex.Message}");
        }
    }
    
    private void AcceptClients()
    {
        while (isRunning)
        {
            try
            {
                Socket clientSocket = serverSocket.Accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                
                lock (clients)
                {
                    clients.Add(client);
                }
                
                Console.WriteLine($"Cliente conectado. Total clientes: {clients.Count}");
                
                // Enviar mapa al cliente recién conectado
                SendMapToClient(client);
                
            }
            catch (Exception ex)
            {
                if (isRunning)
                    Console.WriteLine($"Error aceptando cliente: {ex.Message}");
            }
        }
    }
    
    private void SendMapToClient(ClientHandler client)
    {
        try
        {
            StringBuilder mapData = new StringBuilder("MAP:");
            mapData.Append($"{MAP_WIDTH},{MAP_HEIGHT}:");
            
            for (int y = 0; y < MAP_HEIGHT; y++)
            {
                for (int x = 0; x < MAP_WIDTH; x++)
                {
                    mapData.Append(gameMap[y, x]);
                    if (x < MAP_WIDTH - 1) mapData.Append(",");
                }
                if (y < MAP_HEIGHT - 1) mapData.Append(";");
            }
            
            client.SendMessage(mapData.ToString());
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error enviando mapa: {ex.Message}");
        }
    }
    
    public void BroadcastMessage(string message, ClientHandler sender = null)
    {
        lock (clients)
        {
            for (int i = clients.Count - 1; i >= 0; i--)
            {
                if (clients[i] != sender && clients[i].IsConnected)
                {
                    clients[i].SendMessage(message);
                }
                else if (!clients[i].IsConnected)
                {
                    clients.RemoveAt(i);
                }
            }
        }
    }
    
    public void RemoveClient(ClientHandler client)
    {
        lock (clients)
        {
            clients.Remove(client);
            Console.WriteLine($"Cliente desconectado. Total clientes: {clients.Count}");
        }
    }
    
    private void PrintGameMap()
    {
        for (int y = 0; y < MAP_HEIGHT; y++)
        {
            for (int x = 0; x < MAP_WIDTH; x++)
            {
                Console.Write(gameMap[y, x] == 1 ? "█" : " ");
            }
            Console.WriteLine();
        }
    }
    
    public void StopServer()
    {
        isRunning = false;
        serverSocket?.Close();
        
        lock (clients)
        {
            foreach (var client in clients)
            {
                client.Disconnect();
            }
            clients.Clear();
        }
    }
}

// Clase para manejar cada cliente conectado
public class ClientHandler
{
    private Socket clientSocket;
    private GameServer server;
    private Thread receiveThread;
    private bool isConnected;
    
    public bool IsConnected => isConnected;
    
    public ClientHandler(Socket socket, GameServer gameServer)
    {
        clientSocket = socket;
        server = gameServer;
        isConnected = true;
        
        receiveThread = new Thread(ReceiveMessages);
        receiveThread.Start();
    }
    
    private void ReceiveMessages()
    {
        byte[] buffer = new byte[1024];
        
        while (isConnected)
        {
            try
            {
                int bytesReceived = clientSocket.Receive(buffer);
                if (bytesReceived == 0)
                {
                    break;
                }
                
                string message = Encoding.UTF8.GetString(buffer, 0, bytesReceived);
                Console.WriteLine($"Mensaje recibido: {message}");
                
                // Procesar mensajes del cliente (movimiento del jugador)
                ProcessClientMessage(message);
                
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error recibiendo mensaje: {ex.Message}");
                break;
            }
        }
        
        Disconnect();
    }
    
    private void ProcessClientMessage(string message)
    {
        // Procesar comandos como "MOVE:LEFT", "MOVE:RIGHT", "JUMP", etc.
        if (message.StartsWith("PLAYER:"))
        {
            // Reenviar posición del jugador a otros clientes
            server.BroadcastMessage(message, this);
        }
    }
    
    public void SendMessage(string message)
    {
        try
        {
            if (isConnected)
            {
                byte[] data = Encoding.UTF8.GetBytes(message);
                clientSocket.Send(data);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error enviando mensaje: {ex.Message}");
            Disconnect();
        }
    }
    
    public void Disconnect()
    {
        if (isConnected)
        {
            isConnected = false;
            clientSocket?.Close();
            server.RemoveClient(this);
        }
    }
}

// Programa principal
class Program
{
    static void Main(string[] args)
    {
        GameServer server = GameServer.Instance;
        server.StartServer(8080);
        
        Console.WriteLine("Presiona cualquier tecla para detener el servidor...");
        Console.ReadKey();
        
        server.StopServer();
    }
}