package dev.heliosclient.module.modules.render;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.lists.BlockListSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.List;

public class Xray extends Module_ {
    private final SettingGroup sg = new SettingGroup("General");
    List<Block> defaultXrayBlocks = List.of(  Blocks.DIAMOND_ORE ,
             Blocks.IRON_ORE ,
             Blocks.GOLD_ORE ,
             Blocks.EMERALD_ORE ,
             Blocks.LAPIS_ORE ,
             Blocks.REDSTONE_ORE ,
             Blocks.NETHER_QUARTZ_ORE ,
             Blocks.ANCIENT_DEBRIS ,
             Blocks.NETHER_GOLD_ORE ,
             Blocks.COPPER_ORE ,
             Blocks.LAVA ,
             Blocks.IRON_BLOCK ,
             Blocks.GOLD_BLOCK ,
             Blocks.DIAMOND_BLOCK ,
             Blocks.DEEPSLATE_DIAMOND_ORE ,
             Blocks.DEEPSLATE_IRON_ORE ,
             Blocks.DEEPSLATE_GOLD_ORE ,
             Blocks.DEEPSLATE_EMERALD_ORE ,
             Blocks.DEEPSLATE_LAPIS_ORE ,
             Blocks.DEEPSLATE_REDSTONE_ORE ,
             Blocks.DEEPSLATE_COPPER_ORE ,
             Blocks.CHEST,
             Blocks.ENDER_CHEST);

    public BlockListSetting xrayBlocks = sg.add(new BlockListSetting.Builder()
            .name("X-ray Blocks")
            .description("Blocks to show")
            .blocks(defaultXrayBlocks)
            .iSettingChange(this)
            .build()
    );

    public Xray() {
        super("Xray", "Makes unimportant blocks not render", Categories.RENDER);

        addSettingGroup(sg);
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


    public boolean shouldXray(Block b) {
        if(!this.isActive()){
            return false;
        }
        if(xrayBlocks.getSelectedEntries().isEmpty()){
            return false;
        }
        return xrayBlocks.getSelectedEntries().contains(b);
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if (setting == xrayBlocks && isActive()) {
            mc.worldRenderer.reload();
        }
    }
}
