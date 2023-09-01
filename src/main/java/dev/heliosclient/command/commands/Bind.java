package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.ModuleArgumentType;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeycodeToString;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

public class Bind extends Command
{

    public Bind()
    {
        super("bind", "Binds command to a key", "b", "key");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("module", new ModuleArgumentType())
                .then(argument("key", IntegerArgumentType.integer())
                .executes(context -> {
                    ClientPlayerEntity player = mc.player;
                    assert player != null;

                    Module_ module = context.getArgument("module", Module_.class);
                    module.setKeybind(context.getArgument("key", Integer.class));

                    ChatUtils.sendHeliosMsg("Set keybind for module " + ColorUtils.aqua + module.name + ColorUtils.reset + " to " + ColorUtils.aqua + KeycodeToString.translate(module.getKeybind()) + ColorUtils.reset + ".");
                    
                    return SINGLE_SUCCESS;
        })));


    }

}
