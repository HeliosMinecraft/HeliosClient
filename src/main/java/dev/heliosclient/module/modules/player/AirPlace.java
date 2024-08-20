package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.BlockSelection;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.BlockUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AirPlace extends Module_ {
    int actionTimer = 0;
    HitResult hitResult = null;

    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting blockSelection = sgGeneral.add(new BooleanSetting.Builder()
            .name("Block Selection")
            .description("Uses the BlockSelection module to render the block you will place")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Place Delay")
            .description("Delay in ticks to place the next block, to prevent spamming air blocks")
            .onSettingChange(this)
            .min(0)
            .max(10d)
            .defaultValue(5d)
            .value(5d)
            .roundingPlace(0)
            .build()
    );

    public AirPlace() {
        super("AirPlace", "Allows you to place blocks in air", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        actionTimer = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        hitResult = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        hitResult = mc.getCameraEntity().raycast(mc.interactionManager.getReachDistance(), 0, false);

        if (!mc.options.useKey.isPressed()) return;

        actionTimer++;

        if (hitResult instanceof BlockHitResult bHitResult && mc.world.getBlockState(bHitResult.getBlockPos()).isReplaceable() && mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            if (actionTimer > delay.value) {
                actionTimer = 0;
                BlockUtils.place(bHitResult.getBlockPos(), true, false);
            }
        }
    }

    @SubscribeEvent
    public void render3d(Render3DEvent event) {
        if (blockSelection.value && hitResult instanceof BlockHitResult bHitResult) {
            ModuleManager.get(BlockSelection.class).renderBlockHitResult(bHitResult,true);
        }
    }
}
