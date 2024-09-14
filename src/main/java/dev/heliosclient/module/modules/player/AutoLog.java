package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.entity.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class AutoLog extends Module_ {
    boolean shouldLog = false;
    String logReason = "";
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting logOnCrystal = sgGeneral.add(new BooleanSetting.Builder()
            .name("Crystal")
            .description("Logs when near a crystal")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    DoubleSetting crystalRangeThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Crystal Range Threshold")
            .onSettingChange(this)
            .defaultValue(4D)
            .value(4D)
            .min(0)
            .max(50)
            .roundingPlace(1)
            .shouldRender(() -> logOnCrystal.value)
            .build()
    );
    public BooleanSetting logOnLowHealth = sgGeneral.add(new BooleanSetting.Builder()
            .name("Health")
            .description("Logs when lower than health threshold")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting logOnVehicleHealthLow = sgGeneral.add(new BooleanSetting.Builder()
            .name("Vehicle Health")
            .description("Also logs when vehicle health is lower than health threshold")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .shouldRender(() -> logOnLowHealth.value)
            .build()
    );
    DoubleSetting healthThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Health Threshold")
            .onSettingChange(this)
            .defaultValue(4D)
            .value(4D)
            .min(0)
            .max(32)
            .roundingPlace(0)
            .shouldRender(() -> logOnLowHealth.value)
            .build()
    );
    public BooleanSetting logOnNearbyPlayer = sgGeneral.add(new BooleanSetting.Builder()
            .name("Players")
            .description("Logs when there are nearby players")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .build()
    );
    public BooleanSetting ignoreFriends = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore friends")
            .description("Does not log when your friends are the only ones nearby")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .shouldRender(() -> logOnNearbyPlayer.value)
            .build()
    );
    DoubleSetting playerRangeThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Player Range Threshold")
            .onSettingChange(this)
            .defaultValue(100D)
            .value(100D)
            .min(0)
            .max(200)
            .roundingPlace(1)
            .shouldRender(() -> logOnNearbyPlayer.value)
            .build()
    );

    public AutoLog() {
        super("AutoLog", "Automatically disconnects", Categories.PLAYER);
        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();

        //Print reason on enable for user to see again
        if (!shouldLog && logReason != null && !logReason.isEmpty()) {
            ChatUtils.sendHeliosMsg(logReason);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (shouldLog) {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal(logReason));
            shouldLog = false;
            toggle();
        } else {
            checkShouldLog();
        }
    }

    public void checkShouldLog() {
        if (mc.player == null || mc.player.getWorld() == null)
            return;

        if (logOnLowHealth.value && mc.player.getHealth() < healthThreshold.value) {
            logImmediately("Your health was lower than " + healthThreshold.value + ", at: " + mc.player.getHealth());
        }
        if (logOnVehicleHealthLow.value && mc.player.getVehicle() != null && mc.player.getVehicle() instanceof LivingEntity lv && lv.getHealth() < healthThreshold.value) {
            logImmediately("Your vehicle's health was lower than " + healthThreshold.value + ", at: " + lv.getHealth());
        }
        if (logOnCrystal.value && EntityUtils.getNearestCrystal(mc.player.getWorld(), mc.player, crystalRangeThreshold.value) != null) {
            logImmediately("A nearby crystal within range was found!");
        }
        if (!logOnNearbyPlayer.value) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player && player != mc.player && player.distanceTo(mc.player) < playerRangeThreshold.value) {
                if (ignoreFriends.value && FriendManager.isFriend(new Friend(player.getName().getString()))) continue;
                logImmediately("A nearby player within range was found at pos: " + String.format("%.1f, %.1f, %.1f", player.getPos().x, player.getPos().y, player.getPos().z));
            }
        }
    }

    public void logImmediately(String reason) {
        shouldLog = true;
        logReason = reason;
    }
}
