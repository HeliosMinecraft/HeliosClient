package dev.heliosclient.module.modules;

import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.MinecraftClient;

import java.util.HashSet;

public class ChatHighlight extends Module_ {

    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public HashSet<String> highlightList = new HashSet<String>();
    private final SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting highlightUsername = sgGeneral.add(new BooleanSetting.Builder()
            .name("Highlight Username")
            .description("Whether to highlight your username or not.")
            .module(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting highlightFriends = sgGeneral.add(new BooleanSetting.Builder()
            .name("Highlight Friends")
            .description("Whether to highlight friend's username or not.")
            .module(this)
            .value(false)
            .defaultValue(true)
            .build()
    );

    public ChatHighlight() {
        super("Chat Highlighter", "Highlights specified words in chat messages.", Categories.CHAT);

        addSettingGroup(sgGeneral);

        addQuickSettingGroup(sgGeneral);

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
