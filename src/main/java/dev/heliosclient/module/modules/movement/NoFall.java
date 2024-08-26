package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.system.mixininterface.IVec3d;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.player.DamageUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.List;

public class NoFall extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting cancelBounce = sgGeneral.add(new BooleanSetting.Builder()
            .name("Cancel Bounce")
            .description("Cancels bouncing on certain blocks")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    DoubleSetting fallHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("Trigger height")
            .description("Height on which No Fall triggers")
            .onSettingChange(this)
            .value(2.5)
            .defaultValue(2.5)
            .min(2)
            .max(22)
            .roundingPlace(1)
            .build()
    );
    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Mode which should save player from fall height ")
            .onSettingChange(this)
            .defaultValue(List.of("Classic", "AlwaysOnGround", "Clutch", "Disconnect (annoying)"))
            .defaultListIndex(0)
            .build()
    );
    BooleanSetting forcePlace = sgGeneral.add(new BooleanSetting.Builder()
            .name("ForcePlace")
            .description("Clutches even if there is already a safe block below you (powder snow, cobweb, water, haybale, or slime block)")
            .onSettingChange(this)
            .defaultValue(true)
            .shouldRender(() -> mode.value == 2)
            .build()
    );
    BooleanSetting onlyApplyNearDeath = sgGeneral.add(new BooleanSetting.Builder()
            .name("Only on death")
            .description("Clutches only if you will die or your health will get really low.")
            .onSettingChange(this)
            .defaultValue(false)
            .shouldRender(() -> mode.value == 2)
            .build()
    );
    BooleanSetting airPlace = sgGeneral.add(new BooleanSetting.Builder()
            .name("AirPlace")
            .description("Air Places clutch blocks")
            .onSettingChange(this)
            .defaultValue(false)
            .shouldRender(() -> mode.value == 2)
            .build()
    );
    BooleanSetting rotate = sgGeneral.add(new BooleanSetting.Builder()
            .name("Rotate")
            .description("Rotate before clutching, will only work with blocks")
            .onSettingChange(this)
            .defaultValue(true)
            .shouldRender(() -> mode.value == 2)
            .build()
    );
    BooleanSetting stop = sgGeneral.add(new BooleanSetting.Builder()
            .name("Stop Movement")
            .description("Stops your XZ movement before clutching to ensure you land fine.")
            .onSettingChange(this)
            .defaultValue(false)
            .shouldRender(() -> mode.value == 2)
            .build()
    );

    ClutchManager cm = new ClutchManager();

    public NoFall() {
        super("NoFall", "Prevents you from taking fall damage.", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }


    @Override
    public void onEnable() {
        super.onEnable();
        cm.resetClutch();
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
        if (cancelBounce.value && mode.value == 2) {
            ChatUtils.sendHeliosMsg("(NoFall Clutch) SlimeBlocks will cause fall damage with cancelBounce on!");
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        assert mc.player != null;
        if (mc.player.fallDistance >= fallHeight.value && !mc.player.isCreative()) {
            if (mode.value == 0) {
                // Second condition is to check if the y velocity of player is fast enough to cause damage.
                // Prevents being rate-limited or kicked and only sends a packet when needed
                if (mc.player.getVelocity().y > -0.432f)
                    return;

                // Does half-heart damage before falling but neglects rest
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.PositionAndOnGround(
                                mc.player.getX(),
                                mc.player.getY(),
                                mc.player.getZ(),
                                true
                        )
                );
            } else if (mode.value == 1) {
                // Prevents the half-heart damage from classic mode
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.OnGroundOnly(
                                true
                        )
                );

                //Clutch mode!
            } else if (mode.value == 2) {

                if (shouldResetClutch()) {
                    cm.resetClutch();
                }
                clutch();
            } else if (mode.value == 3) {
                int distance = 0;
                int y = (int) mc.player.getY();
                int maxDistance = y - 1;
                while (distance < maxDistance) {
                    if (!mc.player.clientWorld.isAir(mc.player.getBlockPos().down(distance + 1))) {
                        break;
                    }
                    distance++;
                }
                if (distance <= 3) {
                    assert mc.world != null;
                    mc.player.networkHandler.sendPacket(
                            new PlayerMoveC2SPacket.OnGroundOnly(true)
                    );
                    mc.world.disconnect();
                }
            }
        }
    }

    private void clutch() {
        float health = mc.player.getHealth();
        boolean shouldClutch = !onlyApplyNearDeath.value || DamageUtils.calcFallDamage(mc.player) >= health - 2;

        if (shouldClutch) {
            BlockHitResult result = mc.world.raycast(new RaycastContext(mc.player.getPos(), mc.player.getPos().subtract(0, 4, 0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));

            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                BlockPos bpDown = result.getBlockPos();

                if (!isPlayerAlreadySafe(bpDown)) {
                    for (ClutchItem item : ClutchItem.values()) {
                        cm.clutch(item, bpDown);
                        if (cm.hasClutchedProperly()) {
                            break;
                        }
                    }
                }

            }
        }
    }

    private boolean shouldResetClutch() {
        return mc.player.isOnGround() || mc.player.getBlockStateAtPos().getFluidState() != Fluids.EMPTY.getDefaultState();
    }

    public boolean isPlayerAlreadySafe(BlockPos bpDown) {
        Block block = mc.world.getBlockState(bpDown).getBlock();

        if (forcePlace.value) {
            return false;
        }

        //If the block is air, then it's not safe.
        if (BlockUtils.airBreed(block)) {
            return false;
        }

        //If block is clutch item then it's probably safe. (probably because we are not accounting fallDistance for now,
        // meaning haybales and slimeblocks may still kill you).
        for (ClutchItem item : ClutchItem.values()) {
            if (item.getBlock() == block || block.getDefaultState().isLiquid()) {
                return true;
            }
        }

        //Return not safe if it is not safe (duh)
        return false;
    }

    public enum ClutchItem {
        WATER_BUCKET(Items.WATER_BUCKET, Items.BUCKET, Blocks.WATER),
        POWDER_SNOW_BUCKET(Items.POWDER_SNOW_BUCKET, Items.BUCKET, Blocks.POWDER_SNOW),
        HAY_BLOCK(Items.HAY_BLOCK, null, Blocks.HAY_BLOCK),
        SLIME_BLOCK(Items.SLIME_BLOCK, null, Blocks.SLIME_BLOCK),
        COB_WEB(Items.COBWEB, null, Blocks.COBWEB);


        private final Item item;
        private final Item resultItem;
        private final Block correspondingBlock;

        ClutchItem(Item item, Item resultItem, Block correspondingBlock) {
            this.item = item;
            this.resultItem = resultItem;
            this.correspondingBlock = correspondingBlock;
        }

        public Item getItem() {
            return item;
        }

        public Block getBlock() {
            return correspondingBlock;
        }

        //Result item is the item which will be transformed into after clutching.
        //For blocks, it's they remain the same but for items like Bucket, they will change to empty buckets.
        public Item getResultItem() {
            return resultItem;
        }
    }

    public class ClutchManager {
        private boolean hasClutchedProperly = false;


        public void clutch(ClutchItem item, BlockPos pos) {
            if (item == ClutchItem.WATER_BUCKET && mc.world.getDimensionKey() == DimensionTypes.THE_NETHER) {
                return;
            }

            int slot = InventoryUtils.findItemInHotbar(item.getItem());
            if (slot == -1) {
                hasClutchedProperly = false;
                return;
            }

            if (hasClutchedProperly()) return;

            ItemStack stack = mc.player.getInventory().getStack(slot);
            int count = InventoryUtils.getItemStackCountSafe(stack);


            InventoryUtils.swapToSlot(slot, true);

            //We want to rotate only when the player has set the rotate setting for blocks
            rotate(rotate.value, pos.toCenterPos(), item.getResultItem());

            double prevX = mc.player.getVelocity().x;
            double prevZ = mc.player.getVelocity().z;

            if (stop.value)
                ((IVec3d) mc.player.getVelocity()).heliosClient$setXZ(0, 0);

            if (item.getResultItem() == null) {
                double prevY = mc.player.getVelocity().y;

                ((IVec3d) mc.player.getVelocity()).heliosClient$setY(0);

                BlockUtils.place(pos, rotate.value,false, airPlace.value, slot == InventoryUtils.OFFHAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

                ((IVec3d) mc.player.getVelocity()).heliosClient$setY(prevY);

            } else {
                ((AccessorMinecraftClient) mc).rightClick();
            }

            if (item.getResultItem() != null) {
                hasClutchedProperly = mc.player.getInventory().getStack(slot).getItem() == item.getResultItem();
            } else {
                ItemStack newStack = mc.player.getInventory().getStack(slot);
                if (newStack != null)
                    hasClutchedProperly = newStack.getCount() != count;
            }

            if (stop.value)
                ((IVec3d) mc.player.getVelocity()).heliosClient$setXZ(prevX, prevZ);
        }

        public void rotate(boolean rotate, Vec3d vec, Item resultItem) {
            if (resultItem == null && !rotate) return;

            RotationUtils.lookAt(vec);
        }

        public boolean hasClutchedProperly() {
            return hasClutchedProperly;
        }

        public void resetClutch() {
            hasClutchedProperly = false;
        }
    }
}
