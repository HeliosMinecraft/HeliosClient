package dev.heliosclient.command.commands;

// Import the necessary packages
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.command.Command;
import dev.heliosclient.system.FriendManager;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.command.CommandSource;

// Define the command class and extend the Command class
public class Friend extends Command {
    // Create a constructor that takes the command name, description, and aliases
    public Friend() {
        super("friend", "Adds or removes friend",  "f");
    }

    // Override the build method
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Define the subcommands for adding and removing friends
        builder.then(literal("add")
                        .then(argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    // Get the command source and the name argument
                                    String name = StringArgumentType.getString(context, "name");

                                        // Create a new friend object with the name
                                        dev.heliosclient.system.Friend friend = new  dev.heliosclient.system.Friend(name);
                                            // Add the friend to the list
                                    if (FriendManager.isFriend(friend)) {
                                        ChatUtils.sendHeliosMsg("You are already friends with " + name);
                                    } else {
                                        FriendManager.addFriend(friend);

                                        // Send a feedback message that the friend is added
                                        ChatUtils.sendHeliosMsg("You are now friends with " + name);
                                    }

                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");

                                    // Create a new friend object with the name
                                    dev.heliosclient.system.Friend friend = new dev.heliosclient.system.Friend(name);

                                    if (FriendManager.isFriend(friend)) {
                                        // Send an error message that the friend is not in the list
                                        ChatUtils.sendHeliosMsg("You are not friends with " + name);
                                    } else {
                                        // Remove the friend from the list
                                        FriendManager.removeFriend(friend);
                                        ChatUtils.sendHeliosMsg("You are no longer friends with " + name);

                                    }

                                    return SINGLE_SUCCESS;
                                })
                        )
                );
    }
}

