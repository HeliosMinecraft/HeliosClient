package dev.heliosclient.util.misc;

import dev.heliosclient.util.MathUtils;

import java.util.Map;

public record MapReader(Map<String, Object> map) {

    public int getInt(String key, int defaultVal) {
        return MathUtils.o2iSafe(map.getOrDefault(key, defaultVal));
    }

    public long getLong(String key, long defaultVal) {
        return (long) map.getOrDefault(key, defaultVal);
    }

    public double getDouble(String key, double defaultVal) {
        return (double) map.getOrDefault(key, defaultVal);
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        return (boolean) map.getOrDefault(key, defaultVal);
    }

    public String getString(String key, String defaultVal) {
        if (map.getOrDefault(key, defaultVal) instanceof String s) {
            return s;
        }
        return defaultVal;
    }

    public <T> T getAs(String key, Class<T> clazz) {
        return clazz.cast(map.get(key));
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public MapReader getMap(String key) {
        Map<String, Object> nestedMap = (Map<String, Object>) map.get(key);
        if (nestedMap == null) {
            return null;
        }
        return new MapReader(nestedMap);
    }
}
