package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.command.Command;
import dev.heliosclient.managers.CommandManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.color.ColorUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

public class Prefix extends Command {

    public Prefix() {
        super("prefix", "Sets the command prefix.", "p");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("prefix", StringArgumentType.word())
                .executes(context -> {
                    ClientPlayerEntity player = mc.player;
                    assert player != null;

                    String prefix = context.getArgument("prefix", String.class);
                    CommandManager.prefix = prefix;

                    ChatUtils.sendHeliosMsg("Changed prefix to " + ColorUtils.aqua + prefix + ColorUtils.reset + ".");

                    return SINGLE_SUCCESS;
                }));
    }
}
