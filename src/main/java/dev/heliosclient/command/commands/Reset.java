package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.command.Command;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.command.CommandSource;

public class Reset extends Command {
    public Reset() {
        super("reset", "Resets all config options.", "r");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context ->
        {
            HeliosClient.CONFIG.getDefaultModuleConfig();

            ChatUtils.sendHeliosMsg("Reset config.");

            return SINGLE_SUCCESS;
        });
    }
}
