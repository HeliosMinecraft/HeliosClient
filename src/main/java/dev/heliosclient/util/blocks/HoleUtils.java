package dev.heliosclient.util.blocks;

import dev.heliosclient.HeliosClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HoleUtils {

    public static Set<HoleInfo> getHoles(int range, int vRange) {
        Set<HoleInfo> holes = Collections.synchronizedSet(new HashSet<>());
        BlockIterator iterator = new BlockIterator(HeliosClient.MC.player, range, vRange);
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            HoleInfo hole = checkHole(HeliosClient.MC.world, pos);
            if (hole != null) {
                holes.add(hole);
            }
        }
        return holes;
    }

    private static HoleInfo checkHole(World world, BlockPos pos) {
        HoleType holeType = getHoleType(world, pos);
        if (holeType != null) {
            return new HoleInfo(holeType, pos);
        }
        return null;
    }

    private static HoleType getHoleType(World world, BlockPos pos) {
        if (!world.getBlockState(pos).isAir()) {
            return null;
        }

        int obsidian = 0, bedrock = 0, air = 0;
        Block floorBlock = null;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;

            BlockPos newPos = pos.offset(dir);
            Block block = world.getBlockState(newPos).getBlock();
            if (dir == Direction.DOWN) {
                floorBlock = block;
                continue;
            }
            if (block == Blocks.OBSIDIAN) {
                obsidian++;
            } else if (block == Blocks.BEDROCK) {
                bedrock++;
            } else if (block == Blocks.AIR) {
                //Todo: fix
                int coveringBlocks = 0;

             /*   for (Direction dir1: Direction.values()) {
                    if (dir1 == Direction.UP || dir.getOpposite() == dir1 || dir1 == Direction.DOWN) continue;
                    Block block1 = world.getBlockState(newPos.offset(dir1)).getBlock();

                    if(block1 == Blocks.OBSIDIAN || block1 == Blocks.BEDROCK || block1 == Blocks.ENDER_CHEST){
                        ++coveringBlocks;
                    }
                }

              */

                if (coveringBlocks > 2) {
                    air++;
                }
            }
        }

        if (bedrock >= 4 && floorBlock == Blocks.OBSIDIAN) {
            return HoleType.UNSAFE;
        }
        if (obsidian == 0 && bedrock >= 4 && floorBlock == Blocks.BEDROCK) {
            return HoleType.SAFE;
        }
        if (obsidian >= 4 && bedrock == 0 && (floorBlock == Blocks.OBSIDIAN || floorBlock == Blocks.BEDROCK)) {
            return HoleType.DANGER;
        }
        if (obsidian > 0 && bedrock > 0 && (obsidian + bedrock) >= 4 && floorBlock != Blocks.AIR) {
            return HoleType.UNSAFE;
        }
        //Meaning the size of the hole is more than 1
        if (air <= 2 && air != 0 && obsidian + bedrock >= 2 && floorBlock != Blocks.AIR) {
            return HoleType.SIZED;
        }
        return null;
    }


    public enum HoleType {
        SAFE, UNSAFE, DANGER, SIZED
    }

    public static class HoleInfo {
        public HoleType holeType;
        public BlockPos holePosition;

        public HoleInfo(HoleType holeType, BlockPos holePosition) {
            this.holeType = holeType;
            this.holePosition = holePosition;
        }
    }

}
