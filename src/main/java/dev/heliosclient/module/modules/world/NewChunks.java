package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.util.blocks.ChunkChecker;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NewChunks extends Module_ {
    private final Set<Chunk> newChunks = Collections.synchronizedSet(new HashSet<>());
    private final Set<Chunk> oldChunks = Collections.synchronizedSet(new HashSet<>());
    SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting remove = sgGeneral.add(new BooleanSetting.Builder()
            .name("Remove on disable")
            .description("Removes stored chunks on module disable.")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting color = sgGeneral.add(new RGBASetting.Builder()
            .name("Color")
            .rainbow(false)
            .defaultValue(Color.RED)
            .value(Color.RED)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting outline = sgGeneral.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting outlineWidth = sgGeneral.add(new DoubleSetting.Builder()
            .name("Outline Width")
            .description("Width of the line")
            .min(0.0)
            .max(5.0f)
            .value(1d)
            .defaultValue(1d)
            .roundingPlace(1)
            .onSettingChange(this)
            .shouldRender(() -> outline.value)
            .build()
    );
    BooleanSetting fill = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );

    public NewChunks() {
        super("NewChunks", "Displays chunks which are newly generated by the world/server", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (remove.value) {
            newChunks.clear();
            oldChunks.clear();
        }
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        Renderer3D.renderThroughWalls();

        for (Chunk c : newChunks) {
            ChunkPos cp = c.getPos();
            if (mc.getCameraEntity().getBlockPos().isWithinDistance(cp.getStartPos(), 1024)) {
                Box box = new Box(
                        cp.getStartX(), mc.world.getBottomY() - 3, cp.getStartZ(),
                        cp.getStartX() + 16, mc.world.getBottomY() - 3, cp.getStartZ() + 16);

                renderChunk(box, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH);
            }
        }

        Renderer3D.stopRenderingThroughWalls();
    }

    public void renderChunk(Box box, Direction... exclude) {
        QuadColor chunkColor = QuadColor.single(color.value.getRGB());
        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(box, chunkColor, chunkColor, (float) outlineWidth.value, exclude);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(box, chunkColor, (float) outlineWidth.value, exclude);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(box, chunkColor, exclude);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE e) {
        if (mc.world == null) return;

        if (e.packet instanceof AcknowledgeChunksC2SPacket)return;


        if (e.packet instanceof DisconnectS2CPacket) {
            newChunks.clear();
            oldChunks.clear();
        }
        if (e.packet instanceof ChunkDataS2CPacket packet) {

            ChunkPos cp = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
            WorldChunk chunk = new WorldChunk(mc.world, cp);
            chunk.loadFromPacket(packet.getChunkData().getSectionsDataBuf(), new NbtCompound(), packet.getChunkData().getBlockEntities(packet.getChunkX(), packet.getChunkZ()));

            HeliosExecutor.execute(() -> searchChunk(chunk));
        }
        if (e.packet instanceof BlockUpdateS2CPacket packet) {
            HeliosExecutor.execute(() -> checkBlockUpdate(packet.getPos(), packet.getState()));
        } else if (e.packet instanceof ChunkDeltaUpdateS2CPacket packet) {
            packet.visitUpdates((pos, state) -> {
                BlockPos posImmutable = pos.toImmutable();

                HeliosExecutor.execute(() -> checkBlockUpdate(posImmutable, state));
            });
        }
    }

    public void checkBlockUpdate(BlockPos pos, BlockState state) {
        if (!state.getFluidState().isEmpty() && !state.getFluidState().isStill()) {
            Chunk chunk = mc.world.getChunk(pos);
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN) continue;

                if (mc.world.getBlockState(pos.offset(dir)).getFluidState().isStill() && !oldChunks.contains(chunk)) {
                    newChunks.add(chunk);
                    return;
                }
            }
        }
    }


    private void searchChunk(Chunk chunk) {
        if (!newChunks.contains(chunk) && mc.world.getChunkManager().getChunk(chunk.getPos().x, chunk.getPos().z) == null) {
            HeliosExecutor.submit(new ChunkChecker(mc.world, chunk, (world, pos) -> {
                FluidState fluid = chunk.getFluidState(pos);

                if (!fluid.isEmpty() && !fluid.isStill()) {
                    oldChunks.add(chunk);
                }
            }));
        }
    }
}
