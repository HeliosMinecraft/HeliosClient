package dev.heliosclient.addon;

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
    public abstract void registerCategories();

    public abstract void registerHudElementData();

    public abstract void registerModules();
}
