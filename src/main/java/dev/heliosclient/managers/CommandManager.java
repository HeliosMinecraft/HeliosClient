package dev.heliosclient.managers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.addon.HeliosAddon;
import dev.heliosclient.command.Command;
import dev.heliosclient.command.commands.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

import java.util.*;

public class CommandManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static String prefix = ".";
    private final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    private final CommandSource COMMAND_SOURCE = new ChatCommandSource(mc);
    private final List<Command> commands = new ArrayList<>();
    private final Map<Class<? extends Command>, Command> commandInstances = new HashMap<>();
    private static CommandManager INSTANCE;

    private CommandManager() {
        add(new VClip());
        add(new Help());
        add(new Toggle());
        add(new Teleport());
        add(new Bind());
        add(new Reset());
        add(new Friend());
        add(new Prefix());
        add(new ReloadScripts());
        add(new LoadScript());

        AddonManager.HELIOS_ADDONS.forEach(HeliosAddon::registerCommand);

        commands.sort(Comparator.comparing(Command::getName));
    }

    public static CommandManager get() {
        if(INSTANCE == null){
            INSTANCE = new CommandManager();
        }
        return INSTANCE;
    }

    public void dispatch(String message) throws CommandSyntaxException {
        dispatch(message, new ChatCommandSource(mc));
    }

    public void dispatch(String message, CommandSource source) throws CommandSyntaxException {
        ParseResults<CommandSource> results = DISPATCHER.parse(message, source);
        DISPATCHER.execute(results);
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public CommandSource getCommandSource() {
        return COMMAND_SOURCE;
    }

    public void add(Command command) {
        commands.removeIf(command1 -> command1.getName().equals(command.getName()));
        commandInstances.values().removeIf(command1 -> command1.getName().equals(command.getName()));

        command.registerTo(DISPATCHER);
        commands.add(command);
        commandInstances.put(command.getClass(), command);

        commands.sort(Comparator.comparing(Command::getName));
    }

    public Command getCommandByName(String commandName) {
        for (Command command : commands) {
            if ((command.getName().trim().equalsIgnoreCase(commandName))) {
                return command;
            }
        }
        return null;
    }

    public int getCount() {
        return commands.size();
    }

    public List<Command> getAll() {
        return commands;
    }

    @SuppressWarnings("unchecked")
    public <T extends Command> T get(Class<T> klass) {
        return (T) commandInstances.get(klass);
    }

    public String getPrefix() {
        return prefix == null ? prefix = "." : prefix;
    }

    private final static class ChatCommandSource extends ClientCommandSource {
        public ChatCommandSource(MinecraftClient client) {
            super(null, client);
        }
    }
}
