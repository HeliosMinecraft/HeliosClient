package dev.heliosclient.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class ModuleArgumentType implements ArgumentType<Module_>
{
    private static final Collection<String> EXAMPLES = ModuleManager.INSTANCE.modules
            .stream()
            .limit(3)
            .map(module -> addQuotes(module.name))
            .collect(Collectors.toList());

    private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(o ->
            Text.literal("Module with name " + o + " doesn't exist."));

    public static ModuleArgumentType module() 
    {
        return new ModuleArgumentType();
    }

    public static Module_ getModule(final CommandContext<?> context, final String name) 
    {
        return context.getArgument(name, Module_.class);
    }

    @Override
    public Module_ parse(StringReader reader) throws CommandSyntaxException 
    {
        String argument = reader.readString();
        Module_ module = ModuleManager.INSTANCE.getModuleByName(argument.replace("\"", ""));

        if (module == null) throw NO_SUCH_MODULE.create(argument);

        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) 
    {
        return CommandSource.suggestMatching(ModuleManager.INSTANCE.modules.stream().map(module -> addQuotes(module.name)), builder);
    }

    @Override
    public Collection<String> getExamples() 
    {
        return EXAMPLES;
    }

    private static String addQuotes(String input) {
        if (input.contains(" ")) {
            return "\"" + input + "\"";
        } else {
            return input;
        }
    }
}
