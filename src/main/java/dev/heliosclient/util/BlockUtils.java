package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
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
        if (mc.player.isCreative()) return true;

        if (state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }

    public static boolean canBreakInstantly(BlockState state, float speed) {
        return mc.player.isCreative() || calcBlockBreakingDelta2(state, speed) >= 1;
    }

    public static boolean canPlace(BlockPos pos, BlockState state) {
        if (pos == null || mc.world == null || !World.isValid(pos) || !mc.world.getBlockState(pos).isReplaceable())
            return false;
        return mc.world.getWorldBorder().contains(pos) && mc.world.canPlace(state, pos, ShapeContext.absent());
    }

    public static boolean breakBlock(BlockPos pos, boolean swing) {
        if (!canBreak(pos, mc.world.getBlockState(pos))) return false;
        BlockPos bp = pos instanceof BlockPos.Mutable ? new BlockPos(pos) : pos;


        if (mc.interactionManager.isBreakingBlock())
            mc.interactionManager.updateBlockBreakingProgress(pos, getClosestFace(bp));
        else mc.interactionManager.attackBlock(pos, getClosestFace(bp));

        if (swing) mc.player.swingHand(Hand.MAIN_HAND);
        else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        return true;
    }

    /**
     * @see AbstractBlock#calcBlockBreakingDelta(BlockState, PlayerEntity, BlockView, BlockPos)
     */
    public static double calcBlockBreakingDelta(BlockState state, ItemStack stack) {
       return calcBlockBreakingDelta2(state, getMiningSpeedForBlockState(state, stack) );
    }

    /**
     * @see AbstractBlock#calcBlockBreakingDelta(BlockState, PlayerEntity, BlockView, BlockPos)
     */
    public static double calcBlockBreakingDelta2(BlockState state, double speed) {
        float f = state.getHardness(null, null);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = HeliosClient.MC.player.canHarvest(state) ? 30 : 100;
            return speed / f / (double) i;
        }
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

    /**
     * Returns the best possible block direction to interact with.
     */
    public static Direction getClosestFace(BlockPos blockPos) {
        Vec3d vec = mc.player.getEyePos().subtract(Vec3d.ofCenter(blockPos));
        return Direction.getFacing(vec.x, vec.y, vec.z);
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
        return place(pos, airPlace, rotate, Hand.MAIN_HAND);
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

    public static boolean place(BlockPos pos, boolean rotate, boolean airPlace, Hand hand) {
        if (!mc.world.isInBuildLimit(pos) || !mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty())
            return false;

        for (Direction d : Direction.values()) {
            if (!mc.world.isInBuildLimit(pos.offset(d)))
                continue;

            Block neighborBlock = mc.world.getBlockState(pos.offset(d)).getBlock();

            if (!airPlace && isPlaceable(pos.offset(d), false))
                continue;

            if (rotate) {
                RotationUtils.lookAt(pos.offset(d).toCenterPos());
            }


            if (!isClickable(neighborBlock)) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            }

            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand,
                    new BlockHitResult(Vec3d.ofCenter(pos), airPlace ? d : d.getOpposite(), airPlace ? pos : pos.offset(d), false));

            if (result.shouldSwingHand()) {
                mc.player.swingHand(hand);
            }

            if (!isClickable(neighborBlock))
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));


            return true;
        }

        return false;
    }
}
