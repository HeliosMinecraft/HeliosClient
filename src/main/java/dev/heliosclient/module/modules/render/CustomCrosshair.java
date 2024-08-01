package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.CrosshairRenderEvent;
import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CustomCrosshair extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    public DropDownSetting mode = sgGeneral.add(new DropDownSetting.Builder()
            .name("Crosshair mode")
            .description("CrosshairModes")
            .onSettingChange(this)
            .defaultValue(List.of(CrosshairModes.values()))
            .defaultListIndex(0)
            .build()
    );
    DoubleSetting radius = sgGeneral.add(new DoubleSetting.Builder()
            .name("Radius of rounded")
            .description("Rounded radius")
            .onSettingChange(this)
            .defaultValue(2.0)
            .min(0.0)
            .max(5.0)
            .roundingPlace(1)
            .shouldRender(() -> mode.getOption() == CrosshairModes.ROUNDED || mode.getOption() == CrosshairModes.SQUARE)
            .build());
    DoubleSetting thickness = sgGeneral.add(new DoubleSetting.Builder()
            .name("Thickness")
            .description("Thickness of the crosshair lines")
            .onSettingChange(this)
            .value(1.0)
            .defaultValue(1.0)
            .min(0.1)
            .max(5.0)
            .roundingPlace(1)
            .build());
    DoubleSetting size = sgGeneral.add(new DoubleSetting.Builder()
            .name("Size")
            .description("Length of the crosshair lines")
            .onSettingChange(this)
            .value(5.0)
            .defaultValue(5.0)
            .min(1.0)
            .max(20.0)
            .roundingPlace(1)
            .build());

    public CycleSetting colorMode = sgGeneral.add(new CycleSetting.Builder()
            .name("Color Mode")
            .description("Color mode for parts of the client")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Static", "Gradient")))
            .defaultListIndex(0)
            .build()
    );
    public RGBASetting staticColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Color")
            .description("Simple single color for parts of the client")
            .onSettingChange(this)
            .defaultValue(new Color(-1))
            .shouldRender(() -> colorMode.value == 0)
            .build()
    );
    public CycleSetting gradientType = sgGeneral.add(new CycleSetting.Builder()
            .name("Gradient Type")
            .description("Gradient type for the gradient color mode")
            .onSettingChange(this)
            .value(GradientManager.getAllGradientsNames().stream().toList())
            .defaultListIndex(1)
            .shouldRender(() -> colorMode.value == 1)
            .build()
    );
    public BooleanSetting renderAttackIndicator = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render AttackIndicator")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting dynamic = sgGeneral.add(new BooleanSetting.Builder()
            .name("Dynamic")
            .description("Draws the crosshair only when you are looking at smth (block / entity)")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );


    public CustomCrosshair() {
        super("CustomCrosshair", "Customise your crosshair", Categories.RENDER);
        addSettingGroup(sgGeneral);

        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @SubscribeEvent
    public void onRenderCrosshair(CrosshairRenderEvent event) {
        event.cancel();
        if (dynamic.value && (mc.crosshairTarget == null || mc.crosshairTarget.getType() == HitResult.Type.MISS))
            return;

        switch ((CrosshairModes) mode.getOption()) {
            case STANDARD -> renderStandard(event.drawContext, event.x, event.y, event.width, event.height);
            case ROUNDED -> renderRounded(event.drawContext, event.x, event.y, event.width, event.height);
            case SQUARE -> renderSquare(event.drawContext, event.x, event.y, event.width, event.height);
            case INVERSE_TRIANGLE_GAP -> renderInverseTriangleGap(event.drawContext, event.x, event.y);
            case CIRCLE -> renderCircle(event.drawContext, event.x, event.y, event.width, event.height);
            case DOT -> renderDot(event.drawContext, event.x, event.y, event.width, event.height);
        }
    }

    public void renderStandard(DrawContext dr, int x, int y, int width, int height) {
        mask(dr, () -> {
            Renderer2D.drawHorizontalLine(dr.getMatrices().peek().getPositionMatrix(), x - (float) size.value / 2.0f, (float) size.value, (float) (y - thickness.value / 2.0f), (float) thickness.value, -1);
            Renderer2D.drawVerticalLine(dr.getMatrices().peek().getPositionMatrix(), x - (float) thickness.value / 2.0f, (float) (y - size.value / 2.0f), (float) size.value, (float) thickness.value, -1);
        }, x - getSize() / 2 - 2, y - getSize() / 2 - 2, getSize() + 2, getSize() + 2);

    }

    public void renderRounded(DrawContext dr, int x, int y, int width, int height) {
        mask(dr, () -> {
            Renderer2D.drawRoundedRectangle(dr.getMatrices().peek().getPositionMatrix(), x - (float) size.value / 2.0f, (float) (y - thickness.value / 2.0f), (float) size.value, (float) thickness.value, (float) radius.value, -1);
            Renderer2D.drawRoundedRectangle(dr.getMatrices().peek().getPositionMatrix(), x - (float) thickness.value / 2.0f, (float) (y - size.value / 2.0f), (float) thickness.value, (float) size.value, (float) radius.value, -1);

        }, x - getSize() / 2, y - getSize() / 2, getSize(), getSize());
    }

    public void renderSquare(DrawContext dr, int x, int y, int width, int height) {
        mask(dr, () -> {
            Renderer2D.drawOutlineRoundedBox(dr.getMatrices().peek().getPositionMatrix(), x - (float) size.value / 2.0f, (float) (y - size.value / 2.0f), (float) size.value, (float) size.value, (float) radius.value, (float) thickness.value, -1);
        }, x - getSize() / 2 - 5, y - getSize() / 2 - 5, getSize() + 5, getSize() + 5);
    }

    public void renderInverseTriangleGap(DrawContext dr, int x, int y) {
        Color startG = colorMode.value == 0 ? staticColor.getColor() : GradientManager.getGradient(gradientType.getOption().toString()).getStartGradient();
        Color endG = colorMode.value == 0 ? staticColor.getColor() : GradientManager.getGradient(gradientType.getOption().toString()).getStartGradient();

        Matrix4f mc = dr.getMatrices().peek().getPositionMatrix();
        //left
        Renderer2D.drawFilledTriangle(mc, x - getSize(), y - getSize() / 2, x - getSize(), y + getSize() / 2, (int) (x - thickness.value), y, startG.getRGB());

        //right
        Renderer2D.drawFilledTriangle(mc, x + getSize(), y + getSize() / 2, x + getSize(), y - getSize() / 2, (int) (x + thickness.value), y, endG.getRGB());

        //top
        Renderer2D.drawFilledTriangle(mc, x + getSize() / 2, y - getSize(), x - getSize() / 2, y - getSize(), x, (int) (y - thickness.value), startG.getRGB());

        //bottom
        Renderer2D.drawFilledTriangle(mc, x - getSize() / 2, y + getSize(), x + getSize() / 2, y + getSize(), x, (int) (y + thickness.value), endG.getRGB());

    }

    public void renderDot(DrawContext dr, int x, int y, int width, int height) {
        mask(dr, () -> Renderer2D.drawFilledCircle(dr.getMatrices().peek().getPositionMatrix(), x, y, (float) size.value / 2.0f, -1), x - getSize() * 2, y - getSize() * 2 ,  getSize() * 4, getSize()  * 4);
    }

    public void renderCircle(DrawContext dr, int x, int y, int width, int height) {
        mask(dr, () -> Renderer2D.drawCircle(dr.getMatrices().peek().getPositionMatrix(), x, y, (float) size.value / 2.0f, (float) thickness.value, -1), x - (getSize() * 2) - (int) thickness.value,  y - (getSize() * 2) - (int) thickness.value, getSize() * 4 + (int) thickness.value,  getSize() * 4 + (int) thickness.value);
    }

    private int getSize() {
        return (int) Math.ceil(size.value);
    }

    public void mask(DrawContext dr, Runnable task, int x, int y, int width, int height) {
        Color startG = colorMode.value == 0 ? staticColor.getColor() : GradientManager.getGradient(gradientType.getOption().toString()).getStartGradient();
        Color endG = colorMode.value == 0 ? staticColor.getColor() : GradientManager.getGradient(gradientType.getOption().toString()).getStartGradient();


        Renderer2D.drawToGradientMask(dr.getMatrices().peek().getPositionMatrix(),
                startG,
                endG,
                startG,
                endG,
                x, y, width, height, task);
    }


    private enum CrosshairModes {
        STANDARD,
        CIRCLE,
        DOT,
        SQUARE,
        INVERSE_TRIANGLE_GAP,
        ROUNDED
    }
}
