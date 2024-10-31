package dev.heliosclient.util.render;

import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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

    private static boolean THROUGH_WALLS = true;
    private static boolean OUTLINE = false;
    private static boolean FILL = true;
    private static int MODIFY_ALPHA_VALUE = 125;
    private static float LINE_WIDTH = 1.1f;

    /**
     * This method MUST be called if you modify any of the variables above.
     */
    public static void reset(){
        THROUGH_WALLS = true;
        OUTLINE = false;
        FILL = true;
        MODIFY_ALPHA_VALUE = 125;
        LINE_WIDTH = 1.1f;
    }

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

        gradientBlockBoxMap.put(gb, new Box(pos).expand(0.001f));
        gradientBlocks.add(gb);
    }

    public static void clearGradientBlocks(){
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
            if (gb == null || gradientBlockBoxMap.get(gb) == null || !FrustumUtils.isBoxVisible(gradientBlockBoxMap.get(gb))) {
                continue;
            }
            int startG = gb.gradientStart.get().getRGB();
            int endG = gb.gradientEnd.get().getRGB();

            if(gb.modifyAlpha){
                startG = ColorUtils.changeAlpha(startG,MODIFY_ALPHA_VALUE,(float) MODIFY_ALPHA_VALUE).getRGB();
                endG = ColorUtils.changeAlpha(endG,MODIFY_ALPHA_VALUE,(float) MODIFY_ALPHA_VALUE).getRGB();
            }

            long ageLeft = gb.getAgeLeft();
            long totalAge = gb.ageTimeMS;
            Box modfiedBox = gradientBlockBoxMap.get(gb);

            if (ageLeft <= totalAge * 0.35) {
                // Contract the box size based on the age
                modfiedBox = modfiedBox.contract(MathHelper.clamp(1 - ageLeft / (totalAge * 0.35),0,0.5f));

            }


            if(OUTLINE && FILL){
                Renderer3D.drawBoxBoth(modfiedBox, QuadColor.gradient(startG,endG,gb.dir),LINE_WIDTH,gb.exclude());
                continue;
            }

            if(OUTLINE){
                Renderer3D.drawBoxOutline(modfiedBox, QuadColor.gradient(startG,endG,gb.dir),LINE_WIDTH,gb.exclude());
            }

            if(FILL){
                Renderer3D.drawBoxFill(modfiedBox, QuadColor.gradient(startG,endG,gb.dir),gb.exclude());
            }
        }

        if(THROUGH_WALLS)
            Renderer3D.stopRenderingThroughWalls();
    }
    public static void setShouldFill(boolean FILL) {
        GradientBlockRenderer.FILL = FILL;
    }

    public static void setModifyAlphaValue(int modifyAlphaValue) {
        MODIFY_ALPHA_VALUE = modifyAlphaValue;
    }

    public static void setLineWidth(float lineWidth) {
        LINE_WIDTH = lineWidth;
    }

    public static void setShouldOutline(boolean OUTLINE) {
        GradientBlockRenderer.OUTLINE = OUTLINE;
    }

    public static void setThroughWalls(boolean throughWalls) {
        THROUGH_WALLS = throughWalls;
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
