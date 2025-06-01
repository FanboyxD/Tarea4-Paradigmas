using System;

namespace PlatformGameServer.Game
{
    public enum EnemyType
    {
        Ground,
        Bird,
        Ice
    }

    public enum GroundEnemyState
    {
        Normal,     // Movimiento normal, puede hacer daño
        Retreating  // Regresando hacia donde salió, no hace daño
    }

    public abstract class Enemy
    {
        public string Id { get; set; }
        public float X { get; set; }
        public float Y { get; set; }
        public float VelocityX { get; set; }
        public float VelocityY { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreationTime { get; set; }
        public EnemyType Type { get; protected set; }
        public bool CanCauseDamage { get; set; } = true; // Nueva propiedad para controlar si puede hacer daño

        protected Enemy(float x, float y, EnemyType type)
        {
            Id = Guid.NewGuid().ToString();
            X = x;
            Y = y;
            VelocityX = 0.0f;
            VelocityY = 0.0f;
            IsActive = true;
            CreationTime = DateTime.Now;
            Type = type;
        }

        public abstract void Update(int[,] gameMap, Player player1, Player player2, bool isPlayer2Active);
        public virtual void OnAttacked() { } // Método virtual para manejar cuando es atacado
    }

    public class GroundEnemy : Enemy
    {
        private const float GROUND_SPEED = 0.1f;
        private GroundEnemyState state;
        private bool spawnedFromLeft; // true si apareció desde la izquierda, false si desde la derecha
        private float originalSpeed;

        public GroundEnemy(float x, float y, bool fromLeft = true) : base(x, y, EnemyType.Ground)
        {
            spawnedFromLeft = fromLeft;
            originalSpeed = fromLeft ? GROUND_SPEED : -GROUND_SPEED;
            VelocityX = originalSpeed;
            state = GroundEnemyState.Normal;
        }

        public override void OnAttacked()
        {
            // Al ser atacado, cambia a estado de retirada y no puede hacer daño
            state = GroundEnemyState.Retreating;
            CanCauseDamage = false;
            VelocityX = spawnedFromLeft ? -GROUND_SPEED : GROUND_SPEED; // Se mueve hacia donde salió
            Console.WriteLine($"GroundEnemy atacado - Estado: Retreating, Dirección: {(spawnedFromLeft ? "Izquierda" : "Derecha")}");
        }

        public override void Update(int[,] gameMap, Player player1, Player player2, bool isPlayer2Active)
        {
            X += VelocityX;
            int enemyTileX = (int)Math.Round(X);
            int enemyTileY = (int)Math.Round(Y);

            if (state == GroundEnemyState.Retreating)
            {
                // En estado de retirada, verificar si llegó al borde por donde salió
                if ((spawnedFromLeft && X <= 1) || (!spawnedFromLeft && X >= gameMap.GetLength(1) - 2))
                {
                    // Volver al estado normal y cambiar dirección
                    state = GroundEnemyState.Normal;
                    CanCauseDamage = true;
                    VelocityX = originalSpeed; // Vuelve a su dirección original
                    Console.WriteLine("GroundEnemy volvió al estado Normal");
                }
            }
            else // Estado normal
            {
                // Cambiar dirección si toca una pared o el borde del mapa
                if (enemyTileX <= 0 || enemyTileX >= gameMap.GetLength(1) - 1 ||
                    (enemyTileX + (VelocityX > 0 ? 1 : -1) < gameMap.GetLength(1) && 
                     enemyTileX + (VelocityX > 0 ? 1 : -1) >= 0 &&
                     gameMap[enemyTileY, enemyTileX + (VelocityX > 0 ? 1 : -1)] == 1))
                {
                    VelocityX *= -1;
                    originalSpeed *= -1; // Actualizar la velocidad original también
                }
            }
        }
    }

    public class BirdEnemy : Enemy
    {
        private const float BIRD_SPEED = 0.08f;
        private const float VERTICAL_SPEED = 0.05f;
        private Random random;
        private DateTime lastDirectionChange;
        private const int DIRECTION_CHANGE_INTERVAL = 2000; // Cambiar dirección cada 2 segundos
        private float targetY;
        private bool movingUp;

