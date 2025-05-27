using System;
using System.Net.Sockets;
using System.Text;
using System.Text.Json;
using System.Linq;
using PlatformGameServer.Game;

namespace PlatformGameServer.Networking
{
    public class ClientHandler
    {
        private TcpClient tcpClient;
        private NetworkStream clientStream;
        private GameServer server;
        private GameSession? gameSession;

        public ClientHandler(TcpClient client, GameServer gameServer)
        {
            tcpClient = client;
            server = gameServer;
            clientStream = client.GetStream();
        }

        public void SetGameSession(GameSession session)
        {
            gameSession = session;
        }

        public void HandleClient()
        {
            byte[] message = new byte[4096];
            int bytesRead;

            while (true)
            {
                bytesRead = 0;

                try
                {
                    bytesRead = clientStream.Read(message, 0, 4096);
                }
                catch
                {
                    break;
                }

                if (bytesRead == 0)
                {
                    break;
                }

                string clientMessage = Encoding.UTF8.GetString(message, 0, bytesRead);
                Console.WriteLine($"Sesión {gameSession?.SessionId}: Mensaje del cliente: {clientMessage}");

                gameSession?.HandlePlayerInput(clientMessage.Trim());
            }

            if (gameSession != null)
            {
                server.RemoveSession(gameSession.SessionId);
            }

            tcpClient.Close();
        }

        public void SendMessage(string message)
        {
            try
            {
                if (tcpClient.Connected)
                {
                    byte[] data = Encoding.UTF8.GetBytes(message + "\n");
                    clientStream.Write(data, 0, data.Length);
                    clientStream.Flush();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine($"Error enviando mensaje: {e.Message}");
                if (gameSession != null)
                {
                    gameSession.IsActive = false;
                }
            }
        }

        // Communication methods moved from GameSession
        public void SendMapToClient()
        {
            if (gameSession == null) return;

            var gameMap = gameSession.GameMap;
            int mapHeight = gameMap.GetLength(0);
            int mapWidth = gameMap.GetLength(1);
            int[][] mapArray = new int[mapHeight][];
            
            for (int i = 0; i < mapHeight; i++)
            {
                mapArray[i] = new int[mapWidth];
                for (int j = 0; j < mapWidth; j++)
                    mapArray[i][j] = gameMap[i, j];
            }

            var mapData = new
            {
                type = "MAP",
                sessionId = gameSession.SessionId,
                map = mapArray,
                width = mapWidth,
                height = mapHeight
            };
            
            SendMessage(JsonSerializer.Serialize(mapData));
        }

        public void SendGameStateToClient()
        {
            if (gameSession == null) return;

            var gameState = new
            {
                type = "PLAYER_UPDATE",
                sessionId = gameSession.SessionId,
                player = new
                {
                    x = gameSession.Player.X,
                    y = gameSession.Player.Y,
                    isOnGround = gameSession.Player.IsOnGround,
                    isJumping = gameSession.Player.IsJumping,
                    isAttacking = gameSession.Player.IsAttacking,
                    lives = gameSession.Player.Lives,
                    isInvulnerable = gameSession.Player.IsInvulnerable,
                    isAlive = gameSession.Player.IsAlive
                },
                enemies = gameSession.Enemies.Select(e => new
                {
                    id = e.Id,
                    x = e.X,
                    y = e.Y,
                    isActive = e.IsActive
                }).ToArray()
            };
            
            SendMessage(JsonSerializer.Serialize(gameState));
        }

        public void SendGameOverToClient()
        {
            if (gameSession == null) return;

            var gameOverData = new
            {
                type = "GAME_OVER",
                sessionId = gameSession.SessionId,
                message = "¡Has perdido todas tus vidas! Presiona RESTART para jugar de nuevo."
            };
            
            SendMessage(JsonSerializer.Serialize(gameOverData));
        }
    }
}