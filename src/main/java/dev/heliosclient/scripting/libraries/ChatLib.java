package dev.heliosclient.scripting.libraries;

import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class ChatLib extends TwoArgFunction {
    public ChatLib() {

    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("sendHeliosMsg", new sendHeliosMsg());
        library.set("sendMsg", new sendMsg());
        library.set("sendCommandMsg", new sendCommandMsg());
        library.set("sendCommand", new sendCommand());
        library.set("sendPlayerMsg", new sendPlayerMsg());
        library.set("sendScriptMsg", new sendScriptMsg());

        env.set("ChatLib", library);
        return library;
    }

    static class sendHeliosMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendHeliosMsg(textString.tojstring());
            return NIL;
        }
    }

    static class sendMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendMsg(textString.tojstring());
            return NIL;
        }
    }

    static class sendScriptMsg extends TwoArgFunction {
        public LuaValue call(LuaValue scriptName, LuaValue textString) {
            ChatUtils.sendMsg(ColorUtils.gray + "[" + ColorUtils.darkAqua + scriptName.tojstring() + ColorUtils.gray + "]" + textString.tojstring());
            return NIL;
        }
    }

    static class sendPlayerMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendPlayerMessage(textString.tojstring());
            return NIL;
        }
    }

    static class sendCommandMsg extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendCommand(textString.tojstring());
            return NIL;
        }
    }

    static class sendCommand extends OneArgFunction {
        public LuaValue call(LuaValue textString) {
            ChatUtils.sendCommand(textString.tojstring());
            return NIL;
        }
    }

}