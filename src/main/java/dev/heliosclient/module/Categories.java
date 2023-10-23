package dev.heliosclient.module;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.addon.HeliosAddon;
import dev.heliosclient.managers.CategoryManager;

public class Categories {
    public static final Category MISC = new Category("Misc", "assets/heliosclient/svgIcons/misc.svg");
    public static final Category CHAT = new Category("Chat", "assets/heliosclient/svgIcons/chat.svg");
    public static final Category COMBAT = new Category("Combat", "assets/heliosclient/svgIcons/combat.svg");
    public static final Category MOVEMENT = new Category("Movement", "assets/heliosclient/svgIcons/movement.svg");
    public static final Category PLAYER = new Category("Player", "assets/heliosclient/svgIcons/player.svg");
    public static final Category SEARCH = new Category("Search", "assets/heliosclient/svgIcons/search.svg");
    public static final Category RENDER = new Category("Render", "assets/heliosclient/svgIcons/render.svg");

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
