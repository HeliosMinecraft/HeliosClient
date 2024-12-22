package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.util.List;

public class BlockSelection extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting advanced = sgGeneral.add(new BooleanSetting.Builder()
            .name("Advanced")
            .description("Shows a advanced box rendering on type of block")
            .value(false)
            .defaultValue(false)
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
            .shouldRender(() -> outline.value)
            .build()
    );
    BooleanSetting renderOneSide = sgGeneral.add(new BooleanSetting.Builder()
            .name("Flat Render")
            .description("Renders only the side you are looking at.")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );

    BooleanSetting gradient = sgGeneral.add(new BooleanSetting.Builder()
            .name("Gradient")
            .description("Uses gradients instead of static single fill colors")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    CycleSetting gradientDirection = sgGeneral.add(new CycleSetting.Builder()
            .name("Gradient Direction")
            .description("Direction of gradient")
            .value(List.of(QuadColor.CardinalDirection.values()))
            .defaultListOption(QuadColor.CardinalDirection.DIAGONAL_LEFT)
            .onSettingChange(this)
            .shouldRender(()-> gradient.value)
            .build()
    );
    GradientSetting fillGradient = sgGeneral.add(new GradientSetting.Builder()
            .name("Fill Gradient")
            .description("Gradient of the fill")
            .defaultValue("Rainbow")
            .onSettingChange(this)
            .shouldRender(()-> gradient.value)
            .build()
    );
    GradientSetting lineGradient = sgGeneral.add(new GradientSetting.Builder()
            .name("Line Gradient")
            .description("Gradient of the outline")
            .defaultValue("Rainbow")
            .onSettingChange(this)
            .shouldRender(()-> gradient.value)
            .build()
    );
    RGBASetting fillColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Fill color")
            .description("Color of the Fill")
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 125))
            .onSettingChange(this)
            .shouldRender(()-> !gradient.value)
            .rainbow(false)
            .build()
    );

    RGBASetting lineColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Line color")
            .description("Color of the outline")
            .value(Color.WHITE)
            .defaultValue(Color.WHITE)
            .onSettingChange(this)
            .shouldRender(()-> !gradient.value)
            .rainbow(false)
            .build()
    );

    public BlockSelection() {
        super("Block Selection", "Modifies how block selection is rendered", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (!(mc.crosshairTarget instanceof BlockHitResult result) || result.getType() == HitResult.Type.MISS) return;

        renderBlockHitResult(result,false);
    }

    public void renderBlockHitResult(BlockHitResult result, boolean doEmptyRender) {
        BlockState state =  mc.world.getBlockState(result.getBlockPos());
        VoxelShape shape = state.getOutlineShape(mc.world, result.getBlockPos());

        if(shape.isEmpty() && doEmptyRender){
            renderSelection(new Box(result.getBlockPos()).expand(0.005f));
            return;
        }

        if (shape.isEmpty()) return;

        Direction[] dirs = new Direction[6];

        if(renderOneSide.value){
            dirs =  ArrayUtils.removeElement(Direction.values(),result.getSide());
        }

        if (advanced.value) {
            for (Box b : shape.getBoundingBoxes()) {
                renderSelection(b.offset(result.getBlockPos()).expand(0.0045f),dirs);
            }
        } else {
            renderSelection(shape.getBoundingBox().offset(result.getBlockPos()).expand(0.0045f),dirs);
        }
    }

    public void renderSelection(Box box,Direction... exclude) {
        QuadColor.CardinalDirection gradientDirection = (QuadColor.CardinalDirection) this.gradientDirection.getOption();
        QuadColor fillColor = gradient.value ? QuadColor.gradient(fillGradient.getStartColor().getRGB(), fillGradient.getEndColor().getRGB(),gradientDirection)  : QuadColor.single(this.fillColor.value.getRGB());
        QuadColor lineColor = gradient.value ? QuadColor.gradient(lineGradient.getStartColor().getRGB(), lineGradient.getEndColor().getRGB(), gradientDirection)  : QuadColor.single(this.lineColor.value.getRGB());

        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(box, fillColor, lineColor, (float) outlineWidth.value,exclude);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(box, lineColor, (float) outlineWidth.value,exclude);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(box, fillColor,exclude);
        }
    }
}