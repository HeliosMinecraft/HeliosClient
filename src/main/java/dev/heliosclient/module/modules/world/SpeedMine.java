package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.block.BeginBreakingBlockEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.mixin.AccessorClientPlayerInteractionManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.List;

public class SpeedMine extends Module_ {
    public final BlockPos.Mutable blockPos = new BlockPos.Mutable(0, Integer.MIN_VALUE, 0);
    final SettingGroup sgGeneral = new SettingGroup("General");
    final SettingGroup sgInstaReMine = new SettingGroup("Insta ReMine");
    final SettingGroup sgRender = new SettingGroup("Render Instant ReMine");
    public CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Mode of speed mine")
            .onSettingChange(this)
            .value(List.of(Mode.values()))
            .defaultListOption(Mode.Haste)
            .build()
    );
    public DoubleSetting hasteLevel = sgGeneral.add(new DoubleSetting.Builder()
            .name("Haste Level")
            .description("Level of haste applied")
            .onSettingChange(this)
            .range(1, 10)
            .roundingPlace(0)
            .defaultValue(2)
            .shouldRender(() -> mode.getOption() == Mode.Haste)
            .build()
    );
    public DoubleSetting modifier = sgGeneral.add(new DoubleSetting.Builder()
            .name("Modifier")
            .description("Value to modify your breaking speed by. Every 0.2 is similar 1 haste level")
            .onSettingChange(this)
            .range(0, 5)
            .roundingPlace(2)
            .defaultValue(1.2)
            .shouldRender(() -> mode.getOption() == Mode.Modifier)
            .build()
    );
    int ticksPassed = 0;
    BooleanSetting instaRemine = sgInstaReMine.add(new BooleanSetting.Builder()
            .name("Insta Remine")
            .description("Attempts to instantly remine the same block position, like insta mine")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    DoubleSetting breakDelay = sgInstaReMine.add(new DoubleSetting.Builder()
            .name("Break Delay")
            .description("Delay between instantly remining block")
            .onSettingChange(this)
            .range(0, 30)
            .roundingPlace(0)
            .defaultValue(0)
            .shouldRender(() -> instaRemine.value)
            .build()
    );
    BooleanSetting pickAxeOnly = sgInstaReMine.add(new BooleanSetting.Builder()
            .name("Pickaxe only")
            .description("Only remines if you have are holding a pickaxe")
            .onSettingChange(this)
            .defaultValue(false)
            .shouldRender(() -> instaRemine.value)
            .build()
    );
    RGBASetting instaMineColor = sgRender.add(new RGBASetting.Builder()
            .name("InstaMine Color")
            .description("Color of the highlight")
            .onSettingChange(this)
            .rainbow(false)
            .defaultValue(Color.WHITE)
            .build()
    );
    BooleanSetting outline = sgRender.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline of blocks")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting fill = sgRender.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill of blocks")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    private Direction direction;


    public SpeedMine() {
        super("SpeedMine", "Tries to mine blocks quickly", Categories.WORLD);

        addSettingGroup(sgGeneral);
        addSettingGroup(sgInstaReMine);
        addSettingGroup(sgRender);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgInstaReMine.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ticksPassed = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        hasteEffect(false);
    }

    public void hasteEffect(boolean add) {
        if (mc.player == null) return;

        StatusEffectInstance haste = mc.player.getStatusEffect(StatusEffects.HASTE);

        if (add) {
            if (haste == null || haste.getAmplifier() < hasteLevel.value - 1) {
                mc.player.setStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, -1, (int) (hasteLevel.value - 1), true, false, false), null);
            }
            return;
        }

        if (haste != null && !haste.shouldShowIcon()) {
            mc.player.removeStatusEffect(StatusEffects.HASTE);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.player.getAbilities().creativeMode)
            return;

        if (mode.getOption() == Mode.Haste) {
            hasteEffect(true);
        } else if (mode.getOption() == Mode.Damage) {
            hasteEffect(false);

            AccessorClientPlayerInteractionManager im = (AccessorClientPlayerInteractionManager) mc.interactionManager;
            float progress = mc.interactionManager.getBlockBreakingProgress();
            BlockPos pos = im.getCurrentBreakingBlockPos();

            if (pos == null || progress <= 0) return;
            if (progress + mc.world.getBlockState(pos).calcBlockBreakingDelta(mc.player, mc.world, pos) >= 0.7f)
                im.setCurrentBreakingProgress(1f);
        }

        if (instaRemine.value) {
            if (ticksPassed >= breakDelay.value) {
                ticksPassed = 0;

                if (shouldMine()) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos,direction == null ? Direction.UP : direction));
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction == null ? Direction.UP : direction));

                    mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            } else {
                ticksPassed++;
            }
        }
    }

    private void renderBlock(Box box) {
        QuadColor fillColor = QuadColor.single(instaMineColor.getColor().getRGB());

        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(box, fillColor, fillColor, 1f);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(box, fillColor, 1f);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(box, fillColor);
        }
    }

    public boolean shouldMine() {
        if (mc.world.isOutOfHeightLimit(blockPos) || !BlockUtils.canBreak(blockPos, mc.world.getBlockState(blockPos)))
            return false;

        return !pickAxeOnly.value || mc.player.getMainHandStack().getItem() instanceof PickaxeItem;
    }

    @SubscribeEvent
    public void render3d(Render3DEvent event) {
        BlockState blockState = mc.world.getBlockState(blockPos);
        VoxelShape shape = blockState.getOutlineShape(mc.world, blockPos);
        if (!shape.isEmpty()) {
            renderBlock(shape.getBoundingBox().expand(0.005f).offset(blockPos));
        }
    }

    @SubscribeEvent
    public void blockBreakBegin(BeginBreakingBlockEvent event) {
        direction = event.getDir();
        blockPos.set(event.getPos());
    }

    public enum Mode {
        Haste,
        Damage,
        Modifier,
        None
    }
}
