package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class PlayerModel extends HudElement {

    private MinecraftClient mc = MinecraftClient.getInstance();

    public PlayerModel() {
        super("Player Model", "Shows player model in a small cute way");
        this.width = 30;
        this.height = 55;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        ClientPlayerEntity player=MinecraftClient.getInstance().player;
        if (player == null) {
            Renderer2D.drawRectangle(drawContext,x - width / 2,y  - height/2,width,height-2, Color.BLACK.getRGB());
        } else {
            float yaw = MathHelper.wrapDegrees(player.prevYaw + (player.getYaw() - player.prevYaw) * mc.getTickDelta());
            float pitch = player.getPitch();
            int x = this.x;
            int y = this.y;
            InventoryScreen.drawEntity(drawContext, x, y + height/2 - 3, 25, -yaw, -pitch, player);
        }

        //this.width = 25;

        //drawContext.drawText(textRenderer, text, this.x-width/2, this.y+height/2-10, HeliosClient.uiColorA, false);
    }

}
