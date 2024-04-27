package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.buttonsetting.Button;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.MultiLineInputBox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ConsoleScreen extends Screen {
    public MultiLineInputBox consoleBox;
    public InputBox enterBox;
    public Button button;

    public ConsoleScreen() {
        super(Text.of("Console"));
        consoleBox = new MultiLineInputBox(50, 50, "Test", Long.MAX_VALUE, MultiLineInputBox.InputMode.ALL);
        consoleBox.doSyntaxHighLighting = false;
        consoleBox.displayLineNos = true;
        consoleBox.autoScroll = true;

        enterBox = new InputBox(33, 13, "", 256, InputBox.InputMode.ALL);
        button = new Button("Enter", () -> {
            if (!enterBox.getValue().isEmpty()) {
                HeliosClient.LOGGER.info(enterBox.getValue());
                enterBox.setText("");
            }
        }, 20, 20, 45, 13);
    }

    @Override
    protected void init() {
        super.init();
        consoleBox.setSize(HeliosClient.MC.getWindow().getScaledWidth() - 40, HeliosClient.MC.getWindow().getScaledHeight() - 55);
        enterBox.setSize(HeliosClient.MC.getWindow().getScaledWidth() - 82, 13);
        button.setX(HeliosClient.MC.getWindow().getScaledWidth() - 58);
        button.setY(HeliosClient.MC.getWindow().getScaledHeight() - 33);
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        consoleBox.setSize(HeliosClient.MC.getWindow().getScaledWidth() - 40, HeliosClient.MC.getWindow().getScaledHeight() - 55);
        enterBox.setSize(HeliosClient.MC.getWindow().getScaledWidth() - 82, 13);
        button.setX(HeliosClient.MC.getWindow().getScaledWidth() - 58);
        button.setY(HeliosClient.MC.getWindow().getScaledHeight() - 33);
        consoleBox.scrollToLast();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        consoleBox.render(context, 20, 15, mouseX, mouseY, textRenderer);
        enterBox.render(context, 20, HeliosClient.MC.getWindow().getScaledHeight() - 33, mouseX, mouseY, textRenderer);
        button.render(context, mouseX, mouseY, textRenderer);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.button.mouseClicked(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(this.enterBox.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)){
            if (!enterBox.getValue().isEmpty()) {
                HeliosClient.LOGGER.info(enterBox.getValue());
                enterBox.setText("");
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        consoleBox.mouseScrolled(verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
