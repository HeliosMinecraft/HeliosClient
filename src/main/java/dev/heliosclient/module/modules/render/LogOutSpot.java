package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.entity.EntityAddedEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.heliosclient.module.modules.render.NameTags.getHealthColorInString;


public class LogOutSpot extends Module_ {
    Set<PlayerListEntry> prevPlayerListEntry = new HashSet<>();
    Set<PlayerEntity> loggedOutPlayers = new HashSet<>();
    List<PlayerEntity> lastPlayerEntities = new ArrayList<>();

    int timer = 0;

    SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting removeOnJoin = sgGeneral.add(new BooleanSetting.Builder()
            .name("Remove on join")
            .description("Removes log-out spots if player joins back")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting logInChat = sgGeneral.add(new BooleanSetting.Builder()
            .name("Log in chat")
            .description("Logs player name and position in chat")
            .value(false)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    public RGBASetting outlineColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Outline Color")
            .description("Color of the outline")
            .rainbow(false)
            .defaultValue(new Color(-1))
            .build()
    );
    public RGBASetting fillColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Fill Color")
            .description("Color of the box fill")
            .rainbow(false)
            .defaultValue(ColorUtils.changeAlpha(new Color(-1), 100))
            .build()
    );

    public LogOutSpot() {
        super("Logout Spot", "Renders log out spots of players", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.world != null) {
            prevPlayerListEntry.addAll(mc.getNetworkHandler().getPlayerList());
            updatePlayers();
        }
        timer = 0;
    }

    private void updatePlayers() {
        lastPlayerEntities.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player) lastPlayerEntities.add(player);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        prevPlayerListEntry.clear();
        loggedOutPlayers.clear();
    }

    @SubscribeEvent
    public void onEntityAdded(EntityAddedEvent e) {
        if (!removeOnJoin.value) return;

        if (e.entity instanceof PlayerEntity) {
            //If the player got removed, only then log in chat.
            if (loggedOutPlayers.removeIf(entity -> entity.getUuid().equals(e.entity.getUuid())) && logInChat.value) {
                ChatUtils.sendHeliosMsg(String.format("Player " + ColorUtils.darkRed + "%s " + ColorUtils.reset + "just " + ColorUtils.green + "joined" + ColorUtils.reset + " at pos %d, %d, %d", e.entity.getName().getString(), (int) e.entity.getPos().x, (int) e.entity.getPos().y, (int) e.entity.getPos().z));
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.getNetworkHandler().getPlayerList().size() != prevPlayerListEntry.size()) {
            prevPlayerListEntry.removeAll(mc.getNetworkHandler().getPlayerList());
            for (PlayerListEntry entry : prevPlayerListEntry) {
                prevPlayerListEntry.removeAll(mc.getNetworkHandler().getPlayerList());

                for (PlayerEntity player : lastPlayerEntities) {
                    if (player.getUuid() == null) continue;
                    if (player.getUuid().equals(entry.getProfile().getId()) && mc.player != player) {
                        loggedOutPlayers.add(player);
                        if (logInChat.value) {
                            ChatUtils.sendHeliosMsg(String.format("Player " + ColorUtils.darkRed + "%s " + ColorUtils.reset + "just " + ColorUtils.red + "left" + ColorUtils.reset + " at pos %d, %d, %d", player.getName().getString(), (int) player.getPos().x, (int) player.getPos().y, (int) player.getPos().z));
                        }
                    }
                }
            }
            prevPlayerListEntry.clear();
            prevPlayerListEntry.addAll(mc.getNetworkHandler().getPlayerList());
            updatePlayers();
        }

        timer++;
        if (timer > 10) {
            timer = 0;
            updatePlayers();
        }
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        Renderer3D.renderThroughWalls();
        for (PlayerEntity player : loggedOutPlayers) {
            Renderer3D.drawBoxBoth(new Box(player.getPos().offset(Direction.UP, 0.500F), player.getPos().offset(Direction.UP, 1.445F)).expand(0.5f), QuadColor.single(fillColor.value.getRGB()), QuadColor.single(outlineColor.value.getRGB()), 1f);
            renderNameTag(player, (float) ModuleManager.get(NameTags.class).yOffset.value + 0.4f, player.getName().getString());
        }
        Renderer3D.stopRenderingThroughWalls();
    }

    public void renderNameTag(PlayerEntity entity, float entityYOff, String text) {
        StringBuilder builder = new StringBuilder(text);
        Vec3d pos = entity.getPos();
        float entityHealth = (float) MathUtils.round(entity.getHealth() + entity.getAbsorptionAmount(), 2);
        builder.append(" ").append(getHealthColorInString(entityHealth)).append(entityHealth).append(ColorUtils.reset);


        float dataHeight = FontRenderers.Large_fxfontRenderer.getStringHeight(builder.toString());
        float dataWidth = FontRenderers.Large_fxfontRenderer.getStringWidth(builder.toString());
        float halfDataWidth = dataWidth / 2.0f;

        //Draws 2D elements in 3d space
        Renderer3D.draw2DIn3D((float) pos.x, (float) (entity.getEyeY() + entityYOff), (float) pos.z, (float) ModuleManager.get(NameTags.class).scale.value, stack -> {
            Renderer2D.drawRoundedRectangle(stack.peek().getPositionMatrix(), -(halfDataWidth) - 2f, -1f, dataWidth + 3.0f, dataHeight + 2f, (float) ModuleManager.get(NameTags.class).radius.value, ModuleManager.get(NameTags.class).backgroundColor.value.getRGB());
        });

        Renderer3D.drawText(FontRenderers.Large_fxfontRenderer, builder.toString(), (float) pos.x, (float) (entity.getEyeY() + entityYOff), (float) pos.z, -(dataWidth / 2.0f) - 0.87f, 0, (float) ModuleManager.get(NameTags.class).scale.value, Color.WHITE.getRGB());
    }

}
