package dev.heliosclient.system;

import dev.heliosclient.ui.clickgui.ConsoleScreen;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class ConsoleAppender extends AbstractAppender {
    public ConsoleAppender(ConsoleScreen consoleScreen) {
        super("ConsoleAppender", null, null);
    }

    @Override
    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        if (message.contains("Exception")) {
            message = message.split("\n")[0]; // Only get the first line of the exception
        }
        // Display messages in console
        //consoleScreen.addMessage(message);
    }
}
