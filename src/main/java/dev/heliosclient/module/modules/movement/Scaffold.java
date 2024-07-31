package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.lists.BlockListSetting;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.render.GradientBlockRenderer;
import dev.heliosclient.util.render.color.QuadColor;
import me.x150.renderer.render.Renderer3d;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scaffold extends Module_ {
    final Vec3d dimensions = new Vec3d(1, 1, 1);
    private final SettingGroup sgGeneral = new SettingGroup("General");
    private final SettingGroup sgPlace = new SettingGroup("Place");
    private final SettingGroup sgSwitch = new SettingGroup("Switch");
    private final SettingGroup sgRender = new SettingGroup("Render");
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    /* General */
    BlockListSetting blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("Blocks")
            .description("Blocks to place")
            .iSettingChange(this)
            .blocks(Blocks.DIRT, Blocks.TNT, Blocks.OBSIDIAN)
            .build()
    );
    DropDownSetting towerMode = sgGeneral.add(new DropDownSetting.Builder()
            .name("TowerMode")
            .description("The way to tower up")
            .onSettingChange(this)
            .defaultValue(List.of(TowerMode.values()))
            .defaultListOption(TowerMode.None)
            .build()
    );
    DoubleSetting towerSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Tower Speed")
            .description("Upward speed while towering in motion")
            .onSettingChange(this)
            .defaultValue(0.35d)
            .range(0, 3)
            .roundingPlace(3)
            .shouldRender(() -> towerMode.getOption() != TowerMode.None)
            .build()
    );
    /* Place */
    BooleanSetting whileMoving = sgGeneral.add(new BooleanSetting.Builder()
            .name("Tower While Moving")
            .description("Will only tower if you are moving in x,z axis")
            .onSettingChange(this)
            .defaultValue(false)
            .shouldRender(() -> towerMode.getOption() != TowerMode.None)
            .build()
    );
    DoubleSetting extendRange = sgPlace.add(new DoubleSetting.Builder()
            .name("Extend Range")
            .description("Max range of blocks to be placed in front of the player.")
            .onSettingChange(this)
            .defaultValue(1d)
            .range(0, 8)
            .roundingPlace(0)
            .build()
    );
    DoubleSetting placeRadius = sgPlace.add(new DoubleSetting.Builder()
            .name("Radius")
            .description("Radius around the place to place the blocks")
            .onSettingChange(this)
            .defaultValue(0d)
            .range(0, 5)
            .roundingPlace(0)
            .build()
    );
    DoubleSetting blocksPerTick = sgPlace.add(new DoubleSetting.Builder()
            .name("Blocks / tick")
            .description("The blocks per tick to be placed, essentially the amount of blocks to place before waiting for next tick")
            .onSettingChange(this)
            .defaultValue(5d)
            .range(0, 120)
            .roundingPlace(0)
            .build()
    );
    public BooleanSetting down = sgPlace.add(new BooleanSetting.Builder()
            .name("Down")
            .description("Press shift / sneak to go down")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting airPlace = sgPlace.add(new BooleanSetting.Builder()
            .name("AirPlace")
            .description("Places the blocks in air")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting rotate = sgPlace.add(new BooleanSetting.Builder()
            .name("Rotate")
            .description("Rotates the player to look at the block pos when placing.")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting clientSide = sgPlace.add(new BooleanSetting.Builder()
            .name("ClientSide rotation")
            .onSettingChange(this)
            .defaultValue(false)
            .shouldRender(() -> rotate.value)
            .build()
    );
    /* Switch */
    BooleanSetting autoSwitch = sgSwitch.add(new BooleanSetting.Builder()
            .name("AutoSwitch")
            .description("Automatically switches to the first selected block find")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    BooleanSetting silentSwitch = sgSwitch.add(new BooleanSetting.Builder()
            .name("Silent Switch")
            .description("Silently switches to the item")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    /* Render */
    BooleanSetting render = sgRender.add(new BooleanSetting.Builder()
            .name("Render")
            .description("Render the block placing")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    BooleanSetting clientColorCycle = sgRender.add(new BooleanSetting.Builder()
            .name("Client Color Cycle sync")
            .description("Renders a gradient box with color matching the client")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    RGBASetting fillColor = sgRender.add(new RGBASetting.Builder()
            .name("Fill color")
            .description("Color of the Fill")
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 125))
            .onSettingChange(this)
            .rainbow(false)
            .shouldRender(() -> !clientColorCycle.value)
            .build()
    );
    RGBASetting lineColor = sgRender.add(new RGBASetting.Builder()
            .name("Line color")
            .description("Color of the line")
            .value(Color.WHITE)
            .defaultValue(Color.WHITE)
            .onSettingChange(this)
            .rainbow(false)
            .shouldRender(() -> !clientColorCycle.value)
            .build()
    );
    private int counter = 0;

    public Scaffold() {
        super("Scaffold", "Places blocks below your feet for you.", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgPlace);
        addSettingGroup(sgSwitch);
        addSettingGroup(sgRender);

        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        counter = 0;
        blockPos.set(0);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (blocks.getSelectedEntries().isEmpty()) return;

        Vec3d nextPos = mc.player.getPos().add(mc.player.getVelocity());

        if (!airPlace.value) {
            nextPos = nextPos.subtract(0, 0.6, 0);
        }
        if (down.value && mc.options.sneakKey.isPressed() && !mc.options.jumpKey.isPressed() && mc.player.getY() + nextPos.y > -1) {
            nextPos = nextPos.subtract(0, 1, 0);
        }

        blockPos.set(BlockUtils.toBlockPos(nextPos));

        if (blockPos.getY() >= mc.player.getBlockPos().getY()) {
            blockPos.setY(mc.player.getBlockPos().getY() - 1);
        }

        BlockPos immutable = blockPos.toImmutable();


        placeFromCenter(immutable);


        if (towerMode.getOption() != TowerMode.None && mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) {
            //Only tower if there is air above.
            BlockState state = mc.world.getBlockState(mc.player.getBlockPos().up(2));
            if (state.isAir()) {
                if (whileMoving.value || !PlayerUtils.isMoving(mc.player)) {
                    mc.player.setVelocity(mc.player.getVelocity().x, towerSpeed.value, mc.player.getVelocity().z);
                }
            }
        }
    }

    public void placeFromCenter(BlockPos center) {
        int itemSlot = -1;
        if (autoSwitch.value) {
            for (int i = 0; i < blocks.getSelectedEntries().size(); i++) {
                int finalI = i;
                itemSlot = InventoryUtils.findInHotbar(item -> blocks.getSelectedEntries().get(finalI).asItem() == item);

                if (itemSlot != -1) {
                    break;
                }
            }
        }
        if (itemSlot == -1) {
            //Toggle maybe?
            return;
        }

        //Place the block below the player
        placeBlockPos(center, itemSlot);

        // Calculate the direction the player is moving
        Vec3d direction = Vec3d.ZERO;
        if (mc.options.forwardKey.isPressed()) {
            direction = direction.add(mc.player.getRotationVector());
        }
        if (mc.options.backKey.isPressed()) {
            direction = direction.subtract(mc.player.getRotationVector());
        }
        if (mc.options.leftKey.isPressed()) {
            direction = direction.add(new Vec3d(-mc.player.getRotationVector().z, 0, mc.player.getRotationVector().x));
        }
        if (mc.options.rightKey.isPressed()) {
            direction = direction.add(new Vec3d(mc.player.getRotationVector().z, 0, -mc.player.getRotationVector().x));
        }

        // Normalize the direction vector to prevent faster movement when moving diagonally
        direction = direction.normalize();

        for (int i = 1; i <= extendRange.value; i++) {
            BlockPos pos = center.add((int) (direction.x * i), 0, (int) (direction.z * i));

            // Check if the block can be placed at the position
            BlockState state = mc.world.getBlockState(pos);
            if (!BlockUtils.canPlace(pos, state) || !state.isAir()) continue;

            if (!placeBlockPos(pos, itemSlot)) {
                break;
            }
        }

        int rangeInt = (int) placeRadius.value;

        if (rangeInt == 0) return;


        // A list to hold the blocks to be placed
        List<BlockPos> blocksToPlace = new ArrayList<>();

        for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int z = -rangeInt; z <= rangeInt; z++) {
                BlockPos pos = center.add(x, 0, z);

                if (!BlockUtils.canPlace(pos, mc.world.getBlockState(pos))) continue;

                if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) <= placeRadius.value || (x == blockPos.getX() && z == blockPos.getZ())) {
                    blocksToPlace.add(pos);
                }
            }
        }

        if (!blocksToPlace.isEmpty()) {
            // Sort the blocks by distance to the player, in descending order
            blocksToPlace.sort(Comparator.comparingDouble(PlayerUtils::getSquaredDistanceToBP));


            for (BlockPos pos : blocksToPlace) {
                if (!placeBlockPos(pos, itemSlot)) {
                    break;
                }
            }
        }
    }

    private boolean placeBlockPos(BlockPos pos, int itemSlot) {
        boolean placeResult = false;

        if (counter >= blocksPerTick.value) {
            counter = 0;
            return false;
        }


        if (autoSwitch.value) {
            placeResult = BlockUtils.place(pos, airPlace.value, rotate.value, clientSide.value, itemSlot, silentSwitch.value);
        } else {
            placeResult = BlockUtils.place(pos, airPlace.value, rotate.value);
        }

        if (placeResult) {
            if (render.value) {
                if (clientColorCycle.value) {
                    GradientBlockRenderer.renderGradientBlock(
                            ColorManager.INSTANCE::getPrimaryGradientStart,
                            ColorManager.INSTANCE::getPrimaryGradientEnd,
                            pos,
                            true,
                            500,
                            QuadColor.CardinalDirection.DIAGONAL_LEFT
                    );
                } else {
                    Renderer3d.renderFadingBlock(lineColor.value, fillColor.value, pos.toCenterPos().subtract(0.5f, 0.5f, 0.5f), dimensions, 1000);
                }
            }

            counter++;
        }

        return true;
    }

    private enum TowerMode {
        None,
        Motion
    }

}
