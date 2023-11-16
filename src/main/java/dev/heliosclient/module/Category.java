package dev.heliosclient.module;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.render.Texture;
import net.minecraft.util.Identifier;

public class Category {
    public String name;
    public Texture svgiconSrc;
    public char icon;


    public Category(String name) {
        this.name = name;
    }

    public Category(String name, String svgIconSrc) {
        this.name = name;
        this.svgiconSrc = new Texture(svgIconSrc);

    }
    public Category(String name, Texture iconSrc) {
        this.name = name;
        this.svgiconSrc = iconSrc;
    }
    public Category(String name, char icon) {
        this.name = name;
        this.icon = icon;
    }
}