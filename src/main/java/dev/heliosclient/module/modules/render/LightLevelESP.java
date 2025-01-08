package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.blocks.BlockIterator;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

public class LightLevelESP extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Horizontal Range")
            .description("Horizontal Range to check light levels")
            .min(0f)
            .max(50f)
            .value(10d)
            .defaultValue(10d)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting vRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("Vertical Range")
            .description("Vertical Range to check light levels")
            .min(0f)
            .max(10)
            .value(4d)
            .defaultValue(4d)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );

    public LightLevelESP() {
        super("LightLevelESP", "Shows light levels on surface of blocks", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        BlockIterator iterator = new BlockIterator(mc.player, (int) range.value, vRange.getInt());
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            if (!mc.world.isTopSolid(pos.down(), mc.player) || mc.world.isTopSolid(pos, mc.player)) {
                continue;
            }

            int lightLevel = mc.world.getLightLevel(LightType.BLOCK, pos);

            float ratio = (float) lightLevel/15;
            int red = (int) (255 * (1 - ratio));
            int green = (int) (255 * ratio);
            int color = ColorUtils.rgbaToInt(red,green,0,100);
            renderTop(pos,color);
        }
    }

    public void renderTop(BlockPos pos, int color) {
        QuadColor c = QuadColor.single(color);
        Renderer3D.drawBoxFill(new Box(pos).offset(0, 0.005f, 0), c, Direction.UP, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    }
}
