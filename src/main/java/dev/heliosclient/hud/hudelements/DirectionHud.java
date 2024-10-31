package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import me.x150.renderer.render.Renderer2d;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.List;

public class DirectionHud extends HudElement {
    public SettingGroup sgGeneral = new SettingGroup("General");

    public CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .defaultValue(List.of(Mode.values()))
            .defaultListOption(Mode.Compass)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting shortDirection = sgGeneral.add(new BooleanSetting.Builder()
            .name("Short Direction / Axis")
            .description("The direction will be shown short as axis (like X+, Z-,etc.)")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()-> mode.isOption(Mode.Simple))
            .build()
    );
    public BooleanSetting snapToCrosshair = sgGeneral.add(new BooleanSetting.Builder()
            .name("Snap to crosshair")
            .description("Snaps the compass to the crosshair")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()-> mode.isOption(Mode.Compass))
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
            .shouldRender(()-> mode.isOption(Mode.Compass))
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
            .shouldRender(()-> mode.isOption(Mode.Compass))
            .onSettingChange(this)
            .build()
    );

    public BooleanSetting renderEllipse = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render ellipse")
            .description("Renders the ellipse outline on the compass")
            .defaultValue(true)
            .value(true)
            .shouldRender(()-> mode.isOption(Mode.Compass))
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
            .shouldRender(()-> mode.isOption(Mode.Compass))
            .onSettingChange(this)
            .shouldRender(() -> renderEllipse.value && mode.isOption(Mode.Compass))
            .build()
    );
    public RGBASetting compassColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Compass Color")
            .description("Color of compass outline")
            .defaultValue(Color.WHITE)
            .value(Color.WHITE)
            .onSettingChange(this)
            .shouldRender(() -> renderEllipse.value && mode.isOption(Mode.Compass))
            .build()
    );

    public DirectionHud() {
        super(DATA);
        addSettingGroup(sgGeneral);

        this.width = 100;
        this.height = 100;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        if(mode.isOption(Mode.Compass)) {
            this.width = (int) (xRad.value * 2 + Renderer2D.getStringHeight());
            this.height = (int) (yRad.value * 2 + Renderer2D.getStringHeight() + 1);

            if (snapToCrosshair.value) {
                this.x = mc.getWindow().getScaledWidth() / 2 - this.width / 2;
                this.y = mc.getWindow().getScaledHeight() / 2 - this.height / 2;
            }

            super.renderElement(drawContext, textRenderer);

            // Calculate player orientation (yaw) in radians
            double yawRadians = Math.toRadians(90);
            if (mc.player != null) {
                yawRadians = Math.toRadians(mc.player.getYaw(mc.getTickDelta()));
            }

            int centerX = this.x + this.width / 2;
            int centerY = this.y + this.height / 2;

            // Draw a circular radar around the cross-hair
            if (renderEllipse.value)
                Renderer2d.renderEllipseOutline(drawContext.getMatrices(), compassColor.value, centerX, centerY, xRad.value, yRad.value, outlineWidth.value, outlineWidth.value, 360);

            // Draw cardinal direction indicators
            double quarterPI = Math.PI / 2.0;
            for (int i = 0; i < 4; i++) {
                double angle = yawRadians + i * quarterPI; // Rotate by 90 degrees
                float x = (float) (centerX + xRad.value * Math.cos(angle)) - 3;
                float y = (float) (centerY + yRad.value * Math.sin(angle));

                String text = getCardinalDirection(i);
                Renderer2D.drawString(drawContext.getMatrices(), text, x, y, ColorManager.INSTANCE.hudColor);
            }
        }else {
            String directionAppend = shortDirection.value ? translateToShortAxis() : (mc.player == null ? "North" :  mc.player.getHorizontalFacing().getName());
            String text = ColorUtils.reset + "Direction: " + ColorUtils.white + directionAppend;
            this.width = Math.round(Renderer2D.getStringWidth(text));
            this.height = Math.round(Renderer2D.getStringHeight(text));

            super.renderElement(drawContext, textRenderer);
            Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, ColorManager.INSTANCE.hudColor);
        }
    }

    private String getCardinalDirection(int i) {
        return switch (i) {
            case 0 -> "E";
            case 1 -> "N";
            case 2 -> "W";
            case 3 -> "S";
            default -> "N";
        };
    }

    private String translateToShortAxis(){
        if(mc.player == null){
            return "Z-";
        }
        return switch (mc.player.getHorizontalFacing()){
            case DOWN -> "Y-";
            case UP -> "Y+";
            case NORTH -> "Z-";
            case SOUTH -> "Z+";
            case EAST -> "X+";
            case WEST -> "X-";
        };
    }

    public static HudElementData<DirectionHud> DATA = new HudElementData<>("DirectionHUD", "Displays the direction you are facing", DirectionHud::new);

    enum Mode{
        Simple,
        Compass
    }
}
