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
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.lists.BlockListSetting;
import dev.heliosclient.system.HeliosExecutor;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import dev.heliosclient.util.world.ChunkChecker;
import dev.heliosclient.util.world.ChunkUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

public class BlockESP extends Module_ {
    Set<BlockPos> positions = Sets.newConcurrentHashSet();
    SettingGroup sgGeneral = new SettingGroup("General");
    int prevChunkDistance = -1;

    BlockListSetting blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("Blocks")
            .description("Blocks to highlight")
            .iSettingChange(this)
            .blocks(Blocks.DIAMOND_ORE)
            .filter(block -> !BlockUtils.airBreed(block) && block != Blocks.BUBBLE_COLUMN)
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

    BooleanSetting connectedTextures = sgGeneral.add(new BooleanSetting.Builder()
            .name("Connected Textures")
            .description("Only draws outer faces of connected blocks (faster and looks much better)")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting notAirCheck = sgGeneral.add(new BooleanSetting.Builder()
            .name("Not Air Face")
            .description("Does not draw outer faces of connected blocks which are exposed to air")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
            .build()
    );
    BooleanSetting excludeUp = sgGeneral.add(new BooleanSetting.Builder()
            .name("Excludes Up Direction")
            .description("Excludes the direction from rendering")
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
            .build()
    );
    BooleanSetting excludeDown = sgGeneral.add(new BooleanSetting.Builder()
            .name("Excludes Down Direction")
            .description("Excludes the direction from rendering")
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
            .build()
    );
    BooleanSetting excludeNorth = sgGeneral.add(new BooleanSetting.Builder()
            .name("Excludes North Direction")
            .description("Excludes the direction from rendering")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
            .build()
    );
    BooleanSetting excludeSouth = sgGeneral.add(new BooleanSetting.Builder()
            .name("Excludes South Direction")
            .description("Excludes the direction from rendering")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
            .build()
    );
    BooleanSetting excludeWest = sgGeneral.add(new BooleanSetting.Builder()
            .name("Excludes West Direction")
            .description("Excludes the direction from rendering")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
            .build()
    );
    BooleanSetting excludeEast = sgGeneral.add(new BooleanSetting.Builder()
            .name("Excludes East Direction")
            .description("Excludes the direction from rendering")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->connectedTextures.value)
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
        if (prevChunkDistance != mc.options.getViewDistance().getValue()) {
            updateBlocks();
            prevChunkDistance = mc.options.getViewDistance().getValue();
        }
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if(setting == blocks || setting == connectedTextures) {
            updateBlocks();
        }
    }

    public void updateBlocks() {
        List<Block> selectedBlocks = blocks.getSelectedEntries();

        // Using a local variable for world to avoid repeated calls
        World world = mc.world;

        this.searchBlocks(world, pos -> {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            // Check if the block is selected and not already processed
            if (selectedBlocks.contains(block) && positions.size() < 10000) {
                positions.add(pos);
            }
        });
    }

