package dev.heliosclient.util;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Utils for working with GLFW keycodes.
 */
public class KeycodeToString {

    /**
     * Translates GLFW keycode to readable string.
     *
     * @param keyCode Target GLFW keycode integer.
     * @return Translated string.
     */
    public static String translate(Integer keyCode) {
        if(keyCode == -1){
            return "None";
        }

        //Automatic translation. Works for simple keys like numbers or letters.
        String keyName = GLFW.glfwGetKeyName(keyCode, 0);

        //If automatic translation doesn't work convert to manual.
        if (keyName == null) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_RIGHT_SHIFT -> {
                    return "RIGHT_SHIFT";
                }
                case GLFW.GLFW_KEY_RIGHT_ALT -> {
                    return "RIGHT_ALT";
                }
                case GLFW.GLFW_KEY_RIGHT_CONTROL -> {
                    return "RIGHT_CTRL";
                }
                case GLFW.GLFW_KEY_LEFT_SHIFT -> {
                    return "LEFT_SHIFT";
                }
                case GLFW.GLFW_KEY_LEFT_CONTROL -> {
                    return "LEFT_CTRL";
                }
                case GLFW.GLFW_KEY_LEFT_ALT -> {
                    return "LEFT_ALT";
                }
                case GLFW.GLFW_KEY_UP -> {
                    return "ARROW_UP";
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    return "ARROW_DOWN";
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    return "ARROW_LEFT";
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    return "ARROW_RIGHT";
                }
                case GLFW.GLFW_KEY_TAB -> {
                    return "TAB";
                }
                case GLFW.GLFW_KEY_F1 -> {
                    return "F1";
                }
                case GLFW.GLFW_KEY_F2 -> {
                    return "F2";
                }
                case GLFW.GLFW_KEY_F3 -> {
                    return "F3";
                }
                case GLFW.GLFW_KEY_F4 -> {
                    return "F4";
                }
                case GLFW.GLFW_KEY_F5 -> {
                    return "F5";
                }
                case GLFW.GLFW_KEY_F6 -> {
                    return "F6";
                }
                case GLFW.GLFW_KEY_F7 -> {
                    return "F7";
                }
                case GLFW.GLFW_KEY_F8 -> {
                    return "F8";
                }
                case GLFW.GLFW_KEY_F9 -> {
                    return "F9";
                }
                case GLFW.GLFW_KEY_F10 -> {
                    return "F10";
                }
                case GLFW.GLFW_KEY_F11 -> {
                    return "F11";
                }
                case GLFW.GLFW_KEY_F12 -> {
                    return "F12";
                }
                case GLFW.GLFW_KEY_F13 -> {
                    return "F13";
                }
                case GLFW.GLFW_KEY_F14 -> {
                    return "F14";
                }
                case GLFW.GLFW_KEY_F15 -> {
                    return "F15";
                }
                case GLFW.GLFW_KEY_F16 -> {
                    return "F16";
                }
                case GLFW.GLFW_KEY_F17 -> {
                    return "F17";
                }
                case GLFW.GLFW_KEY_F18 -> {
                    return "F18";
                }
                case GLFW.GLFW_KEY_F19 -> {
                    return "F19";
                }
                case GLFW.GLFW_KEY_F20 -> {
                    return "F20";
                }
                case GLFW.GLFW_KEY_F21 -> {
                    return "F21";
                }
                case GLFW.GLFW_KEY_F22 -> {
                    return "F22";
                }
                case GLFW.GLFW_KEY_F23 -> {
                    return "F23";
                }
                case GLFW.GLFW_KEY_F24 -> {
                    return "F24";
                }
                case GLFW.GLFW_KEY_F25 -> {
                    return "F25";
                }
                case GLFW.GLFW_KEY_F -> {
                    return "F2";
                }
                case GLFW.GLFW_KEY_LEFT_SUPER -> {
                    return "LEFT_SUPER";
                }
                case GLFW.GLFW_KEY_RIGHT_SUPER -> {
                    return "RIGHT_SUPER";
                }
                case GLFW.GLFW_KEY_INSERT -> {
                    return "INS";
                }
                case GLFW.GLFW_KEY_HOME -> {
                    return "HOME";
                }
                case GLFW.GLFW_KEY_PAGE_UP -> {
                    return "PAGE_UP";
                }
                case GLFW.GLFW_KEY_PAGE_DOWN -> {
                    return "PAGE_DOWN";
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    return "DELETE";
                }
                case GLFW.GLFW_KEY_END -> {
                    return "END";
                }
                case GLFW.GLFW_KEY_SCROLL_LOCK -> {
                    return "SCROLL_LOCK";
                }
                case GLFW.GLFW_KEY_PRINT_SCREEN -> {
                    return "PRINT_SCREEN";
                }
                case GLFW.GLFW_KEY_PAUSE -> {
                    return "PAUSE";
                }
                case GLFW.GLFW_KEY_ESCAPE -> {
                    return "ESC";
                }
                case GLFW.GLFW_KEY_CAPS_LOCK -> {
                    return "CAPS_LOCK";
                }
                case GLFW.GLFW_KEY_NUM_LOCK -> {
                    return "NUM_LOCK";
                }
                case GLFW_KEY_SPACE -> {
                    return "SPACE";
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    return "ENTER";
                }
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                    return "MOUSE_LEFT";
                }
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    return "MOUSE_RIGHT";
                }
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> {
                    return "MOUSE_MIDDLE";
                }
                default -> {
                    //If none of the translation methods work return in format of KEY_KEYCODE
                    return "KEY_" + keyCode;
                }
            }
        }
        return keyName;
    }

    /**
     * Translates GLFW keycode to readable string. Shortened version for certain uses.
     *
     * @param keyCode Target GLFW keycode integer.
     * @return Translated string.
     */
    public static String translateShort(Integer keyCode) {
        if(keyCode == -1){
            return "Bind";
        }
        String keyName = GLFW.glfwGetKeyName(keyCode, 0);
        if (keyName == null) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_RIGHT_SHIFT -> {
                    return "RS";
                }
                case GLFW.GLFW_KEY_RIGHT_ALT -> {
                    return "RALT";
                }
                case GLFW.GLFW_KEY_RIGHT_CONTROL -> {
                    return "RCTRL";
                }
                case GLFW.GLFW_KEY_LEFT_SHIFT -> {
                    return "LS";
                }
                case GLFW.GLFW_KEY_LEFT_CONTROL -> {
                    return "LCTRL";
                }
                case GLFW.GLFW_KEY_LEFT_ALT -> {
                    return "LALT";
                }
                case GLFW.GLFW_KEY_UP -> {
                    return "UP";
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    return "DOWN";
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    return "LEFT";
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    return "RIGHT";
                }
                case GLFW.GLFW_KEY_TAB -> {
                    return "TAB";
                }
                case GLFW.GLFW_KEY_F1 -> {
                    return "F1";
                }
                case GLFW.GLFW_KEY_F2 -> {
                    return "F2";
                }
                case GLFW.GLFW_KEY_F3 -> {
                    return "F3";
                }
                case GLFW.GLFW_KEY_F4 -> {
                    return "F4";
                }
                case GLFW.GLFW_KEY_F5 -> {
                    return "F5";
                }
                case GLFW.GLFW_KEY_F6 -> {
                    return "F6";
                }
                case GLFW.GLFW_KEY_F7 -> {
                    return "F7";
                }
                case GLFW.GLFW_KEY_F8 -> {
                    return "F8";
                }
                case GLFW.GLFW_KEY_F9 -> {
                    return "F9";
                }
                case GLFW.GLFW_KEY_F10 -> {
                    return "F10";
                }
                case GLFW.GLFW_KEY_F11 -> {
                    return "F11";
                }
                case GLFW.GLFW_KEY_F12 -> {
                    return "F12";
                }
                case GLFW.GLFW_KEY_F13 -> {
                    return "F13";
                }
                case GLFW.GLFW_KEY_F14 -> {
                    return "F14";
                }
                case GLFW.GLFW_KEY_F15 -> {
                    return "F15";
                }
                case GLFW.GLFW_KEY_F16 -> {
                    return "F16";
                }
                case GLFW.GLFW_KEY_F17 -> {
                    return "F17";
                }
                case GLFW.GLFW_KEY_F18 -> {
                    return "F18";
                }
                case GLFW.GLFW_KEY_F19 -> {
                    return "F19";
                }
                case GLFW.GLFW_KEY_F20 -> {
                    return "F20";
                }
                case GLFW.GLFW_KEY_F21 -> {
                    return "F21";
                }
                case GLFW.GLFW_KEY_F22 -> {
                    return "F22";
                }
                case GLFW.GLFW_KEY_F23 -> {
                    return "F23";
                }
                case GLFW.GLFW_KEY_F24 -> {
                    return "F24";
                }
                case GLFW.GLFW_KEY_F25 -> {
                    return "F25";
                }
                case GLFW.GLFW_KEY_F -> {
                    return "F2";
                }
                case GLFW.GLFW_KEY_LEFT_SUPER -> {
                    return "LSUPER";
                }
                case GLFW.GLFW_KEY_RIGHT_SUPER -> {
                    return "RSUPER";
                }
                case GLFW.GLFW_KEY_INSERT -> {
                    return "INS";
                }
                case GLFW.GLFW_KEY_HOME -> {
                    return "HOME";
                }
                case GLFW.GLFW_KEY_PAGE_UP -> {
                    return "P UP";
                }
                case GLFW.GLFW_KEY_PAGE_DOWN -> {
                    return "P DOWN";
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    return "DEL";
                }
                case GLFW.GLFW_KEY_END -> {
                    return "END";
                }
                case GLFW.GLFW_KEY_SCROLL_LOCK -> {
                    return "SCR";
                }
                case GLFW.GLFW_KEY_PRINT_SCREEN -> {
                    return "PRINT";
                }
                case GLFW.GLFW_KEY_PAUSE -> {
                    return "PAUSE";
                }
                case GLFW.GLFW_KEY_ESCAPE -> {
                    return "ESC";
                }
                case GLFW.GLFW_KEY_CAPS_LOCK -> {
                    return "CAPS";
                }
                case GLFW.GLFW_KEY_NUM_LOCK -> {
                    return "NUM";
                }
                case GLFW_KEY_SPACE -> {
                    return "SPACE";
                }
                case GLFW.GLFW_KEY_ENTER -> {
                    return "ENTER";
                }
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                    return "M_LEFT";
                }
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    return "M_RIGHT";
                }
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> {
                    return "M_MIDDLE";
                }
                default -> {
                    return "KEY_" + keyCode;
                }
            }
        }
        return keyName;
    }

    public static int charToGLFWKeycode(char c) {
        // Convert the char to uppercase
        c = Character.toUpperCase(c);

        // Check if the char is a printable alphanumeric character
        if (c >= 'A' && c <= 'Z') {
            // Return the corresponding GLFW keycode
            return GLFW_KEY_A + (c - 'A');
        }
        if (c >= '0' && c <= '9') {
            // Return the corresponding GLFW keycode
            return GLFW_KEY_0 + (c - '0');
        }

        // Check if the char is a printable non-alphanumeric character
        return switch (c) {
            case ' ' -> GLFW_KEY_SPACE;
            case '\'' -> GLFW_KEY_APOSTROPHE;
            case ',' -> GLFW_KEY_COMMA;
            case '-' -> GLFW_KEY_MINUS;
            case '.' -> GLFW_KEY_PERIOD;
            case '/' -> GLFW_KEY_SLASH;
            case ';' -> GLFW_KEY_SEMICOLON;
            case '=' -> GLFW_KEY_EQUAL;
            case '[' -> GLFW_KEY_LEFT_BRACKET;
            case '\\' -> GLFW_KEY_BACKSLASH;
            case ']' -> GLFW_KEY_RIGHT_BRACKET;
            case '`' -> GLFW_KEY_GRAVE_ACCENT;
            default -> GLFW_KEY_UNKNOWN;
        };
    }


}
