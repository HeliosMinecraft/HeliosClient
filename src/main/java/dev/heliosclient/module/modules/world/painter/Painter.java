package dev.heliosclient.module.modules.world.painter;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.FileUtils;
import dev.heliosclient.util.blocks.BlockUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Painter extends Module_ {

    //Text block
    private static final String FOUR_BY_FOUR_WALL = """
            [0,0,0]{minecraft:obsidian}
            [1,0,0]{minecraft:obsidian}
            [2,0,0]{minecraft:obsidian}
            [3,0,0]{minecraft:obsidian}
            [0,1,0]{minecraft:obsidian}
            [0,2,0]{minecraft:obsidian}
            [0,3,0]{minecraft:obsidian}
            [1,1,0]{minecraft:obsidian}
            [1,2,0]{minecraft:obsidian}
            [1,3,0]{minecraft:obsidian}
            [2,1,0]{minecraft:obsidian}
            [2,2,0]{minecraft:obsidian}
            [2,3,0]{minecraft:obsidian}
            [3,1,0]{minecraft:obsidian}
            [3,2,0]{minecraft:obsidian}
            [3,3,0]{minecraft:obsidian}""";

    private static final String WITHER = """
            [0,0,0]{minecraft:soul_sand}
            [0,1,0]{minecraft:soul_sand}
            [0,1,-1]{minecraft:soul_sand}
            [0,1,1]{minecraft:soul_sand}
            [0,2,0]{minecraft:wither_skeleton_skull}
            [0,2,1]{minecraft:wither_skeleton_skull}
            [0,2,-1]{minecraft:wither_skeleton_skull}""";

    private static final String NOMAD_HUT = """
            [1,0,0]{minecraft:obsidian}
            [1,1,0]{minecraft:obsidian}
            [1,2,0]{minecraft:obsidian}
            [0,2,0]{minecraft:obsidian}
            [-1,0,0]{minecraft:obsidian}
            [-1,1,0]{minecraft:obsidian}
            [-1,2,0]{minecraft:obsidian}
            [1,3,0]{minecraft:obsidian}
            [0,3,0]{minecraft:obsidian}
            [-1,3,0]{minecraft:obsidian}
            [2,0,1]{minecraft:obsidian}
            [2,1,1]{minecraft:obsidian}
            [2,2,1]{minecraft:obsidian}
            [2,0,2]{minecraft:obsidian}
            [2,2,2]{minecraft:obsidian}
            [2,0,3]{minecraft:obsidian}
            [2,1,3]{minecraft:obsidian}
            [2,2,3]{minecraft:obsidian}
            [-2,0,1]{minecraft:obsidian}
            [-2,1,1]{minecraft:obsidian}
            [-2,2,1]{minecraft:obsidian}
            [-2,0,2]{minecraft:obsidian}
            [-2,2,2]{minecraft:obsidian}
            [-2,0,3]{minecraft:obsidian}
            [-2,1,3]{minecraft:obsidian}
            [-2,2,3]{minecraft:obsidian}
            [1,0,4]{minecraft:obsidian}
            [1,1,4]{minecraft:obsidian}
            [1,2,4]{minecraft:obsidian}
            [0,0,4]{minecraft:obsidian}
            [0,2,4]{minecraft:obsidian}
            [-1,0,4]{minecraft:obsidian}
            [-1,1,4]{minecraft:obsidian}
            [-1,2,4]{minecraft:obsidian}
            [1,3,3]{minecraft:obsidian}
            [0,3,3]{minecraft:obsidian}
            [-1,3,3]{minecraft:obsidian}
            [1,3,3]{minecraft:obsidian}
            [0,3,3]{minecraft:obsidian}
            [-1,3,3]{minecraft:obsidian}
            [1,3,1]{minecraft:obsidian}
            [0,3,1]{minecraft:obsidian}
            [-1,3,1]{minecraft:obsidian}
            [1,3,2]{minecraft:obsidian}
            [0,3,2]{minecraft:obsidian}
            [-1,3,2]{minecraft:obsidian}""";

    public Direction lockedDirection = null;
    public BlockPos lockedStartPos = null;
    public Direction prevlockedDirection = null;
    public BlockPos prevlockedStartPos = null;
    public File painterFile = null;
    //The canvas is the positional representation of the structure to be built
    LinkedHashMap<BlockPos, Block> CANVAS_MAP = new LinkedHashMap<>();

    boolean isLocked = false;
    SettingGroup sgGeneral = new SettingGroup("General");
    SettingGroup sgPlace = new SettingGroup("Place");
    SettingGroup sgRender = new SettingGroup("Render");
    CycleSetting structure = sgGeneral.add(new CycleSetting.Builder()
            .name("Structure")
            .description("The structure to build. If its custom then you need to import from a txt or a readable file using the correct format")
            .onSettingChange(this)
            .value(List.of(Structure.values()))
            .defaultListOption(Structure.Wither)
            .build()
    );
    ButtonSetting selectFile = sgGeneral.add(new ButtonSetting.Builder()
            .name("File selected: None")
            .defaultValue(false)
            .shouldRender(() -> structure.getOption() == Structure.Custom)
            .build()
    );
    Vector3dSetting offsetFromPlayer = sgGeneral.add(new Vector3dSetting.Builder()
            .name("Offset from player")
            .description("The value of x,y and z determines how far away the canvas should be from the player on each axis")
            .defaultValue(2, 0, 2)
            .onSettingChange(this)
            .roundingPlace(0)
            .min(-100)
            .max(100)
            .build()
    );
    BooleanSetting autoRotate = sgGeneral.add(new BooleanSetting.Builder()
            .name("AutoRotate Structure")
            .description("Automatically rotates the structure based on your look direction.")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    DropDownSetting rotationDirection = sgGeneral.add(new DropDownSetting.Builder()
            .name("Rotation Direction")
            .description("The direction in which the structure should be")
            .onSettingChange(this)
            .value(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST))
            .defaultListOption(Direction.NORTH)
            .shouldRender(() -> !autoRotate.value)
            .build()
    );
    ButtonSetting canvasState = sgGeneral.add(new ButtonSetting.Builder()
            .name("Canvas state")
            .description("Locks canvas to the current set direction and position or unlocks the canvas to return to normal")
            .build()
    );
    KeyBind lockCanvasKey = sgGeneral.add(new KeyBind.Builder()
            .name("Lock Canvas Key")
            .description("Keybind to lock canvas")
            .value(KeyBind.none())
            .defaultValue(KeyBind.none())
            .onSettingChange(this)
            .build()
    );
    BooleanSetting autoPlace = sgPlace.add(new BooleanSetting.Builder()
            .name("AutoPlace")
            .description("Automatically places blocks")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    BooleanSetting forceBreak = sgPlace.add(new BooleanSetting.Builder()
            .name("Force Break")
            .description("Automatically breaks blocks where air is supposed to be present (only for air). Force Break will break the block even if we don't have the correct tools")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    DoubleSetting ticksEachBlock = sgPlace.add(new DoubleSetting.Builder()
            .name("Ticks / block")
            .description("The ticks per block to be placed, essentially a delay to place each block")
            .onSettingChange(this)
            .defaultValue(5d)
            .range(0, 120)
            .roundingPlace(0)
            .shouldRender(() -> autoPlace.value)
            .build()
    );
    BooleanSetting airPlace = sgPlace.add(new BooleanSetting.Builder()
            .name("AirPlace")
            .description("Places the blocks in air")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting playerRotate = sgPlace.add(new BooleanSetting.Builder()
            .name("Player Rotate")
            .description("Rotates the player to look at the block pos when placing. Stops auto-rotate while the player is rotating")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting outline = sgRender.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline of blocks")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting fill = sgRender.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill of blocks")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting useBlockMapColor = sgRender.add(new BooleanSetting.Builder()
            .name("Use block map color")
            .description("Uses color of the block as per the block's default map color (i.e. the color of the block represented in a map). Alpha is of color setting")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    RGBASetting color = sgRender.add(new RGBASetting.Builder()
            .name("Color")
            .value(ColorUtils.changeAlpha(Color.WHITE, 125))
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 125))
            .onSettingChange(this)
            .shouldRender(() -> !useBlockMapColor.value)
            .build()
    );

    private int ticksPassed = 0;

    public Painter() {
        super("Painter", "Place defined structures in your world via simply a file!", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgPlace);
        addSettingGroup(sgRender);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgPlace.getSettings());
        addQuickSettings(sgRender.getSettings());


        selectFile.addButton("Select a text file", 0, 0, () -> {
            FileUtils.openTinyFileDialog("canvas.txt", (file -> painterFile = file), false);

            if (painterFile != null) {
                selectFile.setButtonCategoryText("File Selected: " + painterFile.getName());
            }

            CANVAS_MAP = PainterFileParser.parseFile(painterFile);


            if (CANVAS_MAP == null) {
                ChatUtils.sendHeliosMsg(ColorUtils.red + "File selected is empty or is not matching the proper syntax. Please review it.");
            }
        });
        selectFile.addButton("Re-parse file", 1, 0, () -> {

            if (painterFile != null) {
                CANVAS_MAP = PainterFileParser.parseFile(painterFile);
            }

            if (painterFile == null) {
                ChatUtils.sendHeliosMsg(ColorUtils.red + "File selected is not selected.");
            }
        });

        canvasState.addButton("Lock",0,0, this::lock);
        canvasState.addButton("Unlock",1,0, this::unlock);

    }

    @SubscribeEvent
    public void keyPressedEvent(KeyPressedEvent e) {
        if (e.getKey() == lockCanvasKey.value) {
            if (isLocked) {
                unlock();
            } else {
                lock();
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ticksPassed = 0;

        loadCanvas();
    }

    private void loadCanvas(){
        if (structure.getOption() == Structure.Custom && painterFile == null) {
            ChatUtils.sendHeliosMsg("Paint file has not been selected! Toggling off...");
            toggle();
        } else if(painterFile == null){
            CANVAS_MAP = PainterFileParser.parseString(((Structure)structure.getOption()).structureString);
        }else {
            CANVAS_MAP = PainterFileParser.parseFile(painterFile);
        }
    }


    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if(!HeliosClient.shouldUpdate()) return;
        if (CANVAS_MAP == null || !autoPlace.value) return;

        ticksPassed++;

        for(Map.Entry<BlockPos, Block> entry: CANVAS_MAP.entrySet()) {
                BlockPos pos = entry.getKey();
                Block block = entry.getValue();
                Vec3i offSet = BlockPos.ofFloored(offsetFromPlayer.getVecX(), offsetFromPlayer.getVecY(), offsetFromPlayer.getVecZ());
                BlockPos changedPos = getPlayerPos().add(rotateBlockPosition(pos, getBlockRotation())).add(offSet);
                BlockState state = mc.world.getBlockState(changedPos);
                if (ticksPassed > ticksEachBlock.value && !state.getBlock().equals(block) && state.isReplaceable()) {
                    ticksPassed = 0;


                    //Experimental, if a block is supposed to be an air, but it is not, then try to break it
                    if ((block == Blocks.AIR || block == Blocks.CAVE_AIR) && !mc.world.isAir(pos) && BlockUtils.canBreak(pos, state)) {
                        int fastestTool = InventoryUtils.getFastestTool(state, false);
                        //Slot may be -1
                        if (fastestTool != -1 || forceBreak.value) {
                            InventoryUtils.swapToSlot(fastestTool, false);
                            BlockUtils.breakBlock(pos, true);
                        }
                    }

                    Item item = Item.BLOCK_ITEMS.get(block);

                    if(item == null)continue;

                    int slot = InventoryUtils.findItemInHotbar(item);

                    if (slot == -1) {
                        ChatUtils.sendHeliosMsg(item.getName().getString() + " was NOT found in hot bar");
                    } else {
                        boolean swapped = InventoryUtils.swapToSlot(slot, false);

                        if (swapped) {
                            BlockUtils.place(changedPos, playerRotate.value,airPlace.value, slot == InventoryUtils.OFFHAND ? Hand.OFF_HAND : Hand.MAIN_HAND);
                        }
                    }
                }
        }
    }

    private void lock() {
        prevlockedDirection = lockedDirection;
        prevlockedStartPos = lockedStartPos;
        lockedDirection = getBlockRotation();
        lockedStartPos = mc.player.getBlockPos();
        isLocked = true;
    }

    private void unlock() {
        isLocked = false;
        prevlockedDirection = lockedDirection;
        prevlockedStartPos = lockedStartPos;
        lockedDirection = null;
        lockedStartPos = null;
    }

    @SubscribeEvent
    public void render(Render3DEvent event) {
        if (CANVAS_MAP == null) return;

        CANVAS_MAP.forEach((pos, block) -> {
            if (getBlockRotation() != null) {
                Vec3i offSet = BlockPos.ofFloored(offsetFromPlayer.getVecX(), offsetFromPlayer.getVecY(), offsetFromPlayer.getVecZ());
                BlockPos changedPos = getAppliedRotation(pos).add(offSet);

                renderBlock(changedPos, block);
            }
        });
    }

    public BlockPos getAppliedRotation(BlockPos pos){
        return getPlayerPos().add(rotateBlockPosition(pos, getBlockRotation()));
    }

    public BlockPos getPlayerPos() {
        return isLocked ? lockedStartPos : mc.player.getBlockPos();
    }

    public void renderBlock(BlockPos pos, Block block) {
        int colorInt = useBlockMapColor.value ? ColorUtils.changeAlpha(block.getDefaultMapColor().color, this.color.getColor().getAlpha()).getRGB() : this.color.value.getRGB();
        QuadColor color = QuadColor.single(colorInt);
        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(pos, color, 1.2f);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(pos, color, 1.2f);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(pos, color);
        }
    }

    public BlockPos rotateBlockPosition(BlockPos originalPos, Direction rotation) {
        // Adjust the position based on the player's orientation
        return switch (rotation) {
            case NORTH -> originalPos; // No change for north-facing
            case SOUTH -> new BlockPos(-originalPos.getX(), originalPos.getY(), -originalPos.getZ());
            case EAST -> new BlockPos(originalPos.getZ(), originalPos.getY(), -originalPos.getX());
            case WEST -> new BlockPos(-originalPos.getZ(), originalPos.getY(), originalPos.getX());
            default -> originalPos; // Default to no rotation
        };
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if (setting == structure) {
            loadCanvas();
        }
    }

    public Direction getBlockRotation() {
        if(isLocked){
            return lockedDirection;
        } else if(autoRotate.value){
            return mc.player.getMovementDirection().getOpposite();
        }else {
            return (Direction) rotationDirection.getOption();
        }
    }

    public enum Structure {
        Nomad_Hut_FitMC(NOMAD_HUT),
        Wither(WITHER),
        Wall4x4(FOUR_BY_FOUR_WALL),
        Custom("");

        public final String structureString;

        Structure(String structureString){
            this.structureString = structureString;
        }
    }
}
