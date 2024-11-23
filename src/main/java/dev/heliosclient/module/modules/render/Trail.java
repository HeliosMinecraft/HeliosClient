package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.MovementUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.LineColor;
import dev.heliosclient.util.timer.TickTimer;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.LinkedList;

public class Trail extends Module_ {

    final LinkedList<Vec3d> lines = new LinkedList<>();
    TickTimer ticksToAdd = new TickTimer();
    TickTimer ticksToRemove = new TickTimer();
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting whenStationary = sgGeneral.add(new BooleanSetting.Builder()
            .name("When stationary")
            .description("Create trails even when you are not moving")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting performance = sgGeneral.add(new BooleanSetting.Builder()
            .name("Performance")
            .description("Does not render lines outside the render distance for performance (Turn off never remove for a more visually better solution)")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting neverRemove = sgGeneral.add(new BooleanSetting.Builder()
            .name("Never remove")
            .description("Never removes the trail")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting ticksExisted = sgGeneral.add(new DoubleSetting.Builder()
            .name("Ticks Existed")
            .description("Removes a trail line after given ticks, -1 for never")
            .onSettingChange(this)
            .min(0)
            .max(450)
            .roundingPlace(0)
            .value(20.0)
            .defaultValue(20.0)
            .shouldRender(() -> !neverRemove.value)
            .build()
    );
    DoubleSetting ticksPerLine = sgGeneral.add(new DoubleSetting.Builder()
            .name("Ticks per line")
            .description("Creates a new trail line after given ticks")
            .onSettingChange(this)
            .min(0.0)
            .max(120)
            .roundingPlace(0)
            .value(10.0)
            .defaultValue(10.0)
            .build()
    );
    DoubleSetting width = sgGeneral.add(new DoubleSetting.Builder()
            .name("Line Width")
            .description("Width of the line drawn")
            .onSettingChange(this)
            .value(1.0d)
            .defaultValue(1.0)
            .min(0.1f)
            .max(5f)
            .roundingPlace(1)
            .build()
    );
    RGBASetting lineColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Line Color")
            .description("Color of the line drawn")
            .onSettingChange(this)
            .value(Color.RED)
            .rainbow(false)
            .build()
    );
    BooleanSetting clearOnDisable = sgGeneral.add(new BooleanSetting.Builder()
            .name("Clear on disable")
            .description("Removes all trails when module is disabled")
            .value(true)
            .onSettingChange(this)
            .build()
    );

    public Trail() {
        super("Trail", "Draws a trail at your feet while you move", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @Override
    public void onEnable() {
        super.onEnable();
        ticksToAdd.restartTimer();
        ticksToRemove.restartTimer();
        if (mc.player != null)
            lines.push(mc.player.getPos());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ticksToAdd.resetTimer();
        ticksToRemove.resetTimer();
        if (clearOnDisable.value)
            lines.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (mc.player == null) return;

        if (!neverRemove.value) {
            ticksToRemove.incrementAndEvery(ticksExisted.getInt(),()->{
                if (!lines.isEmpty()) {
                    lines.removeLast();
                }
            });
        }

        if (!whenStationary.value && !MovementUtils.isMoving(mc.player))
            return;

        ticksToAdd.incrementAndEvery(ticksPerLine.getInt(),()->{
            lines.addFirst(mc.player.getPos());
        });
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        Vec3d prevLine = null;
        for (Vec3d currentLine : lines) {
            if (prevLine != null && currentLine != null) {
                if (performance.value && !currentLine.isWithinRangeOf(mc.player.getPos(), mc.options.getViewDistance().getValue() * 16, 160))
                    return;

                Renderer3D.drawLine(prevLine, currentLine, LineColor.single(lineColor.getColor().getRGB()), (float) width.value);
            }
            prevLine = currentLine;
        }
    }
}
