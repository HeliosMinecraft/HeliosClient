package dev.heliosclient.module.modules.combat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.mixin.AccessorKeybind;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.gen.Accessor;

public class BowSpam extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    private DoubleSetting baseCharge = sgGeneral.add(new DoubleSetting.Builder()
            .name("Charge ")
            .description("Min charge of the bow before releasing (in ticks)")
            .range(3,20)
            .onSettingChange(this)
            .defaultValue(5)
            .roundingPlace(0)
            .build()
    );

    private BooleanSetting onlyOnRightClick = sgGeneral.add(new BooleanSetting.Builder()
            .name("Only On Right Click ")
            .description("Only spams bow when holding right click")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );


    public BowSpam() {
        super("BowSpam","Spams your bow like crazy", Categories.COMBAT);

        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    private boolean hasArrows(){
        if(mc.player.getAbilities().creativeMode){
            return true;
        }

        return InventoryUtils.findItemInInventory(Items.ARROW) != -1|| InventoryUtils.findItemInInventory(Items.SPECTRAL_ARROW) != -1 || InventoryUtils.findItemInInventory(Items.TIPPED_ARROW) != -1;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        mc.options.useKey.setPressed(false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if(!hasArrows())return;

        //The setting name is only on right click so idc if you have it bind to smth else.(for now)
        if(onlyOnRightClick.value && mc.mouse.wasRightButtonClicked()){
            performBowSpam();
        }else if(!onlyOnRightClick.value){
            performBowSpam();
        }
    }

    public void performBowSpam(){
        if(mc.player.getMainHandStack().getItem() == Items.BOW){
            if (mc.player.getItemUseTime() >= baseCharge.value) {
                mc.player.stopUsingItem();
                mc.interactionManager.stopUsingItem(mc.player);
            } else {
                mc.options.useKey.setPressed(true);
            }
        }
    }
}
