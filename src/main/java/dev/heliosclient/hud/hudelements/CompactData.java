package dev.heliosclient.hud.hudelements;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.system.TickRate;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

import static net.fabricmc.loader.impl.util.StringUtil.capitalize;

public class CompactData extends HudElement {
    public CompactData() {
        super(DATA);
        this.width = 75;
        this.height = 50;
    }

    public static int getPing() {
        if (mc.player == null) {
            return 0;
        }
        PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(mc.player.getUuid());
        if (entry != null) {
            return entry.getLatency();
        }
        return 0;
    }

    public static String getBiome() {
        String biomes = "None";
        if (mc.world != null) {
            Optional<RegistryKey<Biome>> biome = mc.world.getBiome(mc.player.getBlockPos()).getKey();

            if (biome.isPresent()) {
                String biomeName = Text.translatable(biome.get().getValue().getNamespace() + "." + biome.get().getValue().getPath()).getString();
                biomes = capitalize(biomeName);
            }
        }
        return formatBiomeString(biomes.trim());
    }

    public static String formatBiomeString(String str) {
        str = str.replace("Minecraft.", "").replace("_", " ");
        return str.isEmpty() ? str : Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        int coordX, coordY, coordZ;
        if (mc.player == null) {
            coordX = 0;
            coordY = 0;
            coordZ = 0;
        } else {
            coordX = (int) MathUtils.round(mc.player.getX(), 0);
            coordY = (int) MathUtils.round(mc.player.getY(), 0);
            coordZ = (int) MathUtils.round(mc.player.getZ(), 0);
        }

        String fps = "FPS: " + ColorUtils.gray + mc.getCurrentFps();
        String speed = "Speed: " + ColorUtils.gray + MathUtils.round(moveSpeed(), 1);
        String ping = "Ping: " + ColorUtils.gray + getPing();
        String biome = "Biome: " + ColorUtils.gray + getBiome();
        String tps = "TPS: " + ColorUtils.gray + ((!Float.isNaN(TickRate.INSTANCE.getTPS())) ? MathUtils.round(TickRate.INSTANCE.getTPS(), 1) : "0.0");

        super.renderElement(drawContext, textRenderer);

        this.width = Math.round(
                Math.max(
                        Math.min(82, Math.max(Renderer2D.getStringWidth(fps + speed), Renderer2D.getStringWidth(ping + biome))),
                        Math.max(Renderer2D.getStringWidth(fps + speed) + 5, Renderer2D.getStringWidth(ping + biome) + 5)));


        this.height = Math.round(Renderer2D.getStringHeight() * 5 + 11);
        float textX = this.x + 1;

        Renderer2D.drawString(drawContext.getMatrices(), "X: " + ColorUtils.gray + coordX, textX, this.y + 1, ColorManager.INSTANCE.hudColor);
        Renderer2D.drawString(drawContext.getMatrices(), tps, this.x + 4 + Renderer2D.getStringWidth("X: " + ColorUtils.gray + coordX), this.y + 1, ColorManager.INSTANCE.hudColor);
        Renderer2D.drawString(drawContext.getMatrices(), "Y: " + ColorUtils.gray + coordY, textX, this.y + Renderer2D.getStringHeight() + 3, ColorManager.INSTANCE.hudColor);
        Renderer2D.drawString(drawContext.getMatrices(), "Z: " + ColorUtils.gray + coordZ, textX, this.y + Renderer2D.getStringHeight() * 2 + 5, ColorManager.INSTANCE.hudColor);

        Renderer2D.drawString(drawContext.getMatrices(), fps, textX, this.y + Renderer2D.getStringHeight() * 3 + 7, ColorManager.INSTANCE.hudColor);
        Renderer2D.drawString(drawContext.getMatrices(), speed, textX + Renderer2D.getStringWidth(fps) + 3, this.y + Renderer2D.getStringHeight() * 3 + 7, ColorManager.INSTANCE.hudColor);
        Renderer2D.drawString(drawContext.getMatrices(), ping, textX, this.y + Renderer2D.getStringHeight() * 4 + 9, ColorManager.INSTANCE.hudColor);
        Renderer2D.drawString(drawContext.getMatrices(), biome, textX + Renderer2D.getStringWidth(ping) + 3, this.y + Renderer2D.getStringHeight() * 4 + 9, ColorManager.INSTANCE.hudColor);

    }

    public static HudElementData<CompactData> DATA = new HudElementData<>("CompactData", "Displays data in compact manner", CompactData::new);

    private double moveSpeed() {
        if (mc.player == null) {
            return 0;
        }
        Vec3d move = new Vec3d(mc.player.getX() - mc.player.prevX, 0, mc.player.getZ() - mc.player.prevZ).multiply(20);

        return Math.abs(MathUtils.length2D(move));
    }

}