        public BirdEnemy(float x, float y) : base(x, y, EnemyType.Bird)
        {
            random = new Random();
            VelocityX = BIRD_SPEED;
            lastDirectionChange = DateTime.Now;
            targetY = y;
            movingUp = random.Next(0, 2) == 0; // Dirección vertical aleatoria inicial
        }

        public override void Update(int[,] gameMap, Player player1, Player player2, bool isPlayer2Active)
        {
            // Cambiar dirección aleatoriamente cada cierto tiempo
            if ((DateTime.Now - lastDirectionChange).TotalMilliseconds >= DIRECTION_CHANGE_INTERVAL)
            {
                ChangeRandomDirection(gameMap);
                lastDirectionChange = DateTime.Now;
            }

            // Aplicar movimiento horizontal
            X += VelocityX;

            // Movimiento vertical hacia el target
            float verticalDistance = targetY - Y;
            if (Math.Abs(verticalDistance) > 0.5f)
            {
                VelocityY = verticalDistance > 0 ? VERTICAL_SPEED : -VERTICAL_SPEED;
            }
            else
            {
                VelocityY = 0;
                // Cambiar target vertical cuando llega
                ChangeVerticalTarget(gameMap);
            }

            Y += VelocityY;

            // Limitar a los bordes del mapa
            if (X < 1) 
            {
                X = 1;
                VelocityX = BIRD_SPEED; // Cambiar dirección horizontal
            }
            if (X > gameMap.GetLength(1) - 2) 
            {
                X = gameMap.GetLength(1) - 2;
                VelocityX = -BIRD_SPEED; // Cambiar dirección horizontal
            }
            if (Y < 1) Y = 1;
            if (Y > gameMap.GetLength(0) - 2) Y = gameMap.GetLength(0) - 2;
        }

        private void ChangeRandomDirection(int[,] gameMap)
        {
            // Cambiar dirección horizontal aleatoriamente
            VelocityX = random.Next(0, 2) == 0 ? BIRD_SPEED : -BIRD_SPEED;
            
            // Cambiar target vertical
            ChangeVerticalTarget(gameMap);
        }

        private void ChangeVerticalTarget(int[,] gameMap)
        {
            // Encontrar pisos válidos para moverse
            var validFloors = new System.Collections.Generic.List<int>();
            
            // Buscar pisos (filas que tienen bloques sólidos)
            for (int y = 1; y < gameMap.GetLength(0) - 1; y++)
            {
                bool hasFloor = false;
                for (int x = 1; x < gameMap.GetLength(1) - 1; x++)
                {
                    if (gameMap[y, x] == 1)
                    {
                        hasFloor = true;
                        break;
                    }
                }
                if (hasFloor && y != (int)Math.Round(Y)) // No incluir el piso actual
                {
                    validFloors.Add(y - 1); // Posición encima del piso
                }
            }

            if (validFloors.Count > 0)
            {
                targetY = validFloors[random.Next(validFloors.Count)];
            }
            else
            {
                // Si no hay pisos válidos, moverse a una altura aleatoria
                targetY = random.Next(1, gameMap.GetLength(0) - 2);
            }
        }
    }

    public class IceEnemy : Enemy
    {
        private const float ICE_FALL_SPEED = 0.15f;
        private const int FALL_DELAY_MS = 1000; // Delay de 1 segundo antes de caer
        private bool hasFallStarted = false;

        public IceEnemy(float x, float y) : base(x, y, EnemyType.Ice)
        {
            VelocityY = 0; // Inicialmente no cae
        }

        public override void Update(int[,] gameMap, Player player1, Player player2, bool isPlayer2Active)
        {
            // Verificar si ha pasado el tiempo de delay
            if (!hasFallStarted && (DateTime.Now - CreationTime).TotalMilliseconds >= FALL_DELAY_MS)
            {
                hasFallStarted = true;
                VelocityY = ICE_FALL_SPEED;
                Console.WriteLine($"IceEnemy comenzó a caer después del delay en posición ({X}, {Y})");
            }

            // Solo aplicar movimiento si ya comenzó a caer
            if (hasFallStarted)
            {
                Y += VelocityY;
                
                // Solo se desactiva cuando sale del mapa por abajo
                if (Y >= gameMap.GetLength(0))
                {
                    IsActive = false;
                    Console.WriteLine("IceEnemy salió del mapa por abajo");
                }
            }
        }
    }
}