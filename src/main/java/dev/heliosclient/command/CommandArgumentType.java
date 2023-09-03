package dev.heliosclient.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandArgumentType implements ArgumentType<Command> {
    private static final CommandManager INSTANCE = CommandManager.get();

    private static final Collection<String> EXAMPLES = new ArrayList<Command>(INSTANCE.getAll())
            .stream()
            .limit(3)
            .map(command -> INSTANCE.getPrefix() + command.getName())
            .collect(Collectors.toList());

    private static final DynamicCommandExceptionType NO_SUCH_COMMAND = new DynamicCommandExceptionType(name ->
            Text.literal("Command with name " + name + " doesn't exist."));

    public static CommandArgumentType command() {
        return new CommandArgumentType();
    }

    public static Command getCommand(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Command.class);
    }

    @Override
    public Command parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString().substring(INSTANCE.getPrefix().length());
        Command command = INSTANCE.getCommandByName(argument);

        if (command == null) throw NO_SUCH_COMMAND.create(argument);

        return command;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(INSTANCE.getAll().stream().map(command -> INSTANCE.getPrefix() + command.getName()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
