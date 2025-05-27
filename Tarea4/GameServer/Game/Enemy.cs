namespace PlatformGameServer.Game
{
    public class Enemy
    {
        public string Id { get; set; }
        public float X { get; set; }
        public float Y { get; set; }
        public float VelocityX { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreationTime { get; set; }

        public Enemy(float x, float y)
        {
            Id = Guid.NewGuid().ToString();
            X = x;
            Y = y;
            VelocityX = 0.2f;
            IsActive = true;
            CreationTime = DateTime.Now;
        }
    }
}
