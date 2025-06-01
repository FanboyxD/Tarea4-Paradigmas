using System;
using System.Windows.Forms;
using PlatformGameServer.UI;

namespace PlatformGameServer
{
    internal static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            Console.WriteLine("Iniciando servidor con interfaz gráfica...");

            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            ServerUI serverUI = new ServerUI();
            serverUI.StartServer();

            Application.Run(serverUI);
        }
    }
}