    public boolean onlyAirCheck(Block b1){
        if(!notAirCheck.value)
            return true;

        return b1 != Blocks.AIR;
    }
    // Using bitwise operations for direction exclusions
    public boolean isExcludedDirection(Direction d) {
        int exclusions = (excludeUp.value ? 1 : 0) |
                (excludeDown.value ? 1 : 0) << 1 |
                (excludeNorth.value ? 1 : 0) << 2 |
                (excludeSouth.value ? 1 : 0) << 3 |
                (excludeEast.value ? 1 : 0) << 4 |
                (excludeWest.value ? 1 : 0) << 5;

        return switch (d) {
            case UP -> (exclusions & 1) != 0;
            case DOWN -> (exclusions & 2) != 0;
            case NORTH -> (exclusions & 4) != 0;
            case SOUTH -> (exclusions & 8) != 0;
            case EAST -> (exclusions & 16) != 0;
            case WEST -> (exclusions & 32) != 0;
            default -> false;
        };
    }
    public void removeExcludedDirection(Direction[] directions) {
        if(excludeNorth.value){
            ArrayUtils.removeElement(directions,Direction.NORTH);
        }
        if(excludeSouth.value ){
            ArrayUtils.removeElement(directions,Direction.SOUTH);
        }
        if(excludeUp.value ){
            ArrayUtils.removeElement(directions,Direction.UP);
        }
        if(excludeDown.value){
            ArrayUtils.removeElement(directions,Direction.DOWN);
        }
        if(excludeEast.value  ){
            ArrayUtils.removeElement(directions,Direction.EAST);
        }
        if(excludeWest.value){
            ArrayUtils.removeElement(directions,Direction.WEST);
        }
    }
    private void floodFill(World world, BlockPos pos, Block block, Set<BlockPos> visited, Set<BlockPos> processed) {
        Stack<BlockPos> stack = new Stack<>();
        stack.push(pos);

        while (!stack.isEmpty() && visited.size() <= 500) {
            BlockPos currentPos = stack.pop();

            // If already visited, skip
            if (visited.contains(currentPos)) continue;
            visited.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = currentPos.offset(direction);
                BlockState adjacentState = world.getBlockState(adjacentPos);
                Block adjacentBlock = adjacentState.getBlock();

                if (adjacentBlock == block) {
                    stack.push(adjacentPos);
                } else if (onlyAirCheck(adjacentBlock) && !isExcludedDirection(direction)) {
                    processed.add(adjacentPos);
                }
            }
        }
    }

    private void renderOuterFaces(BlockPos visitedPos, Set<BlockPos> processed) {
        // Create a copy of all possible directions (up, down, north, south, east, west)
        Direction[] directions = Direction.values().clone();

        // Iterate over all possible directions
        removeExcludedDirection(directions);

        for (Direction direction : Direction.values()) {
            if(!ArrayUtils.contains(directions,direction)){
                continue;
            }

            BlockPos adjacentPos = visitedPos.offset(direction);
            // If the adjacent position is in the processed set, remove the corresponding direction from the directions array
            if (processed.contains(adjacentPos)) {
                directions = ArrayUtils.removeElement(directions, direction);
            }
        }

        // If there are any remaining directions (i.e., not all directions were removed), draw the position with those directions
        if (directions.length > 0) {
            drawPos(visitedPos, directions);
        }
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        Set<BlockPos> processedBlocks = new HashSet<>();

        for (BlockPos pos : positions) {
            if (connectedTextures.value && processedBlocks.contains(pos)) {
                continue;
            }

            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            //Dont even bother with air blocks
            if(BlockUtils.airBreed(block)){
                continue;
            }

            if (connectedTextures.value) {
                // Create a set to store the visited blocks
                Set<BlockPos> visited = new HashSet<>();
                Set<BlockPos> processed = new HashSet<>();

                // Perform a flood fill to find all connected blocks
                floodFill(mc.world, pos, block, visited, processed);

                // Add the visited blocks to the processed blocks set
                processedBlocks.addAll(visited);

                if (visited.size() > 1) {
                    // Loop through each visited block and render its outer faces
                    for (BlockPos visitedPos : visited) {
                        renderOuterFaces(visitedPos,processed);
                    }
                } else {
                    drawPos(pos);
                }
            } else {
                drawPos(pos);
            }
        }
    }

    public void drawPos(BlockPos pos, Direction... exclude){
        BlockState state = mc.world.getBlockState(pos);

        int topColor = state.getMapColor(mc.world, pos).color;
        if (state.getBlock() == Blocks.NETHER_PORTAL) {
            topColor = ColorUtils.rgbToInt(107, 0, 209);
        }
        QuadColor color = QuadColor.single(ColorUtils.changeAlpha(new Color(topColor), (int) fillAlpha.value).getRGB());

        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) {
            shape = VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
        }
        if (throughWalls.value)
            Renderer3D.renderThroughWalls();

        Box box = shape.getBoundingBox().offset(pos).expand(0.005f);

        if (fill.value) {
            Renderer3D.drawBoxFill(box, color,exclude);
        }
        if (outline.value) {
            Renderer3D.drawBoxOutline(box, color, 1f,exclude);
        }
        if (throughWalls.value) {
            Renderer3D.stopRenderingThroughWalls();
        }

        if (tracers.value) {
            Renderer3D.drawLine(Renderer3D.getEyeTracer(), box.getCenter(), LineColor.single(topColor), 1f);
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
        HeliosExecutor.submit(new ChunkChecker(mc.world, event.getChunk(), pos -> {
            if (blocks.getSelectedEntries().contains(mc.world.getBlockState(pos).getBlock()) && positions.size() < 10000) {
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
            if (blocks.getSelectedEntries().contains(mc.world.getBlockState(pos).getBlock())) {
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



    private void searchBlocks(World world, Consumer<BlockPos> consumer) {
        //Loop every chunk
        for (Chunk chunk : ChunkUtils.getLoadedChunks()) {
            //Start a task to search the chunk
            HeliosExecutor.submit(new ChunkChecker(world, chunk, consumer));
        }
    }
}