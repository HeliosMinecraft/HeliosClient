package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.blocks.HoleUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.util.blocks.HoleUtils.HoleType.*;

public class HoleESP extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting throughWalls = sgGeneral.add(new BooleanSetting.Builder()
            .name("ThroughWalls")
            .description("Draw the holes through walls")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting quads = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show Quad holes")
            .description("Shows quad holes (4 block holes). May drain fps.")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting holeRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("Hole Range")
            .description("Maximum distance of the hole to the player")
            .min(3)
            .max(40)
            .value(20d)
            .defaultValue(20d)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting holeRangeVertical = sgGeneral.add(new DoubleSetting.Builder()
            .name("Vertical Hole Range")
            .description("Maximum distance of the hole to the player vertically")
            .min(3)
            .max(20)
            .value(4d)
            .defaultValue(4d)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting height = sgGeneral.add(new DoubleSetting.Builder()
            .name("Hole height")
            .description("Increase/Decrease the Height of the hole")
            .min(-2f)
            .max(1f)
            .value(0d)
            .defaultValue(0d)
            .roundingPlace(3)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting renderSelf = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render self")
            .description("Renders the hole in which you are")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting outline = sgGeneral.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline of holes")
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
            .build()
    );
    BooleanSetting fill = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill of holes")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting renderTop = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render Top")
            .description("Draw top side fill of holes")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting renderBottom = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render Bottom")
            .description("Draw bottom side fill of holes")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    SettingGroup sgColor = new SettingGroup("Color");
    CycleSetting gradientDirection = sgColor.add(new CycleSetting.Builder()
            .name("Gradient Direction")
            .description("Direction of gradient, up/down")
            .value(List.of("Up", "Down"))
            .defaultListOption("Up")
            .onSettingChange(this)
            .build()
    );
    CycleSetting setColorCycle = sgColor.add(new CycleSetting.Builder()
            .name("Set Color")
            .description("Set the color of different holes")
            .value(List.of(HoleUtils.HoleType.values()))
            .defaultListOption(SAFE)
            .onSettingChange(this)
            .build()
    );
    RGBASetting safeColorStart = sgColor.add(new RGBASetting.Builder()
            .name("Safe Gradient Start")
            .description("Start of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.hexToColor("#1dfb00", 123))
            .defaultValue(ColorUtils.hexToColor("#1dfb00", 123))
            .shouldRender(() -> setColorCycle.getOption() == SAFE)
            .build()
    );
    RGBASetting safeColorEnd = sgColor.add(new RGBASetting.Builder()
            .name("Safe Gradient End")
            .description("End of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.hexToColor("#4bfb83", 3))
            .defaultValue(ColorUtils.hexToColor("#4bfb83", 3))
            .shouldRender(() -> setColorCycle.getOption() == SAFE)
            .build()
    );
    RGBASetting unsafeColorStart = sgColor.add(new RGBASetting.Builder()
            .name("Unsafe Gradient Start")
            .description("Start of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.hexToColor("#ff6d00", 142))
            .defaultValue(ColorUtils.hexToColor("#ff6d00", 142))
            .shouldRender(() -> setColorCycle.getOption() == UNSAFE)
            .build()
    );
    RGBASetting unsafeColorEnd = sgColor.add(new RGBASetting.Builder()
            .name("Unsafe Gradient End")
            .description("End of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.hexToColor("#fb9804", 3))
            .defaultValue(ColorUtils.hexToColor("#fb9804", 3))
            .shouldRender(() -> setColorCycle.getOption() == UNSAFE)
            .build()
    );
    RGBASetting dangerColorStart = sgColor.add(new RGBASetting.Builder()
            .name("Danger Gradient Start")
            .description("Start of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.hexToColor("#ff0000", 145))
            .defaultValue(ColorUtils.hexToColor("#ff0000", 145))
            .shouldRender(() -> setColorCycle.getOption() == DANGER)
            .build()
    );
    RGBASetting dangerColorEnd = sgColor.add(new RGBASetting.Builder()
            .name("Danger Gradient End")
            .description("End of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.hexToColor("#f80000", 7))
            .defaultValue(ColorUtils.hexToColor("#f80000", 7))
            .shouldRender(() -> setColorCycle.getOption() == DANGER)
            .build()
    );

    List<HoleUtils.HoleInfo> holes = new ArrayList<>();

    public HoleESP() {
        super("HoleESP", "Displays holes in your area", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgColor);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgColor.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.WORLD event) {
        if(mc.player == null) return;

        holes = HoleUtils.getHoles((int) holeRange.value, (int) holeRangeVertical.value,quads.value).stream().toList();
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (throughWalls.value)
            Renderer3D.renderThroughWalls();

        QuadColor.CardinalDirection direction = gradientDirection.getOption().equals("Down") ? QuadColor.CardinalDirection.SOUTH : QuadColor.CardinalDirection.NORTH;

        for (HoleUtils.HoleInfo info : holes) {
            if (!renderSelf.value && info.holeBox.intersects(mc.player.getBoundingBox().contract(0.2f))) {
                continue;
            }

            // Use the box from HoleInfo
            Box box = info.holeBox.withMaxY(info.holeBox.maxY + height.value);

            if (info.holeType == SAFE) {
                renderHole(QuadColor.gradient(safeColorStart.value.getRGB(), safeColorEnd.value.getRGB(), direction), QuadColor.single(safeColorStart.value.getRGB()), box);
            }
            if (info.holeType == UNSAFE) {
                renderHole(QuadColor.gradient(unsafeColorStart.value.getRGB(), unsafeColorEnd.value.getRGB(), direction), QuadColor.single(unsafeColorStart.value.getRGB()), box);
            }
            if (info.holeType == DANGER) {
                renderHole(QuadColor.gradient(dangerColorStart.value.getRGB(), dangerColorEnd.value.getRGB(), direction), QuadColor.single(dangerColorStart.value.getRGB()), box);
            }
        }
        if (throughWalls.value)
            Renderer3D.stopRenderingThroughWalls();
    }


    private void renderHole(QuadColor fillColor, QuadColor lineColor, Box box) {
        Direction[] excludeDirs = new Direction[2];
        if (!renderTop.value) {
            excludeDirs[0] = Direction.UP;
        }
        if (!renderBottom.value) {
            excludeDirs[1] = Direction.DOWN;
        }
        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(box, fillColor, lineColor, (float) outlineWidth.value, excludeDirs);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(box, lineColor, (float) outlineWidth.value, excludeDirs);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(box, fillColor, excludeDirs);
        }
    }
}
