package dev.heliosclient.module;

public class Category {
    public String name;
    public char icon;


    public Category(String name) {
        this.name = name;
    }

    public Category(String name, char icon) {
        this.name = name;
        this.icon = icon;
    }
}