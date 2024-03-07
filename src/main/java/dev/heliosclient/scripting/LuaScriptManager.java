package dev.heliosclient.scripting;

import dev.heliosclient.module.modules.misc.ScriptModule;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the lifecycle of Lua scripts.
 */
public class LuaScriptManager {
    public static LuaScriptManager INSTANCE = new LuaScriptManager();
    static LuaLoader luaLoader = new LuaLoader();
    public static List<LuaFile> luaFiles = new ArrayList<>();

    /**
     * Retrieves all Lua scripts.
     */
    public static void getScripts(){
        luaFiles.addAll(luaLoader.getScripts());

        ChatUtils.sendHeliosMsg(ColorUtils.green + "Reloaded all scripts successfully");
    }

    /**
     * Loads a Lua script file.
     *
     * @param luaFile The Lua file to load.
     */
    public void loadScript(LuaFile luaFile){
        luaLoader.load(luaFile);
    }

    /**
     * Reloads a Lua script file.
     *
     * @param luaFile The Lua file to reload.
     */
    public static void reloadScript(LuaFile luaFile){
        if(luaFiles.contains(luaFile)) {
            int index = luaFiles.indexOf(luaFile);
            luaFiles.set(index, luaLoader.getScript(luaFile));
            ChatUtils.sendHeliosMsg(ColorUtils.gold + "Reloaded script "+ ColorUtils.blue + luaFile.getAbsolutePath() + ColorUtils.green +" successfully");
        }else{
            // Just add the file and hope that the system reloads the script again sometime
            luaFiles.add(luaLoader.getScript(luaFile));
        }
    }
    /**
     * Toggles on/off a Lua script file.
     *
     * @param luaFile The Lua file to reload.
     */
    public static void toggleScript(LuaFile luaFile){
       luaLoader.toggleScript(luaFile);
    }
    /**
     * Closes a Lua script file.
     *
     * @param luaFile The Lua file to close.
     */
    public void closeScript(LuaFile luaFile){
        luaLoader.close(luaFile);
    }

}
