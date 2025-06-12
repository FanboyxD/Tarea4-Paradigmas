package iquick.client;
import java.awt.Graphics2D;

public class FruitManager {
    private static final int MAX_FRUITS = 4;
    private Fruit[] fruits;
    private Player playerReference; // Referencia al jugador para obtener su posición


    public FruitManager() {
        fruits = new Fruit[MAX_FRUITS];
        for (int i = 0; i < MAX_FRUITS; i++) {
            fruits[i] = new Fruit(0, 0, Fruit.Type.LECHUGA); // Inicializar con tipo NARANJA
        }
    }

    // Método para establecer la referencia al jugador
    public void setPlayerReference(Player player) {
        this.playerReference = player;
    }

    public void updateFruits(Fruit[] newFruits) {
        if (newFruits != null && newFruits.length <= MAX_FRUITS) {
            for (int i = 0; i < newFruits.length && i < MAX_FRUITS; i++) {
                if (newFruits[i] != null) {
                    // Copiar los datos del nuevo fruto
                    fruits[i].spawn(newFruits[i].getX(), newFruits[i].getY());
                }
            }
        }
    }

    public Fruit[] getFruits() {
        return fruits;
    }

    public void drawAll(Graphics2D g) {
        for (Fruit fruit : fruits) {
            if (!fruit.isCollected()) {
                fruit.draw(g);
            }
        }
    }
}