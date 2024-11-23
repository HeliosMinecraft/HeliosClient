package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import dev.heliosclient.util.world.ChunkUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

import static dev.heliosclient.HeliosClient.MC;

public class StorageESP extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting throughWalls = sgGeneral.add(new BooleanSetting.Builder()
            .name("ThroughWalls")
            .description("Draw the ESP through walls")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting tracers = sgGeneral.add(new BooleanSetting.Builder()
            .name("Tracers")
            .description("Draw tracers to blocks")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting outline = sgGeneral.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline of blocks")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting fill = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill of blocks")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting fade = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fade when close")
            .description("Fades out the ESP when you get closer to the storage blocks")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );

    public StorageESP() {
        super("StorageESP", "Highlights storage blocks", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (throughWalls.value)
            Renderer3D.renderThroughWalls();

        ChunkUtils.getBlockEntityStreamInChunks().forEach(blockEntity -> {
            if (blockEntity instanceof TrappedChestBlockEntity)
                renderStorageBlock(new Color(0xFF5B0000), blockEntity.getPos());
            else if (blockEntity instanceof ChestBlockEntity)
                renderStorageBlock(Color.ORANGE, blockEntity.getPos());
            else if (blockEntity instanceof EnderChestBlockEntity)
                renderStorageBlock(Color.MAGENTA, blockEntity.getPos());
            else if (blockEntity instanceof ShulkerBoxBlockEntity)
                renderStorageBlock(Color.PINK, blockEntity.getPos());
            else if (blockEntity instanceof BarrelBlockEntity)
                renderStorageBlock(new Color(0xFF4B3312), blockEntity.getPos());
            else if (blockEntity instanceof HopperBlockEntity)
                renderStorageBlock(Color.DARK_GRAY, blockEntity.getPos());
            else if (blockEntity instanceof DropperBlockEntity)
                renderStorageBlock(Color.LIGHT_GRAY, blockEntity.getPos());
            else if (blockEntity instanceof DecoratedPotBlockEntity)
                renderStorageBlock(Color.BLUE, blockEntity.getPos());
            else if (blockEntity instanceof CrafterBlockEntity)
                renderStorageBlock(Color.YELLOW, blockEntity.getPos());
            else if (blockEntity instanceof DispenserBlockEntity)
                renderStorageBlock(Color.GRAY, blockEntity.getPos());
            else if (blockEntity instanceof AbstractFurnaceBlockEntity)
                renderStorageBlock(Color.GRAY, blockEntity.getPos());
        });

        for (Entity entity : MC.world.getEntities()) {
            if (entity instanceof ChestMinecartEntity)
                renderStorageBlock(Color.YELLOW, entity.getBlockPos());
            else if (entity instanceof HopperMinecartEntity)
                renderStorageBlock(Color.DARK_GRAY, entity.getBlockPos());
            else if (entity instanceof ChestBoatEntity)
                renderStorageBlock(Color.ORANGE, entity.getBlockPos());
        }

        if (throughWalls.value)
            Renderer3D.stopRenderingThroughWalls();
    }

    private void renderStorageBlock(Color c, BlockPos pos) {
        double distanceFromPos = mc.player.getBlockPos().getSquaredDistance(pos);
        int alphaClamp = 100;
        if(fade.value) {
            alphaClamp = (int) MathHelper.clamp(150 * (distanceFromPos / 400), 0, 150);
            if(alphaClamp <= 2)return;
        }

        int changedColor = ColorUtils.changeAlphaGetInt(c.getRGB(), alphaClamp);
        QuadColor color = QuadColor.single(changedColor);
        LineColor lineColor = LineColor.single(changedColor);
        VoxelShape shape;
        BlockState state = mc.world.getBlockState(pos);

        if (state.getBlock() instanceof ChestBlock p) {
            shape = p.getOutlineShape(state,null,null,null);
        } else{
            shape = state.getOutlineShape(mc.world, pos);
        }

        if (shape == null || shape.isEmpty()) return;

        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(shape.getBoundingBox().offset(pos).expand(0.005f), color, 1f);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(shape.getBoundingBox().offset(pos).expand(0.005f), color, 1f);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(shape.getBoundingBox().offset(pos).expand(0.005f), color);
        }
        if (tracers.value) {
            Renderer3D.drawLine(Renderer3D.getEyeTracer(), shape.getBoundingBox().offset(pos).getCenter(), lineColor, 1f);
        }
    }
}
