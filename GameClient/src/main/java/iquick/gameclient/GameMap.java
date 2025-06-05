package iquick.gameclient;

import com.google.gson.JsonArray;

/**
 * Permite cargar el mapa desde JSON y manipular sus elementos.
 */
public class GameMap {
    // Matriz bidimensional que almacena los valores de cada tile del mapa
    private int[][] tiles;
    // Ancho del mapa (número de columnas)
    private int width;
    // Alto del mapa (número de filas)
    private int height;

    /**
     * Carga el mapa desde un objeto JSON que contiene las dimensiones y los datos del mapa.
     * 
     * @param json Objeto JSON wrapper que contiene los datos del mapa
     *             Estructura esperada: {"width": int, "height": int, "map": [[int, int, ...], [int, int, ...], ...]}
     */
    public void loadFromJson(JsonObjectWrapper json) {
        // Obtiene las dimensiones del mapa desde el JSON
        this.width = json.getInt("width");
        this.height = json.getInt("height");

        // Obtiene el array bidimensional que representa el mapa
        JsonArray mapArray = json.getArray("map");
        // Inicializa la matriz de tiles con las dimensiones obtenidas
        tiles = new int[height][width];

        // Itera sobre cada fila del mapa
        for (int i = 0; i < height; i++) {
            // Obtiene la fila actual como JsonArray
            JsonArray row = mapArray.get(i).getAsJsonArray();
            // Itera sobre cada columna de la fila actual
            for (int j = 0; j < width; j++) {
                // Asigna el valor del tile en la posición [i][j]
                tiles[i][j] = row.get(j).getAsInt();
            }
        }
    }

    /**
     * Obtiene la matriz completa de tiles del mapa.
     * 
     * @return Matriz bidimensional con todos los tiles del mapa
     */
    public int[][] getTiles() {
        return tiles;
    }

    /**
     * Obtiene el valor de un tile específico en las coordenadas dadas.
     * 
     * @param x Coordenada X (columna)
     * @param y Coordenada Y (fila)
     * @return Valor del tile en la posición (x,y), o -1 si las coordenadas son inválidas
     */
    public int getTile(int x, int y) {
        if (isValid(x, y)) {
            // Nota: se accede como tiles[y][x] porque y representa la fila y x la columna
            return tiles[y][x];
        }
        // Retorna -1 como valor por defecto para coordenadas inválidas
        return -1;
    }

    /**
     * Establece el valor de un tile específico en las coordenadas dadas.
     * 
     * @param x Coordenada X (columna)
     * @param y Coordenada Y (fila)
     * @param value Nuevo valor para el tile
     */
    public void setTile(int x, int y, int value) {
        if (isValid(x, y)) {
            // Nota: se accede como tiles[y][x] porque y representa la fila y x la columna
            tiles[y][x] = value;
        }
        // Si las coordenadas son inválidas, no se hace nada (operación silenciosa)
    }

    /**
     * Obtiene el ancho del mapa (número de columnas).
     * 
     * @return Ancho del mapa
     */
    public int getWidth() {
        return width;
    }

    /**
     * Obtiene la altura del mapa (número de filas).
     * 
     * @return Altura del mapa
     */
    public int getHeight() {
        return height;
    }

    /**
     * Verifica si las coordenadas dadas están dentro de los límites válidos del mapa.
     * 
     * @param x Coordenada X (columna)
     * @param y Coordenada Y (fila)
     * @return true si las coordenadas están dentro de los límites, false en caso contrario
     */
    public boolean isValid(int x, int y) {
        // Verifica que x esté entre 0 y width-1, y que y esté entre 0 y height-1
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}