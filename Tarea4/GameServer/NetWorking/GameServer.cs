using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using PlatformGameServer.Game;

namespace PlatformGameServer.Networking
{
    public class GameServer
    {
        private TcpListener tcpListener;
        private Thread? tcpListenerThread;
        private bool isListening = false;
        private Dictionary<string, GameSession> gameSessions = new();
        private Thread? gameLoopThread;

        public GameServer()
        {
            tcpListener = new TcpListener(IPAddress.Any, 8888);
        }

        public IEnumerable<GameSession> GetActiveSessions()
        {
            return gameSessions.Values.Where(s => s.IsActive);
        }

        public void Start()
        {
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

        private void ListenForClients()
        {
            tcpListener.Start();

            while (isListening)
            {
                try
                {
                    TcpClient client = tcpListener.AcceptTcpClient();
                    ClientHandler clientHandler = new ClientHandler(client, this);

                    GameSession gameSession = new GameSession(clientHandler);
                    gameSessions[gameSession.SessionId] = gameSession;
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

                Thread.Sleep(16);
            }
        }

        public void RemoveSession(string sessionId)
        {
            if (gameSessions.ContainsKey(sessionId))
            {
                gameSessions[sessionId].Stop();
                gameSessions.Remove(sessionId);
                Console.WriteLine($"Sesión removida: {sessionId}");
            }
        }

        public void Stop()
        {
            isListening = false;
            foreach (var session in gameSessions.Values)
            {
                session.Stop();
            }
            gameSessions.Clear();
            tcpListener?.Stop();
        }
    }
}