package dev.heliosclient.util.render;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.entity.LivingEntity;

/**
 * Basic Target Entity Renderer for TriggerBot, KillAura, etc.
 */
public class TargetRenderer {
    public static TargetRenderer INSTANCE = new TargetRenderer();
    public RenderMode renderMode = RenderMode.Outline;
    public GradientManager.Gradient color = ColorManager.getPrimaryGradient();
    public QuadColor.CardinalDirection dir = QuadColor.CardinalDirection.DIAGONAL_LEFT;
    public float radius = 1f;
    private LivingEntity targetEntity = null;

    public void set(LivingEntity targetEntity){
        this.targetEntity = targetEntity;
    }

    public void render(){
        if(targetEntity == null)return;

        QuadColor color = QuadColor.gradient(this.color.getStartColor().getRGB(),this.color.getEndColor().getRGB(),dir);

        switch (renderMode){
            case Circle -> Renderer3D.drawFlatFilledCircle(radius,Renderer3D.getInterpolatedPosition(targetEntity),320,color);
            case Outline -> Renderer3D.drawBoxOutline(targetEntity.getBoundingBox(),color,3f);
            case WireFrame,
                 PlayerSkeleton -> WireframeEntityRenderer.render(targetEntity,1.0f,color, LineColor.gradient(this.color.getStartColor().getRGB(),this.color.getEndColor().getRGB()),1.5f,true,true,renderMode == RenderMode.PlayerSkeleton);
        }
    }

    public enum RenderMode {
        Outline,
        WireFrame,
        PlayerSkeleton,
        Circle
    }
}
