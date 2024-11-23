package dev.heliosclient.util.blocks;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import static dev.heliosclient.util.render.Renderer3D.mc;

public class BlockUtils {

    public static boolean isOpaqueFullCube(BlockPos pos) {
        return mc.world.getBlockState(pos).isOpaqueFullCube(MinecraftClient.getInstance().world, pos);
    }

    public static Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public static Block getBlockFromString(String blockString) throws InvalidIdentifierException {
        Identifier id = new Identifier(blockString);
        if (!Registries.BLOCK.containsId(id)) {
            throw new InvalidIdentifierException("No block found for identifier: " + blockString);
        }
        return Registries.BLOCK.get(id);
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!mc.player.isCreative() && state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }

    public static boolean canBreak(BlockPos blockPos) {
        BlockState state = mc.world.getBlockState(blockPos);

        return canBreak(blockPos, state);
    }

    public static boolean canBreakInstantly(BlockState state, float speed) {
        return mc.player.isCreative() || calcBlockBreakingDelta3(state, speed) >= 1;
    }

    public static boolean canPlace(BlockPos pos, BlockState state) {
        if (pos == null || mc.world == null || !World.isValid(pos) || !mc.world.getBlockState(pos).isReplaceable() || !mc.world.isInBuildLimit(pos))
            return false;
        return mc.world.getWorldBorder().contains(pos) && mc.world.canPlace(state, pos, ShapeContext.absent());
    }
    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos,mc.world.getBlockState(pos));
    }

    public static boolean breakBlock(BlockPos pos, boolean swing) {
       if (!canBreak(pos, mc.world.getBlockState(pos))) return false;
       BlockPos bp = pos instanceof BlockPos.Mutable ? new BlockPos(pos) : pos;

        if (mc.interactionManager.isBreakingBlock())
            mc.interactionManager.updateBlockBreakingProgress(pos, getBlockDirection(bp));
        else mc.interactionManager.attackBlock(pos, getBlockDirection(bp));

        if (swing) mc.player.swingHand(Hand.MAIN_HAND);
        else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        return true;
    }

    public static BlockPos toBlockPos(Vec3d pos) {
        return new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
    }

    public static float calcBlockBreakingDelta3(BlockState state, float breakSpeed) {
        float f = state.getHardness(null,null);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = mc.player.canHarvest(state) ? 30 : 100;
            return breakSpeed / f / (float) i;
        }
    }

    public static double calcBlockBreakingDelta2(BlockState state) {
        return calcBlockBreakingDelta(state, mc.player.getInventory().selectedSlot);
    }

    /**
     * @see AbstractBlock#calcBlockBreakingDelta(BlockState, PlayerEntity, BlockView, BlockPos)
     */
    public static double calcBlockBreakingDelta(BlockState state, int slot) {
        float f = state.getHardness(null, null);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            ItemStack stack =  mc.player.getInventory().getStack(slot);
            int i = (!state.isToolRequired() || stack.isSuitableFor(state)) ? 30 : 100;
            return getMiningSpeedForBlockState(state, stack) / f / (double) i;
        }
    }

    // Determine the best direction to interact with the block
    public static Direction getBlockDirection(BlockPos position) {
        Vec3d playerEyesPos = mc.player.getEyePos();
        int blockPosY = position.getY();

        BlockPos belowBlockPos = position.down();
        BlockPos aboveBlockPos = position.up();

        // Check if block is above player's eye level
        if (blockPosY > playerEyesPos.y) {
            // Check if block below the target position is replaceable
            if (mc.world.getBlockState(belowBlockPos).isReplaceable()) {
                return Direction.DOWN;
            } else {
                return mc.player.getHorizontalFacing().getOpposite();
            }
        }

        // Check if block above the target position is not replaceable
        if (!mc.world.getBlockState(aboveBlockPos).isReplaceable()) {
            return mc.player.getHorizontalFacing().getOpposite();
        }

        return Direction.UP;
    }

    /**
     * @see PlayerEntity#getBlockBreakingSpeed(BlockState)
     */
    public static double getMiningSpeedForBlockState(BlockState blockState, ItemStack itemStack) {
        double speed = itemStack.getMiningSpeedMultiplier(blockState);


        if (speed > 1) {
            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
            if (efficiency > 0 && !itemStack.isEmpty())
                speed += efficiency * efficiency + 1;
        }

        if (StatusEffectUtil.hasHaste(HeliosClient.MC.player)) {
            speed *= (1 + (StatusEffectUtil.getHasteAmplifier(HeliosClient.MC.player) + 1) * 0.2F);
        }


        if (HeliosClient.MC.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float miningFatigue = switch (HeliosClient.MC.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
            speed *= miningFatigue;
        }

        if (HeliosClient.MC.player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(HeliosClient.MC.player)) {
            speed /= 5.0F;
        }

        if (!HeliosClient.MC.player.isOnGround() || HeliosClient.MC.player.isFallFlying()) {
            speed /= 5.0F;
        }

        return speed;
    }


    public static void useItem(BlockPos pos) {
        useItem(pos, Hand.MAIN_HAND);
    }

    public static void useItem(BlockPos pos, Hand hand) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;
        Direction direction = mc.crosshairTarget != null ? ((BlockHitResult) mc.crosshairTarget).getSide() : Direction.DOWN;
        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(
                Vec3d.ofCenter(pos), direction, pos, false
        ));
        if (result.shouldSwingHand()) {
            mc.player.swingHand(hand);
        }
    }

    public static boolean isPlaceable(BlockPos pos, boolean entityCheck) {
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;

        for (Entity e : mc.world.getEntitiesByClass(Entity.class, new Box(pos), e -> !(e instanceof ExperienceBottleEntity || e instanceof ItemEntity || e instanceof ExperienceOrbEntity))) {
            if (e instanceof PlayerEntity) return false;
            return !entityCheck;
        }
        return true;
    }

    public static boolean place(BlockPos pos, boolean airPlace, boolean rotate) {
        return place(pos, rotate, false, airPlace, Hand.MAIN_HAND);
    }

    public static boolean place(BlockPos pos, boolean airPlace, boolean rotate, boolean clientSideRotation) {
        return place(pos, rotate, clientSideRotation, airPlace, Hand.MAIN_HAND);
    }

    public static boolean place(BlockPos pos, boolean airPlace, boolean rotate, boolean clientSideRotation, int itemSlotHotbar, boolean silentSwitch) {

        InventoryUtils.swapToSlot(itemSlotHotbar, silentSwitch);

        boolean returnVal = place(pos, rotate, clientSideRotation, airPlace, itemSlotHotbar == InventoryUtils.OFFHAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

        if (silentSwitch)
            InventoryUtils.swapBackHotbar();

        return returnVal;
    }


    public static boolean airBreed(Block b) {
        return b == Blocks.AIR || b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR;
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock
                || block instanceof AnvilBlock
                || block instanceof LoomBlock
                || block instanceof CartographyTableBlock
                || block instanceof GrindstoneBlock
                || block instanceof StonecutterBlock
                || block instanceof ButtonBlock
                || block instanceof AbstractPressurePlateBlock
                || block instanceof BlockWithEntity
                || block instanceof BedBlock
                || block instanceof FenceGateBlock
                || block instanceof DoorBlock
                || block instanceof NoteBlock
                || block instanceof TrapdoorBlock;
    }

    public static boolean place(BlockPos pos, boolean rotate, boolean clientSideRotation, boolean airPlace, Hand hand) {
        if (!canPlace(pos, mc.world.getBlockState(pos)) || mc.player.getStackInHand(hand) == null  || mc.player.getStackInHand(hand).isEmpty())
            return false;

        Vec3d hitPos = Vec3d.ofCenter(pos);

        Direction d = getPlaceSide(pos);
        BlockPos neighbour = null;

        if (d == null) {
            d = Direction.UP;
            neighbour = pos;
        } else {
            neighbour = airPlace ? pos : pos.offset(d);

            hitPos = hitPos.add(d.getOffsetX() * 0.5, d.getOffsetY() * 0.5, d.getOffsetZ() * 0.5);
        }

        Block neighborBlock = mc.world.getBlockState(neighbour).getBlock();

        BlockHitResult blockHitResult = new BlockHitResult(hitPos, d.getOpposite(), neighbour, false);

        ActionResult result = ActionResult.FAIL;
        if (rotate) {
            float yaw =   (float) RotationUtils.getYaw(hitPos);
            float pitch = (float) RotationUtils.getPitch(hitPos);

            RotationUtils.setServerRotations(yaw,pitch);
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        }

        result = interactBlock(blockHitResult,neighborBlock,hand);

        return result == ActionResult.SUCCESS;
    }

    private static ActionResult interactBlock(BlockHitResult blockHitResult,Block block, Hand hand){
        if (isClickable(block)) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }

        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, blockHitResult);


        if (result.shouldSwingHand()) {
            mc.player.swingHand(hand);
        }

        if (isClickable(block))
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        return result;
    }

    public static Direction getPlaceSide(BlockPos blockPos) {
        Vec3d lookVec = blockPos.toCenterPos().subtract(mc.player.getEyePos());
        double bestRelevancy = -Double.MAX_VALUE;
        Direction bestSide = null;

        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = mc.world.getBlockState(neighbor);

            if (state.isAir() || isClickable(state.getBlock())) continue;
            if (!state.getFluidState().isEmpty()) continue;

            double relevancy = side.getAxis().choose(lookVec.getX(), lookVec.getY(), lookVec.getZ()) * side.getDirection().offset();
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy;
                bestSide = side;
            }
        }

        return bestSide;
    }
}
