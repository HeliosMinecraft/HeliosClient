package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.InventoryEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.ChunkUtils;
import dev.heliosclient.util.player.RotationSimulator;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.block.entity.*;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

import static dev.heliosclient.util.player.InventoryUtils.moveItemQuickMove;


public class ChestAura extends Module_ {
    boolean isStealing = false;

    Set<BlockEntity> containersOpened = new HashSet<>();
    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting trappedChests = sgGeneral.add(new BooleanSetting("Trapped Chests", "Opens trapped chests as well", this, false, () -> true, false));
    BooleanSetting rotate = sgGeneral.add(new BooleanSetting("Rotate", "Rotates to look at the chest before opening it", this, true, () -> true, true));
    BooleanSetting clearOnDisable = sgGeneral.add(new BooleanSetting("Clear on disable", "Clears opened container cache on module disable", this, true, () -> true, true));
    BooleanSetting autoSteal = sgGeneral.add(new BooleanSetting("AutoSteal", "Automatically steals all the items from the chest (buggy and unreliable).", this, false, () -> true, false));


    public ChestAura() {
        super("ChestAura", "Opens all nearby chests or shulkerboxes instantly", Categories.MISC);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (clearOnDisable.value) {
            containersOpened.clear();
        }
        isStealing = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER e) {
        if (isStealing || mc.player == null || mc.interactionManager == null)
            return;

        ChunkUtils.getBlockEntityStreamInChunks().forEach(blockEntity -> {
            if (!containersOpened.contains(blockEntity) && mc.player.getBlockPos().isWithinDistance(blockEntity.getPos(), mc.interactionManager.getReachDistance() - 0.5)) {
                if ((trappedChests.value && blockEntity instanceof TrappedChestBlockEntity) || blockEntity instanceof ChestBlockEntity || blockEntity instanceof ShulkerBoxBlockEntity || blockEntity instanceof BarrelBlockEntity) {

                    if (isStealing)
                        return;

                    if (rotate.value) {
                        Vec3d pos = blockEntity.getPos().toCenterPos();
                        RotationSimulator.INSTANCE.simulateRotation(RotationUtils.getYaw(pos), RotationUtils.getPitch(pos), false, () -> interact(blockEntity), 4, 0);
                    } else {
                        interact(blockEntity);
                    }
                }
            }
        });

    }

    public void interact(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getPos();
        BlockHitResult blockInteractResult = new BlockHitResult(pos.toCenterPos(), BlockUtils.getClosestFace(pos), pos, false);

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockInteractResult);

        if (result == ActionResult.SUCCESS) {
            containersOpened.add(blockEntity);
            if (!autoSteal.value) {
                if (mc.currentScreen != null)
                    mc.currentScreen.close();

                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }
    }

    @SubscribeEvent
    public void onInventory(InventoryEvent event) {
        if (!autoSteal.value) return;

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            HeliosExecutor.execute(() -> {
                isStealing = true;
                for (int i = 0; i < (genericContainerScreenHandler.getRows()) * 9; i++) {
                    if (!genericContainerScreenHandler.getSlot(i).hasStack()) continue;

                    //Hard coded. Sorry
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    moveItemQuickMove(i);
                }

                mc.currentScreen.close();
                mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(genericContainerScreenHandler.syncId));
                isStealing = false;
            });
        }
    }
}
