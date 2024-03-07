package dev.heliosclient.util.render.textures;

import net.minecraft.util.Identifier;

// https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack
public class Texture extends Identifier {
    public Texture(String path) {
        super("heliosclient", validatePath(path));
    }

    public Texture(Identifier i) {
        super(i.getNamespace(), i.getPath());
    }

    static String validatePath(String path) {
        if (isValid(path)) {
            return path;
        }
        StringBuilder ret = new StringBuilder();
        for (char c : path.toLowerCase().toCharArray()) {
            if (isPathCharacterValid(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }
}