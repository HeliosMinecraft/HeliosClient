package dev.heliosclient.system;

import dev.heliosclient.ui.clickgui.ConsoleScreen;
import dev.heliosclient.util.ColorUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.awt.*;

public class ConsoleAppender extends AbstractAppender {
    ConsoleScreen screen;
    public ConsoleAppender(ConsoleScreen consoleScreen) {
        super("ConsoleAppender", null, null);
        screen = consoleScreen;
        this.start();
    }

    @Override
    public void append(LogEvent event) {
        if(event.getLevel() == Level.DEBUG || event.getLevel() == Level.OFF || event.getLoggerName().equalsIgnoreCase("FabricLoader/Mixin")) return;

        String message = event.getMessage().getFormattedMessage();
        if (message.contains("Exception")) {
            message = message.split("\n")[0]; // Only get the first line of the exception
        }

        if(event.getLevel() == Level.ERROR){
            message = ColorUtils.red + message + ColorUtils.reset;
        }else if(event.getLevel() == Level.WARN){
            message = ColorUtils.gold  + message + ColorUtils.reset ;
        } else if(event.getLevel() == Level.FATAL){
            message = ColorUtils.darkRed + message + ColorUtils.reset;
        }

        if(event.getLoggerName().equalsIgnoreCase("HeliosClient")){
            message = ColorUtils.yellow + "[HeliosClient] " + ColorUtils.reset + message;
        }
        if(event.getLoggerName().contains("net.minecraft") || event.getLoggerName().contains("com.mojang")){
            message = ColorUtils.darkGreen + "[Minecraft] " + ColorUtils.reset + message;
        }
        if(event.getLoggerName().contains("Fabric")){
            message = ColorUtils.darkAqua + "[Fabric] " + ColorUtils.reset + message;
        }
        if(event.getLoggerName().contains("ModMenu")){
            message = ColorUtils.darkBlue + "[ModMenu] " + ColorUtils.reset + message;
        }
        // Display messages in console
        screen.consoleBox.addLine(message);
    }
}
