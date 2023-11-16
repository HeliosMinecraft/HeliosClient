package dev.heliosclient.ui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class HUDOverlay {
    public static HUDOverlay INSTANCE = new HUDOverlay();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public void render(DrawContext drawContext, int scaledWidth, int scaledHeight) {
        // do not draw if F3 enabled
      //  if (mc.options.debugEnabled) return;

        // draw stats
        drawContext.drawTextWithShadow(mc.textRenderer, "FPS: " + ColorUtils.gray + mc.getCurrentFps(), 2, 2, HeliosClient.uiColorA);
        drawContext.drawTextWithShadow(mc.textRenderer, "Ping: " + ColorUtils.gray + (mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) == null ? 0 : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency()), 2, 12, HeliosClient.uiColorA);
        drawContext.drawTextWithShadow(mc.textRenderer, "Meters/s: " + ColorUtils.gray + MathUtils.round(moveSpeed(), 2), 2, scaledHeight - 20, HeliosClient.uiColorA);
        drawContext.drawTextWithShadow(mc.textRenderer, "TPS: " + ColorUtils.gray + ((HeliosClient.MC.getServer() != null) ? HeliosClient.MC.getServer().getTicks() : 0), 2, scaledHeight - 28, HeliosClient.uiColorA);

        // draw coordinates
        drawContext.drawTextWithShadow(mc.textRenderer,
                "X: " + ColorUtils.gray + MathUtils.round(mc.player.getX(), 1) + ColorUtils.reset +
                        " Y: " + ColorUtils.gray + MathUtils.round(mc.player.getY(), 1) + ColorUtils.reset +
                        " Z: " + ColorUtils.gray + MathUtils.round(mc.player.getZ(), 1), 2, scaledHeight - 10, HeliosClient.uiColorA
        );

        // draw client tag (if enabled)
    }

    private double moveSpeed() {
        Vec3d move = new Vec3d(mc.player.getX() - mc.player.prevX, 0, mc.player.getZ() - mc.player.prevZ).multiply(20);

        return Math.abs(MathUtils.length2D(move));
    }
}
