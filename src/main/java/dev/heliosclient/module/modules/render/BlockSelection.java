package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class BlockSelection extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting advanced = sgGeneral.add(new BooleanSetting.Builder()
            .name("Advanced")
            .description("Shows a advanced box rendering on type of block")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting outline = sgGeneral.add(new BooleanSetting.Builder()
            .name("Outline")
            .description("Draw outline of holes")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting outlineWidth = sgGeneral.add(new DoubleSetting.Builder()
            .name("Outline Width")
            .description("Width of the line")
            .min(0.0)
            .max(5.0f)
            .value(1d)
            .defaultValue(1d)
            .roundingPlace(1)
            .onSettingChange(this)
            .shouldRender(() -> outline.value)
            .build()
    );
    BooleanSetting fill = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fill")
            .description("Draw side fill of holes")
            .value(true)
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    RGBASetting fillColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Fill color")
            .description("Color of the Fill")
            .defaultValue(ColorUtils.changeAlpha(Color.WHITE, 125))
            .onSettingChange(this)
            .rainbow(false)
            .build()
    );

    RGBASetting lineColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Line color")
            .description("Color of the line")
            .value(Color.WHITE)
            .defaultValue(Color.WHITE)
            .onSettingChange(this)
            .rainbow(false)
            .build()
    );

    public BlockSelection() {
        super("Block Selection", "Modifies how block selection is rendered", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (!(mc.crosshairTarget instanceof BlockHitResult result) || result.getType() == HitResult.Type.MISS) return;

        renderBlockHitResult(result);
    }

    public void renderBlockHitResult(BlockHitResult result) {
        VoxelShape shape = mc.world.getBlockState(result.getBlockPos()).getOutlineShape(mc.world, result.getBlockPos());

        if (shape.isEmpty()) return;

        if (advanced.value) {
            for (Box b : shape.getBoundingBoxes()) {
                renderSelection(b.offset(result.getBlockPos()).expand(0.0045f));
            }
        } else {
            renderSelection(shape.getBoundingBox().offset(result.getBlockPos()).expand(0.0045f));
        }
    }

    public void renderSelection(Box box) {
        if (outline.value && fill.value) {
            Renderer3D.drawBoxBoth(box, QuadColor.single(fillColor.value.getRGB()), QuadColor.single(lineColor.value.getRGB()), (float) outlineWidth.value);
        } else if (outline.value) {
            Renderer3D.drawBoxOutline(box, QuadColor.single(lineColor.value.getRGB()), (float) outlineWidth.value);
        } else if (fill.value) {
            Renderer3D.drawBoxFill(box, QuadColor.single(fillColor.value.getRGB()));
        }
    }
}