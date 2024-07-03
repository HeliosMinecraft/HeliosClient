package dev.heliosclient.util.interfaces;

import com.moandjiezana.toml.Toml;

import java.util.List;
import java.util.Map;

public interface ISaveAndLoad {
    Object saveToFile(List<Object> list);

    void loadFromFile(Map<String, Object> MAP);
}
