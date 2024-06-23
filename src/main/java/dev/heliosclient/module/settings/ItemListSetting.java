package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.settings.lists.ItemListSettingsScreen;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class ItemListSetting extends ParentScreenSetting<List<Item>> {
    public List<Item> value;
    public List<Item> defaultValue;

    private final Predicate<Item> filter;
    private final List<Item> selectedItems;
    private final List<Item> defaultSelectedItems = new ArrayList<>();
    private final List<Item> searchRelevantItems = new ArrayList<>();
    public int maxSelectable = Integer.MAX_VALUE;
    public boolean showSelected = true;
    private float nameWidth = 10;
    private String searchTerm = ""; // added search term field


    public ItemListSetting(String name, String description, BooleanSupplier shouldRender, List<Item> defaultValue,List<Item> defaultSelected ,Predicate<Item> filter) {
        super(shouldRender, defaultValue);
        this.value = defaultValue;
        this.name = name;
        this.description = description;
        this.filter = filter;
        this.height = 24;
        this.heightCompact = 24;
        this.selectedItems = defaultSelected;
        this.defaultSelectedItems.addAll(defaultSelected);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 4, -1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(name);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.black.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + nameWidth + 12.4f, y + 4.5f, -1);

        if (showSelected) {
            int height = renderSelected(drawContext, x, y + 24, 224);
            this.height = height + 25;
        } else {
            this.height = 24;
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        Renderer2D.drawFixedString(drawContext.getMatrices(), FontRenderers.fxfontRenderer.trimToWidth(name, moduleWidth), x + 2, y + 4, -1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(FontRenderers.fxfontRenderer.trimToWidth(name, moduleWidth));
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.black.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + nameWidth + 12.4f, y + 4.5f, -1);
    }

    private int renderSelected(DrawContext drawContext, int x, int y, int width) {
        if(selectedItems.isEmpty()){
            return 0;
        }

        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width - 30, height - 25, 3, Color.BLACK.getRGB());

        // Render the selected list of items
        int offsetY = 0;
        int offsetX = 0;
        for (Item item : selectedItems) {
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

    protected boolean hoveredOverEdit(double mouseX, double mouseY) {
        return mouseX >= x + nameWidth + 11 && mouseX <= x + nameWidth + 22 && mouseY >= y + 3 && mouseY <= y + 16;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            selectedItems.clear();
            selectedItems.addAll(defaultSelectedItems);
        }

        if (hoveredOverEdit(mouseX, mouseY)) {
            HeliosClient.MC.setScreen(new ItemListSettingsScreen(this));
        }
    }


    public void handleMouseClick(double mouseX, double mouseY, int windowWidth) {
        // Handle mouse clicks on the filtered list
        int offsetY = 0;
        int offsetX = 0;

        for (Item item : checkForSearchRelevancy() ? searchRelevantItems : value) {
            if (filter.test(item)) {
                if (mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16) {
                    // Handle click on the item
                    if (selectedItems.contains(item)) {
                        selectedItems.remove(item);
                    } else if (selectedItems.size() < maxSelectable) {
                        selectedItems.add(item);
                    }
                }
                if (offsetX > windowWidth - 20) {
                    offsetY += 16;
                    offsetX = 0;
                } else {
                    offsetX += 16;
                }
            }
        }
    }

    public boolean checkForSearchRelevancy() {
        if (!searchTerm.isEmpty()) {
            searchRelevantItems.clear();
            for (Item item : value) {
                if (filter.test(item)) {
                    if (item.getName().getString().trim().toLowerCase().contains(searchTerm.trim().toLowerCase())) {
                        searchRelevantItems.add(item);
                    }
                }
            }
            searchRelevantItems.sort((i1, i2) -> {
                int i1Score = StringUtils.getLevenshteinDistance(i1.getName().getString().trim().toLowerCase(), searchTerm.trim().toLowerCase());
                int i2Score = StringUtils.getLevenshteinDistance(i2.getName().getString().trim().toLowerCase(), searchTerm.trim().toLowerCase());
                return Integer.compare(i1Score, i2Score);
            });
        }
        return !searchTerm.isEmpty();
    }

    public int handleItemRendering(int x, int y, int mouseX, int mouseY, DrawContext drawContext, int windowWidth) {
        this.x = x;
        this.y = y;

        // Render the filtered list of items
        int offsetY = 0;
        int offsetX = 0;
        for (Item item : checkForSearchRelevancy() ? searchRelevantItems : value) {
            if (filter.test(item)) {
                if (selectedItems.contains(item) && y + offsetY > 8) {
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
                    Tooltip.tooltip.render(drawContext, HeliosClient.MC.textRenderer, x + windowWidth + 2, y + offsetY + 5);
                    drawContext.getMatrices().pop();
                }

                if (y + offsetY > 8) {
                    drawContext.drawItem(item.getDefaultStack(), x + offsetX, y + offsetY + anyOffset);
                }


                if (offsetX > windowWidth - 20) {
                    offsetY += 16;
                    offsetX = 0;
                } else {
                    offsetX += 16;
                }
            }
        }
        return offsetY + 32;
    }

    @Override
    public void setValue(List<Item> value) {
        this.value.clear();
        for (Item item : value) {
            if (filter.test(item)) {
                this.value.add(item);
            }
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        for (Item item : selectedItems) {
            objectList.add(Item.getRawId(item));
        }
        return objectList;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP, toml);
        List<Long> tomlSelectedItem = toml.getList(getSaveName());
        selectedItems.clear();
        if (tomlSelectedItem != null) {
            for (Long itemID : tomlSelectedItem) {
                Item item = Item.byRawId(Math.toIntExact(itemID));
                if (item != null) {
                    selectedItems.add(item);
                } else {
                    HeliosClient.LOGGER.error("Item of id {} was not found in setting {}", itemID, this.name);
                }
            }
        }else{
            selectedItems.addAll(defaultSelectedItems);
        }
    }

    public Predicate<Item> getFilter() {
        return filter;
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    @Override
    public List<Item> get() {
        return selectedItems;
    }

    public List<Item> getSelectedItems() {
        return selectedItems;
    }

    public static class Builder extends SettingBuilder<Builder, List<Item>, ItemListSetting> {
        List<Item> defaultSelectedItems = new ArrayList<>();
        private Predicate<Item> filter = item -> true; // default filter: always true

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


        public Builder filter(Predicate<Item> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ItemListSetting build() {
            return new ItemListSetting(name, description, shouldRender, defaultValue,defaultSelectedItems, filter);
        }
    }
}