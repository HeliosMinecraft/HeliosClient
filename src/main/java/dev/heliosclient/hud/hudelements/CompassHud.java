package dev.heliosclient.hud.hudelements;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.Renderer2D;
import me.x150.renderer.render.Renderer2d;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class CompassHud extends HudElement {
    public SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting snapToCrosshair = sgGeneral.add(new BooleanSetting.Builder()
            .name("Snap to crosshair")
            .description("Snaps the compass to the crosshair")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting xRad = sgGeneral.add(new DoubleSetting.Builder()
            .name("X radius")
            .description("Radius of the compass ellipse in the X axis")
            .min(0)
            .max(200)
            .roundingPlace(0)
            .defaultValue(50d)
            .value(50D)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting yRad = sgGeneral.add(new DoubleSetting.Builder()
            .name("Y Radius")
            .description("Radius of the compass ellipse in the Y axis")
            .min(0)
            .max(200)
            .roundingPlace(0)
            .defaultValue(20d)
            .onSettingChange(this)
            .build()
    );

    public BooleanSetting renderEllipse = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render ellipse")
            .description("Renders the ellipse outline on the compass")
            .defaultValue(true)
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting outlineWidth = sgGeneral.add(new DoubleSetting.Builder()
            .name("OutlineWidth")
            .description("Width of the ellipse outline")
            .min(0)
            .max(10)
            .roundingPlace(1)
            .defaultValue(0.7d)
            .onSettingChange(this)
            .shouldRender(()->renderEllipse.value)
            .build()
    );
    public RGBASetting compassColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Compass Color")
            .description("Color of compass outline")
            .defaultValue(Color.WHITE)
            .value(Color.WHITE)
            .onSettingChange(this)
            .shouldRender(()->renderEllipse.value)
            .build()
    );

    public CompassHud() {
        super(DATA);
        addSettingGroup(sgGeneral);

        this.width = 100;
        this.height = 100;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        this.width = (int) (xRad.value * 2 + Renderer2D.getStringHeight());
        this.height = (int) (yRad.value * 2 + Renderer2D.getStringHeight() + 1);

        if(snapToCrosshair.value){
            this.x = mc.getWindow().getScaledWidth()/2 - this.width/2;
            this.y = mc.getWindow().getScaledHeight()/2 - this.height/2 ;
        }

        super.renderElement(drawContext, textRenderer);

        // Calculate player orientation (yaw) in radians
        double yawRadians = Math.toRadians(90);
        if(mc.player != null){
            yawRadians = Math.toRadians(mc.player.getYaw(mc.getTickDelta()));
        }

        int centerX = this.x + this.width / 2;
        int centerY = this.y + this.height / 2;

        // Draw a circular radar around the cross-hair
        if(renderEllipse.value)
           Renderer2d.renderEllipseOutline(drawContext.getMatrices(), compassColor.value,centerX,centerY,xRad.value,yRad.value,outlineWidth.value,outlineWidth.value,360);

        // Draw cardinal direction indicators
        double quarterPI =  Math.PI / 2.0;
        for (int i = 0; i < 4; i++) {
            double angle = yawRadians + i * quarterPI; // Rotate by 90 degrees
            float x = (float) (centerX + xRad.value * Math.cos(angle)) - 3;
            float y = (float) (centerY + yRad.value * Math.sin(angle));

            String text = getCardinalDirection(i);
            Renderer2D.drawString(drawContext.getMatrices(), text, x, y, ColorManager.INSTANCE.hudColor);
        }
    }

    private String getCardinalDirection(int i) {
        return switch (i){
            case 0-> "E";
            case 1-> "N";
            case 2-> "W";
            case 3-> "S";
            default -> "N";
        };
    }

    public static HudElementData<CompassHud> DATA = new HudElementData<>("Compass", "Displays an elliptical compass", CompassHud::new);
}
