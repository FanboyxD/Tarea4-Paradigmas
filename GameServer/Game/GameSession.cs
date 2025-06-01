using System;
using System.Collections.Generic;
using System.Linq;
using PlatformGameServer.Networking;

namespace PlatformGameServer.Game
{
    public class GameSession
    {
        public string SessionId { get; private set; }
        public Player Player1 { get; private set; }
        public Player Player2 { get; private set; }
        public bool IsPlayer2Active { get; private set; } = false;
        public int[,] GameMap { get; private set; }
        public Dictionary<string, DateTime> DestroyedBlocks { get; private set; }
        public ClientHandler Client { get; private set; }
        public bool IsActive { get; set; }
        public List<Enemy> Enemies { get; private set; }

        private const int ATTACK_COOLDOWN = 500;
        private const int ATTACK_RANGE = 1;
        private const int BLOCK_REGENERATION_TIME = 3000;
        private const float ENEMY_COLLISION_DISTANCE = 1.0f;

        private static readonly int[,] baseMap = new int[,] {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        public GameSession(ClientHandler client)
        {
            SessionId = Guid.NewGuid().ToString();
            Client = client;
            Player1 = new Player(1);
            Player2 = new Player(2);
            Player2.SetPosition(17.0f, 31.0f);
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
            
            bool anyPlayerAlive = Player1.IsAlive || (IsPlayer2Active && Player2.IsAlive);
            
            if (anyPlayerAlive)
            {
                Player1.Update(GameMap);
                if (IsPlayer2Active)
                {
                    Player2.Update(GameMap);
                }
                
                UpdateEnemies();
                CheckEnemyCollisions();
                RegenerateBlocks();
                Client.SendGameStateToClient();
            }
            else
            {
                Client.SendGameOverToClient();
            }
        }

        public void HandlePlayerInput(string input)
        {
            if (input.ToUpper() == "RESTART")
            {
                RestartGame();
                return;
            }
            
            if ((input.ToUpper() == "LEFT" || input.ToUpper() == "RIGHT" || input.ToUpper() == "JUMP" || input.ToUpper() == "P") && !IsPlayer2Active)
            {
                IsPlayer2Active = true;
                Player2.ResetPosition();
                Player2.SetPosition(17.0f, 31.0f);
                Console.WriteLine("¡Player 2 activado!");
                Client.SendGameStateToClient();
            }
            
            if (!IsActive) return;
            
            if (Player1.IsAlive)
            {
                switch (input.ToUpper())
                {
                    case "A":
                        Player1.HandleInput("LEFT");
                        break;
                    case "D":
                        Player1.HandleInput("RIGHT");
                        break;
                    case "W":
                        Player1.HandleInput("JUMP");
                        break;
                    case "X":
                        PerformAttack(Player1);
                        break;
                }
            }
            
            if (IsPlayer2Active && Player2.IsAlive)
            {
                switch (input.ToUpper())
                {
                    case "LEFT":
                    case "RIGHT":
                    case "JUMP":
                        Player2.HandleInput(input);
                        break;
                    case "P":
                        PerformAttack(Player2);
                        break;
                }
            }
        }

        // Método actualizado para enemigos terrestres - ahora pueden aparecer desde izquierda o derecha
        public void SpawnEnemyNearPlayer()
        {
            List<int> floorsToSpawn = new List<int>();
            
            if (Player1.IsAlive)
            {
                int player1FloorY = (int)Math.Round(Player1.Y);
                floorsToSpawn.Add(player1FloorY);
            }
            
            if (IsPlayer2Active && Player2.IsAlive)
            {
                int player2FloorY = (int)Math.Round(Player2.Y);
                if (!floorsToSpawn.Contains(player2FloorY))
                {
                    floorsToSpawn.Add(player2FloorY);
                }
            }
            
            foreach (int floorY in floorsToSpawn)
            {
                for (int attempts = 0; attempts < 10; attempts++)
                {
                    Random rand = new Random();
                    
                    // Decidir aleatoriamente si aparece desde la izquierda o la derecha
                    bool spawnFromLeft = rand.Next(0, 2) == 0;
                    int spawnX = spawnFromLeft ? 1 : GameMap.GetLength(1) - 2;
                    
                    if (GameMap[floorY, spawnX] == 0 &&
                        floorY + 1 < GameMap.GetLength(0) &&
                        GameMap[floorY + 1, spawnX] == 1)
                    {
                        Enemies.Add(new GroundEnemy(spawnX, floorY, spawnFromLeft));
                        Console.WriteLine($"GroundEnemy generado desde {(spawnFromLeft ? "izquierda" : "derecha")} en ({spawnX}, {floorY})");
                        break;
                    }
                }
            }
        }

        // Método actualizado para generar aves - ahora se mueven aleatoriamente
        public void SpawnBirdEnemy()
        {
            Random rand = new Random();
            // Las aves pueden aparecer desde cualquier lado del mapa
            bool spawnFromLeft = rand.Next(0, 2) == 0;
            int spawnY = rand.Next(1, GameMap.GetLength(0) - 1);
            float spawnX = spawnFromLeft ? 1.0f : GameMap.GetLength(1) - 2.0f;
            
            Enemies.Add(new BirdEnemy(spawnX, spawnY));
            Console.WriteLine($"Ave enemiga generada en posición ({spawnX}, {spawnY}) desde {(spawnFromLeft ? "izquierda" : "derecha")}");
        }

        // Método para generar hielos - sin cambios, pero ahora atraviesan pisos
        public void SpawnIceEnemy(int floorY, int positionX)
        {
            // Validar que la posición sea válida
            if (floorY < 0 || floorY >= GameMap.GetLength(0) || 
                positionX < 1 || positionX >= GameMap.GetLength(1) - 1)
            {
                Console.WriteLine("Posición inválida para generar hielo");
                return;
            }

            // Los hielos pueden generarse en cualquier posición (ya no necesitan verificar si está libre)
            Enemies.Add(new IceEnemy(positionX, floorY));
            Console.WriteLine($"Hielo enemigo generado en piso {floorY}, posición {positionX}");
        }

        // Método sobrecargado para generar hielo en posición aleatoria de un piso específico
        public void SpawnIceEnemy(int floorY)
        {
            Random rand = new Random();
            int randomX = rand.Next(1, GameMap.GetLength(1) - 1);
            SpawnIceEnemy(floorY, randomX);
        }

        // Método modificado para verificar colisiones - ahora considera si el enemigo puede hacer daño
        private void CheckEnemyCollisions()
        {
            foreach (var enemy in Enemies.Where(e => e.IsActive))
            {
                if (Player1.IsAlive)
                {
                    float distance1 = (float)Math.Sqrt(
                        Math.Pow(Player1.X - enemy.X, 2) + 
                        Math.Pow(Player1.Y - enemy.Y, 2)
                    );
                    
                    // Solo hace daño si el enemigo puede causar daño
                    if (distance1 <= ENEMY_COLLISION_DISTANCE && Player1.CanTakeDamage() && enemy.CanCauseDamage)
                    {
                        Player1.TakeDamage();
                        float pushDirection = Player1.X > enemy.X ? 1.0f : -1.0f;
                        Player1.VelocityX = pushDirection * 0.5f;
                        
                        // Los hielos se destruyen al tocar a un jugador
                        if (enemy.Type == EnemyType.Ice)
                        {
                            enemy.IsActive = false;
                        }
                        
                        if (!Player1.IsAlive)
                        {
                            Console.WriteLine("¡El Player 1 ha perdido todas sus vidas!");
                        }
                    }
                }
                
                if (IsPlayer2Active && Player2.IsAlive)
                {
                    float distance2 = (float)Math.Sqrt(
                        Math.Pow(Player2.X - enemy.X, 2) + 
                        Math.Pow(Player2.Y - enemy.Y, 2)
                    );
                    
                    // Solo hace daño si el enemigo puede causar daño
                    if (distance2 <= ENEMY_COLLISION_DISTANCE && Player2.CanTakeDamage() && enemy.CanCauseDamage)
                    {
                        Player2.TakeDamage();
                        float pushDirection = Player2.X > enemy.X ? 1.0f : -1.0f;
                        Player2.VelocityX = pushDirection * 0.5f;
                        
                        // Los hielos se destruyen al tocar a un jugador
                        if (enemy.Type == EnemyType.Ice)
                        {
                            enemy.IsActive = false;
                        }
                        
                        if (!Player2.IsAlive)
                        {
                            Console.WriteLine("¡El Player 2 ha perdido todas sus vidas!");
                        }
                    }
                }
            }
        }

        private void UpdateEnemies()
        {
            foreach (var enemy in Enemies.ToList())
            {
                enemy.Update(GameMap, Player1, Player2, IsPlayer2Active);

                // Remover enemigos inactivos o que han expirado
                if (!enemy.IsActive || (DateTime.Now - enemy.CreationTime).TotalSeconds > 30)
                {
                    Enemies.Remove(enemy);
                }
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

        private void PerformAttack(Player player)
        {
            if ((DateTime.Now - player.LastAttackTime).TotalMilliseconds < ATTACK_COOLDOWN)
                return;

            player.IsAttacking = true;
            player.LastAttackTime = DateTime.Now;
            DestroyBlocksInRadius(player);
            AttackEnemies(player);
        }

        // Método modificado para atacar enemigos - ahora maneja diferentes comportamientos según el tipo
        private void AttackEnemies(Player player)
        {
            var enemiesToRemove = new List<Enemy>();
            
            foreach (var enemy in Enemies.Where(e => e.IsActive))
            {
                float distance = (float)Math.Sqrt(
                    Math.Pow(player.X - enemy.X, 2) + 
                    Math.Pow(player.Y - enemy.Y, 2)
                );
                
                if (distance <= ATTACK_RANGE + 0.5f)
                {
                    switch (enemy.Type)
                    {
                        case EnemyType.Ground:
                            // Los enemigos terrestres no se destruyen, solo se retiran
                            enemy.OnAttacked();
                            Console.WriteLine($"GroundEnemy atacado por Player {player.PlayerId} - Iniciando retirada");
                            break;
                            
                        case EnemyType.Bird:
                            // Las aves se destruyen al ser atacadas
                            enemy.IsActive = false;
                            enemiesToRemove.Add(enemy);
                            Console.WriteLine($"BirdEnemy eliminado por ataque del Player {player.PlayerId}");
                            break;
                            
                        case EnemyType.Ice:
                            // Los hielos se destruyen al ser atacados
                            enemy.IsActive = false;
                            enemiesToRemove.Add(enemy);
                            Console.WriteLine($"IceEnemy eliminado por ataque del Player {player.PlayerId}");
                            break;
                    }
                }
            }
            
            foreach (var enemy in enemiesToRemove)
            {
                Enemies.Remove(enemy);
            }
        }

        private void DestroyBlocksInRadius(Player player)
        {
            int mapWidth = GameMap.GetLength(1);
            int mapHeight = GameMap.GetLength(0);
            int playerTileX = (int)Math.Round(player.X);
            int playerTileY = (int)Math.Round(player.Y);

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
            IsActive = true;
            Player1.Lives = 3;
            Player1.IsInvulnerable = false;
            Player1.LastDamageTime = DateTime.MinValue;
            Player1.ResetPosition();
            Player1.SetPosition(2.0f, 31.0f);
            
            if (IsPlayer2Active)
            {
                Player2.Lives = 3;
                Player2.IsInvulnerable = false;
                Player2.LastDamageTime = DateTime.MinValue;
                Player2.ResetPosition();
                Player2.SetPosition(17.0f, 31.0f);
            }
            
            Enemies.Clear();
            DestroyedBlocks.Clear();
            GameMap = CloneMap(baseMap);
            Console.WriteLine($"Juego reiniciado para sesión: {SessionId}");
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