package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.heliosclient.command.Command;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;

public class Teleport extends Command
{

    public Teleport() 
    {
        super("teleport", "Teleports you to specified coordinates.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) 
    {
        builder
            .then(argument("x", DoubleArgumentType.doubleArg())
                .then(argument("y", DoubleArgumentType.doubleArg())
                    .then(argument("z", DoubleArgumentType.doubleArg())
            .executes(context -> 
        {
            ClientPlayerEntity player = mc.player;
            assert player != null;

            double x = context.getArgument("x", Double.class);
            double y = context.getArgument("y", Double.class);
            double z = context.getArgument("z", Double.class);

            if (player.hasVehicle()) 
            {
                Entity vehicle = player.getVehicle();
                vehicle.setPosition(x, y, z);
            }
            player.setPosition(x, y, z);

            return SINGLE_SUCCESS;
        }))));
    }
    
}
