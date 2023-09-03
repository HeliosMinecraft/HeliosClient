package dev.heliosclient.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    PLAYER("Player"),
    CHAT("Chat"),
    MISC("Misc"),
    SEARCH("Search");


    public String name;

    Category(String name) {
        this.name = name;
    }
}
