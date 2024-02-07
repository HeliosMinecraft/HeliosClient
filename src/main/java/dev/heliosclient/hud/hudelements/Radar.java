package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Radar extends HudElement {
    private static int RADAR_SIZE = 100; // Size of the radar in pixels

    public static HudElementData<Radar> DATA = new HudElementData<>("Radar", "Shows entities radar", Radar::new);
    private static int MAX_DISTANCE = 25; // Maximum entity distance
    public SettingGroup sgRadarSettings = new SettingGroup("General");
    public SettingGroup sgRadarColors = new SettingGroup("Colors");
    public DoubleSetting distance = sgRadarSettings.add(new DoubleSetting.Builder()
            .name("Distance")
            .description("Max Entity distance to be included in the radar")
            .min(20)
            .max(200)
            .roundingPlace(0)
            .defaultValue(25.0D)
            .value(25.0D)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting size = sgRadarSettings.add(new DoubleSetting.Builder()
            .name("Size")
            .description("Radar size")
            .min(50)
            .max(300)
            .roundingPlace(0)
            .defaultValue(100.0D)
            .value(100.0D)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting includePlayers = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Include Players")
            .description("Render players in radar")
            .defaultValue(true)
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting includeHostile = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Include Hostile mobs")
            .description("Render hostile mobs in radar")
            .defaultValue(true)
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting includeTamed = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Include Tamed mobs")
            .description("Render Tamed mobs in radar")
            .defaultValue(true)
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting includePassive = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Include Passive mobs")
            .description("Render Passive mobs in radar")
            .defaultValue(true)
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting includeItems = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Include Items")
            .description("Render dropped items in radar")
            .defaultValue(true)
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting includeOthers = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Include Other entities")
            .description("Render other entities in radar like snowballs, armor stands, boats, ender pearls,etc.")
            .defaultValue(true)
            .value(false)
            .onSettingChange(this)
            .build()
    );

    public RGBASetting playerColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Player")
            .description("Color of players in radar")
            .defaultValue(Color.BLUE)
            .value(Color.BLUE)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting hostileColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Hostile Mobs")
            .description("Color of Hostile Mobs in radar")
            .defaultValue(Color.RED)
            .value(Color.RED)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting passiveColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Passive Mobs")
            .description("Color of passive mobs in radar")
            .defaultValue(Color.GREEN)
            .value(Color.GREEN)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting tamedColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Tamed Mobs")
            .description("Color of Tamed mobs in radar")
            .defaultValue(Color.PINK)
            .value(Color.PINK)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting itemsColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Items")
            .description("Color of items in radar")
            .defaultValue(Color.YELLOW)
            .value(Color.YELLOW)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting otherColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Other")
            .description("Color of other entities in radar")
            .defaultValue(Color.WHITE)
            .value(Color.WHITE)
            .onSettingChange(this)
            .build()
    );


    public Radar() {
        super(DATA);
        addSettingGroup(sgRadarSettings);
        addSettingGroup(sgRadarColors);

        this.width = (int) size.value;
        this.height = (int) size.value;
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        this.width = (int) size.value;
        this.height = (int) size.value;
        RADAR_SIZE = this.width;

        super.renderElement(drawContext, textRenderer);

        if (mc.player == null || mc.world == null) {
            return;
        }

        int centerX = x + RADAR_SIZE / 2;
        int centerY = y + RADAR_SIZE / 2;

        renderBg.value = true;
        rounded.value = true;

        Vec3d playerPos = mc.player.getPos();

        for (Entity entity : mc.world.getEntities()) {
            if (isBlackListed(entity)) {
                continue;
            }
            Vec3d entityPos = entity.getPos();
            // Ignore the Y-level difference
            double distance = playerPos.distanceTo(new Vec3d(entityPos.x, playerPos.y, entityPos.z));

            if (distance <= MAX_DISTANCE) {
                // Calculate the entity's position on the radar
                double angle = Math.atan2(entityPos.z - playerPos.z, entityPos.x - playerPos.x) - Math.toRadians(mc.player.getYaw()) - Math.PI;
                float x2 = (float) (centerX + Math.cos(angle) * (distance / MAX_DISTANCE) * ((float) RADAR_SIZE / 2));
                float y2 = (float) (centerY + Math.sin(angle) * (distance / MAX_DISTANCE) * ((float) RADAR_SIZE / 2));

                // Choose a color based on the entity type
                int color = getColor(entity);
                float radius = 0.5f;
                if (entity instanceof PlayerEntity) {
                    radius = 1f;
                }
                if (entity == mc.player) {
                    FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(), "\uF123", x2 - 1.5f, y2 - 1.2f, color);
                } else {
                    // Draw a dot for the entity
                    Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), x2, y2, radius, color);
                }
            }
        }
    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);
        if (setting == distance) {
            MAX_DISTANCE = (int) distance.value;
        }
    }

    public boolean isBlackListed(Entity entity) {
        return (!(entity instanceof HostileEntity) || !includeHostile.value) &&
                (!(entity instanceof PassiveEntity) || !includePassive.value) &&
                (!(entity instanceof TameableEntity) || !includeTamed.value) &&
                (!(entity instanceof PlayerEntity) || !includePlayers.value) &&
                (!(entity instanceof ItemEntity) || !includeItems.value) &&
                !includeOthers.value;
    }


    private int getColor(Entity entity) {
        int color;
        if (entity instanceof TameableEntity e && includeTamed.value) {
            if (e.isTamed())
                return tamedColor.value.getRGB();
        }
        if (entity instanceof PlayerEntity) {
            color = playerColor.value.getRGB();
        } else if (entity instanceof HostileEntity) {
            color = hostileColor.value.getRGB();
        } else if (entity instanceof PassiveEntity) {
            color = passiveColor.value.getRGB();
        } else if (entity instanceof ItemEntity) {
            color = itemsColor.value.getRGB();
        } else {
            color = otherColor.value.getRGB();
        }
        return color;
    }
}
