package dev.heliosclient.module.modules.combat;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.player.DamageUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.timer.TickTimer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoTotem extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting always = sgGeneral.add(new BooleanSetting.Builder()
            .name("Always")
            .description("Always holds a totem in your offhand")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting healthThreshold = sgGeneral.add(new DoubleSetting.Builder()
            .name("Health Threshold")
            .description("Holds a totem as soon as your health goes below this threshold")
            .range(0,36)
            .defaultValue(7d)
            .roundingPlace(0)
            .onSettingChange(this)
            .shouldRender(()-> !always.value)
            .build()
    );
    DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Delay")
            .description("Delay (in ticks) between each restock")
            .range(0,36)
            .defaultValue(7d)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting log = sgGeneral.add(new BooleanSetting.Builder()
            .name("Notify")
            .description("Notifies when you restock a totem or there are no totems left")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting predictDamage = sgGeneral.add(new BooleanSetting.Builder()
            .name("Predict Damage")
            .description("Will try to predict the damage you will take and auto totem")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting immediateNearCrystal = sgGeneral.add(new BooleanSetting.Builder()
            .name("Immediate Near Crystal")
            .description("Will autototem instantly as soon as we find a crystal near you that can damage you")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting stopMotion = sgGeneral.add(new BooleanSetting.Builder()
            .name("Stop Motion")
            .description("This will immediately stop you before switching to a totem")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting stopSprint = sgGeneral.add(new BooleanSetting.Builder()
            .name("Stop Sprint")
            .description("This will send a packet to the server to stop sprinting")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting swapInstead = sgGeneral.add(new BooleanSetting.Builder()
            .name("Swap Instead")
            .description("This will try to swap the totem instead of picking and moving it")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    KeyBind totemSwitchKey = sgGeneral.add(new KeyBind.Builder()
            .name("Totem Switch Key")
            .description("When you press this key, you will automatically switch to a totem")
            .value(KeyBind.none())
            .onSettingChange(this)
            .build()
    );

    int totemCount = 0;
    private final TickTimer timer = new TickTimer();
    boolean lastNoTotemNotified = false, didTotemPop = false;

    public AutoTotem() {
        super("AutoTotem","Automatically holds a totem in your hand", Categories.COMBAT);

        addSettingGroup(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.startTicking();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        timer.resetTimer();
        lastNoTotemNotified = false;
    }

    @SubscribeEvent
    public void onKey(KeyPressedEvent e){
        if(e.getKey() == totemSwitchKey.value && mc.currentScreen == null){
            doAutoTotem();
            timer.restartTimer();
        }
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onTick(TickEvent.WORLD event) {
        if(!HeliosClient.shouldUpdate()) return;

        totemCount = InventoryUtils.getItemCountInInventory(Items.TOTEM_OF_UNDYING);
        if(totemCount > 0){
            lastNoTotemNotified = false;
            timer.incrementAndEvery(delay.getInt(),()->{
               if(mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING){
                   return;
               }
               if(always.value || didTotemPop || isPlayerLow()) {
                   boolean status = doAutoTotem();
                   didTotemPop = false;

                   if(log.value && status){
                       ChatUtils.sendHeliosMsg("Restocked Totem, Totems left: " + InventoryUtils.getItemCountInInventory(Items.TOTEM_OF_UNDYING));
                   }
               }
            });
        } else if(log.value && !lastNoTotemNotified){
            ChatUtils.sendHeliosMsg(ColorUtils.red + "No Totems left in your inventory");
            lastNoTotemNotified = true;
        }
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGH)
    public void onPacketReceive(PacketEvent.RECEIVE event) {
        if(event.packet instanceof EntitySpawnS2CPacket p){
            if(p.getEntityType() == EntityType.END_CRYSTAL){
                Vec3d crystalPos = new Vec3d(p.getX(),p.getY(),p.getZ());
                boolean willCrystalDamage = mc.player.getPos().squaredDistanceTo(crystalPos) < DamageUtils.CRYSTAL_POWER * DamageUtils.CRYSTAL_POWER;
                if(willCrystalDamage){
                    if(immediateNearCrystal.value){
                        doAutoTotem();
                        return;
                    }

                    if(DamageUtils.calculateCrystalDamage(crystalPos,mc.player) >= healthThreshold.get()){
                        timer.setTicks(delay.getInt());
                    }
                }
            }
        }
    }

    public boolean doAutoTotem(){
        if(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)return false;

        boolean offhandHasItem = !mc.player.getOffHandStack().isEmpty();
        int itemSlot = InventoryUtils.findItemInInventory(Items.TOTEM_OF_UNDYING);
        if(itemSlot == -1 || itemSlot == InventoryUtils.OFFHAND || mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING){
            return false;
        }
        if(stopMotion.value){
            mc.player.setVelocity(0,mc.player.getVelocity().y,0);
        }

        if(stopSprint.value){
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        //if is hotbar then swap item with offhand super-fast.
        if(itemSlot >= 0 && itemSlot < 9){
            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
            mc.player.getInventory().selectedSlot = itemSlot;
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            mc.player.getInventory().selectedSlot = prevSlot;
            return true;
        } else if(mc.player.playerScreenHandler == mc.player.currentScreenHandler){
            if(swapInstead.value) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 40, SlotActionType.SWAP, mc.player);
                return true;
            }

            if(offhandHasItem){
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,InventoryUtils.OFFHAND,0,SlotActionType.PICKUP,mc.player);
            }

            InventoryUtils.moveItem(itemSlot,InventoryUtils.OFFHAND, SlotActionType.PICKUP, SlotActionType.PICKUP);
        }

        return true;
    }

    @SubscribeEvent
    public void packetReceive(PacketEvent.RECEIVE e){
        if(e.packet instanceof EntityStatusS2CPacket packet){
            if(packet.getStatus() != 35 || packet.getEntity(mc.world) != mc.player) return;

            didTotemPop = true;
            timer.setTicks(delay.getInt());
        }
    }

    private boolean isPlayerLow(){
        return mc.player.getHealth() + mc.player.getAbsorptionAmount() - (predictDamage.value ? DamageUtils.calculateDamageByEnv() : 0)  <= healthThreshold.value;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(totemCount);
    }
}
