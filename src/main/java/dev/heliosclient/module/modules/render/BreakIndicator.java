package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.mixin.AccessorClientPlayerInteractionManager;
import dev.heliosclient.mixin.AccessorWorldRenderer;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static dev.heliosclient.module.modules.render.BreakIndicator.IndicateType.*;

public class BreakIndicator extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");

    CycleSetting type = sgGeneral.add(new CycleSetting.Builder()
            .name("Indicator Type")
            .description("Type of break indication")
            .onSettingChange(this)
            .value(List.of(IndicateType.values()))
            .defaultListOption(Highlight)
            .build()
    );
    BooleanSetting gradientBool = sgGeneral.add(new BooleanSetting.Builder()
            .name("Use a gradient")
            .description("Whether to use gradient or not")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .shouldRender(() -> type.getOption() == Highlight)
            .build()
    );
    GradientSetting gradient = sgGeneral.add(new GradientSetting.Builder()
            .name("Gradient Value")
            .description("The gradient to use")
            .onSettingChange(this)
            .defaultValue("Rainbow")
            .shouldRender(() -> type.getOption() == Highlight && gradientBool.value)
            .build()
    );
    DoubleSetting alpha = sgGeneral.add(new DoubleSetting.Builder()
            .name("Gradient Alpha/Opacity")
            .description("Desired alpha (opacity) value of the gradients")
            .onSettingChange(this)
            .value(150)
            .defaultValue(150)
            .min(0)
            .max(255)
            .shouldRender(() -> type.getOption() == Highlight && gradientBool.value)
            .roundingPlace(0)
            .build()
    );
    RGBASetting highlightColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Highlight color")
            .description("Color of the highlight")
            .defaultValue(Color.WHITE)
            .value(Color.WHITE)
            .onSettingChange(this)
            .rainbow(true)
            .shouldRender(() -> type.getOption() == Highlight && !gradientBool.value)
            .build()
    );


    public BreakIndicator() {
        super("Break Indicator", "Indicates block breaking, aka MineESP", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }


    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (mc.interactionManager == null) return;
        Renderer3D.renderThroughWalls();
        BlockPos currentBreakingPos = ((AccessorClientPlayerInteractionManager) mc.interactionManager).getCurrentBreakingBlockPos();

        Map<Integer, BlockBreakingInfo> breakingInfos = ((AccessorWorldRenderer) mc.worldRenderer).getBlockBreakingInfos();

        float selfBreakingProgress = mc.interactionManager.getBlockBreakingProgress();

        if (selfBreakingProgress > 0 && currentBreakingPos != null) {
            
            BlockState state = mc.world.getBlockState(currentBreakingPos);
            VoxelShape shape = state.getOutlineShape(mc.world, currentBreakingPos);
            if (shape == null || shape.isEmpty()) return;
            int start = gradientBool.value ? ColorUtils.changeAlpha(gradient.get().getStartGradient().getRGB(),alpha.getInt()).getRGB() : highlightColor.value.getRGB();
            int end = gradientBool.value ? ColorUtils.changeAlpha(gradient.get().getEndGradient().getRGB(),alpha.getInt()).getRGB() : highlightColor.value.getRGB();

            renderIndicator(shape.getBoundingBox().expand(0.001f).offset(currentBreakingPos), selfBreakingProgress/10.0f, (IndicateType) type.getOption(),start,end);
        }

        breakingInfos.forEach((integer, info) -> {
            BlockPos pos = info.getPos();
            if (pos.equals(currentBreakingPos)) return;

            int breakProgress = info.getStage();

            BlockState state = mc.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(mc.world, pos);

            if (shape == null || shape.isEmpty()) return;
            int start = gradientBool.value ? ColorUtils.changeAlpha(gradient.get().getStartGradient().getRGB(),alpha.getInt()).getRGB() : highlightColor.value.getRGB();
            int end = gradientBool.value ? ColorUtils.changeAlpha(gradient.get().getEndGradient().getRGB(),alpha.getInt()).getRGB() : highlightColor.value.getRGB();

            renderIndicator(shape.getBoundingBox().expand(0.001f).offset(pos), (float) (breakProgress + 1) / 10, (IndicateType) type.getOption(),start,end);
        });
        Renderer3D.stopRenderingThroughWalls();
    }

    public void renderIndicator(Box box, float breakingProg,IndicateType type, int start, int end) {
        if (type == Highlight) {
            QuadColor color = QuadColor.gradient(start,end, QuadColor.CardinalDirection.DIAGONAL_LEFT);

            Renderer3D.drawBoxFill(box, color);
        } else if (type == Stretch) {
            Box stretchedBox = shrinkBoxExtreme(box).stretch(0,0,breakingProg);
            Renderer3D.drawBoxBoth(stretchedBox, QuadColor.single(getColor(breakingProg)), 1f);
        } else if (type == Contract) {
            Box shrunkBox = box.expand((breakingProg / 2) - 1.0);
            Renderer3D.drawBoxBoth(shrunkBox, QuadColor.single(getColor(breakingProg)), 1f);
        }
    }

    public Box shrinkBoxExtreme(Box box){
        //Shrinks box by changing its maxZ to minZ (i.e. its Z will stay at 0)
        return new Box(box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.minZ);
    }

    public int getColor(float breakingPos) {
        return breakingPos > 0.5 ? (breakingPos > 0.8 ? get(Color.GREEN) : get(Color.YELLOW)) : breakingPos > 0.3 ? get(Color.ORANGE) : get(Color.RED);
    }

    public int get(Color c) {
        return ColorUtils.changeAlpha(c, 100).getRGB();
    }

    public enum IndicateType {
        Highlight,
        Contract,
        Stretch
    }
}
