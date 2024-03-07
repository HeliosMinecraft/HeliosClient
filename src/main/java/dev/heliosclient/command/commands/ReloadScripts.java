package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.command.Command;
import dev.heliosclient.scripting.LuaScriptManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.command.CommandSource;

public class ReloadScripts extends Command {
    public ReloadScripts() {
        super("reloadScripts", "Reload all Lua Scripts");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes((context -> {
            LuaScriptManager.getScripts();
            ChatUtils.sendHeliosMsg(ColorUtils.green + "Successfully reloaded all lua scripts");
            return SINGLE_SUCCESS;
        }));
    }
}
