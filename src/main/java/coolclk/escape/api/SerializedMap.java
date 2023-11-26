package coolclk.escape.api;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class SerializedMap {
    public static class MapObject {
        private final Object object;
        @Getter private final String key;

        public MapObject(String k, Object o) {
            this.key = k;
            this.object = o;
        }

        public <T> T get() {
            return (T) this.object;
        }

        public String getAsString() {
            return this.get();
        }

        public int getAsInteger() {
            return this.get();
        }

        public boolean getAsBoolean() {
            return this.get();
        }

        public double getAsDouble() {
            return this.get();
        }

        public <K, V> Map<K, V> getAsMap() {
            return this.get();
        }

        public SerializedMap getAsSerializedMap() {
            return new SerializedMap(this.<Map<String, Object>>get());
        }
    }

    private final Map<String, Object> map;

    public SerializedMap(MapObject... objects) {
        this(new HashMap<>());
        for (MapObject object : objects) {
            this.put(object.getKey(), object.get());
        }
    }

    public SerializedMap(Map<String, Object> map) {
        this.map = map;
    }

    public MapObject get(String key) {
        return new MapObject(key, map.getOrDefault(key, null));
    }

    public void put(String key, Object value) {
        this.map.put(key, value);
    }

    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }
}
