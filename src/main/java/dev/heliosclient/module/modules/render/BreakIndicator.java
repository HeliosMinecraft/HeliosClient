package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.mixin.AccessorClientPlayerInteractionManager;
import dev.heliosclient.mixin.AccessorWorldRenderer;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ColorUtils;
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
    RGBASetting highlightColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Highlight color")
            .description("Color of the highlight")
            .defaultValue(Color.WHITE)
            .value(Color.WHITE)
            .onSettingChange(this)
            .rainbow(true)
            .shouldRender(() -> type.getOption() == Highlight)
            .build()
    );


    public BreakIndicator() {
        super("Break Indicator", "Indicates block breaking", Categories.RENDER);
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

            renderIndicator(shape.getBoundingBox().expand(0.005f).offset(currentBreakingPos), selfBreakingProgress);
        }

        breakingInfos.forEach((integer, info) -> {
            BlockPos pos = info.getPos();
            if (pos.equals(currentBreakingPos)) return;

            int breakProgress = info.getStage();

            BlockState state = mc.world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(mc.world, pos);

            if (shape == null || shape.isEmpty()) return;

            renderIndicator(shape.getBoundingBox().expand(0.005f).offset(pos), breakProgress + 1);
        });
        Renderer3D.stopRenderingThroughWalls();
    }

    public void renderIndicator(Box box, float breakingProg) {
        if (IndicateType.values()[type.value] == Highlight) {
            Renderer3D.drawBoxFill(box, QuadColor.single(highlightColor.value.getRGB()));

        } else if (IndicateType.values()[type.value] == Stretch) {
            Box stretchedBox = shrinkBoxExtreme(box).stretch(0,0,breakingProg/10.0);
            Renderer3D.drawBoxBoth(stretchedBox, QuadColor.single(getColor((int)breakingProg)), 1f);

        } else if (IndicateType.values()[type.value] == Contract) {
            Box shrunkBox = box.expand(breakingProg / 20.0 - 1.0);
            Renderer3D.drawBoxBoth(shrunkBox, QuadColor.single(getColor((int)breakingProg)), 1f);
        }
    }

    public Box shrinkBoxExtreme(Box box){
        //Shrinks box by changing its maxZ to minZ (i.e. its Z will stay at 0)
        return new Box(box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.minZ);
    }

    public int getColor(int breakingPos) {
        return breakingPos > 5 ? (breakingPos > 8 ? get(Color.GREEN) : get(Color.YELLOW)) : breakingPos > 3 ? get(Color.ORANGE) : get(Color.RED);
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
