package iquick.gameclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonObjectWrapper {
    private final JsonObject json;

    public JsonObjectWrapper(JsonObject json) {
        this.json = json;
    }

    public int getInt(String key) {
        return json.get(key).getAsInt();
    }

    public JsonArray getArray(String key) {
        return json.getAsJsonArray(key);
    }

    public JsonObject getRaw() {
        return json;
    }
}
