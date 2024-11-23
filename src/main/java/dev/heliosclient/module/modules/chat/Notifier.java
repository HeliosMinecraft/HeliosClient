package dev.heliosclient.module.modules.chat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.entity.EntityAddedEvent;
import dev.heliosclient.event.events.entity.EntityRemovedEvent;
import dev.heliosclient.event.events.player.DisconnectEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.AntiBot;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.color.ColorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.HashMap;

public class Notifier extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("Settings");
    private final SettingGroup sgGeneralNotifications = new SettingGroup("General Notifications");
    private final SettingGroup sgVisualRange = new SettingGroup("Visual Range");

    BooleanSetting showAsNotification = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show as Notification")
            .description("Should this appear as a client notification or not using the notification module")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .build()
    );
    BooleanSetting totemPops = sgGeneralNotifications.add(new BooleanSetting.Builder()
            .name("Totem Pops")
            .description("Whether to notify totem pops of players or not.")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .build()
    );
    BooleanSetting deathEvent = sgGeneralNotifications.add(new BooleanSetting.Builder()
            .name("Death Event")
            .description("Whether to notify death position of players or not")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting visualRangeInfo = sgVisualRange.add(new BooleanSetting.Builder()
            .name("Visual Range")
            .description("Notify if a player has entered your visual range.")
            .onSettingChange(this)
            .value(false)
            .defaultValue(true)
            .build()
    );
    BooleanSetting antiBotCheck = sgVisualRange.add(new BooleanSetting.Builder()
            .name("AntiBot Check")
            .description("Will first check if the entity is a bot or not using the AntiBot module")
            .onSettingChange(this)
            .value(false)
            .defaultValue(true)
            .build()
    );
    private final HashMap<String, Integer> totemCountMap = new HashMap<>();

    public Notifier() {
        super("Notifier","Get notified of world events in chat", Categories.WORLD);

        //TODO: Make a new notification class for tis
       // addSettingGroup(sgGeneral);
        addSettingGroup(sgGeneralNotifications);
        addSettingGroup(sgVisualRange);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        totemCountMap.clear();
    }
    @SubscribeEvent
    public void onDisconnect(DisconnectEvent e){
        totemCountMap.clear();
    }

    @SubscribeEvent
    public void packetReceive(PacketEvent.RECEIVE e){
        if(!totemPops.value) return;

        if(e.packet instanceof EntityStatusS2CPacket packet){
            if(packet.getStatus() != 35 ||!(packet.getEntity(mc.world) instanceof PlayerEntity player)) return;

            String playerName = player.getName().getString();
            totemCountMap.put(playerName,totemCountMap.getOrDefault(playerName,0) + 1);
            int totemCount = totemCountMap.get(playerName);
            String suffix = totemCount > 1 ? " totems" : " totem";
            ChatUtils.sendHeliosMsg( "("+ColorUtils.aqua + playerName + ColorUtils.reset +") has popped "+ ColorUtils.darkRed + totemCount + ColorUtils.reset + suffix);
        }
    }
    @SubscribeEvent
    public void onEntityAdded(EntityAddedEvent e){
        if(!visualRangeInfo.value) return;
        if(e.getEntity() instanceof PlayerEntity player && !isBot(player)) {
            String playerName = player.getName().getString();
            ChatUtils.sendHeliosMsg("(" + ColorUtils.aqua + playerName + ColorUtils.reset + ") has " + ColorUtils.green + "entered" + ColorUtils.reset +  " your visual range");
        }
    }
    @SubscribeEvent
    public void onEntityRemoved(EntityRemovedEvent e){
        if(totemPops.value && e.getEntity() instanceof PlayerEntity player) {
            if(e.getRemovalReason() == Entity.RemovalReason.KILLED){
                var totemCount = totemCountMap.remove(player.getName().getString());
                String suffix = totemCount > 0 ? "after popping " + ColorUtils.yellow + totemCount + ColorUtils.reset + (totemCount > 1 ? " totems" : " totem") : "";
                ChatUtils.sendHeliosMsg("[" + ColorUtils.blue + ColorUtils.underline + player.getName().getString() + ColorUtils.reset + "] died " + suffix);
            }
        }

        if(!visualRangeInfo.value) return;

        if(e.getEntity() instanceof PlayerEntity player&& !isBot(player)) {
            String playerName = player.getName().getString();
            ChatUtils.sendHeliosMsg("(" + ColorUtils.aqua + playerName + ColorUtils.reset + "] has " + ColorUtils.darkRed + "left" + ColorUtils.reset +  " your visual range");
        }
    }
    @SubscribeEvent
    public void onPlayerDeathEvent(PlayerDeathEvent e){
        if(!deathEvent.value) return;
        String playerName = e.getPlayer().getName().getString();
        ChatUtils.sendHeliosMsg("(" + ColorUtils.aqua + playerName + ColorUtils.reset + ") has " + ColorUtils.darkRed + "died at "+ ColorUtils.reset + "(" + ColorUtils.green +  e.getPlayer().getBlockPos()  + ColorUtils.reset + ")");
    }
    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if(!totemPops.value) return;

        //Should be handled in #onEntityRemoved
        /*
         for(PlayerEntity player: mc.world.getPlayers()){
             String playerName = player.getName().getString();
             if(!totemCountMap.containsKey(playerName) || isBot(player)) continue;

             if(player.getHealth() < 0.0 || player.deathTime > 0.0) {
                 int totemCount = totemCountMap.remove(playerName);
                 String suffix = totemCount > 1 ? " totems" : " totem";
                 ChatUtils.sendHeliosMsg("[" + ColorUtils.blue + ColorUtils.underline + playerName + ColorUtils.reset + "] died after popping " + ColorUtils.yellow + totemCount + ColorUtils.reset + suffix);
             }
         }

         */
    }

    private boolean isBot(LivingEntity e){
        if(!antiBotCheck.value){
            return false;
        }
        return AntiBot.isBot(e);
    }
}
