package dev.heliosclient.module.settings.lists;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class ItemListSetting extends ListSetting<Item> {
    public List<Item> value;
    public List<Item> defaultValue;

    public ItemListSetting(String name, String description, BooleanSupplier shouldRender, List<Item> defaultValue, List<Item> defaultSelected, Predicate<Item> filter, ISettingChange iSettingChange) {
        super(name, description, shouldRender, defaultValue, defaultSelected, filter, iSettingChange, Registries.ITEM);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        if (showSelected) {
            int height = renderSelected(drawContext, x, y + 24, 224);
            this.height = height + 25;
        } else {
            this.height = 24;
        }
    }

    @Override
    public String getEntryName(Item e) {
        return e.getName().getString();
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button, Window window) {
        // Handle mouse clicks on the filtered list
        int offsetY = 0;
        int offsetX = 0;

        for (Item item : getDisplayableEntries()) {
            if (filter.test(item)) {
                if (mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16) {
                    // Handle click on the item
                    if (selectedEntries.contains(item)) {
                        selectedEntries.remove(item);
                    } else if (selectedEntries.size() < maxSelectable) {
                        selectedEntries.add(item);
                    }
                    postSettingChange();
                }
                if (offsetX > window.getWindowWidth() - 20) {
                    offsetY += 16;
                    offsetX = 0;
                } else {
                    offsetX += 16;
                }
            }
        }
    }

    @Override
    public int handleRenderingEntries(DrawContext drawContext, int x, int y, int mouseX, int mouseY, Window window) {
        this.x = x;
        this.y = y;

        // Render the filtered list of items
        int offsetY = 0;
        int offsetX = 0;
        for (Item item : getDisplayableEntries()) {
            if (filter.test(item)) {
                if (selectedEntries.contains(item) && y + offsetY > 8) {
                    // Render a selection indicator for selected items
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX, y + offsetY, 16, 16, Color.GREEN.getRGB());
                }
                if (item == Items.AIR) {
                    FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Air", x + offsetX + 4, y + offsetY + 3, -1);
                }

                int anyOffset = 0;
                if (mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16) {
                    anyOffset = -1;
                    drawContext.getMatrices().push();
                    drawContext.getMatrices().translate(0, 0, 160);
                    Tooltip.tooltip.changeText(item.getName().getString());
                    Tooltip.tooltip.render(drawContext, HeliosClient.MC.textRenderer, x + window.getWindowWidth() + 2, y + offsetY + 5);
                    drawContext.getMatrices().pop();
                }

                if (y + offsetY > 8) {
                    drawContext.drawItem(item.getDefaultStack(), x + offsetX, y + offsetY + anyOffset);
                }


                if (offsetX > window.getWindowWidth() - 20) {
                    offsetY += 16;
                    offsetX = 0;
                } else {
                    offsetX += 16;
                }
            }
        }
        return offsetY + 32;
    }

    private int renderSelected(DrawContext drawContext, int x, int y, int width) {
        if (selectedEntries.isEmpty()) {
            return 0;
        }

        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width - 30, height - 25, 3, Color.BLACK.getRGB());

        // Render the selected list of items
        int offsetY = 0;
        int offsetX = 0;
        for (Item item : selectedEntries) {
            if (item == Items.AIR) {
                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Air", x + offsetX + 4, y + offsetY + 3, -1);
            }

            if (y + offsetY > 8) {
                drawContext.drawItem(item.getDefaultStack(), x + 2 + offsetX, y + offsetY);
            }


            if (offsetX > width - 49) {
                offsetY += 16;
                offsetX = 0;
            } else {
                offsetX += 16;
            }
        }
        return offsetY + 16;
    }

    public static class Builder extends SettingBuilder<Builder, List<Item>, ItemListSetting> {
        List<Item> defaultSelectedItems = new ArrayList<>();
        private Predicate<Item> filter = item -> true; // default filter: always true
        private ISettingChange iSettingChange;

        public Builder() {
            super(Registries.ITEM.stream().toList());
        }

        public Builder items(List<Item> defaultSelectedItems) {
            this.defaultSelectedItems = defaultSelectedItems;
            return this;
        }

        public Builder items(Item... defaultSelectedItems) {
            if (defaultSelectedItems != null)
                Collections.addAll(this.defaultSelectedItems, defaultSelectedItems);
            return this;
        }

        public Builder iSettingChange(ISettingChange change) {
            this.iSettingChange = change;
            return this;
        }


        public Builder filter(Predicate<Item> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ItemListSetting build() {
            return new ItemListSetting(name, description, shouldRender, defaultValue, defaultSelectedItems, filter, iSettingChange);
        }
    }
}