package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.textures.Texture;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ClientTag extends HudElement {
    public static HudElementData<ClientTag> DATA = new HudElementData<>("Client Tag", "Shows client watermark", ClientTag::new);

    public SettingGroup sgSettings = new SettingGroup("General");

    static Texture LOGO = new Texture("icon.png");
    static Texture FULL_SPLASH = new Texture("splashscreen/client_splash.png");

    private final CycleSetting mode = sgSettings.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Mode to show the client tag")
            .defaultValue(List.of(Mode.values()))
            .defaultListOption(Mode.Text)
            .onSettingChange(this)
            .build()
    );
    private final DoubleSetting scale = sgSettings.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Change the scale of the tag")
            .min(0.3d)
            .max(5d)
            .value(1D)
            .defaultValue(1D)
            .onSettingChange(this)
            .roundingPlace(2)
            .shouldRender(()-> !mode.isOption(Mode.Text))
            .build()
    );

    public ClientTag() {
        super(DATA);
        this.width = 20;
        this.height = Math.round(Renderer2D.getStringHeight());
        addSettingGroup(sgSettings);
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);

        switch ((Mode)mode.getOption()){
            case Text -> {
                String text = HeliosClient.clientTag + " " + HeliosClient.versionTag;
                this.width = Math.round(Renderer2D.getStringWidth(text));
                this.height = Math.round(Renderer2D.getStringHeight());

                Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, ColorManager.INSTANCE.hudColor);
            }
            case FullSplash -> {
                this.width = (int) Math.round(80 * scale.value);
                this.height = (int) Math.round(24 * scale.value);

                drawContext.drawTexture(FULL_SPLASH, this.x,  (int) Math.round(this.y - (6 * scale.value)),0,0, this.width,(int) Math.round(36 * scale.value),this.width,(int) Math.round(36 * scale.value));
            }
            case Logo -> {
                //Square 303x303 reduced by 8 times. (303/8 ~= 38)
                this.width = this.height = (int) Math.round(38 * scale.value);

                drawContext.drawTexture(LOGO, this.x, this.y,0,0, this.width,this.height, this.width,this.height);
            }
        }
    }


    enum Mode{
        FullSplash,
        Logo,
        Text
    }

}
