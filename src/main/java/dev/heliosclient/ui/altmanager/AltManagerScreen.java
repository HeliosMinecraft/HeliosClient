package dev.heliosclient.ui.altmanager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AltManagerScreen extends Screen {
    //WIP

    public static AltManagerScreen INSTANCE = new AltManagerScreen();

    protected AltManagerScreen() {
        super(Text.literal("Alt Manager"));
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(drawContext,MENU_BACKGROUND_TEXTURE,0,0,0,0,width,height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
