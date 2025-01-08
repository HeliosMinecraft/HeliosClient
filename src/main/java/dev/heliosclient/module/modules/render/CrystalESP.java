package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.WireframeEntityRenderer;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;

import java.awt.*;

public class CrystalESP extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public DoubleSetting scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Scale of crystal")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0.01f)
            .max(6f)
            .roundingPlace(2)
            .build()
    );

    public DoubleSetting bounce = sgGeneral.add(new DoubleSetting.Builder()
            .name("Bounce")
            .description("Bouncing of crystal")
            .onSettingChange(this)
            .value(0.6d)
            .defaultValue(0.6d)
            .min(0f)
            .max(10f)
            .roundingPlace(1)
            .build()
    );
    public DoubleSetting yOffset = sgGeneral.add(new DoubleSetting.Builder()
            .name("Y Offset")
            .description("Y offset of crystal")
            .onSettingChange(this)
            .value(0d)
            .defaultValue(0d)
            .min(-10f)
            .max(10f)
            .roundingPlace(1)
            .build()
    );

    public BooleanSetting wireFrame = sgGeneral.add(new BooleanSetting.Builder()
            .name("WireFrame")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    public BooleanSetting sides = sgGeneral.add(new BooleanSetting.Builder()
            .name("Sides")
            .description("Render wireframe sides")
            .onSettingChange(this)
            .defaultValue(false)
            .value(true)
            .shouldRender(() -> wireFrame.value)
            .build()
    );
    public BooleanSetting lines = sgGeneral.add(new BooleanSetting.Builder()
            .name("Lines")
            .description("Render wireframe lines")
            .onSettingChange(this)
            .defaultValue(false)
            .value(true)
            .shouldRender(() -> wireFrame.value)
            .build()
    );
    public DoubleSetting lineWidth = sgGeneral.add(new DoubleSetting.Builder()
            .name("Line Width")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0d)
            .max(30f)
            .roundingPlace(1)
            .shouldRender(() -> lines.value && wireFrame.value)
            .build()
    );

    public BooleanSetting texture = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render Texture")
            .onSettingChange(this)
            .defaultValue(false)
            .value(true)
            .build()
    );
    public BooleanSetting renderFrameInside = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render Frame Inside")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    public BooleanSetting renderFrameOutSide = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render Frame Outside")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    public RGBASetting frameColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Frame Color")
            .description("Color of the frame")
            .rainbow(false)
            .defaultValue(new Color(-1))
            .build()
    );

    public BooleanSetting renderCore = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render Core")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );

    public RGBASetting coreColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Core Color")
            .description("Color of the core")
            .rainbow(false)
            .defaultValue(new Color(-1))
            .shouldRender(() -> renderCore.value)
            .build()
    );

    public CrystalESP() {
        super("CrystalESP", "Modifies crystal rendering", Categories.RENDER);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void render3d(Render3DEvent event) {
        if (!wireFrame.value) return;

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof EndCrystalEntity endCrystal) {
                WireframeEntityRenderer.render(endCrystal, 1f, QuadColor.single(frameColor.value.getRGB()), LineColor.single(frameColor.value.getRGB()), ((float) lineWidth.value), sides.value, lines.value, false);
            }
        }
    }
}