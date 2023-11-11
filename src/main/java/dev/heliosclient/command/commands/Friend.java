package dev.heliosclient.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.heliosclient.command.Command;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.command.CommandSource;

public class Friend extends Command {

    public Friend() {
        super("friend", "Adds or removes friend", "f");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Define the subcommands for adding and removing friends
        builder.then(literal("add")
                        .then(argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    // Get the command source and the name argument
                                    String name = StringArgumentType.getString(context, "name");

                                    // Create a new friend object with the name
                                    dev.heliosclient.system.Friend friend = new dev.heliosclient.system.Friend(name);
                                    // Add the friend to the list
                                    if (FriendManager.isFriend(friend)) {
                                        ChatUtils.sendHeliosMsg(ColorUtils.red + "You are already friends with " + name);
                                    } else {
                                        FriendManager.addFriend(friend);

                                        // Send a feedback message that the friend is added
                                        ChatUtils.sendHeliosMsg(ColorUtils.green + "You are now friends with " + name);
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
                                        // Remove the friend from the list
                                        FriendManager.removeFriend(friend);
                                        ChatUtils.sendHeliosMsg(ColorUtils.green + "You are no longer friends with " + name);
                                    } else {
                                        // Send an error message that the friend is not in the list
                                        ChatUtils.sendHeliosMsg(ColorUtils.red + "You are not friends with " + name);
                                    }
                                    return SINGLE_SUCCESS;
                                })
                        )
                )
                .then(literal("list")
                        .executes(context -> {
                            FriendManager friendManager = new FriendManager();
                            if (!friendManager.getFriendsName().isEmpty()) {
                                ChatUtils.sendHeliosMsg("You are friends with " + ColorUtils.green
                                        + String.join(", ", friendManager.getFriendsName()));
                            } else {
                                ChatUtils.sendHeliosMsg("You don't have any friends. :(");
                            }

                            return SINGLE_SUCCESS;
                        })
                );

    }
}

