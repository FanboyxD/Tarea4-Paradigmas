using System;
using System.Collections.Generic;
using System.Windows.Forms;
using PlatformGameServer.Networking;
using Timer = System.Windows.Forms.Timer;

namespace PlatformGameServer.UI
{
    public partial class ServerUI : Form
    {
        // Botones y etiquetas
        private Button? spawnGroundEnemyButton;
        private Button? spawnBirdEnemyButton;
        private Button? spawnIceEnemyButton;
        private Button? spawnFruitButton;
        private ListBox? sessionsListBox;
        private NumericUpDown? iceFloorSelector;
        private NumericUpDown? icePositionSelector;
        private Label? iceFloorLabel;
        private Label? icePositionLabel;
        private Timer? uiUpdateTimer;

        // Mapeo de pisos válidos: índice del selector -> fila real en la matriz
        private readonly Dictionary<int, int> floorMapping = new Dictionary<int, int>
        {
            { 2, 29 },  // Piso 2 -> fila 29
            { 3, 25 },  // Piso 3 -> fila 25
            { 4, 21 },  // Piso 4 -> fila 21
            { 5, 17 },  // Piso 5 -> fila 17
            { 6, 13 },  // Piso 6 -> fila 13
            { 7, 9 },   // Piso 7 -> fila 9
            { 8, 5 },   // Piso 8 -> fila 5
            { 9, 1 }    // Piso 9 (más arriba) -> fila 1
        };

        public ServerUI()
        {
            InitializeComponent();
            // Usamos el Singleton
        }

        private void InitializeComponent()
        {
            this.Text = "Game Server Control";
            this.Size = new System.Drawing.Size(500, 400);

            // Botón para enemigos terrestres
            spawnGroundEnemyButton = new Button();
            spawnGroundEnemyButton.Text = "Spawn Ground Enemy";
            spawnGroundEnemyButton.Location = new System.Drawing.Point(10, 10);
            spawnGroundEnemyButton.Size = new System.Drawing.Size(140, 30);
            spawnGroundEnemyButton.Click += SpawnGroundEnemyButton_Click;

            // Botón para aves
            spawnBirdEnemyButton = new Button();
            spawnBirdEnemyButton.Text = "Spawn Bird Enemy";
            spawnBirdEnemyButton.Location = new System.Drawing.Point(160, 10);
            spawnBirdEnemyButton.Size = new System.Drawing.Size(130, 30);
            spawnBirdEnemyButton.Click += SpawnBirdEnemyButton_Click;

            // Botón para frutas
            spawnFruitButton = new Button();
            spawnFruitButton.Text = "Spawn Fruit";
            spawnFruitButton.Location = new System.Drawing.Point(300, 10);
            spawnFruitButton.Size = new System.Drawing.Size(100, 30);
            spawnFruitButton.Click += SpawnFruitButton_Click;
            spawnFruitButton.Enabled = false; // Inicialmente deshabilitado

            // Controles para hielos
            iceFloorLabel = new Label();
            iceFloorLabel.Text = "Ice Floor (2-9):";
            iceFloorLabel.Location = new System.Drawing.Point(10, 50);
            iceFloorLabel.Size = new System.Drawing.Size(80, 20);

            iceFloorSelector = new NumericUpDown();
            iceFloorSelector.Location = new System.Drawing.Point(100, 50);
            iceFloorSelector.Size = new System.Drawing.Size(60, 20);
            iceFloorSelector.Minimum = 2;
            iceFloorSelector.Maximum = 9;
            iceFloorSelector.Value = 3;

            icePositionLabel = new Label();
            icePositionLabel.Text = "Ice Position (X):";
            icePositionLabel.Location = new System.Drawing.Point(170, 50);
            icePositionLabel.Size = new System.Drawing.Size(90, 20);

            icePositionSelector = new NumericUpDown();
            icePositionSelector.Location = new System.Drawing.Point(270, 50);
            icePositionSelector.Size = new System.Drawing.Size(60, 20);
            icePositionSelector.Minimum = 1;
            icePositionSelector.Maximum = 24;
            icePositionSelector.Value = 12;

            // Botón para hielos
            spawnIceEnemyButton = new Button();
            spawnIceEnemyButton.Text = "Spawn Ice Enemy";
            spawnIceEnemyButton.Location = new System.Drawing.Point(10, 80);
            spawnIceEnemyButton.Size = new System.Drawing.Size(130, 30);
            spawnIceEnemyButton.Click += SpawnIceEnemyButton_Click;

            // Lista de sesiones
            sessionsListBox = new ListBox();
            sessionsListBox.Location = new System.Drawing.Point(10, 120);
            sessionsListBox.Size = new System.Drawing.Size(460, 220);

            // Agregar controles al formulario
            this.Controls.Add(spawnGroundEnemyButton);
            this.Controls.Add(spawnBirdEnemyButton);
            this.Controls.Add(spawnFruitButton);
            this.Controls.Add(iceFloorLabel);
            this.Controls.Add(iceFloorSelector);
            this.Controls.Add(icePositionLabel);
            this.Controls.Add(icePositionSelector);
            this.Controls.Add(spawnIceEnemyButton);
            this.Controls.Add(sessionsListBox);

            // Timer para actualizar UI
            uiUpdateTimer = new Timer();
            uiUpdateTimer.Interval = 1000;
            uiUpdateTimer.Tick += UpdateUI;
            uiUpdateTimer.Start();
        }

