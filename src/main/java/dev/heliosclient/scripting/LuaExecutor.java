package dev.heliosclient.scripting;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.*;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.world.biome.BiomeKeys;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * This class provides an execution environment for Lua scripts.
 */
public class LuaExecutor {
    private Globals globals;
    /**
     * Constructs a new LuaExecutor with the given Minecraft client, Helios client, and Lua event manager instance.
     *
     * @param mc The Minecraft client.
     * @param eventManager The Lua event manager.
     */
    public LuaExecutor(MinecraftClient mc, LuaEventManager eventManager) {
        globals = JsePlatform.standardGlobals();
        globals.load(new DebugLib());

        globals.set("mc", CoerceJavaToLua.coerce(mc));
        globals.set("hc", CoerceJavaToLua.coerce(HeliosClient.INSTANCE));
        globals.set("eventManager", CoerceJavaToLua.coerce(eventManager));
        globals.set("ChatUtils", CoerceJavaToLua.coerce(ChatUtils.class));
        globals.set("InventoryUtils", CoerceJavaToLua.coerce(InventoryUtils.class));
        globals.set("PlayerUtils", CoerceJavaToLua.coerce(PlayerUtils.class));
        globals.set("MathUtils", CoerceJavaToLua.coerce(MathUtils.class));
        globals.set("DamageUtils", CoerceJavaToLua.coerce(DamageUtils.class));
        globals.set("SoundUtils", CoerceJavaToLua.coerce(SoundUtils.class));
        globals.set("EntityUtils", CoerceJavaToLua.coerce(EntityUtils.class));
        globals.set("ColorUtils", CoerceJavaToLua.coerce(ColorUtils.class));
        globals.set("Renderer2D", CoerceJavaToLua.coerce(Renderer2D.class));
        globals.set("Renderer3D", CoerceJavaToLua.coerce(Renderer3D.class));
        globals.set("EntityType", CoerceJavaToLua.coerce(EntityType.class));
        globals.set("Items", CoerceJavaToLua.coerce(Items.class));
        globals.set("Biomes", CoerceJavaToLua.coerce(BiomeKeys.class));
        globals.set("Blocks", CoerceJavaToLua.coerce(Blocks.class));

        for(Module_ module: ModuleManager.INSTANCE.modules){
            globals.set(module.name, CoerceJavaToLua.coerce(module));
        }
    }

    /**
     * Loads a Lua script from a reader.
     *
     * @param reader The reader of the Lua script.
     * @return The loaded Lua script.
     */
    public LuaValue load(Reader reader) {
        return globals.load(reader, "script");
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
}
