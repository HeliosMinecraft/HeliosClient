package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.world.PaletteExploit;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class Radar extends HudElement {
    private static int RADAR_SIZE = 100; // Size of the radar in pixels
    private static int MAX_DISTANCE = 25; // Maximum entity distance
    public SettingGroup sgRadarSettings = new SettingGroup("General");
    public SettingGroup sgRadarColors = new SettingGroup("Colors");    public static HudElementData<Radar> DATA = new HudElementData<>("Radar", "Shows entities radar", Radar::new);
    public DoubleSetting distance = sgRadarSettings.add(new DoubleSetting.Builder()
            .name("Distance To Scale")
            .description("Max Entity distance to be included in the radar")
            .min(20)
            .max(200)
            .roundingPlace(0)
            .defaultValue(25.0D)
            .value(25.0D)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting showGridLines = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Show Grid Lines")
            .description("Renders grid lines in the center of the radar")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting showChunkTrails = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Show Chunk Trails")
            .description("Renders old/new chunks from the Palette exploit module (because it is the most accurate currently) in the radar")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting showAllChunkTrails = sgRadarSettings.add(new BooleanSetting.Builder()
            .name("Show All Chunk Trails")
            .description("This will render all the stored chunks in a small way such that all trails are fit inside the radar. Useful for finding previous trail paths. This will also make the position of entities and chunks not match.")
            .defaultValue(false)
            .value(false)
            .onSettingChange(this)
            .shouldRender(() -> showChunkTrails.value)
            .build()
    );
    public DropDownSetting drawingMode = sgRadarSettings.add(new DropDownSetting.Builder()
            .name("Drawing Mode")
            .description("Mode of drawing entities on the radar")
            .defaultValue(List.of(DrawingMode.values()))
            .defaultListOption(DrawingMode.DOT)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting drawingScale = sgRadarSettings.add(new DoubleSetting.Builder()
            .name("Scale of the drawn entities")
            .description("Scale of the drawn entities, specifically for text or Face mode")
            .min(0.1)
            .max(2f)
            .roundingPlace(1)
            .defaultValue(0.5f)
            .value(0.5f)
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
    public RGBASetting yourColor = sgRadarColors.add(new RGBASetting.Builder()
            .name("Your Color")
            .description("Color of you in the radar")
            .defaultValue(Color.CYAN)
            .value(Color.CYAN)
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
        renderBg.setValue(true);
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        this.width = size.getInt();
        this.height = size.getInt();

        super.renderElement(drawContext, textRenderer);

        if (mc.player == null || mc.world == null) {
            return;
        }

        int centerX = x + RADAR_SIZE / 2;
        int centerY = y + RADAR_SIZE / 2;

        Vec3d playerPos = mc.player.getPos();

        Renderer2D.enableScissor(x - this.padding.getInt() - 2, y - this.padding.getInt() - 2, RADAR_SIZE + this.padding.getInt() + 2, RADAR_SIZE + this.padding.getInt() + 2);
        if (showChunkTrails.value) {
            renderChunks(drawContext, centerX, centerY);
        }

        for (Entity entity : mc.world.getEntities()) {
            if (isBlackListed(entity)) {
                continue;
            }
            Vec3d entityPos = entity.getPos();
            // Ignore the Y-level difference
            double dx = playerPos.x - entityPos.x;
            double dz = playerPos.z - entityPos.z;
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance <= MAX_DISTANCE) {
                // Calculate the entity's position on the radar
                double angle = Math.atan2(entityPos.z - playerPos.z, entityPos.x - playerPos.x) - Math.toRadians(mc.player.getYaw()) - Math.PI;
                float x2 = (float) (centerX + Math.cos(angle) * (distance / MAX_DISTANCE) * ((float) RADAR_SIZE / 2));
                float y2 = (float) (centerY + Math.sin(angle) * (distance / MAX_DISTANCE) * ((float) RADAR_SIZE / 2));

                // Choose a color based on the entity type
                int color = getColor(entity);
                float radius = 1.2f;
                if (entity instanceof PlayerEntity) {
                    radius = 1.5f;
                }
                if (entity == mc.player) {
                    FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(), "\uF18B", x2 - FontRenderers.Small_iconRenderer.getStringWidth("\uF18B") / 2f, y2 - FontRenderers.Small_iconRenderer.getStringHeight("\uF18B") / 2f, yourColor.value.getRGB());
                } else {
                    drawEntityOnRadar(drawContext, entity, x2, y2, radius, color);
                }
            }
        }
        if (showGridLines.value) {
            renderGridLines(drawContext);
        }
        Renderer2D.disableScissor();
    }

    public void drawEntityOnRadar(DrawContext drawContext, Entity entity, float x, float y, float radius, int color) {
        switch ((DrawingMode) drawingMode.getOption()) {
            case DOT ->
                    Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, radius, color);
            case FIRST_LETTER -> {
                float scaledX = x / drawingScale.getFloat();
                float scaledY = y / drawingScale.getFloat();
                drawContext.getMatrices().push();
                drawContext.getMatrices().scale(drawingScale.getFloat(), drawingScale.getFloat(), 1f);

                String text = entity.getType().getUntranslatedName().substring(0, 1);

                drawContext.drawText(mc.textRenderer, text, (int) scaledX, (int) scaledY, color, false);
                drawContext.getMatrices().pop();
            }
            case FACE -> {
                var e = (EntityRenderer<?, ?>) mc.getEntityRenderDispatcher().getRenderer(entity);
                if (e instanceof LivingEntityRenderer<?, ?, ?> entityRenderer) {
                    Identifier texture = entityRenderer.getTexture(null);

                    if (texture != null) {
                        drawContext.getMatrices().push();
                        drawContext.getMatrices().scale(drawingScale.getFloat(), drawingScale.getFloat(), 1f);
                        drawContext.drawTexture(RenderLayer::getEntityCutoutNoCull, texture, (int) (x / drawingScale.getFloat() - 8), (int) (y / drawingScale.getFloat() - 8), 0, 0, 8, 8, 64, 64);
                        drawContext.getMatrices().pop();
                    }
                }
            }
        }
    }

    private void renderGridLines(DrawContext drawContext) {
        int color = ColorUtils.changeAlphaGetInt(0xFFFFFF, 200); // White color with some transparency

        // Draw horizontal and vertical grid lines at the center
        float paddingOff = this.padding.getFloat()/2.0f;
        Renderer2D.drawHorizontalLine(drawContext.getMatrices().peek().getPositionMatrix(), x - paddingOff,RADAR_SIZE + (paddingOff*4.0f),y + (float) RADAR_SIZE /2,0.7f,color);
        Renderer2D.drawVerticalLine(drawContext.getMatrices().peek().getPositionMatrix(),x + (float) RADAR_SIZE /2,y - paddingOff,RADAR_SIZE + (paddingOff*4.0f),0.7f,color);
       // Renderer2D.drawLine(drawContext.getMatrices(), hudBox.getX(), y + (float) RADAR_SIZE / 2, hudBox.getX() + hudBox.getWidth(), y + (float) RADAR_SIZE / 2, 1.2f, color);
        //Renderer2D.drawLine(drawContext.getMatrices(), x + (float) RADAR_SIZE / 2, hudBox.getY(), hudBox.getX() + (float) RADAR_SIZE / 2, hudBox.getY() + hudBox.getHeight(), 1.2f, color);
    }

    @Override
    protected void drawOutline(DrawContext drawContext, Color bgStart, Color bgEnd, Color finalShadowColor) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0, 0, 5);
        super.drawOutline(drawContext, bgStart, bgEnd, finalShadowColor);
        drawContext.getMatrices().pop();
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
        if (setting == distance) {
            MAX_DISTANCE = (int) distance.value;
        }
        if (setting == size) {
            RADAR_SIZE = this.size.getInt();
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

    private void renderChunks(DrawContext drawContext, int centerX, int centerY) {
        Vec3d playerPos = mc.player.getPos();
        PaletteExploit paletteExploit = ModuleManager.get(PaletteExploit.class);
        if (paletteExploit == null) return;

        if (showAllChunkTrails.value) {
            renderAllChunks(drawContext, paletteExploit.newChunks, PaletteExploit.ChunkType.NEW, centerX, centerY, playerPos);
            renderAllChunks(drawContext, paletteExploit.oldChunks, PaletteExploit.ChunkType.OLD, centerX, centerY, playerPos);
        } else {
            for (ChunkPos cp : paletteExploit.newChunks) {
                renderChunk(drawContext, cp, PaletteExploit.ChunkType.NEW, centerX, centerY, playerPos);
            }
            for (ChunkPos cp : paletteExploit.oldChunks) {
                renderChunk(drawContext, cp, PaletteExploit.ChunkType.OLD, centerX, centerY, playerPos);
            }
        }
    }

    private void renderChunk(DrawContext drawContext, ChunkPos cp, PaletteExploit.ChunkType type, int centerX, int centerY, Vec3d playerPos) {
        double dx = playerPos.x - cp.getStartX();
        double dz = playerPos.z - cp.getStartZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= MAX_DISTANCE) {
            double angle = Math.atan2(cp.getStartZ() - playerPos.z, cp.getStartX() - playerPos.x) - Math.toRadians(mc.player.getYaw()) - Math.PI;
            float x2 = (float) (centerX + Math.cos(angle) * (distance / this.distance.value) * ((float) size.value / 2));
            float y2 = (float) (centerY + Math.sin(angle) * (distance / this.distance.value) * ((float) size.value / 2));
            int color = ColorUtils.changeAlphaGetInt(ModuleManager.get(PaletteExploit.class).getChunkTypeColor(type), 150);

            // Scale the radar size to fit the distance setting
            float radarScale = size.getFloat() / this.distance.getFloat();

            // Each chunk gets a proportionate size based on the radar scale
            float chunkSize = radarScale * 8;

            // Calculate half the chunk size for easier manipulation
            float halfChunkSize = chunkSize / 2;

            // Calculate the sine and cosine of the player's yaw (rotation)
            float sinYaw = (float) Math.sin(-Math.toRadians(mc.player.getYaw()));
            float cosYaw = (float) Math.cos(-Math.toRadians(mc.player.getYaw()));

            // Calculate the top-left corner position of the rectangle
            float xTopLeft = x2 + (-halfChunkSize * cosYaw - halfChunkSize * sinYaw);
            float yTopLeft = y2 + (-halfChunkSize * sinYaw + halfChunkSize * cosYaw);

            // Calculate the top-right corner position of the rectangle
            float xTopRight = x2 + (halfChunkSize * cosYaw - halfChunkSize * sinYaw);
            float yTopRight = y2 + (halfChunkSize * sinYaw + halfChunkSize * cosYaw);

            // Calculate the bottom-left corner position of the rectangle
            float xBottomLeft = x2 + (-halfChunkSize * cosYaw + halfChunkSize * sinYaw);
            float yBottomLeft = y2 + (-halfChunkSize * sinYaw - halfChunkSize * cosYaw);

            // Calculate the bottom-right corner position of the rectangle
            float xBottomRight = x2 + (halfChunkSize * cosYaw + halfChunkSize * sinYaw);
            float yBottomRight = y2 + (halfChunkSize * sinYaw - halfChunkSize * cosYaw);

            // Draw the rotated rectangle with the calculated corner positions
            Renderer2D.drawQuad(drawContext.getMatrices().peek().getPositionMatrix(),
                    xTopLeft, yTopLeft, xTopRight, yTopRight, xBottomRight, yBottomRight, xBottomLeft, yBottomLeft, color);

        }
    }

    private void renderAllChunks(DrawContext drawContext, Set<ChunkPos> chunks, PaletteExploit.ChunkType type, int centerX, int centerY, Vec3d playerPos) {
        for (ChunkPos cp : chunks) {
            double dx = playerPos.x - cp.getStartX();
            double dz = playerPos.z - cp.getStartZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double angle = Math.atan2(cp.getStartZ() - playerPos.z, cp.getStartX() - playerPos.x) - Math.toRadians(mc.player.getYaw()) - Math.PI;
            float x2 = (float) (centerX + Math.cos(angle) * (distance / (this.distance.value * 16)) * ((float) size.value / 2));
            float y2 = (float) (centerY + Math.sin(angle) * (distance / (this.distance.value * 16)) * ((float) size.value / 2));
            int color = ColorUtils.changeAlphaGetInt(ModuleManager.get(PaletteExploit.class).getChunkTypeColor(type), 150);

            float radarScale = size.getFloat() / this.distance.getFloat();
            float chunkSize = radarScale * 8;

            float halfChunkSize = chunkSize / 2;
            float sinYaw = (float) Math.sin(-Math.toRadians(mc.player.getYaw()));
            float cosYaw = (float) Math.cos(-Math.toRadians(mc.player.getYaw()));

            float xTopLeft = x2 + (-halfChunkSize * cosYaw - halfChunkSize * sinYaw);
            float yTopLeft = y2 + (-halfChunkSize * sinYaw + halfChunkSize * cosYaw);
            float xTopRight = x2 + (halfChunkSize * cosYaw - halfChunkSize * sinYaw);
            float yTopRight = y2 + (halfChunkSize * sinYaw + halfChunkSize * cosYaw);
            float xBottomLeft = x2 + (-halfChunkSize * cosYaw + halfChunkSize * sinYaw);
            float yBottomLeft = y2 + (-halfChunkSize * sinYaw - halfChunkSize * cosYaw);
            float xBottomRight = x2 + (halfChunkSize * cosYaw + halfChunkSize * sinYaw);
            float yBottomRight = y2 + (halfChunkSize * sinYaw - halfChunkSize * cosYaw);

            Renderer2D.drawQuad(drawContext.getMatrices().peek().getPositionMatrix(),
                    xTopLeft, yTopLeft, xTopRight, yTopRight, xBottomRight, yBottomRight, xBottomLeft, yBottomLeft, color);
        }
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


    public enum DrawingMode {
        DOT,
        FIRST_LETTER,
        FACE
    }


}