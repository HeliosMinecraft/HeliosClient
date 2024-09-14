package dev.heliosclient.util.blocks;

import dev.heliosclient.HeliosClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HoleUtils {

    public static Set<HoleInfo> getHoles(int range, int vRange) {
        Set<HoleInfo> holes = Collections.synchronizedSet(new HashSet<>());
        BlockIterator iterator = new BlockIterator(HeliosClient.MC.player, range, vRange);
        Set<BlockPos> checkedPositions = Collections.synchronizedSet(new HashSet<>());

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (checkedPositions.contains(pos)) continue;

            HoleInfo hole = getHoleType(HeliosClient.MC.world, pos, checkedPositions);
            if (hole != null) {
                holes.add(hole);
            }
        }
        return holes;
    }

    private static HoleInfo getHoleType(World world, BlockPos pos, Set<BlockPos> checkedPositions) {
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir() || checkedPositions.contains(pos)) {
            return null;
        }
        checkedPositions.add(pos);

        HoleType hT = null;

        int obsidian = 0, bedrock = 0, air = 0;
        BlockPos doubleStart = null;
        Block floorBlock = null;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;

            BlockPos newPos = pos.offset(dir);
            Block block = world.getBlockState(newPos).getBlock();
            if (dir == Direction.DOWN) {
                floorBlock = block;
                continue;
            }
            if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ENDER_CHEST) {
                obsidian++;
            } else if (block == Blocks.BEDROCK) {
                bedrock++;
            } else if (block == Blocks.AIR) {
                int coveringBlocks = 0;
                for (Direction dir1 : Direction.values()) {
                    if (dir1 == Direction.UP || dir.getOpposite() == dir1) continue;
                    Block block1 = world.getBlockState(newPos.offset(dir1)).getBlock();

                    if (block1 == Blocks.OBSIDIAN || block1 == Blocks.BEDROCK || block1 == Blocks.ENDER_CHEST || block1 == Blocks.CRYING_OBSIDIAN || block1 == Blocks.RESPAWN_ANCHOR) {
                        coveringBlocks++;
                    }
                }

                if (4 - coveringBlocks == 0 && !checkedPositions.contains(newPos)) {
                    checkedPositions.add(newPos);
                    air++;
                    doubleStart = newPos;
                }
            }
        }

        if (bedrock >= 4 - air && floorBlock == Blocks.OBSIDIAN) {
            hT = HoleType.UNSAFE;
        }
        if (obsidian == 0 && bedrock >= 4 - air && floorBlock == Blocks.BEDROCK) {
            hT = HoleType.SAFE;
        }
        if (obsidian >= 4 - air && bedrock == 0 && (floorBlock == Blocks.OBSIDIAN || floorBlock == Blocks.BEDROCK)) {
            hT = HoleType.DANGER;
        }
        if (obsidian > 0 && bedrock > 0 && (obsidian + bedrock) >= 4 - air && floorBlock != Blocks.AIR) {
            hT = HoleType.UNSAFE;
        }

        if(air > 0 && hT != null){
            return new HoleInfo(hT,pos,new Box(pos).union(new Box(doubleStart)).contract(0.005f, 0f, 0.005f).offset(0, 0.005f, 0));
        }

        if(hT != null){
            return new HoleInfo(hT,pos,new Box(pos).contract(0.005f, 0f, 0.005f).offset(0, 0.005f, 0));
        }

        return null;
    }

    public enum HoleType {
        SAFE, UNSAFE, DANGER
    }

    public static class HoleInfo {
        public HoleType holeType;
        public BlockPos holePosition;
        public Box holeBox;

        public HoleInfo(HoleType holeType, BlockPos holePosition, Box holeBox) {
            this.holeType = holeType;
            this.holePosition = holePosition;
            this.holeBox = holeBox;
        }
    }
}
