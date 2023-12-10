package dev.heliosclient.util.interfaces;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.util.Map;

public interface ISaveAndLoad {
    Map<String, Object> saveToToml(Map<String, Object> MAP);
    void loadFromToml(Map<String, Object> MAP, Toml toml);
}
