using System;

namespace PlatformGameServer.Game
{
    public class Player
    {
        public int PlayerId { get; private set; }
        public float X { get; set; } = 2.0f;
        public float Y { get; set; } = 31.0f;
        public float VelocityX { get; set; } = 0.0f;
        public float VelocityY { get; set; } = 0.0f;
        public bool IsOnGround { get; set; } = true;
        public bool IsJumping { get; set; } = false;
        public bool IsAttacking { get; set; } = false;
        public DateTime LastAttackTime { get; set; } = DateTime.MinValue;
        
        // Sistema de vidas
        public int Lives { get; set; } = 3;
        public bool IsInvulnerable { get; set; } = false;
        public DateTime LastDamageTime { get; set; } = DateTime.MinValue;
        public bool IsAlive => Lives > 0;
        
        private const int INVULNERABILITY_TIME = 2000;
        private const float GRAVITY = 0.1f;
        private const float JUMP_FORCE = -1.0f;
        private const float MOVE_SPEED = 0.2f;
        
        // Constructor que acepta un ID de jugador
        public Player(int playerId = 1)
        {
            PlayerId = playerId;
            // Player 1 empieza en la izquierda, Player 2 en la derecha
            if (playerId == 2)
            {
                X = 17.0f;
                Y = 31.0f;
            }
        }
        
        public void SetPosition(float x, float y)
        {
            X = x;
            Y = y;
        }
        
        public bool CanTakeDamage()
        {
            return !IsInvulnerable && (DateTime.Now - LastDamageTime).TotalMilliseconds >= INVULNERABILITY_TIME;
        }
        
        public void TakeDamage()
        {
            if (CanTakeDamage() && Lives > 0)
            {
                Lives--;
                IsInvulnerable = true;
                LastDamageTime = DateTime.Now;
                Console.WriteLine($"Player {PlayerId} recibió daño. Vidas restantes: {Lives}");
            }
        }
        
        public void UpdateInvulnerability()
        {
            if (IsInvulnerable && (DateTime.Now - LastDamageTime).TotalMilliseconds >= INVULNERABILITY_TIME)
            {
                IsInvulnerable = false;
            }
        }
        
        public void ResetPosition()
        {
            // Posiciones iniciales diferentes para cada jugador
            if (PlayerId == 1)
            {
                X = 2.0f;
                Y = 31.0f;
            }
            else
            {
                X = 17.0f;
                Y = 31.0f;
            }
            
            VelocityX = 0.0f;
            VelocityY = 0.0f;
            IsOnGround = true;
            IsJumping = false;
        }

        public void Update(int[,] gameMap)
        {
            UpdateInvulnerability();
            
            if (!IsOnGround) VelocityY += GRAVITY;
            X += VelocityX;
            Y += VelocityY;
            CheckCollisions(gameMap);
            VelocityX *= IsOnGround ? 0.8f : 0.95f;
            if (IsAttacking && (DateTime.Now - LastAttackTime).TotalMilliseconds > 200)
                IsAttacking = false;
        }

        public void HandleInput(string input)
        {
            switch (input.ToUpper())
            {
                case "LEFT": 
                    VelocityX = -MOVE_SPEED; 
                    break;
                case "RIGHT": 
                    VelocityX = MOVE_SPEED; 
                    break;
                case "JUMP":
                    if (IsOnGround && !IsJumping)
                    {
                        VelocityY = JUMP_FORCE;
                        IsOnGround = false;
                        IsJumping = true;
                    }
                    break;
            }
        }

        private void CheckCollisions(int[,] gameMap)
        {
            int mapWidth = gameMap.GetLength(1);
            int mapHeight = gameMap.GetLength(0);
            
            // Límites del mapa
            if (X < 0) X = 0;
            if (X >= mapWidth) X = mapWidth - 0.1f;
            if (Y < 0) Y = 0;
            if (Y >= mapHeight) Y = mapHeight - 0.1f;

            int playerTileX = Math.Max(0, Math.Min((int)Math.Round(X), mapWidth - 1));
            int playerTileY = Math.Max(0, Math.Min((int)Math.Round(Y), mapHeight - 1));
            IsOnGround = false;

            // Colisión con bloque actual
            if (gameMap[playerTileY, playerTileX] == 1)
            {
                if (VelocityY < 0) // Subiendo, golpear techo
                {
                    Y = playerTileY + 1;
                    VelocityY = 0;
                }
                else if (VelocityY > 0) // Cayendo, aterrizar
                {
                    Y = playerTileY - 1;
                    VelocityY = 0;
                    IsOnGround = true;
                    IsJumping = false;
                }
                else // Colisión horizontal
                {
                    if (VelocityX > 0) X = playerTileX - 1;
                    else if (VelocityX < 0) X = playerTileX + 1;
                    VelocityX = 0;
                }
            }

            // Verificar suelo debajo
            if (!IsOnGround && playerTileY + 1 < mapHeight && gameMap[playerTileY + 1, playerTileX] == 1 && VelocityY >= 0)
            {
                Y = playerTileY;
                VelocityY = 0;
                IsOnGround = true;
                IsJumping = false;
            }

            // Verificar techo arriba
            if (playerTileY > 0 && gameMap[playerTileY - 1, playerTileX] == 1 && VelocityY < 0)
            {
                Y = playerTileY;
                VelocityY = 0;
            }
        }
    }
}