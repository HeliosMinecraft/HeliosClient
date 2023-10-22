package dev.heliosclient.module.modules;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;

public class ModulesList extends Module_ {
    public ModulesList() {
        super("ModulesList", "Shows enabled modules on side of screen", Categories.RENDER);
        this.active.value = true;
        this.showInModulesList.value = false;
    }
}
