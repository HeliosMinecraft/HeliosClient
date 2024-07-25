package dev.heliosclient.addon;

import dev.heliosclient.command.Command;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;

public abstract class HeliosAddon {
    /**
     * This field is automatically assigned from fabric.mod.json file of the addon
     */
    public String name;

    /**
     * This field is automatically assigned from fabric.mod.json file of the addon
     */
    public String[] authors;

    public abstract void onInitialize();

    // Register stuff

    /**
     * Use {@link dev.heliosclient.managers.CategoryManager#register(Category)} in here
     */
    public abstract void registerCategories();

    /**
     * Use {@link dev.heliosclient.hud.HudElementList#registerElement(HudElementData)} in here
     */
    public abstract void registerHudElementData();

    /**
     * Use {@link dev.heliosclient.managers.ModuleManager#registerModule(Module_)} in here
     */
    public abstract void registerModules();

    /**
     * Use {@link dev.heliosclient.managers.CommandManager#add(Command)} in here
     */
    public abstract void registerCommand();

}
