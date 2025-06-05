using System;
using System.Collections.Generic;

namespace PlatformGameServer.Game
{
    public enum EnemyType
    {
        Ground,
        Bird,
        Ice,
        Fruit
    }

    public enum GroundEnemyState
    {
        Normal,     // Movimiento normal, puede hacer daño
        Retreating  // Regresando hacia donde salió, no hace daño
    }

    public enum FruitType
    {
        Orange,
        Banana,
        Eggplant,
        Lettuce
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
        public bool CanCauseDamage { get; set; } = true;

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

    // Clase enemigo foca
    public class GroundEnemy : Enemy
    {
        private const float BASE_GROUND_SPEED = 0.1f;
        private float GROUND_SPEED => BASE_GROUND_SPEED * GameSession.SpeedMultiplier;
        private GroundEnemyState state;
        private bool spawnedFromLeft;
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
                    originalSpeed = spawnedFromLeft ? GROUND_SPEED : -GROUND_SPEED; // Actualizar con nueva velocidad
                    VelocityX = originalSpeed;
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

    // Clase enemigo ave
    public class BirdEnemy : Enemy
    {
        private const float BASE_BIRD_SPEED = 0.08f;
        private const float BASE_VERTICAL_SPEED = 0.05f;
        private float BIRD_SPEED => BASE_BIRD_SPEED * GameSession.SpeedMultiplier;
        private float VERTICAL_SPEED => BASE_VERTICAL_SPEED * GameSession.SpeedMultiplier;
        private const int DIRECTION_CHANGE_INTERVAL = 2000;
        
        private Random random;
        private DateTime lastDirectionChange;
        private float targetY;
        private bool movingUp;

        public BirdEnemy(float x, float y) : base(x, y, EnemyType.Bird)
        {
            random = new Random();
            VelocityX = BIRD_SPEED;
            lastDirectionChange = DateTime.Now;
            targetY = y;
            movingUp = random.Next(0, 2) == 0;
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
                ChangeVerticalTarget(gameMap);
            }

            Y += VelocityY;

            // Limitar a los bordes del mapa
            if (X < 1) 
            {
                X = 1;
                VelocityX = BIRD_SPEED;
            }
            if (X > gameMap.GetLength(1) - 2) 
            {
                X = gameMap.GetLength(1) - 2;
                VelocityX = -BIRD_SPEED;
            }
            if (Y < 1) Y = 1;
            if (Y > gameMap.GetLength(0) - 2) Y = gameMap.GetLength(0) - 2;
        }

        private void ChangeRandomDirection(int[,] gameMap)
        {
            // Cambiar dirección horizontal aleatoriamente con velocidad actualizada
            VelocityX = random.Next(0, 2) == 0 ? BIRD_SPEED : -BIRD_SPEED;
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
        private const float BASE_ICE_FALL_SPEED = 0.15f;
        private float ICE_FALL_SPEED => BASE_ICE_FALL_SPEED * GameSession.SpeedMultiplier;
        private const int FALL_DELAY_MS = 1000;
        private bool hasFallStarted = false;

        public IceEnemy(float x, float y) : base(x, y, EnemyType.Ice)
        {
            VelocityY = 0;
        }

        public override void Update(int[,] gameMap, Player player1, Player player2, bool isPlayer2Active)
        {
            if (!hasFallStarted && (DateTime.Now - CreationTime).TotalMilliseconds >= FALL_DELAY_MS)
            {
                hasFallStarted = true;
                VelocityY = ICE_FALL_SPEED; // Usa la velocidad escalada
                Console.WriteLine($"IceEnemy comenzó a caer después del delay en posición ({X}, {Y}) con velocidad {ICE_FALL_SPEED:F2}");
            }

            if (hasFallStarted)
            {
                Y += VelocityY;
                
                if (Y >= gameMap.GetLength(0))
                {
                    IsActive = false;
                    Console.WriteLine("IceEnemy salió del mapa por abajo");
                }
            }
        }
    }

    // Clase abstracta base para todas las frutas
    public abstract class Fruit : Enemy
    {
        public FruitType FruitType { get; protected set; }
        public int Points { get; protected set; }

        protected Fruit(float x, float y, FruitType fruitType, int points) : base(x, y, EnemyType.Fruit)
        {
            FruitType = fruitType;
            Points = points;
            CanCauseDamage = false; // Las frutas no hacen daño
        }

        public override void Update(int[,] gameMap, Player player1, Player player2, bool isPlayer2Active)
        {
            // Las frutas no se mueven, solo existen
        }
    }

    // Clases concretas de frutas
    public class Orange : Fruit
    {
        public Orange(float x, float y) : base(x, y, FruitType.Orange, 100) { }
    }

    public class Banana : Fruit
    {
        public Banana(float x, float y) : base(x, y, FruitType.Banana, 200) { }
    }

    public class Eggplant : Fruit
    {
        public Eggplant(float x, float y) : base(x, y, FruitType.Eggplant, 300) { }
    }

