using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using PlatformGameServer.Game;
using System.Text;

namespace PlatformGameServer.Networking
{
    public class GameServer
    {
        // Instancia única del servidor (thread-safe)
        private static readonly Lazy<GameServer> _instance = new Lazy<GameServer>(() => new GameServer());
        
        // Propiedad pública para obtener la instancia única
        public static GameServer Instance => _instance.Value;

        private TcpListener tcpListener;
        private Thread? tcpListenerThread;
        private bool isListening = false;
        private Dictionary<string, GameSession> gameSessions = new();
        private Thread? gameLoopThread;
        private readonly object lockObject = new object(); // Para thread safety
        private const int MAX_CLIENTS = 2;

        // Constructor privado - previene la creación de instancias externas
        private GameServer()
        {
            tcpListener = new TcpListener(IPAddress.Any, 8888);
        }

        public IEnumerable<GameSession> GetActiveSessions()
        {
            lock (lockObject)
            {
                return gameSessions.Values.Where(s => s.IsActive).ToList();
            }
        }

        public void Start()
        {
            lock (lockObject)
            {
                // Prevenir múltiples inicios
                if (isListening)
                {
                    Console.WriteLine("El servidor ya está en ejecución.");
                    return;
                }

                tcpListenerThread = new Thread(ListenForClients)
                {
                    IsBackground = true
                };
                tcpListenerThread.Start();
                isListening = true;

                gameLoopThread = new Thread(MainGameLoop)
                {
                    IsBackground = true
                };
                gameLoopThread.Start();

                Console.WriteLine("Servidor iniciado en puerto 8888");
                Console.WriteLine("Esperando conexiones de clientes...");
            }
        }

        private void ListenForClients()
        {
            tcpListener.Start();

            while (isListening)
            {
                try
                {
                    TcpClient client = tcpListener.AcceptTcpClient();

                    lock (lockObject)
                    {
                        if (gameSessions.Count >= MAX_CLIENTS)
                        {
                            Console.WriteLine("Conexión rechazada: límite de clientes alcanzado.");
                            using NetworkStream stream = client.GetStream();
                            string jsonError = "{\"type\":\"ERROR\",\"message\":\"Servidor lleno. Intente más tarde.\"}";
                            byte[] rejectionMessage = Encoding.UTF8.GetBytes(jsonError + "\n");
                            stream.Write(rejectionMessage, 0, rejectionMessage.Length);
                            client.Close();
                            continue;
                        }
                    }

                    ClientHandler clientHandler = new ClientHandler(client);
                    GameSession gameSession = new GameSession(clientHandler);

                    lock (lockObject)
                    {
                        gameSessions[gameSession.SessionId] = gameSession;
                    }
   
                    clientHandler.SetGameSession(gameSession);

                    Thread clientThread = new Thread(clientHandler.HandleClient)
                    {
                        IsBackground = true
                    };
                    clientThread.Start();

                    Console.WriteLine($"Cliente conectado! Nueva partida creada: {gameSession.SessionId}");
                    clientHandler.SendMapToClient();
                }
                catch (Exception e)
                {
                    Console.WriteLine("Error al aceptar cliente: " + e.Message);
                }
            }
        }

        private void MainGameLoop()
        {
            while (isListening)
            {
                var sessionsToRemove = new List<string>();

                lock (lockObject)
                {
                    foreach (var kvp in gameSessions)
                    {
                        var session = kvp.Value;
                        if (session.IsActive)
                        {
                            try
                            {
                                session.Update();
                            }
                            catch (Exception e)
                            {
                                Console.WriteLine($"Error actualizando sesión {session.SessionId}: {e.Message}");
                                sessionsToRemove.Add(kvp.Key);
                            }
                        }
                        else
                        {
                            sessionsToRemove.Add(kvp.Key);
                        }
                    }

                    foreach (var sessionId in sessionsToRemove)
                    {
                        gameSessions.Remove(sessionId);
                    }
                }

                Thread.Sleep(16);
            }
        }

        public void RemoveSession(string sessionId)
        {
            lock (lockObject)
            {
                if (gameSessions.ContainsKey(sessionId))
                {
                    gameSessions[sessionId].Stop();
                    gameSessions.Remove(sessionId);
                    Console.WriteLine($"Sesión removida: {sessionId}");
                }
            }
        }

        public void Stop()
        {
            lock (lockObject)
            {
                if (!isListening)
                {
                    Console.WriteLine("El servidor ya está detenido.");
                    return;
                }

                isListening = false;
                foreach (var session in gameSessions.Values)
                {
                    session.Stop();
                }
                gameSessions.Clear();
                tcpListener?.Stop();
                
                Console.WriteLine("Servidor detenido.");
            }
        }

        // Método para obtener estadísticas del servidor
        public ServerStats GetStats()
        {
            lock (lockObject)
            {
                return new ServerStats
                {
                    IsRunning = isListening,
                    ActiveSessions = gameSessions.Count(kvp => kvp.Value.IsActive),
                    TotalSessions = gameSessions.Count
                };
            }
        }
    }

    // Struct auxiliar para estadísticas
    public struct ServerStats
    {
        public bool IsRunning { get; set; }
        public int ActiveSessions { get; set; }
        public int TotalSessions { get; set; }
    }
}