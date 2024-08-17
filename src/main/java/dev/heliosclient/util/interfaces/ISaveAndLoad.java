package dev.heliosclient.util.interfaces;

import java.util.List;
import java.util.Map;

public interface ISaveAndLoad {
    //Json Serializar??? Never heard of it. Seriously, someone fix this abomination.
    Object saveToFile(List<Object> list);

    void loadFromFile(Map<String, Object> MAP);
}
