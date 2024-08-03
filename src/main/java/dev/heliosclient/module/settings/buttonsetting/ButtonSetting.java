package dev.heliosclient.module.settings.buttonsetting;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.tables.Table;
import dev.heliosclient.ui.clickgui.gui.tables.TableEntry;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ButtonSetting extends Setting<Boolean> {
    private final Table buttonTable = new Table();
    private int hovertimer = 0;


    public ButtonSetting(String name, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.heightCompact = 0;
        this.name = name;
    }

    public void addButton(String buttonText, int rowIndex, int columnIndex, Runnable task) {
        Button button = new Button(buttonText, task, this.x, this.y, this.width, this.height);
        buttonTable.addEntry(rowIndex, columnIndex, button);

        // Adjust the layout after every button has been added
        this.height = (int) buttonTable.adjustTableLayout(this.x, this.y, this.width, false) + 5;
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, (float) HeliosClient.MC.getWindow().getScaledWidth() / 2 - (float) textRenderer.getWidth(name) / 2 + 1, y + 2, ColorManager.INSTANCE.defaultTextColor());
        this.height = (int) buttonTable.adjustTableLayout(x, Math.round(y + 4 + Renderer2D.getFxStringHeight(name)), this.width, false) + 5;


        for (List<TableEntry> row : buttonTable.table) {
            for (TableEntry entry : row) {
                if (entry instanceof Button button) {
                    button.render(drawContext, mouseX, mouseY, textRenderer);
                }
            }
        }

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        for (List<TableEntry> row : buttonTable.table) {
            for (TableEntry entry : row) {
                if (entry instanceof Button button1) {
                    button1.mouseClicked(mouseX, mouseY);
                }
            }
        }
    }

    public void setButtonCategoryText(String name) {
        this.name = name;
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
