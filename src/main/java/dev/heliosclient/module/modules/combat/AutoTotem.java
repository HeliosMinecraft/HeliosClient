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
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.TickTimer;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
            .description("Notifies when we restock a totem or there is no totems left")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );

    KeyBind totemSwitchKey = sgGeneral.add(new KeyBind.Builder()
            .name("Totem Switch Key")
            .description("When you press this key, the module will automatically switch to a totem")
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
        if(e.getKey() == totemSwitchKey.value){
            doAutoTotem();
            timer.restartTimer();
        }
    }

    //High
    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onTick(TickEvent.WORLD event) {
        if(!HeliosClient.shouldUpdate()) return;

        totemCount = InventoryUtils.getItemCountInInventory(Items.TOTEM_OF_UNDYING);
        if(totemCount > 0){
            lastNoTotemNotified = false;
            timer.incrementAndEvery(delay.getInt(),()->{
               if(mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING || mc.player.playerScreenHandler != mc.player.currentScreenHandler){
                   return;
               }
               if(always.value || isPlayerLow() || didTotemPop) {
                   doAutoTotem();
                   didTotemPop = false;

                   if(log.value){
                       ChatUtils.sendHeliosMsg("Restocked Totem, Totems left: " + InventoryUtils.getItemCountInInventory(Items.TOTEM_OF_UNDYING));
                   }
               }
            });
        } else if(log.value && !lastNoTotemNotified){
            ChatUtils.sendHeliosMsg(ColorUtils.red + "No Totems left in your inventory");
            lastNoTotemNotified = true;
        }
    }

    public void doAutoTotem(){
        boolean offhandHasItem = !mc.player.getOffHandStack().isEmpty();
        int itemSlot = InventoryUtils.findItemInInventory(Items.TOTEM_OF_UNDYING);
        if(itemSlot == -1 || itemSlot == InventoryUtils.OFFHAND){
            return;
        }

        //if is hotbar then swap item with offhand super-fast.
        if(itemSlot >= 0 && itemSlot < 9){
            InventoryUtils.swapToSlot(itemSlot,true);
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            InventoryUtils.swapBackHotbar();
        }else {
            InventoryUtils.moveItem(itemSlot,45, SlotActionType.PICKUP, SlotActionType.PICKUP);

            if(offhandHasItem){
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,itemSlot,0,SlotActionType.PICKUP,mc.player);
            }
        }
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
        return mc.player.getHealth() + mc.player.getAbsorptionAmount() <= healthThreshold.value;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(totemCount);
    }
}
