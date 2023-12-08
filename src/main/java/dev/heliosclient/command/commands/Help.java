package dev.heliosclient.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.command.Command;
import dev.heliosclient.command.CommandArgumentType;
import dev.heliosclient.command.ModuleArgumentType;
import dev.heliosclient.managers.CommandManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public class Help extends Command {
    public Help() {
        super("help", "Gives you a list of all of the commands", "c", "commands", "h");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("command", new CommandArgumentType()).executes(context -> {
            ClientPlayerEntity player = mc.player;
            assert player != null;

            Command command = context.getArgument("command", Command.class);

            ChatUtils.sendMsg(ColorUtils.bold + ColorUtils.yellow + command.getName());
            ChatUtils.sendMsg(command.getDescription());

            if (command.getAliases().size() > 0) {
                ChatUtils.sendMsg(
                        ColorUtils.aqua + "Aliases" + ColorUtils.gray + ": " + String.join(", ", command.getAliases()));
            }

            return SINGLE_SUCCESS;
        }));

        builder.then(argument("module", new ModuleArgumentType()).executes(context -> {
            ClientPlayerEntity player = mc.player;
            assert player != null;

            Module_ module = context.getArgument("module", Module_.class);

            ChatUtils.sendMsg(ColorUtils.bold + ColorUtils.yellow + module.name);
            ChatUtils.sendMsg(module.description);
            ChatUtils.sendMsg("");

            for (SettingGroup settingBuilder : module.settingGroups) {
                for (Setting setting : settingBuilder.getSettings()) {
                    ChatUtils.sendMsg(ColorUtils.aqua + setting.name + ColorUtils.gray + ": " + setting.description);
                }
            }

            return SINGLE_SUCCESS;
        }));
        builder.executes(context ->
        {
            ChatUtils.sendMsg(ColorUtils.bold + ColorUtils.yellow + "Commands:");

            for (Command cmd : CommandManager.get().getAll()) {

                List<String> aliases = new ArrayList<>(cmd.getAliases());
                aliases.add(0, ColorUtils.bold + ColorUtils.aqua + cmd.getName());

                ChatUtils.sendMsg(ColorUtils.aqua + String.join(ColorUtils.reset + ", ", aliases) + ColorUtils.gray + ": " + cmd.getDescription());
            }
            return SINGLE_SUCCESS;
        });

    }

}
