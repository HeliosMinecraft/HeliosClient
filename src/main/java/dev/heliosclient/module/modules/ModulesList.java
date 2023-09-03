package dev.heliosclient.module.modules;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;

public class ModulesList extends Module_ {
    public ModulesList() {
        super("ModulesList", "Shows enabled modules on side of screen", Category.RENDER);
        this.active.value = true;
        this.showInModulesList.value = false;
    }
}
