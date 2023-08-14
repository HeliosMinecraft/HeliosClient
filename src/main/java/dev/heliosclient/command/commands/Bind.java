package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.ModuleArgumentType;
import dev.heliosclient.module.Module_;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

public class Bind extends Command
{

    public Bind()
    {
        super("bind", "Binds command to a key");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("module", new ModuleArgumentType())
                .then(argument("key", IntegerArgumentType.integer())
                .executes(context ->
        {
            ClientPlayerEntity player = mc.player;
            assert player != null;

            Module_ m = context.getArgument("module", Module_.class);
            m.setKeybind(context.getArgument("key", Integer.class));

            return SINGLE_SUCCESS;
        })));


    }

}
