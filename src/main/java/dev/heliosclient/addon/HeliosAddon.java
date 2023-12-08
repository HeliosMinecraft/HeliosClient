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

    public abstract String getMainClassPath();

    public void registerCategories() {
    }

}
