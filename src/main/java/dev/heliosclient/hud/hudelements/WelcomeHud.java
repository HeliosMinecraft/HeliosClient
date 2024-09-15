package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringSetting;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.inputbox.InputBox;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class WelcomeHud extends HudElement {
    SettingGroup sgGeneral = new SettingGroup("General");    public static HudElementData<WelcomeHud> DATA = new HudElementData<>("WelcomeHud", "Welcomes you", WelcomeHud::new);
    final StringSetting textValue = sgGeneral.add(new StringSetting.Builder()
            .name("Text")
            .description("%s is the player session name. Only that will display the current player name")
            .onSettingChange(this)
            .inputMode(InputBox.InputMode.ALL)
            .characterLimit(1000)
            .defaultValue("Good day, %s!")
            .build()
    );
    public WelcomeHud() {
        super(DATA);
        addSettingGroup(sgGeneral);

        this.height = Math.round(Renderer2D.getStringHeight());
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);
        try {
            String text = String.format(textValue.value, ColorUtils.gray + mc.getSession().getUsername() + ColorUtils.reset);
            this.width = Math.round(Renderer2D.getStringWidth(text) + 1);
            Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, ColorManager.INSTANCE.hudColor);
        } catch (Exception e) {
            ChatUtils.sendHeliosMsg("Error while formatting welcome hud");
        }
    }
}
