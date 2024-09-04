package dev.heliosclient.scripting;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.scripting.libraries.ChatLib;
import dev.heliosclient.scripting.libraries.PacketLib;
import dev.heliosclient.scripting.libraries.PlayerLib;
import dev.heliosclient.util.*;
import dev.heliosclient.util.player.DamageUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.player.RotationUtils;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.Reader;

/**
 * This class provides an execution environment for Lua scripts.
 */
public class LuaExecutor {
    private Globals globals;
    @Nullable
    public LuaFile luaFile;

    /**
     * Constructs a new LuaExecutor with the given Minecraft client, Helios client, and Lua event manager instance.
     *
     * @param mc           The Minecraft client.
     * @param eventManager The Lua event manager.
     */
    public LuaExecutor(MinecraftClient mc, LuaEventManager eventManager) {
        globals = JsePlatform.standardGlobals();
        globals.load(new DebugLib());

        globals.load(new PlayerLib());
        globals.load(new ChatLib());
        globals.load(new PacketLib());

        globals.set("mc", CoerceJavaToLua.coerce(mc));
        globals.set("hc", CoerceJavaToLua.coerce(HeliosClient.INSTANCE));
        globals.set("moduleManager", CoerceJavaToLua.coerce(ModuleManager.class));
        globals.set("eventManager", CoerceJavaToLua.coerce(eventManager));
        globals.set("ChatUtils", CoerceJavaToLua.coerce(ChatUtils.class));
        globals.set("InventoryUtils", CoerceJavaToLua.coerce(InventoryUtils.class));
        globals.set("PlayerUtils", CoerceJavaToLua.coerce(PlayerUtils.class));
        globals.set("MathUtils", CoerceJavaToLua.coerce(MathUtils.class));
        globals.set("DamageUtils", CoerceJavaToLua.coerce(DamageUtils.class));
        globals.set("SoundUtils", CoerceJavaToLua.coerce(SoundUtils.class));
        globals.set("EntityUtils", CoerceJavaToLua.coerce(EntityUtils.class));
        globals.set("ColorUtils", CoerceJavaToLua.coerce(ColorUtils.class));
        globals.set("RotationUtils", CoerceJavaToLua.coerce(RotationUtils.class));
        globals.set("Renderer2D", CoerceJavaToLua.coerce(Renderer2D.class));
        globals.set("Renderer3D", CoerceJavaToLua.coerce(Renderer3D.class));
        globals.set("EntityType", CoerceJavaToLua.coerce(EntityType.class));
        globals.set("Items", CoerceJavaToLua.coerce(Items.class));
        globals.set("Biomes", CoerceJavaToLua.coerce(BiomeKeys.class));
        globals.set("Blocks", CoerceJavaToLua.coerce(Blocks.class));
        globals.set("Vec3d", CoerceJavaToLua.coerce(Vec3d.class));
        globals.set("Box", CoerceJavaToLua.coerce(Box.class));
        globals.set("Hand", CoerceJavaToLua.coerce(Hand.class));
    }

    /**
     * Loads a Lua script from a reader.
     *
     * @param reader The reader of the Lua script.
     * @return The loaded Lua script.
     */
    public LuaValue load(Reader reader) {
        if(luaFile == null){
            throw new RuntimeException("Load called before LuaFile was initialised");
        }
        return globals.load(reader, luaFile.getScriptName());
    }

    /**
     * Retrieves a function from the Lua environment.
     *
     * @param name The name of the function.
     * @return The Lua function.
     */
    public LuaValue getFunction(String name) {
        return globals.get(name);
    }

    public Globals getGlobals() {
        return globals;
    }

    public void setGlobals(Globals globals) {
        this.globals = globals;
    }

    public void setLuaFile(LuaFile luaFile) {
        this.luaFile = luaFile;
    }
}
