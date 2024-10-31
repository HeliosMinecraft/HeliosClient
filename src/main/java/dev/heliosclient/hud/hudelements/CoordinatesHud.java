package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;


public class CoordinatesHud extends HudElement {

    public SettingGroup sgSettings = new SettingGroup("Settings");

    private final BooleanSetting noXYZ = sgSettings.add(new BooleanSetting.Builder()
            .name("NoXYZ")
            .description("Does not show the XYZ tag")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    private final BooleanSetting cameraEntity = sgSettings.add(new BooleanSetting.Builder()
            .name("Camera Coords")
            .description("Shows the coordinates of camera instead of player. Works with Freecam and other camera manipulating modules")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    private final BooleanSetting netherCoords = sgSettings.add(new BooleanSetting.Builder()
            .name("Nether Coords")
            .description("Shows the current coordinates translated to nether coordinates")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    int coordX, coordY, coordZ;

    public CoordinatesHud() {
        super(DATA);
        this.width = 50;
        this.height = Math.round(Renderer2D.getStringHeight());
        addSettingGroup(sgSettings);
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);

        Entity entityForCoords = cameraEntity.value ? mc.getCameraEntity() : mc.player;

        if (entityForCoords == null) {
            coordX = 0;
            coordY = 0;
            coordZ = 0;
        } else {
            int divideBy = netherCoords.value ? 8 : 1;
            coordX = (int) MathUtils.round(entityForCoords.getX() / divideBy, 0);
            coordY = (int) MathUtils.round(entityForCoords.getY() / divideBy, 0);
            coordZ = (int) MathUtils.round(entityForCoords.getZ() / divideBy, 0);
        }

        String x = ColorUtils.gray + coordX + ColorUtils.reset;
        String y = ColorUtils.gray + coordY + ColorUtils.reset;
        String z = ColorUtils.gray + coordZ;


        String text = (!noXYZ.value ? "X: " : "") + x +
                (!noXYZ.value ? " Y: " : ", ") + y +
                (!noXYZ.value ? " Z: " : ", ") + z;

        this.width = Math.round(Renderer2D.getStringWidth(text));
        this.height = Math.round(Renderer2D.getStringHeight());

        Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, ColorManager.INSTANCE.hudColor);
    }

    public static HudElementData<CoordinatesHud> DATA = new HudElementData<>("Coordinates", "Shows player coords", CoordinatesHud::new);


}
