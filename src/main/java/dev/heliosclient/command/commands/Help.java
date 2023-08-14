package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.CommandManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.command.CommandSource;

public class Help extends Command
{
    public Help() 
    {
		super("help", "Gives you a list of all of the commands");
	}

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) 
    {
        builder.executes(context -> 
        {
			for (Command cmd : CommandManager.get().getAll()) {
				ChatUtils.sendMsg(ColorUtils.aqua + "Command: " + ColorUtils.gray + cmd.getName());
				ChatUtils.sendMsg(ColorUtils.gray + cmd.getDescription());
			}
			return SINGLE_SUCCESS;
		});
        
    }
    
}
