package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.CommandManager;
import dev.heliosclient.command.ModuleArgumentType;
import dev.heliosclient.module.Module_;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

public class Prefix extends Command
{

    public Prefix()
    {
        super("prefix", "Sets the command prefix.", "p");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("prefix", StringArgumentType.word())
                .executes(context -> {
                    ClientPlayerEntity player = mc.player;
                    assert player != null;

                    String prefix = context.getArgument("prefix", String.class);
                    CommandManager.prefix = prefix;

                    return SINGLE_SUCCESS;
        }));
    }
}
