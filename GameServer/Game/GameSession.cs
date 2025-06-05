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
        public static float SpeedMultiplier { get; private set; } = 1.0f;

        private const int ATTACK_COOLDOWN = 500;
        private const int ATTACK_RANGE = 1;
        private const int BLOCK_REGENERATION_TIME = 3000;
        private const float ENEMY_COLLISION_DISTANCE = 1.0f;
        public List<Fruit> Fruits { get; private set; }
        private const int MAX_FRUITS_IN_BONUS = 4;

        public bool IsBonusPhase { get; private set; } = false;
        public DateTime BonusPhaseStartTime { get; private set; }
        public Player BonusPlayer { get; private set; }
        private const int BONUS_PHASE_DURATION = 30000; // 30 segundos en milisegundos

        // Matriz del juego base
        private static readonly int[,] baseMap = new int[,] {
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
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

        private static readonly int[,] bonusMap = new int[,] {
            // Matriz de fase Bonus
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };
        
        // Constructor del GameSession
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
            Fruits = new List<Fruit>();
            GameMap = CloneMap(baseMap);
            Console.WriteLine($"Nueva sesión de juego creada: {SessionId}");
        }

        // Copia del mapa para que el original no cambie 
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

        // Metodo para verificar actualizaciones en objetos
        public void Update()
        {
            if (!IsActive) return;
            
            if (IsBonusPhase)
            {
                // Verificar si el tiempo de bonus ha terminado
                if ((DateTime.Now - BonusPhaseStartTime).TotalMilliseconds >= BONUS_PHASE_DURATION)
                {
                    EndBonusPhase();
                    return;
                }
                
                // Solo actualizar el jugador en fase bonus
                if (BonusPlayer != null && BonusPlayer.IsAlive)
                {
                    BonusPlayer.Update(GameMap);
                    UpdateEnemies();
                    CheckEnemyCollisions();
                    RegenerateBlocks();
                    Client.SendGameStateToClient();
                }
                else
                {
                    // Si el jugador bonus muere, terminar la fase bonus
                    EndBonusPhase();
                }
            }
            else // En el juego normal
            {
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
                    CheckPlayerFloorDistance();
                    CheckBonusZoneCollision();
                    RegenerateBlocks();
                    Client.SendGameStateToClient();
                }
                else
                {
                    Client.SendGameOverToClient();
                }
            }
        }

        // Traduce el input enviado por el cliente e interpreta la accion relacionada
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

        // Verifica la distancia de pisos entre ambos jugadores
        private void CheckPlayerFloorDistance()
        {
            if (!IsPlayer2Active || !Player1.IsAlive || !Player2.IsAlive) 
                return;

            int player1Floor = GetPlayerFloor(Player1);
            int player2Floor = GetPlayerFloor(Player2);
            
            if (Math.Abs(player1Floor - player2Floor) > 4) // Maximo 4
            {
                Player lowerPlayer = player1Floor > player2Floor ? Player1 : Player2;
                Player upperPlayer = player1Floor > player2Floor ? Player2 : Player1;
                int upperFloor = player1Floor > player2Floor ? player2Floor : player1Floor;
                
                // El jugador más abajo pierde una vida
                lowerPlayer.TakeDamage();
                
                if (lowerPlayer.IsAlive)
                {
                    // Reaparecer en el mismo piso del jugador más arriba
                    RespawnPlayerOnFloor(lowerPlayer, upperFloor);
                    Console.WriteLine($"Player {lowerPlayer.PlayerId} perdió una vida por distancia y reapareció en el piso {upperFloor}");
                }
            }
        }

        // Obtiene el piso actual de cada jugador
        private int GetPlayerFloor(Player player)
        {
            int playerY = (int)Math.Round(player.Y);
            
            // Buscar el piso más cercano hacia abajo
            for (int y = playerY; y < GameMap.GetLength(0); y++)
            {
                // Verificar si esta fila tiene solo 1s (es un piso)
                bool isFloor = true;
                for (int x = 0; x < GameMap.GetLength(1); x++)
                {
                    if (GameMap[y, x] != 1)
                    {
                        isFloor = false;
                        break;
                    }
                }
                
                if (isFloor)
                {
                    return y;
                }
            }
            
            return playerY; // Si no encuentra piso, devolver posición actual
        }

        // Hace reaparecer al jugador muerto
        private void RespawnPlayerOnFloor(Player player, int targetFloor)
        {
            // Buscar una posición libre en el piso objetivo
            for (int x = 1; x < GameMap.GetLength(1) - 1; x++)
            {
                if (targetFloor > 0 && GameMap[targetFloor - 1, x] == 0) // Espacio libre encima del piso
                {
                    player.SetPosition(x, targetFloor - 1);
                    player.VelocityX = 0;
                    player.VelocityY = 0;
                    break;
                }
            }
        }

        // Método para que los enemigos aparezcan en el mismo piso del player
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
                        // USANDO ABSTRACT FACTORY PATTERN
                        var factory = EnemyFactoryProvider.GetGroundEnemyFactory(spawnFromLeft);
                        var groundEnemy = factory.CreateEnemy(spawnX, floorY);
                        Enemies.Add(groundEnemy);
                        
                        Console.WriteLine($"GroundEnemy generado desde {(spawnFromLeft ? "izquierda" : "derecha")} en ({spawnX}, {floorY})");
                        break;
                    }
                }
            }
        }

        // Método para generar aves
        public void SpawnBirdEnemy()
        {
            Random rand = new Random();
            // Las aves aparecen por la izquierda
            bool spawnFromLeft = rand.Next(0, 1) == 0;
            int spawnY = rand.Next(1, GameMap.GetLength(0) - 1);
            float spawnX = spawnFromLeft ? 1.0f : GameMap.GetLength(1) - 2.0f;
            
            // USANDO ABSTRACT FACTORY PATTERN
            var factory = EnemyFactoryProvider.GetFactory(EnemyType.Bird);
            var birdEnemy = factory.CreateEnemy(spawnX, spawnY);
            Enemies.Add(birdEnemy);
            
            Console.WriteLine($"Ave enemiga generada en posición ({spawnX}, {spawnY}) desde izquierda");
        }

        // Método para generar hielos
        public void SpawnIceEnemy(int floorY, int positionX)
        {
            // Validar que la posición sea válida
            if (floorY < 0 || floorY >= GameMap.GetLength(0) || 
                positionX < 1 || positionX >= GameMap.GetLength(1) - 1)
            {
                Console.WriteLine("Posición inválida para generar hielo");
                return;
            }

            // USANDO ABSTRACT FACTORY PATTERN
            var factory = EnemyFactoryProvider.GetFactory(EnemyType.Ice);
            var iceEnemy = factory.CreateEnemy(positionX, floorY);
            Enemies.Add(iceEnemy);
            
            Console.WriteLine($"Hielo enemigo generado en piso {floorY}, posición {positionX}");
        }

        // Método para generar frutas
        public void SpawnRandomFruits()
        {
            if (!IsBonusPhase || Fruits.Count >= MAX_FRUITS_IN_BONUS) return; // Solo 4 a la vez

            Random rand = new Random();
            
            // Buscar una posición válida para la fruta
            for (int attempts = 0; attempts < 20; attempts++)
            {
                int randomX = rand.Next(2, GameMap.GetLength(1) - 2);
                int randomY = rand.Next(1, GameMap.GetLength(0) - 1);
                
                // Verificar que la posición esté libre
                if (GameMap[randomY, randomX] == 0)
                {
                    // USANDO ABSTRACT FACTORY PATTERN con EnemyCreator
                    var fruit = EnemyCreator.CreateRandomFruit(randomX, randomY, rand);
                    Fruits.Add(fruit);
                    Console.WriteLine($"Fruta {fruit.FruitType} generada en posición ({randomX}, {randomY})");
                    break;
                }
            }
        }

        // Método para verificar colisiones
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
            CheckFruitCollisions();
        }

        // Revisa colisiones de frutas
        private void CheckFruitCollisions()
        {
            var fruitsToRemove = new List<Fruit>();
            
            foreach (var fruit in Fruits.Where(f => f.IsActive))
            {
                // Verificar colisión con Player1
                if (Player1.IsAlive)
                {
                    float distance1 = (float)Math.Sqrt(
                        Math.Pow(Player1.X - fruit.X, 2) + 
                        Math.Pow(Player1.Y - fruit.Y, 2)
                    );
                    
                    if (distance1 <= ENEMY_COLLISION_DISTANCE)
                    {
                        Player1.AddScore(fruit.Points);
                        fruit.IsActive = false;
                        fruitsToRemove.Add(fruit);
                        Console.WriteLine($"Player 1 recolectó {fruit.FruitType} - Puntos: {fruit.Points}");
                        continue;
                    }
                }
                
                // Verificar colisión con Player2 (solo si está en fase bonus)
                if (IsBonusPhase && BonusPlayer == Player2 && Player2.IsAlive)
                {
                    float distance2 = (float)Math.Sqrt(
                        Math.Pow(Player2.X - fruit.X, 2) + 
                        Math.Pow(Player2.Y - fruit.Y, 2)
                    );
                    
                    if (distance2 <= ENEMY_COLLISION_DISTANCE)
                    {
                        Player2.AddScore(fruit.Points);
                        fruit.IsActive = false;
                        fruitsToRemove.Add(fruit);
                        Console.WriteLine($"Player 2 recolectó {fruit.FruitType} - Puntos: {fruit.Points}");
                        continue;
                    }
                }
            }
            
            // Remover frutas recolectadas
            foreach (var fruit in fruitsToRemove)
            {
                Fruits.Remove(fruit);
            }
        }

        private void UpdateEnemies()
        {
            foreach (var enemy in Enemies.ToList())
            {
                enemy.Update(GameMap, Player1, Player2, IsPlayer2Active);

                // Remover enemigos inactivos o que han expirado
                if (!enemy.IsActive || (DateTime.Now - enemy.CreationTime).TotalSeconds > 30) // 30 segundo de duracion
                {
                    Enemies.Remove(enemy);
                }
            }
        }

        private void RegenerateBlocks()
        {
            var blocksToRegenerate = DestroyedBlocks
                .Where(kvp => (DateTime.Now - kvp.Value).TotalMilliseconds >= BLOCK_REGENERATION_TIME) // 3 segundos de regeneracion
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
            if ((DateTime.Now - player.LastAttackTime).TotalMilliseconds < ATTACK_COOLDOWN) // Enfriamiento entre ataques 0.5 segundos
                return;

            player.IsAttacking = true;
            player.LastAttackTime = DateTime.Now;
            DestroyBlocksInRadius(player);
            AttackEnemies(player);
        }

        // Método para atacar enemigos
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
                            player.AddScore(400); // 400 puntos por golpear Ground enemy
                            Console.WriteLine($"GroundEnemy atacado por Player {player.PlayerId} - Iniciando retirada");
                            break;
                            
                        case EnemyType.Bird:
                            // Las aves se destruyen al ser atacadas
                            enemy.IsActive = false;
                            enemiesToRemove.Add(enemy);
                            player.AddScore(800); // 800 puntos por destruir Bird enemy
                            Console.WriteLine($"BirdEnemy eliminado por ataque del Player {player.PlayerId}");
                            break;
                            
                        case EnemyType.Ice:
                            // Los hielos se destruyen al ser atacados
                            enemy.IsActive = false;
                            enemiesToRemove.Add(enemy);
                            player.AddScore(400); // 400 puntos por destruir Ice enemy
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

        // Método para incrementar la velocidad global de los enemigos
        private static void IncreaseEnemySpeed()
        {
            SpeedMultiplier += 0.2f; // Incrementar 20% cada vez
            Console.WriteLine($"Velocidad de enemigos incrementada. Multiplicador actual: {SpeedMultiplier:F1}x");
        }

        // Metodo que destruye los bloques en cierto rango
        private void DestroyBlocksInRadius(Player player)
        {
            int mapWidth = GameMap.GetLength(1);
            int mapHeight = GameMap.GetLength(0);
            int playerTileX = (int)Math.Round(player.X);
            int playerTileY = (int)Math.Round(player.Y);

            bool blocksDestroyed = false;
            int blocksDestroyedCount = 0;

            for (int dy = -ATTACK_RANGE; dy <= 0; dy++) // Rango de ataque 1 bloque en y 
            {
                for (int dx = -ATTACK_RANGE; dx <= ATTACK_RANGE; dx++) // Rango de ataque 1 bloque en x
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
                                blocksDestroyedCount++;
                            }
                        }
                    }
                }
            }

            // Agregar puntos por bloques destruidos
            if (blocksDestroyedCount > 0)
            {
                player.AddScore(blocksDestroyedCount * 10); // 10 puntos por bloque
            }

            if (blocksDestroyed)
                Client.SendMapToClient(); // Avisa al cliente del bloque roto
        }

        // Metodo para verificar colisión con zona bonus:
        private void CheckBonusZoneCollision()
        {
            if (IsBonusPhase) return;
            
            // Verificar si Player1 toca un bloque tipo 2
            if (Player1.IsAlive && IsPlayerOnBonusBlock(Player1))
            {
                StartBonusPhase(Player1);
                return;
            }
            
            // Verificar si Player2 toca un bloque tipo 2
            if (IsPlayer2Active && Player2.IsAlive && IsPlayerOnBonusBlock(Player2))
            {
                StartBonusPhase(Player2);
                return;
            }
        }

        // Verificar si un jugador está tocando un bloque tipo 2:
        private bool IsPlayerOnBonusBlock(Player player)
        {
            int playerTileX = (int)Math.Round(player.X);
            int playerTileY = (int)Math.Round(player.Y);
            
            // Verificar la posición actual y adyacentes
            for (int dy = -1; dy <= 1; dy++)
            {
                for (int dx = -1; dx <= 1; dx++)
                {
                    int checkX = playerTileX + dx;
                    int checkY = playerTileY + dy;
                    
                    if (checkX >= 0 && checkX < GameMap.GetLength(1) && 
                        checkY >= 0 && checkY < GameMap.GetLength(0))
                    {
                        if (GameMap[checkY, checkX] == 2)
                        {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        }

        // Iniciar la fase bonus:
        private void StartBonusPhase(Player player)
        {
            IsBonusPhase = true;
            BonusPlayer = player;
            BonusPhaseStartTime = DateTime.Now;
            
            // Cambiar al mapa bonus
            GameMap = CloneMap(bonusMap);
            
            // Posicionar al jugador en el inicio del mapa bonus
            BonusPlayer.SetPosition(2.0f, 1.0f);
            BonusPlayer.VelocityX = 0;
            BonusPlayer.VelocityY = 0;
            
            // Limpiar enemigos actuales
            Enemies.Clear();
            DestroyedBlocks.Clear();
            
            // Limpiar frutas anteriores PERO NO generar nuevas automaticamente
            Fruits.Clear();
            
            Console.WriteLine($"¡Player {player.PlayerId} ha entrado en la fase bonus!");
            Client.SendMapToClient();
            Client.SendGameStateToClient();
        }

        // Terminar la fase bonus:
        private void EndBonusPhase()
        {
            IsBonusPhase = false;
            
            // Restaurar el mapa original
            GameMap = CloneMap(baseMap);
            
            // GUARDAR los puntajes ANTES de cualquier reset
            int player1Score = Player1.Score;
            int player2Score = Player2.Score;
            
            // SUMAR UNA VIDA AL JUGADOR QUE ENTRÓ EN LA FASE BONUS
            if (BonusPlayer != null)
            {
                BonusPlayer.Lives = Math.Min(BonusPlayer.Lives + 1, 6); // Máximo 6 vidas 
                Console.WriteLine($"¡Player {BonusPlayer.PlayerId} ganó una vida extra! Vidas actuales: {BonusPlayer.Lives}");
            }
            
            // INCREMENTAR VELOCIDAD DE ENEMIGOS
            IncreaseEnemySpeed();
            
            // Resetear jugadores SIN tocar el puntaje NI las vidas del bonus player
            Player1.IsInvulnerable = false;
            Player1.LastDamageTime = DateTime.MinValue;
            Player1.ResetPosition();
            Player1.SetPosition(2.0f, 31.0f);
            // RESTAURAR el puntaje DESPUES del reset
            Player1.Score = player1Score;
            
            // Si Player1 NO fue el bonus player, resetear sus vidas a 3
            if (BonusPlayer != Player1)
            {
                Player1.Lives = 3;
            }
            
            if (IsPlayer2Active)
            {
                Player2.IsInvulnerable = false;
                Player2.LastDamageTime = DateTime.MinValue;
                Player2.ResetPosition();
                Player2.SetPosition(17.0f, 31.0f);
                // RESTAURAR el puntaje DESPUES del reset
                Player2.Score = player2Score;
                
                // Si Player2 NO fue el bonus player, resetear sus vidas a 3
                if (BonusPlayer != Player2)
                {
                    Player2.Lives = 3;
                }
            }
            
            // Limpiar estado del juego
            BonusPlayer = null;
            Enemies.Clear();
            Fruits.Clear();
            DestroyedBlocks.Clear();
            
            Console.WriteLine("¡Fase bonus terminada! Regresando al juego principal.");
            Console.WriteLine($"Puntajes conservados - Player 1: {Player1.Score}, Player 2: {Player2.Score}");
            Client.SendMapToClient();
            Client.SendGameStateToClient();
        }

        private void RestartGame()
        {
            // Si estamos en fase bonus, salir de ella primero
            if (IsBonusPhase)
            {
                EndBonusPhase();
                return;
            }
            
            IsActive = true;
            
            // RESETEAR velocidad de enemigos al reiniciar
            SpeedMultiplier = 1.0f;
            Console.WriteLine("Velocidad de enemigos reseteada a normal");
            
            // Resetear Player1 completamente incluyendo score
            Player1.Lives = 3;
            Player1.IsInvulnerable = false;
            Player1.LastDamageTime = DateTime.MinValue;
            Player1.ResetPosition();
            Player1.SetPosition(2.0f, 31.0f);
            Player1.Score = 0; // Aquí SI se resetea el score
            
            if (IsPlayer2Active)
            {
                Player2.Lives = 3;
                Player2.IsInvulnerable = false;
                Player2.LastDamageTime = DateTime.MinValue;
                Player2.ResetPosition();
                Player2.SetPosition(17.0f, 31.0f);
                Player2.Score = 0; // Aquí SI se resetea el score
            }
            
            Enemies.Clear();
            Fruits.Clear();
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