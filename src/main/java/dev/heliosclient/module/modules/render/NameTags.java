package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.entity.ItemPhysicsEvent;
import dev.heliosclient.event.events.render.EntityLabelRenderEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.player.FreeCamEntity;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;

public class NameTags extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    public DoubleSetting scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Scale of nametags")
            .onSettingChange(this)
            .value(0.5d)
            .defaultValue(0.5d)
            .min(0d)
            .max(10f)
            .roundingPlace(1)
            .build()
    );
    public BooleanSetting scaleOnDistance = sgGeneral.add(new BooleanSetting.Builder()
            .name("Scale Based on distance")
            .description("Scales the nametag depending on your distance from it")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public DoubleSetting yOffset = sgGeneral.add(new DoubleSetting.Builder()
            .name("Y offset")
            .description("Y offset above the eyepos of entity")
            .onSettingChange(this)
            .value(0d)
            .defaultValue(0d)
            .min(0d)
            .max(10f)
            .roundingPlace(1)
            .build()
    );
    public BooleanSetting items = sgGeneral.add(new BooleanSetting.Builder()
            .name("Items")
            .description("Renders nametags for items.")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting displayCount = sgGeneral.add(new BooleanSetting.Builder()
            .name("Display Item count")
            .description("Displays itemstack count of dropped items")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(() -> items.value)
            .build()
    );
    public BooleanSetting players = sgGeneral.add(new BooleanSetting.Builder()
            .name("Players")
            .description("Renders nametags for players")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting renderSelf = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render self")
            .description("Renders your nametag as well")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting mobs = sgGeneral.add(new BooleanSetting.Builder()
            .name("Mobs")
            .description("Renders nametags for mobs")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting ignoreArmorStand = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore Armor Stand")
            .description("Ignores armor stand nametags")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting ignoreInvisible = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore invisible")
            .description("Ignores invisible entities")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting team = sgGeneral.add(new BooleanSetting.Builder()
            .name("Team")
            .description("Override render colors of players to their team color.")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    SettingGroup sgNameTagData = new SettingGroup("Display data");
    public BooleanSetting background = sgNameTagData.add(new BooleanSetting.Builder()
            .name("Background")
            .description("Renders background")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting backgroundColor = sgNameTagData.add(new RGBASetting.Builder()
            .name("Background Color")
            .description("Color of background")
            .value(ColorUtils.changeAlpha(Color.BLACK, 100))
            .defaultValue(ColorUtils.changeAlpha(Color.BLACK, 100))
            .onSettingChange(this)
            .rainbow(false)
            .shouldRender(() -> background.value)
            .build()
    );
    public BooleanSetting renderOutline = sgNameTagData.add(new BooleanSetting.Builder()
            .name("Render outline")
            .description("Renders a sweet looking outline of the nametag")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting useClientColorsForOutline = sgNameTagData.add(new BooleanSetting.Builder()
            .name("Use client color for outline")
            .description("Uses the GUI color for outline")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(() -> renderOutline.value)
            .build()
    );
    public RGBASetting outlineStart = sgNameTagData.add(new RGBASetting.Builder()
            .name("Outline Start")
            .description("Start Color of outline")
            .value(ColorUtils.changeAlpha(Color.WHITE, 100))
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 100))
            .onSettingChange(this)
            .rainbow(false)
            .shouldRender(() -> renderOutline.value && !useClientColorsForOutline.value)
            .build()
    );
    public RGBASetting outlineEnd = sgNameTagData.add(new RGBASetting.Builder()
            .name("Outline End")
            .description("End Color of outline")
            .value(ColorUtils.changeAlpha(Color.WHITE, 100))
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 100))
            .onSettingChange(this)
            .rainbow(false)
            .shouldRender(() -> !useClientColorsForOutline.value)
            .build()
    );
    public DoubleSetting radius = sgNameTagData.add(new DoubleSetting.Builder()
            .name("Radius")
            .description("Radius of the rounded rectangle")
            .onSettingChange(this)
            .value(2d)
            .defaultValue(2d)
            .min(0d)
            .max(5d)
            .roundingPlace(1)
            .build()
    );
    public BooleanSetting ping = sgNameTagData.add(new BooleanSetting.Builder()
            .name("Ping")
            .description("Shows player ping")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting gameMode = sgNameTagData.add(new BooleanSetting.Builder()
            .name("Game Mode")
            .description("Shows player gamemode")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting health = sgNameTagData.add(new BooleanSetting.Builder()
            .name("Health")
            .description("Shows entity health")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public CycleSetting healthBarStyle = sgNameTagData.add(new CycleSetting.Builder()
            .name("Heath Style")
            .description("Style of health displayed")
            .value(List.of(HealthBarStyle.values()))
            .defaultListOption(HealthBarStyle.Bar)
            .onSettingChange(this)
            .shouldRender(() -> health.value)
            .build()
    );

    public NameTags() {
        super("NameTags", "Renders a more detailed nametags on entities", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgNameTagData);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgNameTagData.getSettings());

    }

    public static Color getHealthColor(float health) {
        if (health <= 5) {
            return Color.RED;
        } else if (health <= 10) {
            return Color.ORANGE;
        } else if (health <= 15) {
            return Color.YELLOW;
        } else if (health <= 20) {
            return Color.GREEN;
        } else {
            //Dark green
            return new Color(66, 203, 66, 255);
        }
    }

    public static String getHealthColorInString(float health) {
        if (health <= 5) {
            return ColorUtils.red;
        } else if (health <= 15) {
            return ColorUtils.yellow;
        } else
            return ColorUtils.green;
    }

    @SubscribeEvent
    public void entityLabelRenderEvent(EntityLabelRenderEvent event) {
        if (event.getEntity() instanceof FreeCamEntity) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (entity instanceof PlayerEntity && !players.value) return;
        if (entity instanceof MobEntity && !mobs.value) return;

        if (ignoreArmorStand.value && entity instanceof ArmorStandEntity) {
            event.setCanceled(false);
            return;
        }
        if (ignoreInvisible.value && entity.isInvisible()) {
            event.setCanceled(false);
            return;
        }

        event.setCanceled(true);

        boolean isFriend = entity instanceof PlayerEntity && FriendManager.isFriend(new Friend(entity.getName().getString()));

        renderNameTag(entity, (float) (yOffset.value + 0.5f), (isFriend ? ColorUtils.aqua : "") + event.getEntityName().getString() + ColorUtils.white);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.LOW)
    public void itemLabelRenderEvent(ItemPhysicsEvent event) {
        String text = event.item.getName().getString();
        if (displayCount.value) {
            text = text + " x" + event.item.getStack().getCount();
        }
        renderItemNameTag(event.item, (float) (yOffset.value + 0.5f), text);
    }

    public void renderNameTag(LivingEntity entity, float entityYOff, String name) {
        StringBuilder builder = new StringBuilder();
        if (gameMode.value && entity instanceof PlayerEntity p) {
            builder.append(ColorUtils.white).append(getGameMode(p)).append(" ").append(ColorUtils.reset);
        }

        builder.append(name);

        Vec3d entityPos = Renderer3D.getInterpolatedPosition(entity);
        float entityHealth = (float) MathUtils.round(entity.getHealth() + entity.getAbsorptionAmount(), 2);
        float maxHealth = entity.getMaxHealth() + entity.getAbsorptionAmount();
        double distance = MathHelper.clamp(mc.player.distanceTo(entity), 0, 150);
        float adjustedScale = (float) (scale.value);

        boolean showBar = health.value && healthBarStyle.getOption() == HealthBarStyle.Bar;

        if (scaleOnDistance.value) {
            // Do not adjust scale if less than the scale selected by player
            adjustedScale = (float) Math.max((adjustedScale * (distance / 10.0)), scale.value);
        }

        if (health.value && healthBarStyle.getOption() == HealthBarStyle.Number) {
            builder.append(" ").append(getHealthColorInString(entityHealth)).append(entityHealth).append(ColorUtils.white);
        }

        if (ping.value && !(entity instanceof MobEntity)) {
            PlayerListEntry entry = mc.player.networkHandler.getPlayerListEntry(entity.getUuid());
            if (entry != null) {
                builder.append(" ").append(entry.getLatency()).append("ms");
            }
        }
        try {
            float dataHeight = FontRenderers.Large_fxfontRenderer.getStringHeight(builder.toString());
            float dataWidth = FontRenderers.Large_fxfontRenderer.getStringWidth(builder.toString());
            float halfDataWidth = dataWidth / 2.0f;


            //Draws 2D elements in 3d space
            Renderer3D.draw2DIn3D((float) entityPos.x, (float) (entityPos.y + entity.getHeight() + entityYOff), (float) entityPos.z, adjustedScale, stack -> {
                if (background.value) {
                    //Render background
                    Renderer2D.drawRoundedRectangle(stack.peek().getPositionMatrix(), -(halfDataWidth) - 2f, -1f, true, true, !showBar, !showBar, dataWidth + 3.0f, dataHeight + 2f, (float) radius.value, backgroundColor.value.getRGB());
                }
                if (showBar) {
                    float healthBarWidth = (entityHealth / maxHealth) * (dataWidth + 3.0f - (float) radius.value / 5.0f);
                    Renderer2D.drawRectangle(stack.peek().getPositionMatrix(), -(halfDataWidth) - 2f + (float) radius.value / 5.0f, dataHeight + 0.1f, healthBarWidth, 1.2f, getHealthColor(entityHealth).getRGB());
                }
                if (renderOutline.value) {
                    if (useClientColorsForOutline.value) {
                        Renderer2D.drawOutlineGradientRoundedBox(stack.peek().getPositionMatrix(), -(halfDataWidth) - 3f, -2f, dataWidth + 4f, dataHeight + 3f + (showBar ? 1.4f : 0f), (float) radius.value, 1.1f, ColorManager.INSTANCE.getPrimaryGradientStart().darker(), ColorManager.INSTANCE.getPrimaryGradientEnd().darker(), ColorManager.INSTANCE.getPrimaryGradientEnd().darker(), ColorManager.INSTANCE.getPrimaryGradientStart().darker());
                    } else {
                        Renderer2D.drawOutlineGradientRoundedBox(stack.peek().getPositionMatrix(), -(halfDataWidth) - 3f, -2f, dataWidth + 4f, dataHeight + 3f + (showBar ? 1.4f : 0f), (float) radius.value, 1.1f, outlineStart.value, outlineEnd.value, outlineEnd.value, outlineStart.value);
                    }
                }
            });
            int textColor = team.value ? ModuleManager.get(Teams.class).getActualTeamColor(entity) : Color.WHITE.getRGB();

            Renderer3D.drawText(FontRenderers.Large_fxfontRenderer, builder.toString(), (float) entityPos.x, (float) (entityPos.y + entity.getHeight() + entityYOff), (float) entityPos.z, -(dataWidth / 2.0f) - 0.9f, 0, adjustedScale, textColor);
        } catch (NullPointerException ignored) {
        }
    }

    private void renderItemNameTag(ItemEntity entity, float entityYOff, String text) {
        Vec3d entityPos = Renderer3D.getInterpolatedPosition(entity);

        double distance = MathHelper.clamp(mc.player.distanceTo(entity), 0, 150);
        float adjustedScale = (float) (scale.value);
        if (scaleOnDistance.value) {
            adjustedScale = distance > 15 ? (float) (adjustedScale * (distance / 10.0)) : (float) scale.value;
        }

        float dataHeight = FontRenderers.Large_fxfontRenderer.getStringHeight(text);
        float dataWidth = FontRenderers.Large_fxfontRenderer.getStringWidth(text);
        Renderer3D.draw2DIn3D((float) entityPos.x, (float) (entityPos.y + entity.getHeight() + entityYOff), (float) entityPos.z, adjustedScale, stack -> {
            if (background.value) {
                Renderer2D.drawRoundedRectangle(stack.peek().getPositionMatrix(), -(dataWidth / 2.0f) - 2f, -1f, dataWidth + 3.0f, dataHeight + 2f, (float) radius.value, backgroundColor.value.getRGB());
            }
            if (renderOutline.value) {
                if (useClientColorsForOutline.value) {
                    Renderer2D.drawOutlineGradientRoundedBox(stack.peek().getPositionMatrix(), -(dataWidth / 2.0f) - 2.5f, -1.5f, dataWidth + 3.5f, dataHeight + 2.5f, (float) radius.value, 0.5f, ColorManager.INSTANCE.getPrimaryGradientStart().darker(), ColorManager.INSTANCE.getPrimaryGradientEnd().darker(), ColorManager.INSTANCE.getPrimaryGradientEnd().darker(), ColorManager.INSTANCE.getPrimaryGradientStart().darker());
                } else {
                    Renderer2D.drawOutlineGradientRoundedBox(stack.peek().getPositionMatrix(), -(dataWidth / 2.0f) - 2.5f, -1.5f, dataWidth + 3.5f, dataHeight + 2.5f, (float) radius.value, 0.5f, outlineStart.value, outlineEnd.value, outlineEnd.value, outlineStart.value);
                }
            }
        });

        Renderer3D.drawText(FontRenderers.Large_fxfontRenderer, text, (float) entityPos.x, (float) (entityPos.y + entity.getHeight() + entityYOff), (float) entityPos.z, -(dataWidth / 2.0f) - 0.87f, 0, adjustedScale, Color.WHITE.getRGB());
    }

    public String getGameMode(PlayerEntity entity) {
        if (entity.isCreative()) {
            return "[C]";
        } else if (entity.isSpectator()) {
            return "[Sp]";
        } else if (!entity.getAbilities().allowModifyWorld) {
            return "[A]";
        } else {
            return "[S]";
        }
    }

    public enum HealthBarStyle {
        Bar,
        Number
    }
}
