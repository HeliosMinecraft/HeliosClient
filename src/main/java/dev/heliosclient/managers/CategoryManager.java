package dev.heliosclient.managers;

import dev.heliosclient.module.Category;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;

import java.util.HashMap;
import java.util.Map;

public class CategoryManager {
    private static final Map<String, Category> categories = new HashMap<>();

    public static void register(String name) {
        categories.put(name, new Category(name));
    }

    public static void register(Category category) {
        categories.put(category.name, category);
    }

    public static Category getCategory(String name) {
        return categories.get(name);
    }

    public static Category getCategory(Category category) {
        return categories.get(category.name);
    }

    public static Map<String, Category> getCategories() {
        return categories;
    }

    public static CategoryPane findCategoryPane(Category category) {
        for (CategoryPane pane : ClickGUIScreen.INSTANCE.categoryPanes) {
            if (pane.category.equals(category)) {
                return pane;
            }
        }
        return null;
    }
}