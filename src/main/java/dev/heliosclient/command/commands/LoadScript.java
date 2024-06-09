package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.command.Command;
import dev.heliosclient.scripting.LuaFile;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.command.CommandSource;

public class LoadScript extends Command {
    public LoadScript() {
        super("loadScript", "Loads a lua script");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("scriptname", StringArgumentType.word())
                .executes(context -> {
                    String scriptname = StringArgumentType.getString(context, "scriptname");
                    for (LuaFile file : LuaScriptManager.luaFiles) {
                        if (file.getScriptName().equalsIgnoreCase(scriptname)) {
                            LuaScriptManager.INSTANCE.loadScript(file);
                            ChatUtils.sendHeliosMsg(ColorUtils.green + "Loaded script " + ColorUtils.darkGray + "[" + ColorUtils.aqua + file.getFile().getName() + ColorUtils.darkGray + "]");
                            return SINGLE_SUCCESS;
                        }
                    }

                    ChatUtils.sendHeliosMsg(ColorUtils.red + "Script of name {"+ scriptname +"} not found");
                    return SINGLE_SUCCESS;
                }));
    }
}
