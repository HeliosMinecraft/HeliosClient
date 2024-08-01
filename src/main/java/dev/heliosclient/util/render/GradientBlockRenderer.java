package dev.heliosclient.util.render;

import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class GradientBlockRenderer {
    static final List<GradientBlock> gradientBlocks = new CopyOnWriteArrayList<>();

    //Map to hold the Box of each gradient block
    static final Map<GradientBlock,Box> gradientBlockBoxMap = new ConcurrentHashMap<>();

    public static boolean THROUGH_WALLS = false;
    public static boolean OUTLINE = false;
    public static boolean FILL = true;
    public static int MODIFY_ALPHA_VALUE = 125;
    public static float LINE_WIDTH = 1.1f;



    public static void renderGradientBlock(Supplier<Color> gradientStart, Supplier<Color> gradientEnd, BlockPos pos, boolean modifyAlpha, long ageTimeMS, QuadColor.CardinalDirection dir, Direction... exclude){
        gradientBlocks.removeIf(gradientBlock -> gradientBlock.pos == pos);

        GradientBlock gb = new GradientBlock(
                gradientStart,
                gradientEnd,
                pos,
                modifyAlpha,
                System.currentTimeMillis(),
                ageTimeMS,
                dir,
                exclude
        );

        gradientBlockBoxMap.put(gb, new Box(pos).expand(0.005f));
        gradientBlocks.add(gb);
    }

    private static void clearGradientBlocks(){
        gradientBlocks.clear();
    }

    /**
     * Called in {@link dev.heliosclient.mixin.GameRendererMixin#render(float, long, MatrixStack, CallbackInfo)}  }
     */
    @ApiStatus.Internal
    public static void renderGradientBlocks() {
        gradientBlocks.removeIf(GradientBlock::isDead);
        gradientBlockBoxMap.keySet().removeIf(GradientBlock::isDead);

        if(THROUGH_WALLS)
           Renderer3D.renderThroughWalls();

        for (GradientBlock gb : gradientBlocks) {
            if (gb == null || gradientBlockBoxMap.get(gb) == null) {
                continue;
            }
            int startG = gb.gradientStart.get().getRGB();
            int endG = gb.gradientEnd.get().getRGB();

            if(gb.modifyAlpha){
                startG = ColorUtils.argbToRgb(startG,MODIFY_ALPHA_VALUE);
                endG = ColorUtils.argbToRgb(endG,MODIFY_ALPHA_VALUE);
            }

            QuadColor color = null;
            if(gb.dir == null){
                color = QuadColor.gradient(startG,endG);
            }else{
               color = QuadColor.gradient(startG,endG, gb.dir);
            }

            if(OUTLINE && FILL){
                Renderer3D.drawBoxBoth(gradientBlockBoxMap.get(gb), color,LINE_WIDTH,gb.exclude());
                continue;
            }

            if(OUTLINE){
                Renderer3D.drawBoxOutline(gradientBlockBoxMap.get(gb), color,LINE_WIDTH,gb.exclude());
            }
            if(FILL){
                Renderer3D.drawBoxFill(gradientBlockBoxMap.get(gb), color,gb.exclude());
            }
        }

        if(THROUGH_WALLS)
            Renderer3D.stopRenderingThroughWalls();
    }

    record GradientBlock(Supplier<Color> gradientStart, Supplier<Color> gradientEnd, BlockPos pos, boolean modifyAlpha, long created, long ageTimeMS, QuadColor.CardinalDirection dir, Direction... exclude) {
        long getAgeLeft() {
            return Math.max(0, created - System.currentTimeMillis() + ageTimeMS);
        }

        boolean isDead() {
            return getAgeLeft() == 0;
        }
    }
}
