package dev.heliosclient.managers;

import dev.heliosclient.module.Category;

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
}