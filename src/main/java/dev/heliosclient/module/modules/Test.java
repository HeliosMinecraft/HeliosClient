package dev.heliosclient.module.modules;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.Renderer3D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


public class Test extends Module_ {
    BooleanSetting rectangle = new BooleanSetting("Rectangle","",this,false);
    BooleanSetting rounded = new BooleanSetting("Rounded","",this,false);
    BooleanSetting Circle = new BooleanSetting("Circle","",this,false);
    BooleanSetting Triangle = new BooleanSetting("Triangle","",this,false);
    BooleanSetting Arc = new BooleanSetting("Arc","",this,false);
    BooleanSetting TracerLine = new BooleanSetting("TracerLine","",this,false);
    BooleanSetting blockOutlineAndFIll = new BooleanSetting("blockOutlineAndFIll","",this,false);


    public Test() {
        super("Test", "Render Test",  Category.RENDER);
        settings.add(rectangle);
        settings.add(rounded);
        settings.add(Circle);
        settings.add(Arc);
        settings.add(Triangle);
        settings.add(TracerLine);
        settings.add(blockOutlineAndFIll);
        quickSettings.add(rectangle);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void render(DrawContext drawContext, float tickDelta, CallbackInfo info) {
        // Draw a 2D rectangle using the CustomRenderer class
        if (rectangle.value)
            Renderer2D.fill(drawContext,10, 10, 100, 50, 0xFFFF0000);

        // Draw a 2D rounded rectangle using the CustomRenderer class
        if (rounded.value)
            Renderer2D.drawRoundedRectangle(drawContext,120, 10, 100, 50, 10, Color.DARK_GRAY.getRGB());

        // Draw a 2D circle using the CustomRenderer class
        if (Circle.value)
            Renderer2D.drawFilledCircle(drawContext,165, 35, 20, 0xFF0000FF);

        // Draw a 2D triangle using the CustomRenderer class
        if (Triangle.value)
            Renderer2D.drawTriangle(drawContext,10, 10, 100, 50, 60, 120, Color.WHITE.getRGB());

        // Draw a 2D arc using the CustomRenderer class
        if (Arc.value)
            Renderer2D.drawArc(drawContext,165, 35, 50, 0, 90, Color.WHITE.getRGB());

        Entity entity = mc.player;
        // Draw a 3D block outline using the CustomRenderer3D class
        if (entity != null) {
            BlockPos blockPos = new BlockPos(entity.getBlockX() + 5, entity.getBlockY(), entity.getBlockZ() - 5);
                if (blockOutlineAndFIll.value) {
                    Renderer3D.drawBlockOutline(drawContext, blockPos, 2.0f, 0xFF0000FF);
                    //Renderer3D.drawFilledBox(drawContext, blockPos, 0xFF0000FF);
                    //  Renderer3D.drawTracerLine(blockPos,2.0f, Color.WHITE.getRGB(),drawContext);
                }
                // Draw a tracer line towards an entity using the CustomRenderer3D class
                if (TracerLine.value) {
                    System.out.println("drawing at" + blockPos);
                    Renderer3D.drawLineFromPlayer(drawContext, blockPos.toCenterPos(), Color.WHITE.getRGB(), 5.0f, tickDelta);
            }
        }
    }
}
