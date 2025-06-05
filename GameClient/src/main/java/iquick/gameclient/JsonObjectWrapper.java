package iquick.gameclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Wrapper para JsonObject que proporciona métodos de acceso simplificados
 * para obtener valores específicos del objeto JSON.
 * 
 * Esta clase encapsula un JsonObject de Gson y ofrece métodos convenientes
 * para acceder a tipos de datos específicos sin necesidad de realizar
 * conversiones manuales repetitivas.
 */
public class JsonObjectWrapper {
    // El objeto JSON encapsulado que contiene los datos
    private final JsonObject json;

    /**
     * Constructor que inicializa el wrapper con un JsonObject.
     * 
     * @param json El objeto JSON que será encapsulado por este wrapper
     */
    public JsonObjectWrapper(JsonObject json) {
        this.json = json;
    }

    /**
     * Obtiene un valor entero del JSON usando la clave especificada.
     * 
     * @param key La clave del campo que contiene el valor entero
     * @return El valor entero asociado con la clave
     * @throws NumberFormatException si el valor no puede ser convertido a int
     * @throws NullPointerException si la clave no existe en el JSON
     */
    public int getInt(String key) {
        return json.get(key).getAsInt();
    }

    /**
     * Obtiene un array JSON usando la clave especificada.
     * 
     * @param key La clave del campo que contiene el array JSON
     * @return El JsonArray asociado con la clave
     * @throws ClassCastException si el valor no es un array JSON
     * @throws NullPointerException si la clave no existe en el JSON
     */
    public JsonArray getArray(String key) {
        return json.getAsJsonArray(key);
    }

    /**
     * Proporciona acceso directo al JsonObject encapsulado.
     * 
     * Este método permite acceder al objeto JSON original para casos
     * donde se necesite funcionalidad más avanzada que no está cubierta
     * por los métodos wrapper específicos.
     * 
     * @return El JsonObject original sin modificaciones
     */
    public JsonObject getRaw() {
        return json;
    }
}