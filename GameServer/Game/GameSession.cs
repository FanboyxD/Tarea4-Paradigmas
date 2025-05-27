using System;
using System.Collections.Generic;
using System.Linq;
using PlatformGameServer.Networking;

namespace PlatformGameServer.Game
{
    public class GameSession
    {
        public string SessionId { get; private set; }
        public Player Player { get; private set; }
        public int[,] GameMap { get; private set; }
        public Dictionary<string, DateTime> DestroyedBlocks { get; private set; }
        public ClientHandler Client { get; private set; }
        public bool IsActive { get; set; }
        public List<Enemy> Enemies { get; private set; }

        private const float GRAVITY = 0.5f;
        private const float JUMP_FORCE = -2.0f;
        private const float MOVE_SPEED = 0.3f;
        private const int ATTACK_COOLDOWN = 500;
        private const int ATTACK_RANGE = 1;
        private const int BLOCK_REGENERATION_TIME = 3000;
        private const float ENEMY_COLLISION_DISTANCE = 1.0f; // Distancia para detectar colisión con enemigos

        private static readonly int[,] baseMap = new int[,] {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        public GameSession(ClientHandler client)
        {
            SessionId = Guid.NewGuid().ToString();
            Client = client;
            Player = new Player();
            DestroyedBlocks = new Dictionary<string, DateTime>();
            IsActive = true;
            Enemies = new List<Enemy>();
            GameMap = CloneMap(baseMap);
            Console.WriteLine($"Nueva sesión de juego creada: {SessionId}");
        }

        private int[,] CloneMap(int[,] original)
        {
            int rows = original.GetLength(0);
            int cols = original.GetLength(1);
            int[,] clone = new int[rows, cols];
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                    clone[i, j] = original[i, j];
            return clone;
        }

        public void Update()
        {
            if (!IsActive) return;
            
            if (Player.IsAlive)
            {
                UpdatePlayer();
                UpdateEnemies();
                CheckEnemyCollisions();
                RegenerateBlocks();
                Client.SendGameStateToClient();
            }
            else
            {
                // Enviar mensaje de game over
                Client.SendGameOverToClient();
                // NO llamar Stop() aquí para permitir restart
            }
        }

        public void HandlePlayerInput(string input)
        {
            // Permitir RESTART incluso cuando el juego no está activo o el jugador está muerto
            if (input.ToUpper() == "RESTART")
            {
                RestartGame();
                return;
            }
            
            // Para otros inputs, verificar que el juego esté activo y el jugador vivo
            if (!IsActive || !Player.IsAlive) return;
            
            switch (input.ToUpper())
            {
                case "LEFT": Player.VelocityX = -MOVE_SPEED; break;
                case "RIGHT": Player.VelocityX = MOVE_SPEED; break;
                case "JUMP":
                    if (Player.IsOnGround && !Player.IsJumping)
                    {
                        Player.VelocityY = JUMP_FORCE;
                        Player.IsOnGround = false;
                        Player.IsJumping = true;
                    }
                    break;
                case "ATTACK": PerformAttack(); break;
            }
        }

        public void SpawnEnemyNearPlayer()
        {
            int playerFloorY = (int)Math.Round(Player.Y);
            for (int attempts = 0; attempts < 10; attempts++)
            {
                Random rand = new Random();
                int spawnX = rand.Next(1, GameMap.GetLength(1) - 1);
                if (GameMap[playerFloorY, spawnX] == 0 &&
                    playerFloorY + 1 < GameMap.GetLength(0) &&
                    GameMap[playerFloorY + 1, spawnX] == 1)
                {
                    Enemies.Add(new Enemy(spawnX, playerFloorY));
                    break;
                }
            }
        }

        private void UpdatePlayer()
        {
            Player.UpdateInvulnerability();
            
            if (!Player.IsOnGround) Player.VelocityY += GRAVITY;
            Player.X += Player.VelocityX;
            Player.Y += Player.VelocityY;
            CheckCollisions();
            Player.VelocityX *= Player.IsOnGround ? 0.8f : 0.95f;
            if (Player.IsAttacking && (DateTime.Now - Player.LastAttackTime).TotalMilliseconds > 200)
                Player.IsAttacking = false;
        }

        private void CheckCollisions()
        {
            int mapWidth = GameMap.GetLength(1);
            int mapHeight = GameMap.GetLength(0);
            if (Player.X < 0) Player.X = 0;
            if (Player.X >= mapWidth) Player.X = mapWidth - 0.1f;
            if (Player.Y < 0) Player.Y = 0;
            if (Player.Y >= mapHeight) Player.Y = mapHeight - 0.1f;

            int playerTileX = Math.Max(0, Math.Min((int)Math.Round(Player.X), mapWidth - 1));
            int playerTileY = Math.Max(0, Math.Min((int)Math.Round(Player.Y), mapHeight - 1));
            Player.IsOnGround = false;

            if (GameMap[playerTileY, playerTileX] == 1)
            {
                if (Player.VelocityY < 0)
                {
                    Player.Y = playerTileY + 1;
                    Player.VelocityY = 0;
                }
                else if (Player.VelocityY > 0)
                {
                    Player.Y = playerTileY - 1;
                    Player.VelocityY = 0;
                    Player.IsOnGround = true;
                    Player.IsJumping = false;
                }
            }
            // Verificaciones de colisión horizontal (muros laterales)
            if (GameMap[playerTileY, playerTileX] == 1)
            {
                // Si hay colisión horizontal, empujar al jugador fuera del muro
                if (Player.VelocityX > 0) // Moviéndose hacia la derecha
                {
                    Player.X = playerTileX - 1; // Empujar hacia la izquierda
                }
                else if (Player.VelocityX < 0) // Moviéndose hacia la izquierda
                {
                    Player.X = playerTileX + 1; // Empujar hacia la derecha
                }
                Player.VelocityX = 0; // Detener el movimiento horizontal
            }


            if (!Player.IsOnGround && playerTileY + 1 < mapHeight && GameMap[playerTileY + 1, playerTileX] == 1 && Player.VelocityY >= 0)
            {
                Player.Y = playerTileY;
                Player.VelocityY = 0;
                Player.IsOnGround = true;
                Player.IsJumping = false;
            }

            if (playerTileY > 0 && GameMap[playerTileY - 1, playerTileX] == 1 && Player.VelocityY < 0)
            {
                Player.Y = playerTileY;
                Player.VelocityY = 0;
            }
        }

        private void CheckEnemyCollisions()
        {
            foreach (var enemy in Enemies.Where(e => e.IsActive))
            {
                float distance = (float)Math.Sqrt(
                    Math.Pow(Player.X - enemy.X, 2) + 
                    Math.Pow(Player.Y - enemy.Y, 2)
                );
                
                if (distance <= ENEMY_COLLISION_DISTANCE && Player.CanTakeDamage())
                {
                    Player.TakeDamage();
                    
                    // Empujar al jugador ligeramente para simular el impacto
                    float pushDirection = Player.X > enemy.X ? 1.0f : -1.0f;
                    Player.VelocityX = pushDirection * 0.5f;
                    
                    if (!Player.IsAlive)
                    {
                        Console.WriteLine("¡El jugador ha perdido todas sus vidas!");
                        break;
                    }
                }
            }
        }

        private void UpdateEnemies()
        {
            foreach (var enemy in Enemies.ToList())
            {
                enemy.X += enemy.VelocityX;
                int enemyTileX = (int)Math.Round(enemy.X);
                int enemyTileY = (int)Math.Round(enemy.Y);

                if (enemyTileX <= 0 || enemyTileX >= GameMap.GetLength(1) - 1 ||
                    GameMap[enemyTileY, enemyTileX + (enemy.VelocityX > 0 ? 1 : -1)] == 1)
                {
                    enemy.VelocityX *= -1;
                }

                if ((DateTime.Now - enemy.CreationTime).TotalSeconds > 30)
                    Enemies.Remove(enemy);
            }
        }

        private void RegenerateBlocks()
        {
            var blocksToRegenerate = DestroyedBlocks
                .Where(kvp => (DateTime.Now - kvp.Value).TotalMilliseconds >= BLOCK_REGENERATION_TIME)
                .Select(kvp => kvp.Key).ToList();

            foreach (var blockKey in blocksToRegenerate)
            {
                var coords = blockKey.Split(',');
                int y = int.Parse(coords[0]);
                int x = int.Parse(coords[1]);
                GameMap[y, x] = 1;
                DestroyedBlocks.Remove(blockKey);
                Client.SendMapToClient();
            }
        }

        private void PerformAttack()
        {
            if ((DateTime.Now - Player.LastAttackTime).TotalMilliseconds < ATTACK_COOLDOWN)
                return;

            Player.IsAttacking = true;
            Player.LastAttackTime = DateTime.Now;
            DestroyBlocksInRadius();
            AttackEnemies();
        }

        private void AttackEnemies()
        {
            var enemiesToRemove = new List<Enemy>();
            
            foreach (var enemy in Enemies.Where(e => e.IsActive))
            {
                float distance = (float)Math.Sqrt(
                    Math.Pow(Player.X - enemy.X, 2) + 
                    Math.Pow(Player.Y - enemy.Y, 2)
                );
                
                if (distance <= ATTACK_RANGE + 0.5f) // Un poco más de rango para atacar
                {
                    enemy.IsActive = false;
                    enemiesToRemove.Add(enemy);
                    Console.WriteLine($"Enemigo {enemy.Id} eliminado por ataque del jugador");
                }
            }
            
            foreach (var enemy in enemiesToRemove)
            {
                Enemies.Remove(enemy);
            }
        }

        private void DestroyBlocksInRadius()
        {
            int mapWidth = GameMap.GetLength(1);
            int mapHeight = GameMap.GetLength(0);
            int playerTileX = (int)Math.Round(Player.X);
            int playerTileY = (int)Math.Round(Player.Y);

            bool blocksDestroyed = false;

            for (int dy = -ATTACK_RANGE; dy <= 0; dy++)
            {
                for (int dx = -ATTACK_RANGE; dx <= ATTACK_RANGE; dx++)
                {
                    double distance = Math.Sqrt(dx * dx + dy * dy);
                    if (distance <= ATTACK_RANGE)
                    {
                        int targetX = playerTileX + dx;
                        int targetY = playerTileY + dy;
                        if (targetX >= 0 && targetX < mapWidth && targetY >= 0 && targetY < mapHeight)
                        {
                            if (GameMap[targetY, targetX] == 1 &&
                                !(targetY == playerTileY + 1 && targetX == playerTileX) &&
                                targetX != 0 && targetX != mapWidth - 1)
                            {
                                GameMap[targetY, targetX] = 0;
                                DestroyedBlocks[$"{targetY},{targetX}"] = DateTime.Now;
                                blocksDestroyed = true;
                            }
                        }
                    }
                }
            }

            if (blocksDestroyed)
                Client.SendMapToClient();
        }

        private void RestartGame()
        {
            // Reactivar la sesión
            IsActive = true;
            
            // Reiniciar el jugador
            Player.Lives = 3;
            Player.IsInvulnerable = false;
            Player.LastDamageTime = DateTime.MinValue;
            Player.ResetPosition();
            
            // Limpiar enemigos y bloques destruidos
            Enemies.Clear();
            DestroyedBlocks.Clear();
            
            // Restaurar el mapa original
            GameMap = CloneMap(baseMap);
            
            Console.WriteLine($"Juego reiniciado para sesión: {SessionId}");
            
            // Enviar el mapa actualizado y el estado del jugador al cliente
            Client.SendMapToClient();
            Client.SendGameStateToClient();
        }

        public void Stop()
        {
            IsActive = false;
            Console.WriteLine($"Sesión de juego terminada: {SessionId}");
        }
    }
}