package dev.heliosclient.module.modules.world;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.block.BeginBreakingBlockEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.BreakIndicator;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.heliosclient.module.modules.render.BreakIndicator.IndicateType.Highlight;

public class PacketMine extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    private final SettingGroup sgRender = new SettingGroup("Break Render");

    private final MiningQueue miningQueue = new MiningQueue();

    private final DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Delay")
            .description("Delay between mining blocks (in ticks).")
            .defaultValue(1d)
            .value(1d)
            .min(0)
            .max(150)
            .build()
    );
    private final BooleanSetting rotate = sgGeneral.add(new BooleanSetting.Builder()
            .name("Rotate")
            .description("Rotates to look at the block before mining it")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .build()
    );
    private final BooleanSetting reattempt = sgGeneral.add(new BooleanSetting.Builder()
            .name("Re-attempt")
            .description("If a block fails to get mined, then should we attempt to re break it again? This may break the block")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    private final DoubleSetting maxReattempts = sgGeneral.add(new DoubleSetting.Builder()
            .name("Max Re-attempts")
            .description("The max number of attempts to mine a block before discarding it.")
            .onSettingChange(this)
            .value(2)
            .defaultValue(2)
            .min(2)
            .max(10)
            .shouldRender(() -> reattempt.value)
            .roundingPlace(0)
            .build()
    );
    private final BooleanSetting autoSwitch = sgGeneral.add(new BooleanSetting.Builder()
            .name("Auto Switch")
            .description("Automatically switches to the best tool in inventory when a block is ready to be mined.")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    private final BooleanSetting notOnUse = sgGeneral.add(new BooleanSetting.Builder()
            .name("Not on use")
            .description("It will not switch when you are using an item")
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(() -> autoSwitch.value)
            .build()
    );
    private final CycleSetting type = sgRender.add(new CycleSetting.Builder()
            .name("Indicator Type")
            .description("Type of break indication")
            .onSettingChange(this)
            .value(List.of(BreakIndicator.IndicateType.values()))
            .defaultListOption(Highlight)
            .build()
    );
    private final BooleanSetting gradientBool = sgRender.add(new BooleanSetting.Builder()
            .name("Use a gradient")
            .description("Whether to use gradient or not")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .shouldRender(() -> type.getOption() == Highlight)
            .build()
    );
    private final GradientSetting gradient = sgRender.add(new GradientSetting.Builder()
            .name("Gradient Value")
            .description("The gradient to use")
            .onSettingChange(this)
            .defaultValue("Rainbow")
            .shouldRender(() -> type.getOption() == Highlight && gradientBool.value)
            .build()
    );
    private final DoubleSetting alpha = sgRender.add(new DoubleSetting.Builder()
            .name("Gradient Alpha/Opacity")
            .description("Desired alpha (opacity) value of the gradients")
            .onSettingChange(this)
            .value(150)
            .defaultValue(150)
            .min(0)
            .max(255)
            .shouldRender(() -> type.getOption() == Highlight && gradientBool.value)
            .roundingPlace(0)
            .build()
    );
    private final RGBASetting highlightColor = sgRender.add(new RGBASetting.Builder()
            .name("Highlight color")
            .description("Color of the highlight")
            .defaultValue(Color.WHITE)
            .value(Color.WHITE)
            .onSettingChange(this)
            .rainbow(true)
            .shouldRender(() -> type.getOption() == Highlight && !gradientBool.value)
            .build()
    );

    private boolean swapped, shouldUpdateSlot;

    public PacketMine() {
        super("PacketMine", "Mines blocks via packets and allows you to mine several blocks in queue", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgRender);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgRender.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (shouldUpdateSlot) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            shouldUpdateSlot = false;
        }

        miningQueue.clear();
    }

    @SubscribeEvent
    public void onStartBreakingBlock(BeginBreakingBlockEvent event) {
        if (!BlockUtils.canBreak(event.getPos())) return;

        swapped = false;

        event.cancel();

        miningQueue.add(event.getPos(), event.getDir());
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT.PRE event) {
        if(!HeliosClient.shouldUpdate()) return;

        if (shouldUpdateSlot) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            shouldUpdateSlot = false;
        }

        miningQueue.update();

        if (!swapped && autoSwitch.value && (!mc.player.isUsingItem() || !notOnUse.value)) {
            miningQueue.queue.forEach((pos,info)->{
                if(info.progress >= 1.0f) {
                    switchIfNeeded(miningQueue.getNextBlock());
                    return;
                }
            });
        }
    }

    private boolean shouldRemove(BlockPos pos, Direction direction) {
        boolean isGettingRemoved = mc.world.getBlockState(pos).isAir() ||
                                    mc.player.getEyePos()
                                            .subtract(0.5, 0, 0.5f)
                                            .distanceTo(pos.offset(direction).toCenterPos()) > mc.interactionManager.getReachDistance();
        if (isGettingRemoved) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        return isGettingRemoved;
    }

    private void switchIfNeeded(BlockPos pos) {
        if (pos == null) return;
        int bestSlot = InventoryUtils.getFastestTool(mc.world.getBlockState(pos),false);
        if (bestSlot == -1 || mc.player.getInventory().selectedSlot == bestSlot) return;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bestSlot));
        swapped = true;
        shouldUpdateSlot = true;
    }

    @SubscribeEvent
    public void render3d(Render3DEvent event) {
        Renderer3D.renderThroughWalls();
        miningQueue.getBlocks().forEach((pos,info)->{
            BlockState state = mc.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(mc.world, pos);
            if (shape == null || shape.isEmpty()) return;

            int start = gradientBool.value ? ColorUtils.changeAlpha(gradient.get().getStartGradient().getRGB(),alpha.getInt()).getRGB() : highlightColor.value.getRGB();
            int end = gradientBool.value ? ColorUtils.changeAlpha(gradient.get().getEndGradient().getRGB(),alpha.getInt()).getRGB() : highlightColor.value.getRGB();

            ModuleManager.get(BreakIndicator.class).renderIndicator(shape.getBoundingBox().expand(0.001f).offset(pos), (float) info.progress, (BreakIndicator.IndicateType) type.getOption(),start,end);
        });
        Renderer3D.stopRenderingThroughWalls();
    }

    private class MiningQueue {
        private final LinkedHashMap<BlockPos, MiningInfo> queue = new LinkedHashMap<>();

        void add(BlockPos pos, Direction dir) {
            queue.put(pos, new MiningInfo(dir));
        }

        void update() {
            queue.entrySet().removeIf(entry -> shouldRemove(entry.getKey(),entry.getValue().direction));
            BlockPos nextBlock = getNextBlock();
            if(nextBlock == null) return;

            MiningInfo info = queue.get(nextBlock);

            mine(nextBlock, info);

            //Sometimes the blocks dont get mined but the calculated progress is 1. So we attempt to mine it again from scratch.
            if(info.progress >= 1.0f && !mc.world.getBlockState(nextBlock).isAir() && reattempt.value){
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, nextBlock, info.direction));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

                if(info.attempts > maxReattempts.getInt()){
                    queue.remove(nextBlock);
                    return;
                }

                info.restart();
            }
        }

        void clear() {
            queue.clear();
        }

        BlockPos getNextBlock() {
            return queue.keySet().stream()
                    .findFirst()
                    .orElse(null);
        }

        Map<BlockPos, MiningInfo> getBlocks() {
            return queue;
        }

        private void mine(BlockPos pos, MiningInfo info) {
            if (rotate.get()) {
                RotationUtils.rotate((float) RotationUtils.getYaw(pos), (float) RotationUtils.getPitch(pos), true, () -> sendMinePackets(pos,info));
            } else {
                sendMinePackets(pos, info);
            }
            int slot = InventoryUtils.getFastestTool(mc.world.getBlockState(pos),false);
            info.progress += BlockUtils.calcBlockBreakingDelta(mc.world.getBlockState(pos),slot != -1 ? slot : mc.player.getInventory().selectedSlot);
        }

        private void sendMinePackets(BlockPos pos, MiningInfo info) {
            if (info.timer <= 0 && !info.started) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, info.direction));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, info.direction));
                info.started = true;
            } else {
                info.timer--;
            }
        }
    }

    private class MiningInfo {
        Direction direction;
        double progress;
        int timer;
        boolean started;
        int attempts;


        MiningInfo(Direction dir) {
            this.direction = dir;
            restart();
        }

        void restart() {
            this.progress = 0.0;
            this.timer = delay.getInt();
            this.started = false;
            this.attempts++;
        }
    }
}
