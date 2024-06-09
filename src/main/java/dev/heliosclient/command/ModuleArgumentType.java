package dev.heliosclient.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModuleArgumentType implements ArgumentType<Module_> {
    private static final Collection<String> EXAMPLES = ModuleManager.getModules()
            .stream()
            .limit(3)
            .map(module -> addQuotes(module.name))
            .collect(Collectors.toList());

    private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(o ->
            Text.literal("Module with name " + o + " doesn't exist."));

    public static ModuleArgumentType module() {
        return new ModuleArgumentType();
    }

    public static Module_ getModule(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Module_.class);
    }

    private static String addQuotes(String input) {
        if (input.contains(" ")) {
            return "\"" + input + "\"";
        } else {
            return input;
        }
    }

    @Override
    public Module_ parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Module_ module = ModuleManager.getModuleByName(argument.replace("\"", ""));

        if (module == null) throw NO_SUCH_MODULE.create(argument);

        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ModuleManager.getModules().stream().map(module -> addQuotes(module.name)), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
