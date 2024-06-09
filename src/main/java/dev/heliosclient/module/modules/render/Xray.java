package dev.heliosclient.module.modules.render;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class Xray extends Module_ {
    private final SettingGroup sg = new SettingGroup("General");

    public DoubleSetting alpha = sg.add(new DoubleSetting.Builder()
            .name("Opacity")
            .description("Change the opacity of blocks")
            .value(75.0)
            .defaultValue(75.0)
            .min(0)
            .max(255)
            .roundingPlace(0)
            .onSettingChange(this)
            .build()
    );

    public Xray() {
        super("Xray", "Makes unimportant blocks not render", Categories.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.worldRenderer.reload();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.worldRenderer.reload();
    }

    //Todo: Basic asf. Placeholder logic
    public boolean shouldXray(Block b) {
        return b == Blocks.DIAMOND_ORE ||
                b == Blocks.IRON_ORE ||
                b == Blocks.GOLD_ORE ||
                b == Blocks.EMERALD_ORE ||
                b == Blocks.LAPIS_ORE ||
                b == Blocks.REDSTONE_ORE ||
                b == Blocks.NETHER_QUARTZ_ORE ||
                b == Blocks.ANCIENT_DEBRIS ||
                b == Blocks.NETHER_GOLD_ORE ||
                b == Blocks.COPPER_ORE ||
                b == Blocks.LAVA ||
                b == Blocks.IRON_BLOCK ||
                b == Blocks.GOLD_BLOCK ||
                b == Blocks.DIAMOND_BLOCK ||
                b == Blocks.DEEPSLATE_DIAMOND_ORE ||
                b == Blocks.DEEPSLATE_IRON_ORE ||
                b == Blocks.DEEPSLATE_GOLD_ORE ||
                b == Blocks.DEEPSLATE_EMERALD_ORE ||
                b == Blocks.DEEPSLATE_LAPIS_ORE ||
                b == Blocks.DEEPSLATE_REDSTONE_ORE ||
                b == Blocks.DEEPSLATE_COPPER_ORE ||
                b == Blocks.CHEST ||
                b == Blocks.ENDER_CHEST;
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        if (setting == alpha && isActive()) {
            mc.worldRenderer.reload();
        }
    }
}