        private void SpawnGroundEnemyButton_Click(object? sender, EventArgs e)
        {
            // Usar la instancia Singleton
            foreach (var session in GameServer.Instance.GetActiveSessions())
            {
                session.SpawnEnemyNearPlayer();
            }
        }

        private void SpawnBirdEnemyButton_Click(object? sender, EventArgs e)
        {
            // Usar la instancia Singleton
            foreach (var session in GameServer.Instance.GetActiveSessions())
            {
                session.SpawnBirdEnemy();
            }
        }

        private void SpawnIceEnemyButton_Click(object? sender, EventArgs e)
        {
            // Convertir el valor del selector (1-9) a la fila real de la matriz
            int selectedFloor = (int)(iceFloorSelector?.Value ?? 3);
            int actualFloorY = floorMapping[selectedFloor];
            
            // Usar la instancia Singleton
            foreach (var session in GameServer.Instance.GetActiveSessions())
            {
                
                {
                    // Generar en posición específica
                    int positionX = (int)(icePositionSelector?.Value ?? 12);
                    session.SpawnIceEnemy(actualFloorY, positionX);
                }
            }
        }

        private void SpawnFruitButton_Click(object? sender, EventArgs e)
        {
            foreach (var session in GameServer.Instance.GetActiveSessions())
            {
                if (session.IsBonusPhase)
                {
                    session.SpawnRandomFruits();
                }
            }
        }

        private void UpdateUI(object? sender, EventArgs e)
        {
            if (sessionsListBox == null || spawnFruitButton == null) return;

            sessionsListBox.Items.Clear();
            
            bool anyBonusPhase = false;
            
            foreach (var session in GameServer.Instance.GetActiveSessions())
            {
                if (session.IsBonusPhase)
                {
                    anyBonusPhase = true;
                }
                
                int groundEnemies = 0;
                int birdEnemies = 0;
                int iceEnemies = 0;
                int fruits = session.Fruits.Count;

                foreach (var enemy in session.Enemies)
                {
                    switch (enemy.Type)
                    {
                        case PlatformGameServer.Game.EnemyType.Ground:
                            groundEnemies++;
                            break;
                        case PlatformGameServer.Game.EnemyType.Bird:
                            birdEnemies++;
                            break;
                        case PlatformGameServer.Game.EnemyType.Ice:
                            iceEnemies++;
                            break;
                    }
                }

                string bonusStatus = session.IsBonusPhase ? " [BONUS]" : "";
                string sessionInfo = $"Session: {session.SessionId.Substring(0, 8)}... - " +
                                $"Ground: {groundEnemies}, Birds: {birdEnemies}, Ice: {iceEnemies}, Fruits: {fruits}{bonusStatus}";
                sessionsListBox.Items.Add(sessionInfo);
            }
            
            // Habilitar/deshabilitar el botón de frutas según si hay alguna sesión en fase bonus
            spawnFruitButton.Enabled = anyBonusPhase;
        }

        public void StartServer()
        {
            // Usar la instancia Singleton
            GameServer.Instance.Start();
        }

        // Método para detener el servidor cuando se cierre la UI
        protected override void OnFormClosed(FormClosedEventArgs e)
        {
            uiUpdateTimer?.Stop();
            uiUpdateTimer?.Dispose();
            base.OnFormClosed(e);
        }

        // Método adicional para obtener estadísticas del servidor
        public void ShowServerStats()
        {
            var stats = GameServer.Instance.GetStats();
            MessageBox.Show($"Servidor: {(stats.IsRunning ? "Activo" : "Inactivo")}\n" +
                          $"Sesiones Activas: {stats.ActiveSessions}\n" +
                          $"Total Sesiones: {stats.TotalSessions}",
                          "Estadísticas del Servidor",
                          MessageBoxButtons.OK,
                          MessageBoxIcon.Information);
        }
    }
}