using System;
using System.Windows.Forms;
using PlatformGameServer.Networking;
using Timer = System.Windows.Forms.Timer;

namespace PlatformGameServer.UI
{
    public partial class ServerUI : Form
    {
        private GameServer gameServer;
        private Button? spawnEnemyButton;
        private ListBox? sessionsListBox;
        private Timer uiUpdateTimer;

        public ServerUI()
        {
            InitializeComponent();
            gameServer = new GameServer();
        }

        private void InitializeComponent()
        {
            this.Text = "Game Server Control";
            this.Size = new System.Drawing.Size(400, 300);

            spawnEnemyButton = new Button();
            spawnEnemyButton.Text = "Spawn Enemy";
            spawnEnemyButton.Location = new System.Drawing.Point(10, 10);
            spawnEnemyButton.Size = new System.Drawing.Size(100, 30);
            spawnEnemyButton.Click += SpawnEnemyButton_Click;

            sessionsListBox = new ListBox();
            sessionsListBox.Location = new System.Drawing.Point(10, 50);
            sessionsListBox.Size = new System.Drawing.Size(360, 200);

            this.Controls.Add(spawnEnemyButton);
            this.Controls.Add(sessionsListBox);

            uiUpdateTimer = new Timer();
            uiUpdateTimer.Interval = 1000;
            uiUpdateTimer.Tick += UpdateUI;
            uiUpdateTimer.Start();
        }

        private void SpawnEnemyButton_Click(object sender, EventArgs e)
        {
            foreach (var session in gameServer.GetActiveSessions())
            {
                session.SpawnEnemyNearPlayer();
            }
        }

        private void UpdateUI(object sender, EventArgs e)
        {
            sessionsListBox.Items.Clear();
            foreach (var session in gameServer.GetActiveSessions())
            {
                sessionsListBox.Items.Add($"Session: {session.SessionId} - Enemies: {session.Enemies.Count}");
            }
        }

        public void StartServer()
        {
            gameServer.Start();
        }
    }
}
