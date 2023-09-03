package dev.heliosclient.ui.altmanager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AltManagerScreen extends Screen {
    public static AltManagerScreen INSTANCE = new AltManagerScreen();

    protected AltManagerScreen() {
        super(Text.literal("Alt Manager"));
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(drawContext);
    }

}
