package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.blocks.HoleUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.List;

import static dev.heliosclient.util.blocks.HoleUtils.HoleType.*;

public class HoleESP extends Module_ {
    static Color TRANSPARENT_BLUE = new Color(0, 30, 175, 179);
    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting throughWalls = sgGeneral.add(new BooleanSetting.Builder()
            .name("ThroughWalls")
            .description("Draw the holes through walls")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting holeRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("Hole Range")
            .description("Maximum distance of the hole to the player")
            .min(3)
            .max(100)
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
            .value(ColorUtils.changeAlpha(ColorUtils.hexToColor("#1dfb00"), 123))
            .defaultValue(ColorUtils.changeAlpha(ColorUtils.hexToColor("#1dfb00"), 123))
            .shouldRender(() -> setColorCycle.getOption() == SAFE)
            .build()
    );
    RGBASetting safeColorEnd = sgColor.add(new RGBASetting.Builder()
            .name("Safe Gradient End")
            .description("End of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.changeAlpha(ColorUtils.hexToColor("#4bfb83"), 3))
            .defaultValue(ColorUtils.changeAlpha(ColorUtils.hexToColor("#4bfb83"), 3))
            .shouldRender(() -> setColorCycle.getOption() == SAFE)
            .build()
    );
    RGBASetting unsafeColorStart = sgColor.add(new RGBASetting.Builder()
            .name("Unsafe Gradient Start")
            .description("Start of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.changeAlpha(ColorUtils.hexToColor("#ff6d00"), 142))
            .defaultValue(ColorUtils.changeAlpha(ColorUtils.hexToColor("#ff6d00"), 142))
            .shouldRender(() -> setColorCycle.getOption() == UNSAFE)
            .build()
    );
    RGBASetting unsafeColorEnd = sgColor.add(new RGBASetting.Builder()
            .name("Unsafe Gradient End")
            .description("End of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.changeAlpha(ColorUtils.hexToColor("#fb9804"), 3))
            .defaultValue(ColorUtils.changeAlpha(ColorUtils.hexToColor("#fb9804"), 3))
            .shouldRender(() -> setColorCycle.getOption() == UNSAFE)
            .build()
    );
    RGBASetting dangerColorStart = sgColor.add(new RGBASetting.Builder()
            .name("Danger Gradient Start")
            .description("Start of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.changeAlpha(ColorUtils.hexToColor("#ff0000"), 145))
            .defaultValue(ColorUtils.changeAlpha(ColorUtils.hexToColor("#ff0000"), 145))
            .shouldRender(() -> setColorCycle.getOption() == DANGER)
            .build()
    );
    RGBASetting dangerColorEnd = sgColor.add(new RGBASetting.Builder()
            .name("Danger Gradient End")
            .description("End of the gradient.")
            .onSettingChange(this)
            .rainbow(false)
            .value(ColorUtils.changeAlpha(ColorUtils.hexToColor("#fb794b"), 7))
            .defaultValue(ColorUtils.changeAlpha(ColorUtils.hexToColor("#fb794b"), 7))
            .shouldRender(() -> setColorCycle.getOption() == DANGER)
            .build()
    );

    public HoleESP() {
        super("HoleESP", "Displays holes in your area", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgColor);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgColor.getSettings());

    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (throughWalls.value)
            Renderer3D.renderThroughWalls();
        QuadColor.CardinalDirection direction = gradientDirection.getOption().equals("Down") ? QuadColor.CardinalDirection.SOUTH : QuadColor.CardinalDirection.NORTH;

        for (HoleUtils.HoleInfo info : HoleUtils.getHoles((int) holeRange.value, (int) holeRangeVertical.value)) {
            if (!renderSelf.value && mc.player.getBlockPos().isWithinDistance(info.holePosition, 1d)) {
                continue;
            }

            //Contract and offset so that it does not fight for Z level with block textures.
            Box box = new Box(info.holePosition);
            double ogMaxY = box.maxY;
            box = box.contract(0.005f, 0f, 0.005f).offset(0, 0.005f, 0).withMaxY(ogMaxY + height.value);

            if (info.holeType == SAFE) {
                renderHole(QuadColor.gradient(safeColorStart.value.getRGB(), safeColorEnd.value.getRGB(), direction), QuadColor.single(safeColorStart.value.getRGB()), box);
            }
            if (info.holeType == UNSAFE) {
                renderHole(QuadColor.gradient(unsafeColorStart.value.getRGB(), unsafeColorEnd.value.getRGB(), direction), QuadColor.single(unsafeColorStart.value.getRGB()), box);
            }
            if (info.holeType == DANGER) {
                renderHole(QuadColor.gradient(dangerColorStart.value.getRGB(), dangerColorEnd.value.getRGB(), direction), QuadColor.single(dangerColorStart.value.getRGB()), box);
            }
            if (info.holeType == SIZED) {
                //Renderer3D.drawBoxFill(box, QuadColor.single(TRANSPARENT_BLUE.getRGB()), Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST);
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
