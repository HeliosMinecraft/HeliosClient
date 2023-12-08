package dev.heliosclient.module.settings.buttonsetting;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.ui.clickgui.gui.Table;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ButtonSetting extends Setting<Boolean> {
    private final String ButtonCategoryText;
    private final Table table = new Table();


    public ButtonSetting(String ButtonCategoryText, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.heightCompact = 0;
        this.ButtonCategoryText = ButtonCategoryText;
    }

    public void addButton(String buttonText, int rowIndex, int columnIndex, Runnable task) {
        Button button = new Button(buttonText, task, this.x, this.y, this.width, this.height);
        table.addButton(rowIndex, columnIndex, button);
        this.height = table.adjustButtonLayout(this.x, this.y, this.width, false) + 5;
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        Renderer2D.drawFixedString(drawContext.getMatrices(), ButtonCategoryText, (float) HeliosClient.MC.getWindow().getScaledWidth() / 2 - (float) textRenderer.getWidth(ButtonCategoryText) / 2 + 1, y + 2, ColorManager.INSTANCE.defaultTextColor());
        this.height = table.adjustButtonLayout(x, Math.round(y + 4 + Renderer2D.getFxStringHeight(ButtonCategoryText)), this.width, false) + 5;


        for (List<Button> row : table.table) {
            for (Button button : row) {
                if (button != null)
                    button.render(drawContext, mouseX, mouseY, textRenderer);
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        for (List<Button> row : table.table) {
            for (Button button1 : row) {
                if (button1 != null)
                    button1.mouseClicked(mouseX, mouseY);
            }
        }
    }


    public static class Builder extends SettingBuilder<Builder, Boolean, ButtonSetting> {
        public Builder() {
            super(false);
        }

        @Override
        public ButtonSetting build() {
            return new ButtonSetting(name, shouldRender, defaultValue);
        }
    }
}
