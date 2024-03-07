package dev.heliosclient.module.modules.misc;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.scripting.LuaScriptManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptModule extends Module_ {
    private static ScriptModule INSTANCE = new ScriptModule();
    Map<String, Object> MAP = new HashMap<>();
    private ScriptModule() {
        super("Lua-Scripts","Placeholder to save/load script binds", Categories.MISC);
    }

    @Override
    public Object saveToToml(List<Object> list) {
        Map<String, Object> config = (Map<String, Object>) super.saveToToml(list);

        for(int i = 0; i<LuaScriptManager.luaFiles.size(); i++) {
            LuaFile file = LuaScriptManager.luaFiles.get(i);
            config.put(file.getScriptName(),file.bindKey);
        }

        return config;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP, toml);
        for (int i = 0; i < LuaScriptManager.luaFiles.size(); i++) {
            LuaFile file = LuaScriptManager.luaFiles.get(i);
            if (MAP.containsKey(file.getScriptName())) {
                file.bindKey = (int) MAP.get(file.getScriptName());
            }
        }
        this.MAP = MAP;
    }
    public static ScriptModule get() {
        return INSTANCE;
    }
}
