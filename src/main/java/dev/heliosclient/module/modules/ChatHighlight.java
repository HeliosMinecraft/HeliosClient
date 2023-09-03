package dev.heliosclient.module.modules;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.system.Friend;
import dev.heliosclient.system.FriendManager;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;

import java.util.HashSet;

public class ChatHighlight extends Module_ {

    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public HashSet<String> highlightList = new HashSet<String>();
    BooleanSetting highlightUsername = new BooleanSetting("Highlight Username",
            "Whether to highlight your username or not.", this, true);
    BooleanSetting highlightFriends = new BooleanSetting("Highlight Friends",
            "Whether to highlight your friend's usernames or not.", this, false);

    public ChatHighlight() {
        super("Chat Highlighter", "Highlights specified words in chat messages.", Category.CHAT);

        settings.add(highlightUsername);
        settings.add(highlightFriends);

        quickSettings.add(highlightUsername);
        quickSettings.add(highlightFriends);

        this.highlightList.add("ElBe_Gaming");
        ChatUtils.sendHeliosMsg("" + this.highlightList);
    }

    public String onMessage(String message) {
        if (highlightUsername.value) {
            this.highlightList.add(mc.getSession().getUsername());
        }

        if (highlightFriends.value) {
            for (Friend friend : FriendManager.getFriends()) {
                this.highlightList.add(friend.getPlayerName());
            }
        }

        ChatUtils.sendHeliosMsg(String.join(", ", this.highlightList));

        for (String text : this.highlightList) {
            ChatUtils.sendHeliosMsg("Looping over highlighted list: " + text + " - " + message.contains(text));
            if (message.contains(text)) {
                ChatUtils.sendHeliosMsg("Found " + text + " in " + message);
                message = message.replace(text, ColorUtils.red + text + ColorUtils.reset);
            }
        }

        return message;
    }
}
