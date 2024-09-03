package dev.heliosclient.system;

import dev.heliosclient.ui.clickgui.ConsoleScreen;
import dev.heliosclient.util.ColorUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.List;


public class ConsoleAppender extends AbstractAppender {
    ConsoleScreen screen;
    String previousMessage = null;
    int duplicateCount = 1;

    public ConsoleAppender(ConsoleScreen consoleScreen) {
        super("ConsoleAppender", null, null);
        screen = consoleScreen;
        this.start();
    }

    /**
     * We need to supply as fewer lines as we can for the multi-input box text list to not cause memory issues.,
     * therefore we are trimming exceptions, and avoiding duplicate messages from being added.
     * The aim of this box is to provide the user with some necessary details of what is going on in the background.
     * Another way would be to open a stream to the "latest.log" file and read it continuously.
     */
    @Override
    public void append(LogEvent event) {
        if (event.getLevel() == Level.DEBUG || event.getLevel() == Level.OFF || event.getLoggerName().equalsIgnoreCase("FabricLoader/Mixin"))
            return;

        String message = event.getMessage().getFormattedMessage();
        if (event.getMessage().getThrowable() != null) {
            // Only get the first line.
            message = event.getMessage().getThrowable().getMessage() + "... Check log file for more details!";
        }

        if (event.getLevel() == Level.ERROR) {
            message = ColorUtils.red + message + ColorUtils.reset;
        } else if (event.getLevel() == Level.WARN) {
            message = ColorUtils.gold + message + ColorUtils.reset;
        } else if (event.getLevel() == Level.FATAL) {
            message = ColorUtils.darkRed + message + ColorUtils.reset;
        }

        if (event.getLoggerName().contains("net.minecraft") || event.getLoggerName().contains("com.mojang") || event.getLoggerName().contains("minecraft")) {
            message = ColorUtils.darkGreen + "[Minecraft] " + ColorUtils.reset + message;
        } else if (event.getLoggerName().equalsIgnoreCase("HeliosClient")) {
            message = ColorUtils.yellow + "[HeliosClient] " + ColorUtils.reset + message;
        } else if (event.getLoggerName().contains("Fabric")) {
            message = ColorUtils.darkAqua + "[Fabric] " + ColorUtils.reset + message;
        } else if (event.getLoggerName().contains("ModMenu")) {
            message = ColorUtils.darkBlue + "[ModMenu] " + ColorUtils.reset + message;
        }


        // Does not log same messages again to prevent console spam.
        if (message.equals(previousMessage)) {
            List<String> lines = screen.consoleBox.getlines();

            duplicateCount++;
            if (!lines.isEmpty()) {
                String lastLine = lines.get(lines.size() - 1);
                if (lastLine.contains(" (x")) {
                    lastLine = lastLine.substring(0, lastLine.lastIndexOf(" (x"));
                }
                lines.set(lines.size() - 1, lastLine + " (x" + duplicateCount + ")");
            }
        } else {
            duplicateCount = 1;
            screen.consoleBox.addLine(message);
        }
        previousMessage = message;
    }
}
