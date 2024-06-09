package dev.heliosclient.system;

import dev.heliosclient.ui.clickgui.ConsoleScreen;
import dev.heliosclient.util.ColorUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.Objects;


public class ConsoleAppender extends AbstractAppender {
    ConsoleScreen screen;
    String previousMessage = null;

    public ConsoleAppender(ConsoleScreen consoleScreen) {
        super("ConsoleAppender", null, null);
        screen = consoleScreen;
        this.start();
    }

    /**
     * We need to supply as fewer lines as we can for the multi-input box text list to not cause memory issues.,
     * therefore we are trimming exceptions, and avoiding duplicate messages from being added.
     * The aim of this box is to provide the user with some necessary details of what is going on in the background.
     * Another way would be to open a stream to the "latest.log" file and read it continously.
     */
    @Override
    public void append(LogEvent event) {
        if (event.getLevel() == Level.DEBUG || event.getLevel() == Level.OFF || event.getLoggerName().equalsIgnoreCase("FabricLoader/Mixin"))
            return;

        String message = event.getMessage().getFormattedMessage();
        if (message.contains("Exception") || event.getLevel() == Level.TRACE || message.length() > 200) {
            message = message.split("\n")[0] + ".. Check Log file for more details!"; // Only get the first line, to prevent stack trace spam.
        }

        if (event.getLevel() == Level.ERROR) {
            message = ColorUtils.red + message + ColorUtils.reset;
        } else if (event.getLevel() == Level.WARN) {
            message = ColorUtils.gold + message + ColorUtils.reset;
        } else if (event.getLevel() == Level.FATAL) {
            message = ColorUtils.darkRed + message + ColorUtils.reset;
        }

        if (event.getLoggerName().equalsIgnoreCase("HeliosClient")) {
            message = ColorUtils.yellow + "[HeliosClient] " + ColorUtils.reset + message;
        }
        if (event.getLoggerName().contains("net.minecraft") || event.getLoggerName().contains("com.mojang")) {
            message = ColorUtils.darkGreen + "[Minecraft] " + ColorUtils.reset + message;
        }
        if (event.getLoggerName().contains("Fabric")) {
            message = ColorUtils.darkAqua + "[Fabric] " + ColorUtils.reset + message;
        }
        if (event.getLoggerName().contains("ModMenu")) {
            message = ColorUtils.darkBlue + "[ModMenu] " + ColorUtils.reset + message;
        }

        // Does not log same messages again to prevent console spam.
        if (!Objects.equals(message, previousMessage)) {
            // Display messages in console
            screen.consoleBox.addLine(message);
        }
        previousMessage = message;
    }
}
