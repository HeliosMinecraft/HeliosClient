package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.block.BeginBreakingBlockEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PacketMine extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    private final SettingGroup sgRender = new SettingGroup("Render");
    private final Map<BlockPos, Direction> blocks = new HashMap<>();
    private final Map<BlockPos, Double> progressMap = new HashMap<>();
    private final Map<BlockPos, Integer> timerMap = new HashMap<>();
    private final Map<BlockPos, Boolean> miningMap = new HashMap<>();

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
            .onSettingChange(this)
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
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    RGBASetting color = sgRender.add(new RGBASetting.Builder()
            .name("Color")
            .value(ColorUtils.changeAlpha(Color.WHITE, 125))
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 125))
            .onSettingChange(this)
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
        blocks.clear();

        if (mc.interactionManager != null)
            mc.interactionManager.syncSelectedSlot();
    }

    @SubscribeEvent
    public void onStartBreakingBlock(BeginBreakingBlockEvent event) {
        if (!BlockUtils.canBreak(event.getPos(), mc.world.getBlockState(event.getPos()))) return;

        event.setCanceled(true);
        swapped = false;

        if (!isMiningBlock(event.getPos())) {
            blocks.put(event.getPos(), event.getDir());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        blocks.entrySet().removeIf(blockPosDirectionEntry -> shouldRemove(blockPosDirectionEntry.getKey(), blockPosDirectionEntry.getValue()));

        if (shouldUpdateSlot) {
            mc.interactionManager.syncSelectedSlot();
            shouldUpdateSlot = false;
        }

        if (!blocks.isEmpty()) {
            Optional<Map.Entry<BlockPos, Direction>> firstBlockAndDirection = blocks.entrySet().stream().findFirst();
            Map.Entry<BlockPos, Direction> entry = firstBlockAndDirection.get();
            mineBlock(entry.getKey(), entry.getValue());
        }

        if (!swapped && autoSwitch.value && (!mc.player.isUsingItem() || !notOnUse.value)) {
            for (BlockPos blockPos : blocks.keySet()) {
                if (isBlockReady(blockPos)) {
                    int slot = InventoryUtils.getFastestTool(mc.world.getBlockState(blockPos), false);
                    if (slot == -1 || mc.player.getInventory().selectedSlot == slot) continue;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    shouldUpdateSlot = true;
                    swapped = true;
                    break;
                }
            }
        }
    }

    private boolean isBlockReady(BlockPos pos) {
        return progressMap.getOrDefault(pos, 0.0) >= 1;
    }

    private void mineBlock(BlockPos pos, Direction direction) {
        if (rotate.value) {
            RotationUtils.rotate((float) RotationUtils.getYaw(pos), (float) RotationUtils.getPitch(pos), false, () -> sendMinePackets(pos, direction));
        } else {
            sendMinePackets(pos, direction);
        }

        int slot = InventoryUtils.getFastestTool(mc.world.getBlockState(pos), false);

        progressMap.put(pos, progressMap.getOrDefault(pos, 0.0) + BlockUtils.calcBlockBreakingDelta(mc.world.getBlockState(pos), mc.player.getInventory().getStack(slot != -1 ? slot : mc.player.getInventory().selectedSlot)));
    }


    public boolean isMiningBlock(BlockPos pos) {
        return blocks.containsKey(pos);
    }

    private boolean shouldRemove(BlockPos pos, Direction direction) {
        return mc.world.getBlockState(pos).isAir() ||
                mc.player.getEyePos().subtract(0.5, 0, 0.5f).distanceTo(pos.add(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()).toCenterPos()) > mc.interactionManager.getReachDistance();
    }

    private void sendMinePackets(BlockPos pos, Direction direction) {
        if (timerMap.getOrDefault(pos, (int) delay.value) <= 0) {
            if (!miningMap.getOrDefault(pos, false)) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction));
                miningMap.put(pos, true);
            }
        } else {
            timerMap.put(pos, timerMap.getOrDefault(pos, (int) delay.value) - 1);
        }
    }

    @SubscribeEvent
    public void render3d(Render3DEvent event) {
        for (BlockPos pos : blocks.keySet()) {
            QuadColor color = QuadColor.single(this.color.value.getRGB());
            if (outline.value && fill.value) {
                Renderer3D.drawBoxBoth(pos, color, 1.2f);
            } else if (outline.value) {
                Renderer3D.drawBoxOutline(pos, color, 1.2f);
            } else if (fill.value) {
                Renderer3D.drawBoxFill(pos, color);
            }
        }
    }

}