    public class Lettuce : Fruit
    {
        public Lettuce(float x, float y) : base(x, y, FruitType.Lettuce, 400) { }
    }

    // ========== PATRON ABSTRACT FACTORY ==========

    // Abstract Factory - Clase abstracta base para todas las factories
    public abstract class EnemyFactory
    {
        public abstract Enemy CreateEnemy(float x, float y);
    }

    // Concrete Factories para cada tipo de Enemy

    // Ground Enemy Factory
    public class GroundEnemyFactory : EnemyFactory
    {
        private bool _fromLeft;

        public GroundEnemyFactory(bool fromLeft = true)
        {
            _fromLeft = fromLeft;
        }

        public override Enemy CreateEnemy(float x, float y)
        {
            return new GroundEnemy(x, y, _fromLeft);
        }
    }

    // Bird Enemy Factory
    public class BirdEnemyFactory : EnemyFactory
    {
        public override Enemy CreateEnemy(float x, float y)
        {
            return new BirdEnemy(x, y);
        }
    }

    // Ice Enemy Factory
    public class IceEnemyFactory : EnemyFactory
    {
        public override Enemy CreateEnemy(float x, float y)
        {
            return new IceEnemy(x, y);
        }
    }

    // Abstract Factory para frutas 
    public abstract class FruitFactory : EnemyFactory
    {
        public abstract Fruit CreateFruit(float x, float y);
        
        // Implementación del método base
        public override Enemy CreateEnemy(float x, float y)
        {
            return CreateFruit(x, y);
        }
    }

    // Concrete Factories para frutas
    public class OrangeFactory : FruitFactory
    {
        public override Fruit CreateFruit(float x, float y)
        {
            return new Orange(x, y);
        }
    }

    public class BananaFactory : FruitFactory
    {
        public override Fruit CreateFruit(float x, float y)
        {
            return new Banana(x, y);
        }
    }

    public class EggplantFactory : FruitFactory
    {
        public override Fruit CreateFruit(float x, float y)
        {
            return new Eggplant(x, y);
        }
    }

    public class LettuceFactory : FruitFactory
    {
        public override Fruit CreateFruit(float x, float y)
        {
            return new Lettuce(x, y);
        }
    }

    // Factory Provider - Proveedor centralizado de factories
    public class EnemyFactoryProvider
    {
        private static readonly Dictionary<EnemyType, EnemyFactory> _basicFactories = new Dictionary<EnemyType, EnemyFactory>
        {
            { EnemyType.Bird, new BirdEnemyFactory() },
            { EnemyType.Ice, new IceEnemyFactory() }
        };

        private static readonly Dictionary<FruitType, FruitFactory> _fruitFactories = new Dictionary<FruitType, FruitFactory>
        {
            { FruitType.Orange, new OrangeFactory() },
            { FruitType.Banana, new BananaFactory() },
            { FruitType.Eggplant, new EggplantFactory() },
            { FruitType.Lettuce, new LettuceFactory() }
        };

        // Método para obtener factory básica por tipo de enemigo
        public static EnemyFactory GetFactory(EnemyType enemyType)
        {
            if (_basicFactories.ContainsKey(enemyType))
            {
                return _basicFactories[enemyType];
            }
            throw new ArgumentException($"No factory available for enemy type: {enemyType}");
        }

        // Método especializado para GroundEnemy con dirección
        public static EnemyFactory GetGroundEnemyFactory(bool fromLeft = true)
        {
            return new GroundEnemyFactory(fromLeft);
        }

        // Método para obtener factory de fruta por tipo de fruta
        public static FruitFactory GetFruitFactory(FruitType fruitType)
        {
            if (_fruitFactories.ContainsKey(fruitType))
            {
                return _fruitFactories[fruitType];
            }
            throw new ArgumentException($"No factory available for fruit type: {fruitType}");
        }

        // Método para obtener todas las factories disponibles
        public static IEnumerable<EnemyType> GetAvailableEnemyTypes()
        {
            return _basicFactories.Keys.Concat(new[] { EnemyType.Ground });
        }

        public static IEnumerable<FruitType> GetAvailableFruitTypes()
        {
            return _fruitFactories.Keys;
        }
    }

    // Clase de utilidad para facilitar la creación de enemigos
    public static class EnemyCreator
    {
        // Crear fruta específica
        public static Fruit CreateFruit(FruitType fruitType, float x, float y)
        {
            var factory = EnemyFactoryProvider.GetFruitFactory(fruitType);
            return factory.CreateFruit(x, y);
        }

        // Crear fruta aleatoria
        public static Fruit CreateRandomFruit(float x, float y, Random random = null)
        {
            random = random ?? new Random();
            var availableTypes = EnemyFactoryProvider.GetAvailableFruitTypes().ToArray();
            var selectedType = availableTypes[random.Next(availableTypes.Length)];
            
            return CreateFruit(selectedType, x, y);
        }
    }
}