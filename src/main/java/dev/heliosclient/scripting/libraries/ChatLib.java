package dev.heliosclient.scripting.libraries;

import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.color.ColorUtils;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class ChatLib extends TwoArgFunction {
    public ChatLib() {}

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("sendHeliosMsg", new sendHeliosMsg());
        library.set("sendMsg", new sendMsg());
        library.set("sendCommandMsg", new sendCommandMsg());
        library.set("sendCommand", new sendCommand());
        library.set("sendPlayerMsg", new sendPlayerMsg());
        library.set("sendScriptMsg", new sendScriptMsg());

        env.set("chatLib", library);

        if (!env.get("package").isnil())
            env.get("package").get("loaded").set("chatLib", library);
        return library;
    }

    static final class sendHeliosMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendHeliosMsg(textString.tojstring());
            return NIL;
        }
    }

    static final class sendMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendMsg(textString.tojstring());
            return NIL;
        }
    }

    static final class sendScriptMsg extends TwoArgFunction {
        public LuaValue call(LuaValue scriptName, LuaValue textString) {
            ChatUtils.sendMsg(ColorUtils.gray + "[" + ColorUtils.darkAqua + scriptName.tojstring() + ColorUtils.gray + "]" + textString.tojstring());
            return NIL;
        }
    }

    static final class sendPlayerMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendPlayerMessage(textString.tojstring());
            return NIL;
        }
    }

    static final class sendCommandMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendCommand(textString.tojstring());
            return NIL;
        }
    }

    static final class sendCommand extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendCommand(textString.tojstring());
            return NIL;
        }
    }

}