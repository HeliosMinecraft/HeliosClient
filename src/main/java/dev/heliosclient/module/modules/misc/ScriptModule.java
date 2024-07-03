package dev.heliosclient.module.modules.misc;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.scripting.LuaScriptManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


//Todo: LuaScripts rewrite - Elbe
@Deprecated(forRemoval = true)
public class ScriptModule extends Module_ {
    Map<String, Object> MAP = new HashMap<>();

    public ScriptModule() {
        super("Lua-Scripts", "Placeholder to save/load script binds", Categories.MISC);
    }
}
