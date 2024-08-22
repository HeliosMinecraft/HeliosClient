package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.player.FreeCamEntity;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.WireframeEntityRenderer;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;
import java.util.List;

public class ESP extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");
    SettingGroup sgEntities = new SettingGroup("Entities");
    SettingGroup sgColor = new SettingGroup("Color");

    public CycleSetting boxMode = sgGeneral.add(new CycleSetting.Builder()
            .name("BoxMode")
            .description("Mode to draw box / outline of entities")
            .onSettingChange(this)
            .value(List.of(BoxMode.values()))
            .defaultListOption(BoxMode.EntityOutline)
            .build()
    );
    BooleanSetting playerSkeleton = sgGeneral.add(new BooleanSetting.Builder()
            .name("Player skeleton")
            .description("Draws skeleton of players.")
            .value(false)
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(() -> boxMode.getOption() == BoxMode.WireFrame)
            .build()
    );
    BooleanSetting outline = sgGeneral.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline of entities")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(() -> boxMode.getOption() == BoxMode.HitBoxOutline)
            .build()
    );
    BooleanSetting fill = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill of entities")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(() -> boxMode.getOption() == BoxMode.HitBoxOutline)
            .build()
    );
    public BooleanSetting throughWalls = sgGeneral.add(new BooleanSetting.Builder()
            .name("Render entities through walls")
            .value(false)
            .defaultValue(false)
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
    BooleanSetting tracers = sgGeneral.add(new BooleanSetting.Builder()
            .name("Tracers")
            .description("Draw tracers to entities")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting width = sgGeneral.add(new DoubleSetting.Builder()
            .name("Tracer Line Width")
            .description("Width of the tracers drawn")
            .onSettingChange(this)
            .value(1.2d)
            .defaultValue(1.2d)
            .min(0.1f)
            .max(5f)
            .roundingPlace(1)
            .shouldRender(() -> tracers.value)
            .build()
    );


    //Todo: Basic and add gradients.
    BooleanSetting players = sgEntities.add(new BooleanSetting("Players", "Show players", this, true, () -> true, true));
    RGBASetting playerColor = sgColor.add(new RGBASetting("Player Color", "Color of player", Color.BLUE, false, this, () -> players.value));

    BooleanSetting friends = sgEntities.add(new BooleanSetting("Friends", "Show Friends", this, true, () -> true, true));
    RGBASetting friendsColor = sgColor.add(new RGBASetting("Friends Color", "Color of Friends", Color.CYAN, false, this, () -> friends.value));


    BooleanSetting passive = sgEntities.add(new BooleanSetting("Passive", "Show passives", this, false, () -> true, true));
    RGBASetting passiveColor = sgColor.add(new RGBASetting("Passive Color", "Color of Passive", Color.GREEN, false, this, () -> passive.value));

    BooleanSetting hostiles = sgEntities.add(new BooleanSetting("Hostiles", "Show hostiles", this, false, () -> true, true));
    RGBASetting hostilesColor = sgColor.add(new RGBASetting("Hostiles Color", "Color of hostiles", Color.RED, false, this, () -> hostiles.value));

    BooleanSetting items = sgEntities.add(new BooleanSetting("Items", "Show items", this, false, () -> true, true));
    RGBASetting itemsColor = sgColor.add(new RGBASetting("Items Color", "Color of items", Color.WHITE, false, this, () -> items.value));

    BooleanSetting tamed = sgEntities.add(new BooleanSetting("Tamed", "Show tamed", this, false, () -> true, true));
    RGBASetting tamedColor = sgColor.add(new RGBASetting("Tamed Color", "Color of tamed", Color.PINK, false, this, () -> tamed.value));

    BooleanSetting others = sgEntities.add(new BooleanSetting("Other", "Show others", this, false, () -> true, true));
    RGBASetting othersColor = sgColor.add(new RGBASetting("Other Color", "Color of other", Color.MAGENTA, false, this, () -> others.value));


    public ESP() {
        super("ESP", "Draw lines to highlight entities and block entities", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgEntities);
        addSettingGroup(sgColor);
        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgEntities.getSettings());
        addQuickSettings(sgColor.getSettings());
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (mc.world.getEntities() == null) return;

        Renderer3D.renderThroughWalls();
        for (Entity entity : mc.world.getEntities()) {
            if (isBlackListed(entity) || entity == mc.player || entity instanceof FreeCamEntity) {
                continue;
            }
            render(entity);
        }
        Renderer3D.stopRenderingThroughWalls();
    }

    public void render(Entity entity) {
        QuadColor color = QuadColor.single(getColor(entity));
        LineColor lineColor = LineColor.single(getColor(entity));

        if (boxMode.getOption() == BoxMode.HitBoxOutline) {
            if (outline.value && fill.value) {
                Renderer3D.drawBoxBoth(entity.getBoundingBox(), color, (float) width.value);
            } else if (outline.value) {
                Renderer3D.drawBoxOutline(entity.getBoundingBox(), color, (float) width.value);
            } else if (fill.value) {
                Renderer3D.drawBoxFill(entity.getBoundingBox(), color);
            }
        } else if (boxMode.getOption() == BoxMode.WireFrame) {
            boolean shouldDisplaySkeleton = playerSkeleton.value && entity instanceof PlayerEntity;

            WireframeEntityRenderer.render(entity, 1f, color, lineColor, (float) width.value, !shouldDisplaySkeleton, !shouldDisplaySkeleton, shouldDisplaySkeleton);
        }

        if (tracers.value) {
            Renderer3D.drawLine(Renderer3D.getEyeTracer(), entity.getBoundingBox().getCenter(), lineColor, (float) width.value);
        }
    }

    public boolean isBlackListed(Entity entity) {
        return (!(entity instanceof HostileEntity) || !hostiles.value) &&
                (!(entity instanceof PassiveEntity) || !passive.value) &&
                (!(entity instanceof TameableEntity) || !tamed.value) &&
                (!(entity instanceof PlayerEntity) || !players.value) &&
                (!(entity instanceof ItemEntity) || !items.value) &&
                !others.value;
    }

    public boolean isFriend(PlayerEntity p) {
        return FriendManager.isFriend(new Friend(p.getName().getString()));
    }

    public int getColor(Entity entity) {
        if (entity instanceof TameableEntity) {
            return tamedColor.getColor().getRGB();
        }
        if (entity instanceof HostileEntity) {
            return hostilesColor.getColor().getRGB();
        }
        if (entity instanceof PassiveEntity) {
            return passiveColor.getColor().getRGB();
        }
        if (entity instanceof PlayerEntity p) {
            if (team.value) {
                return ColorUtils.changeAlpha(
                        ModuleManager.get(Teams.class).getActualTeamColor(p),
                        playerColor.getColor().getAlpha()
                ).getRGB();
            }

            if (isFriend(p) && friends.value) {
                return friendsColor.getColor().getRGB();
            }
            return playerColor.getColor().getRGB();
        }
        if (entity instanceof ItemEntity) {
            return itemsColor.getColor().getRGB();
        }

        return othersColor.getColor().getRGB();
    }


    public enum BoxMode {
        None,
        EntityOutline,
        HitBoxOutline,
        WireFrame
    }
}
