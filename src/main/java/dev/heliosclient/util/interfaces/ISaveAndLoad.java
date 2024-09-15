package dev.heliosclient.util.interfaces;

import dev.heliosclient.util.misc.MapReader;

import java.util.List;

public interface ISaveAndLoad {
    //Json Serializar??? Never heard of it. Seriously, someone fix this abomination.
    Object saveToFile(List<Object> list);

    void loadFromFile(MapReader map);
}
