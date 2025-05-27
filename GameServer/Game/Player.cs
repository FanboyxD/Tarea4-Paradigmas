namespace PlatformGameServer.Game
{
    public class Player
    {
        public float X { get; set; } = 2.0f;
        public float Y { get; set; } = 29.0f;
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
        
        private const int INVULNERABILITY_TIME = 2000; // 2 segundos de invulnerabilidad
        
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
                Console.WriteLine($"Jugador recibió daño. Vidas restantes: {Lives}");
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
            X = 2.0f;
            Y = 29.0f;
            VelocityX = 0.0f;
            VelocityY = 0.0f;
            IsOnGround = true;
            IsJumping = false;
        }
    }
}