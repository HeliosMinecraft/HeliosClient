package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.BlockShapeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IVec3d;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;

public class Collisions extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting noCollision = sgGeneral.add(new BooleanSetting("NoCollision", "Removes collision for the selected blocks instead of adding", this, false));
    BooleanSetting noWorldBorder = sgGeneral.add(new BooleanSetting("NoWorldBorder", "Removes world border collision", this, false));
    BooleanSetting unloadedChunks = sgGeneral.add(new BooleanSetting("Unloaded chunks", "Prevents you from going into unloaded chunks ", this, false));

    SettingGroup sgBlocks = new SettingGroup("Blocks");

    BooleanSetting cactus = sgBlocks.add(new BooleanSetting("Cactus", "Makes cactus a solid block", this, true));
    BooleanSetting magma = sgBlocks.add(new BooleanSetting("Magma", "Prevents you from touching magma blocks", this, true));
    BooleanSetting fire = sgBlocks.add(new BooleanSetting("Fire", "Makes fire hard", this, true));


    public Collisions() {
        super("Collisions", "Modify collisions of certain blocks client side.", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgBlocks);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgBlocks.getSettings());
    }

    @SubscribeEvent
    public void onBlockShapeEvent(BlockShapeEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (!event.getState().getFluidState().isEmpty()) return;

        if (shouldCollideWithBlock(event.getState().getBlock())) {
            if (noCollision.value) {
                event.setShape(VoxelShapes.empty());
                return;
            }
            event.setShape(VoxelShapes.fullCube());
        } else if (magma.value && !mc.player.isSneaking() && event.getState().isAir() && mc.world.getBlockState(event.getPos().down()).getBlock() == Blocks.MAGMA_BLOCK) {
            event.setShape(VoxelShapes.fullCube());
        }
    }

    public boolean shouldCollideWithBlock(Block block) {
        return cactus.value && block instanceof CactusBlock ||
                fire.value && block instanceof AbstractFireBlock;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event) {
        if (!unloadedChunks.value) return;
        if (event.packet instanceof VehicleMoveC2SPacket packet) {
            if (!mc.world.getChunkManager().isChunkLoaded((int) packet.position().x >> 4, (int) packet.position().z >> 4)) {
                mc.player.getVehicle().updatePosition(mc.player.getVehicle().prevX, mc.player.getVehicle().prevY, mc.player.getVehicle().prevZ);
                event.setCanceled(true);
            }
        } else if (event.packet instanceof PlayerMoveC2SPacket packet) {
            if (!mc.world.getChunkManager().isChunkLoaded((int) packet.getX(mc.player.getX()) >> 4, (int) packet.getZ(mc.player.getZ()) >> 4)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event) {
        int x = (int) (mc.player.getX() + event.getMovement().x) >> 4;
        int z = (int) (mc.player.getZ() + event.getMovement().z) >> 4;
        if (unloadedChunks.value && !mc.world.getChunkManager().isChunkLoaded(x, z)) {
            ((IVec3d) event.getMovement()).heliosClient$set(0, event.getMovement().y, 0);
        }
    }

    public boolean ignoreBorder() {
        return isActive() && noWorldBorder.value;
    }
}
