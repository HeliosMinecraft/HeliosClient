package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.command.Command;
import dev.heliosclient.ui.clickgui.ConsoleScreen;
import net.minecraft.command.CommandSource;

public class OpenConsole extends Command {

    public OpenConsole() {
        super("console", "Open Console", "con");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.setScreen(HeliosClient.CONSOLE);
            return SINGLE_SUCCESS;
        });
    }

}
