package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.blocks.BlockIterator;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

import java.awt.*;

public class LightLevelESP extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .description("Range to check light levels")
            .min(0f)
            .max(50f)
            .value(10d)
            .defaultValue(10d)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );

    public LightLevelESP() {
        super("LightLevelESP", "Shows light levels on surface of blocks", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    int RED_LOW = ColorUtils.changeAlpha(Color.RED,100).getRGB();
    int GREEN_LOW = ColorUtils.changeAlpha(Color.GREEN,100).getRGB();
    int YELLOW_LOW = ColorUtils.changeAlpha(Color.YELLOW,100).getRGB();

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        BlockIterator iterator = new BlockIterator(mc.player, (int) range.value, 4);
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            if (!mc.world.isTopSolid(pos.down(), mc.player) || mc.world.isTopSolid(pos, mc.player)) {
                continue;
            }

            int lightLevel = mc.world.getLightLevel(LightType.BLOCK, pos);

            if (lightLevel < 4) {
                renderTop(pos, RED_LOW); // Red for very dark
            } else if (lightLevel < 8) {
                renderTop(pos, GREEN_LOW); // Orange for moderately dark
            } else if (lightLevel < 12) {
                renderTop(pos, YELLOW_LOW); // Yellow for moderately lit
            }
            //Nothing for well lit
        }
    }

    public void renderTop(BlockPos pos, int color) {
        QuadColor c = QuadColor.single(color);
        Renderer3D.drawBoxFill(new Box(pos).offset(0, 0.005f, 0), c, Direction.UP, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    }
}
