package dev.heliosclient.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class CommandArgumentType implements ArgumentType<Command>
{
	private static final Collection<String> EXAMPLES = new ArrayList<Command>(CommandManager.get().getAll())
			.stream()
            .limit(3)
            .map(command -> command.getName())
            .collect(Collectors.toList());

    private static final DynamicCommandExceptionType NO_SUCH_COMMAND = new DynamicCommandExceptionType(o ->
            Text.literal("Command with name " + o + " doesn't exist."));

    public static CommandArgumentType command() 
    {
        return new CommandArgumentType();
    }

    public static Command getCommand(final CommandContext<?> context, final String name) 
    {
        return context.getArgument(name, Command.class);
    }

    @Override
    public Command parse(StringReader reader) throws CommandSyntaxException 
    {
        String argument = reader.readString();
        Command command = CommandManager.get().getCommandByName(argument);

        if (command == null) throw NO_SUCH_COMMAND.create(argument);

        return command;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) 
    {
        return CommandSource.suggestMatching(CommandManager.get().getAll().stream().map(command -> command.getName()), builder);
    }

    @Override
    public Collection<String> getExamples() 
    {
        return EXAMPLES;
    }
}
