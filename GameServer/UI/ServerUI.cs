using System;
using System.Collections.Generic;
using System.Windows.Forms;
using PlatformGameServer.Networking;
using Timer = System.Windows.Forms.Timer;

namespace PlatformGameServer.UI
{
    public partial class ServerUI : Form
    {
        private GameServer gameServer;
        private Button? spawnGroundEnemyButton;
        private Button? spawnBirdEnemyButton;
        private Button? spawnIceEnemyButton;
        private ListBox? sessionsListBox;
        private NumericUpDown? iceFloorSelector;
        private NumericUpDown? icePositionSelector;
        private Label? iceFloorLabel;
        private Label? icePositionLabel;
        private CheckBox? randomIcePositionCheckBox;
        private Timer uiUpdateTimer;

        // Mapeo de pisos válidos: índice del selector -> fila real en la matriz
        private readonly Dictionary<int, int> floorMapping = new Dictionary<int, int>
        {
            { 2, 28 },  // Piso 2 -> fila 28
            { 3, 24 },  // Piso 3 -> fila 24
            { 4, 20 },  // Piso 4 -> fila 20
            { 5, 16 },  // Piso 5 -> fila 16
            { 6, 12 },  // Piso 6 -> fila 12
            { 7, 8 },   // Piso 7 -> fila 8
            { 8, 4 },   // Piso 8 -> fila 4
            { 9, 0 }    // Piso 9 (más arriba) -> fila 0
        };

        public ServerUI()
        {
            InitializeComponent();
            gameServer = new GameServer();
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

            // Controles para hielos
            iceFloorLabel = new Label();
            iceFloorLabel.Text = "Ice Floor (2-9):";
            iceFloorLabel.Location = new System.Drawing.Point(10, 50);
            iceFloorLabel.Size = new System.Drawing.Size(80, 20);

            iceFloorSelector = new NumericUpDown();
            iceFloorSelector.Location = new System.Drawing.Point(100, 50);
            iceFloorSelector.Size = new System.Drawing.Size(60, 20);
            iceFloorSelector.Minimum = 2;  // Piso 1 (más abajo)
            iceFloorSelector.Maximum = 9;  // Piso 9 (más arriba)
            iceFloorSelector.Value = 3;    // Valor por defecto (piso central)

            icePositionLabel = new Label();
            icePositionLabel.Text = "Ice Position (X):";
            icePositionLabel.Location = new System.Drawing.Point(170, 50);
            icePositionLabel.Size = new System.Drawing.Size(90, 20);

            icePositionSelector = new NumericUpDown();
            icePositionSelector.Location = new System.Drawing.Point(270, 50);
            icePositionSelector.Size = new System.Drawing.Size(60, 20);
            icePositionSelector.Minimum = 1;
            icePositionSelector.Maximum = 24; // Basado en el ancho del mapa
            icePositionSelector.Value = 12;

            randomIcePositionCheckBox = new CheckBox();
            randomIcePositionCheckBox.Text = "Random Position";
            randomIcePositionCheckBox.Location = new System.Drawing.Point(340, 50);
            randomIcePositionCheckBox.Size = new System.Drawing.Size(120, 20);
            randomIcePositionCheckBox.CheckedChanged += RandomIcePositionCheckBox_CheckedChanged;

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
            this.Controls.Add(iceFloorLabel);
            this.Controls.Add(iceFloorSelector);
            this.Controls.Add(icePositionLabel);
            this.Controls.Add(icePositionSelector);
            this.Controls.Add(randomIcePositionCheckBox);
            this.Controls.Add(spawnIceEnemyButton);
            this.Controls.Add(sessionsListBox);

            // Timer para actualizar UI
            uiUpdateTimer = new Timer();
            uiUpdateTimer.Interval = 1000;
            uiUpdateTimer.Tick += UpdateUI;
            uiUpdateTimer.Start();
        }

        private void SpawnGroundEnemyButton_Click(object sender, EventArgs e)
        {
            foreach (var session in gameServer.GetActiveSessions())
            {
                session.SpawnEnemyNearPlayer();
            }
        }

        private void SpawnBirdEnemyButton_Click(object sender, EventArgs e)
        {
            foreach (var session in gameServer.GetActiveSessions())
            {
                session.SpawnBirdEnemy();
            }
        }

        private void SpawnIceEnemyButton_Click(object sender, EventArgs e)
        {
            // Convertir el valor del selector (1-9) a la fila real de la matriz
            int selectedFloor = (int)iceFloorSelector.Value;
            int actualFloorY = floorMapping[selectedFloor];
            
            foreach (var session in gameServer.GetActiveSessions())
            {
                if (randomIcePositionCheckBox.Checked)
                {
                    // Generar en posición aleatoria del piso especificado
                    session.SpawnIceEnemy(actualFloorY);
                }
                else
                {
                    // Generar en posición específica
                    int positionX = (int)icePositionSelector.Value;
                    session.SpawnIceEnemy(actualFloorY, positionX);
                }
            }
        }

        private void RandomIcePositionCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            // Habilitar/deshabilitar el selector de posición según el checkbox
            icePositionSelector.Enabled = !randomIcePositionCheckBox.Checked;
            icePositionLabel.Enabled = !randomIcePositionCheckBox.Checked;
        }

        private void UpdateUI(object sender, EventArgs e)
        {
            sessionsListBox.Items.Clear();
            foreach (var session in gameServer.GetActiveSessions())
            {
                int groundEnemies = 0;
                int birdEnemies = 0;
                int iceEnemies = 0;

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

                string sessionInfo = $"Session: {session.SessionId.Substring(0, 8)}... - " +
                                   $"Ground: {groundEnemies}, Birds: {birdEnemies}, Ice: {iceEnemies}";
                sessionsListBox.Items.Add(sessionInfo);
            }
        }

        public void StartServer()
        {
            gameServer.Start();
        }
    }
}