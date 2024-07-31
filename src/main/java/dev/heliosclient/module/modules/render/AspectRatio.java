package dev.heliosclient.module.modules.render;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.lists.BlockListSetting;
import dev.heliosclient.util.BlockUtils;
import net.minecraft.block.Blocks;

public class AspectRatio extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public DoubleSetting aspectRatio = sgGeneral.add(new DoubleSetting.Builder()
            .name("Aspect Ratio")
            .description("Ratio of the aspect :hmm:")
            .min(0.1)
            .max(5.0f)
            .defaultValue(1.6)
            .roundingPlace(2)
            .onSettingChange(this)
            .build()
    );

    public DoubleSetting cameraDepth = sgGeneral.add(new DoubleSetting.Builder()
            .name("Camera Depth")
            .description("Depth of the camera")
            .min(-1f)
            .max(2f)
            .value(0.05d)
            .defaultValue(0.05d)
            .roundingPlace(2)
            .onSettingChange(this)
            .build()
    );

    public AspectRatio() {
        super("AspectRatio","Customise the aspect ratio of minecraft and camera depth!", Categories.RENDER);
        addSettingGroup(sgGeneral);
    }
}
