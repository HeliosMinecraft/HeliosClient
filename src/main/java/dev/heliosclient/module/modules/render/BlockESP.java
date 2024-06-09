package dev.heliosclient.module.modules.render;

import com.google.common.collect.Sets;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.event.events.world.ChunkDataEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringListSetting;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.util.*;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class BlockESP extends Module_ {
    static Set<BlockPos> positions = Sets.newConcurrentHashSet();
    Set<Block> prevBlockList = new HashSet<>();
    SettingGroup sgGeneral = new SettingGroup("General");
    int prevChunkDistance = -1;


    StringListSetting blocks = sgGeneral.add(new StringListSetting.Builder()
            .name("Blocks")
            .description("Blocks to highlight")
            .defaultValue(new String[]{"minecraft:diamond_ore"})
            .value(new String[]{"minecraft:diamond_ore"})
            .characterLimit(50)
            .defaultBoxes(1)
            .inputMode(InputBox.InputMode.ALL)
            .build()
    );
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
    DoubleSetting fillAlpha = sgGeneral.add(new DoubleSetting.Builder()
            .name("Fill Alpha")
            .description("Opacity of the fill")
            .min(0)
            .max(255)
            .value(155d)
            .defaultValue(155d)
            .roundingPlace(0)
            .onSettingChange(this)
            .shouldRender(() -> fill.value)
            .build()
    );


    public BlockESP() {
        super("BlockESP", "Block highlighter", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Block block;
        for (String val : blocks.value) {
            try {
                block = BlockUtils.getBlockFromString(val);
                prevBlockList.add(block);
            } catch (InvalidIdentifierException ignored) {
            }
        }
        if (mc.world != null) {
            updateBlocks();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        positions.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        Block block = null;

        if (!blocks.isWriting) {
            Set<Block> newBlockList = new HashSet<>();
            for (String val : blocks.value) {
                try {
                    block = BlockUtils.getBlockFromString(val);
                    newBlockList.add(block);
                } catch (InvalidIdentifierException ignored) {
                }
            }
            if (!newBlockList.equals(prevBlockList) || prevChunkDistance != mc.options.getViewDistance().getValue()) {
                updateBlocks();
                prevBlockList = new HashSet<>(newBlockList);
                prevChunkDistance = mc.options.getViewDistance().getValue();
            }
        }
    }

    public void updateBlocks() {
        this.searchBlocks(mc.world, (world, pos) -> {
            if (prevBlockList.contains(world.getBlockState(pos).getBlock()) && positions.size() < 10000) {
                positions.add(pos);
            }
        });
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        for (BlockPos pos : positions) {
            int topColor = mc.world.getBlockState(pos).getMapColor(mc.world, pos).color;
            if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL) {
                topColor = ColorUtils.rgbToInt(107, 0, 209);
            }


            VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
            if (shape.isEmpty()) {
                shape = VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
            }
            if (throughWalls.value)
                Renderer3D.renderThroughWalls();

            if (fill.value) {
                Renderer3D.drawBoxFill(shape.getBoundingBox().offset(pos).expand(0.005f), QuadColor.single(ColorUtils.changeAlpha(new Color(topColor), (int) fillAlpha.value).getRGB()));
            }
            if (outline.value) {
                Renderer3D.drawBoxOutline(shape.getBoundingBox().offset(pos).expand(0.005f), QuadColor.single(ColorUtils.changeAlpha(new Color(topColor), (int) fillAlpha.value).getRGB()), 1f);
            }
            if (throughWalls.value) {
                Renderer3D.stopRenderingThroughWalls();
            }

            if (tracers.value) {
                Renderer3D.drawLine(Renderer3D.getEyeTracer(), shape.getBoundingBox().getCenter(), LineColor.single(topColor), 1f);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        positions.clear();
        updateBlocks();
    }

    @SubscribeEvent
    public void onChunkDataEvent(ChunkDataEvent event) {
        // Start a task to search the chunk
        HeliosExecutor.submit(new ChunkChecker(mc.world, event.getChunk(), (world, pos) -> {
            if (prevBlockList.contains(world.getBlockState(pos).getBlock()) && positions.size() < 10000) {
                positions.add(pos);
            }
        }));
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE e) {
        if (mc.world == null) return;

        if (e.packet instanceof DisconnectS2CPacket
                || e.packet instanceof PlayerRespawnS2CPacket) {
            positions.clear();
        }

        if (e.packet instanceof BlockUpdateS2CPacket packet) {
            BlockPos pos = packet.getPos();
            if (prevBlockList.contains(mc.world.getBlockState(pos).getBlock())) {
                positions.add(pos);
            } else {
                positions.remove(pos);
            }
        } else if (e.packet instanceof UnloadChunkS2CPacket p) {
            ChunkPos cp = p.pos();
            positions.removeIf(pos -> pos.getX() >= cp.getStartX()
                    && pos.getX() <= cp.getEndX()
                    && pos.getZ() >= cp.getStartZ()
                    && pos.getZ() <= cp.getEndZ());

        }
    }


    private void searchBlocks(World world, BiConsumer<World, BlockPos> consumer) {
        //Loop every chunk
        for (Chunk chunk : ChunkUtils.getLoadedChunks()) {
            //Start a task to search the chunk
            HeliosExecutor.submit(new ChunkChecker(world, chunk, consumer));
        }
    }
}