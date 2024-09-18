package dev.heliosclient.util.blocks;

import dev.heliosclient.HeliosClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class HoleUtils {
    // List of blocks considered blast-proof (cannot be destroyed by explosions)
    static List<Block> blastProofBlocks = List.of(Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.ENDER_CHEST,Blocks.CRYING_OBSIDIAN);

    // Arrays to define the relative positions of blast-resistant blocks and air blocks in a quad hole
    // North and south are on the Z axis

    // All three down below are configured using the 2D diagram, so the "0"th rel position of the block corresponds to the 0th element of these array.
    // (That's why these are lookup tables)
    static Direction[][] blastResistantOffset = new Direction[][]{
            {Direction.NORTH, Direction.WEST},
            {Direction.NORTH, Direction.EAST},
            {Direction.SOUTH, Direction.WEST},
            {Direction.SOUTH, Direction.EAST}
    };

    //This is basically "relPosNum" array but instead of byte, we use Direction to specify the relative pos offset
    // (because it is easier to work with BlockPos this way)
    static Direction[][] airOffset = new Direction[][]{
            {Direction.SOUTH, Direction.EAST},
            {Direction.SOUTH, Direction.WEST},
            {Direction.NORTH, Direction.EAST},
            {Direction.NORTH, Direction.WEST}
    };

    // Lookup table for relative position numbering in a quad hole
    // This one is required because the above 2 arrays use index same as the block position (check the diagram).
    static byte[][] relPosNum = new byte[][]{
            {2, 1},
            {3, 0},
            {0, 3},
            {1, 2}
    };

    /**
     * Finds all holes within a given range around the player.
     * @param range Horizontal range to search for holes
     * @param vRange Vertical range to search for holes
     * @return A set of HoleInfo objects representing found holes
     */
    public static Set<HoleInfo> getHoles(int range, int vRange, boolean quadChecker) {
        Set<HoleInfo> holes = Collections.synchronizedSet(new HashSet<>());
        BlockIterator iterator = new BlockIterator(HeliosClient.MC.player, range, vRange);
        Set<BlockPos> checkedPositions = Collections.synchronizedSet(new HashSet<>());

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            if (checkedPositions.contains(pos)) continue;

            HoleInfo hole = getHoleType(HeliosClient.MC.world, pos, checkedPositions,quadChecker);
            if (hole != null) {
                holes.add(hole);
            }
        }
        return holes;
    }

    public static boolean isQuad(BlockPos pos, World world, Set<BlockPos> checked) {
        if (!world.isAir(pos)) return false;

        // This is to get the position of the pos as 0,1,2,3 instead of BlockPos (For lookup tables).
        // Also, this is relative otherwise 4 quads will be displayed.
        byte relPos = getRelativePosition(pos, world);
        if (relPos < 0 || relPos > 3) return false;

        // Okay, so I will do the full 2D diagram I used to derive this algorithm (??):

        // Key short forms I used:
        // "B" - Blast resistant block, "0,1,2,3" - look up for block offsets from the origin (which is the relPos byte)
        // "N,S,E,W" respectively are the cardinal directions with the N facing this direction ^ for this diagram.
        // All the arrays have been configured using the 2D representative of the quad hole. (Because we only check horizontally and down)

        // So, lets assume, the following diagram is our quad hole. So in this, if we search from the pos number "0",
        // then we will first check if the "B" around pos 0 are solid or not, at the same time we check if 1 and 2 is air or not (for pos 0).
        // If one of these conditions fail (i.e. 1 and 2 are not air OR "0" is not covered by B blocks)
        // Then using the offset of X and Z from 1 and 2 (respectively) we get the diagonal block 3 (pythagoras).
        // Before checking if 1 and 2 is covered by B, we check if 3 is air and if it is covered by B blocks.
        // This is to do early return, so that we don't waste time for checking 1 and 2 as well.
        // Using the Lookup tables (the static arrays defined above), we check with the same above algorithm (??).
        // Use of byte is enough since we only need to go from 0 to 3 at max.
        // This might be complex (who even knows to use D.D Arrays) and unnecessary but, I thought of this while sleeping and the anxiety got to me.
        //              B       B
        //           - - - - - - - - -
        //     B    |        |        |    B
        //          |   0    |   1    |
        //          |- - - - | - - - -|
        //          |        |        |
        //     B    |   2    |   3    |    B
        //           - - - - - - - - -
        //              B        B

        // Note: In all the arrays above, the first element is for Z position and the second element is for X position.

        // And at last, this is kind of better than running loops or using BlockPos offsets to check (sometimes recursive).
        // All of this becomes linear and checks 2 things at once.

        // P.S If you remove the check for diagonals, and with a few more tweaks, you could make it show Triples as well.

        return checkQuadCorners(relPos, world, pos, checked);
    }

    /**
     * Determines the relative position of a block in a potential quad hole.
     * @param pos The position to check
     * @param world The world in which to check
     * @return A byte representing the relative position, or -1 if not part of a quad hole
     */
    private static byte getRelativePosition(BlockPos pos, World world) {
        byte relPos = -1;

        boolean northBlastProof = isBlastProof(world.getBlockState(pos.north()).getBlock());
        boolean southBlastProof = isBlastProof(world.getBlockState(pos.south()).getBlock());
        boolean westBlastProof = isBlastProof(world.getBlockState(pos.west()).getBlock());
        boolean eastBlastProof = isBlastProof(world.getBlockState(pos.east()).getBlock());

        // Determine the relative position based on blast-proof and air blocks
        if (northBlastProof && world.isAir(pos.south())) relPos = 0;
        else if (southBlastProof && world.isAir(pos.north())) relPos = 2;

        if (westBlastProof && world.isAir(pos.east())) return relPos;
        else if (eastBlastProof && world.isAir(pos.west()) && relPos != -1) return (byte)(relPos + 1);

        return relPos;
    }

    /**
     * Checks all corners of a potential quad hole.
     * @param relPos The relative position of the initial block
     * @param world The world in which to check
     * @param origin The origin position of the potential quad hole
     * @param checked Set of already checked positions
     * @return true if all corners form a valid quad hole, false otherwise
     */
    private static boolean checkQuadCorners(byte relPos, World world, BlockPos origin, Set<BlockPos> checked) {
        // Check the diagonal position
        BlockPos diagPos = origin.offset(airOffset[relPos][0]).offset(airOffset[relPos][1]);
        Direction[] blastResisOffset = blastResistantOffset[3 - relPos];

        Block diagXBlock = world.getBlockState(diagPos.offset(blastResisOffset[1])).getBlock();
        Block diagZBlock = world.getBlockState(diagPos.offset(blastResisOffset[0])).getBlock();

        // If diagonal position isn't air or isn't surrounded by blast-proof blocks or the block below it is not blast-proof then it's not a quad hole
        if (!world.isAir(diagPos) || !checkIfIsBlastProof(diagXBlock,diagZBlock) || /* This checks below the diag Block */ !isBlastProof(world.getBlockState(diagPos.down()).getBlock())) return false;

        addToChecked(checked, diagPos);

        // Check the other two corners
        byte airOffX = relPosNum[relPos][1];
        byte airOffZ = relPosNum[relPos][0];
        //System.out.println(airOffX + " airOffX");
        //System.out.println(airOffZ + " airOffZ");

        return checkCorner(world, origin, airOffX,relPos,checked,false) && checkCorner(world, origin, airOffZ,relPos, checked,true);
    }

    private static boolean checkCorner(World world, BlockPos origin, byte airOff,byte relPos, Set<BlockPos> checked, boolean isZ) {
        Direction[] blastResistOff = blastResistantOffset[airOff];
        BlockPos airPos = origin.offset(airOffset[relPos][isZ ? 0 : 1]);

        BlockPos xPos = airPos.offset(blastResistOff[1]);
        BlockPos zPos = airPos.offset(blastResistOff[0]);

        Block xBlock = world.getBlockState(xPos).getBlock();
        Block zBlock = world.getBlockState(zPos).getBlock();

        addToChecked(checked, airPos, xPos, zPos);

        // We don't need to check if the block is air because we already did in the getRelativePosition() (except for the diagonal);

        // If the air position isn't surrounded by blast-proof blocks
        // or the block below it is not blast-proof then it's not a valid corner
        return checkIfIsBlastProof(xBlock,zBlock) && isBlastProof(world.getBlockState(airPos.down()).getBlock());
    }

    private static void addToChecked(Set<BlockPos> checked, BlockPos... positions) {
        checked.addAll(Arrays.asList(positions));
    }

    private static boolean checkIfIsBlastProof(Block pos1, Block pos2){
        return isBlastProof(pos1) && isBlastProof(pos2);
    }

    private static boolean isBlastProof(Block block){
        return blastProofBlocks.contains(block);
    }

    private static HoleInfo getHoleType(World world, BlockPos pos, Set<BlockPos> checkedPositions, boolean quadChecker) {
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir() || checkedPositions.contains(pos)) {
            return null;
        }


        if (quadChecker && isQuad(pos, world,checkedPositions)) {
            //System.out.println("QUAD: " + pos);

            //Return a box containing all the four poses.
            BlockPos start = pos.offset(airOffset[0][0]).offset(airOffset[0][1]);
            checkedPositions.add(pos);

            return new HoleInfo(HoleType.UNSAFE, pos, new Box(pos).union(new Box(start)).contract(0.005f, 0f, 0.005f).offset(0, 0.005f, 0));
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
