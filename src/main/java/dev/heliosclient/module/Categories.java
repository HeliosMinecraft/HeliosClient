package dev.heliosclient.module;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.addon.HeliosAddon;
import dev.heliosclient.managers.CategoryManager;

public class Categories {
    public static final Category MISC = new Category("Misc");
    public static final Category CHAT = new Category("Chat");
    public static final Category COMBAT = new Category("Combat");
    public static final Category MOVEMENT = new Category("Movement");
    public static final Category PLAYER = new Category("Player");
    public static final Category SEARCH = new Category("Search");
    public static final Category RENDER = new Category("Render");

    public static void registerCategories() {
        CategoryManager.register(MISC);
        CategoryManager.register(CHAT);
        CategoryManager.register(MOVEMENT);
        CategoryManager.register(COMBAT);
        CategoryManager.register(PLAYER);
        CategoryManager.register(RENDER);
        CategoryManager.register(SEARCH);

        AddonManager.addons.forEach(HeliosAddon::registerCategories);
    }

}
