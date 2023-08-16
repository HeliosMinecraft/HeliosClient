package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.heliosclient.command.Command;
import dev.heliosclient.command.CommandManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.command.CommandSource;

public class Help extends Command
{
	// TODO (ElBe): Add optional "command" argument to show specific help and help about arguments

    public Help() 
    {
		super("help", "Gives you a list of all of the commands", "c", "commands", "h");
	}

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) 
    {
        builder.executes(context -> 
		{
			ChatUtils.sendMsg(ColorUtils.bold + ColorUtils.yellow + "Commands:");

			for (Command cmd : CommandManager.get().getAll()) {
				List<String> aliases = new ArrayList<>();

				for (String alias : cmd.getAliases()) aliases.add(alias);
				aliases.add(0, ColorUtils.bold + ColorUtils.aqua + cmd.getName());

				ChatUtils.sendMsg(ColorUtils.aqua + String.join(ColorUtils.reset + ", ", aliases) + ColorUtils.gray + ": " + cmd.getDescription());
			}
			return SINGLE_SUCCESS;
		});
        
    }
    
}
