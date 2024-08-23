package dev.heliosclient.module.modules.chat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.ChatMessageEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringSetting;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatTweaks extends Module_ {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting slideInAnimation = sgGeneral.add(new BooleanSetting.Builder()
            .name("Slide In Animation")
            .description("Animates chat messages to look like they are sliding in from left side")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public DoubleSetting time = sgGeneral.add(new DoubleSetting.Builder()
            .name("Time")
            .description("Time for the slide in animation in milliseconds")
            .onSettingChange(this)
            .defaultValue(100d)
            .range(0, 1000)
            .roundingPlace(0)
            .shouldRender(() -> slideInAnimation.value)
            .build()
    );
    public BooleanSetting removeMessageIndicator = sgGeneral.add(new BooleanSetting.Builder()
            .name("Remove Message Indicator")
            .description("Removes line left to the messages")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting noKeyRestriction = sgGeneral.add(new BooleanSetting.Builder()
            .name("No key restriction")
            .description("Allows you to write whatever you want into the chat-box")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting keepHistory = sgGeneral.add(new BooleanSetting.Builder()
            .name("Keep history")
            .description("Keeps your chat history even after disconnecting")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting longerChatBox = sgGeneral.add(new BooleanSetting.Builder()
            .name("Longer chat box")
            .description("Allows you to enter basically infinite text into the chat box")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting appendTimeStamp = sgGeneral.add(new BooleanSetting.Builder()
            .name("TimeStamp")
            .description("Shows the time stamp on which the message was received")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    public BooleanSetting logYourDeathPos = sgGeneral.add(new BooleanSetting.Builder()
            .name("Log your death pos")
            .description("Logs your death pos in a simple way")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    SettingGroup sgPrefix = new SettingGroup("Prefix");
    public BooleanSetting prefix = sgPrefix.add(new BooleanSetting.Builder()
            .name("Prefix")
            .description("Adds prefix to your chat msgs")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    public StringSetting prefixTxt = sgPrefix.add(new StringSetting.Builder()
            .name("Prefix Text")
            .onSettingChange(this)
            .defaultValue(">")
            .shouldRender(() -> prefix.value)
            .build()
    );
    SettingGroup sgSuffix = new SettingGroup("Suffix");
    public BooleanSetting suffix = sgSuffix.add(new BooleanSetting.Builder()
            .name("Suffix")
            .description("Adds suffix to your chat msgs")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    public StringSetting suffixTxt = sgSuffix.add(new StringSetting.Builder()
            .name("Suffix Text")
            .onSettingChange(this)
            .defaultValue(" | HeliosClient On Top! |")
            .shouldRender(() -> suffix.value)
            .build()
    );
    public BooleanSetting antiantiSpam = sgSuffix.add(new BooleanSetting.Builder()
            .name("Anti - AntiSpam")
            .description("Adds random letters at the end of message of given length to bypass anti spams")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting antispam_length = sgSuffix.add(new DoubleSetting.Builder()
            .name("Anti-Anti Spam length")
            .description("Length of the random letters added at the end of the message")
            .onSettingChange(this)
            .value(10d)
            .defaultValue(10d)
            .min(0)
            .max(256)
            .roundingPlace(0)
            .shouldRender(() -> antiantiSpam.value)
            .build()
    );

    public ChatTweaks() {
        super("ChatTweaks", "Tweaks various parts of your chat.", Categories.MISC);

        addSettingGroup(sgGeneral);
        addSettingGroup(sgPrefix);
        addSettingGroup(sgSuffix);

        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent e) {
        if (appendTimeStamp.value) {
            e.cancel();
            e.setMessage(Text.literal("<" + dateFormat.format(new Date()) + "> ").copy().append(e.getMessage()));
        }
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!logYourDeathPos.value) return;

        String playerPos = String.format("x= %f,y= %f, z= %f", event.getPlayer().getPos().x, event.getPlayer().getPos().y, event.getPlayer().getPos().z);
        ChatUtils.sendHeliosMsg("You just died at " + playerPos);
    }

    public boolean noKeyRestriction() {
        return isActive() && noKeyRestriction.value;
    }
}
