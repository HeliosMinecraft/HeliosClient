package dev.heliosclient.module;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.addon.HeliosAddon;
import dev.heliosclient.managers.CategoryManager;

public class Categories {
    public static final Category MISC = new Category("Misc", '\uF156');
    public static final Category CHAT = new Category("Chat", '\uF172');
    public static final Category COMBAT = new Category("Combat", '\uEAC4');
    public static final Category MOVEMENT = new Category("Movement", '\uF16A');
    public static final Category PLAYER = new Category("Player", '\uEA08');
    public static final Category SEARCH = new Category("Search", '\uEA17');
    public static final Category RENDER = new Category("Render", '\uF164');

    public static void registerCategories() {
        CategoryManager.register(MISC);
        CategoryManager.register(CHAT);
        CategoryManager.register(MOVEMENT);
        CategoryManager.register(COMBAT);
        CategoryManager.register(PLAYER);
        CategoryManager.register(RENDER);
        CategoryManager.register(SEARCH);

        AddonManager.HELIOS_ADDONS.forEach(HeliosAddon::registerCategories);
    }

}
