package dev.heliosclient.util.interfaces;

import com.moandjiezana.toml.Toml;

import java.util.List;
import java.util.Map;

public interface ISaveAndLoad {
    Object saveToToml(List<Object> list);

    void loadFromToml(Map<String, Object> MAP, Toml toml);
}
