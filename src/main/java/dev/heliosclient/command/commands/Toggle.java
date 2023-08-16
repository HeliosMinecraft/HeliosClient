package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.ModuleArgumentType;
import dev.heliosclient.module.Module_;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

public class Toggle extends Command
{

    public Toggle() 
    {
        super("toggle", "Toggle a module.", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) 
    {
        builder.then(argument("module", new ModuleArgumentType()).executes(context ->
        {
            ClientPlayerEntity player = mc.player;
            assert player != null;

            Module_ m = context.getArgument("module", Module_.class);
            m.toggle();

            return SINGLE_SUCCESS;
        }));
    }
    
}
