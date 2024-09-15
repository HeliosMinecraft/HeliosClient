package dev.heliosclient.module.settings.lists;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.ParentScreenSetting;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.ui.clickgui.settings.ListSettingsScreen;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.misc.MapReader;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registry;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public abstract class ListSetting<T extends Object> extends ParentScreenSetting<List<T>> {
    protected final Predicate<T> filter;
    protected List<T> selectedEntries;
    protected final List<T> defaultSelectedEntries = new ArrayList<>();
    protected final List<T> searchRelevantEntries = new ArrayList<>();
    public List<T> value;
    public List<T> defaultValue;
    public int maxSelectable;
    public boolean showSelected = true;
    public ISettingChange iSettingChange;
    protected float nameWidth = 10;
    protected String searchTerm = ""; // added search term field
    protected Registry<T> registry;
    protected final static int MAX_PREVIEW_COUNT = 70;

    public ListSetting(String name, String description, BooleanSupplier shouldRender, List<T> defaultValue, List<T> defaultSelected, Predicate<T> filter, ISettingChange iSettingChange, Registry<T> registry) {
        super(shouldRender, defaultValue);
        this.value = new ArrayList<>();
        this.filter = filter;
        this.name = name;
        this.description = description;
        this.height = 24;
        this.heightCompact = 20;
        this.maxSelectable = defaultValue.size();
        this.selectedEntries = new ArrayList<>(maxSelectable);
        this.selectedEntries.addAll(defaultSelected);
        this.defaultSelectedEntries.addAll(defaultSelected);
        this.registry = registry;
        this.iSettingChange = iSettingChange;

        for(T entry : defaultValue){
            if(filter.test(entry)){
                value.add(entry);
            }
        }
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 4, -1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(name);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.BLACK.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + nameWidth + 12.4f, y + 4.5f, -1);
    }

    public void renderSelectAllBox(DrawContext drawContext, int x, int y) {
        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x, y, 10, 10, 2f, 0.7f, 0xFFFFFFFF);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x  + 1.5f, y + 1.7f, 6.5f, 6.5f, 2f, new HashSet<>(selectedEntries).containsAll(value) ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222);
    }

    public void handleMouseClickOnSelectAllBox(double mouseX, double mouseY, int boxX, int boxY){
        if(isMouseOver(mouseX,mouseY,boxX,boxY,10,10)){
            if(new HashSet<>(selectedEntries).containsAll(value)){
                selectedEntries.clear();
            }else if(selectedEntries.size() < maxSelectable){
                selectedEntries.clear();
                selectedEntries.addAll(value);
            }
        }
    }

    public static boolean isMouseOver(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }


    public abstract String getEntryName(T e);

    public abstract void handleMouseClick(double mouseX, double mouseY, int button, Window window);

    public abstract int handleRenderingEntries(DrawContext drawContext, int x, int y, int mouseX, int mouseY, Window window);

    public List<T> getDisplayableEntries() {
        return checkForSearchRelevancy() ? searchRelevantEntries : value;
    }

    public boolean checkForSearchRelevancy() {
        if (!searchTerm.isEmpty()) {
            searchRelevantEntries.clear();
            for (T entry : value) {
                if (filter.test(entry)) {
                    if (getEntryName(entry).trim().toLowerCase().contains(searchTerm.trim().toLowerCase())) {
                        searchRelevantEntries.add(entry);
                    }
                }
            }
            searchRelevantEntries.sort((i1, i2) -> {
                int i1Score = StringUtils.getLevenshteinDistance(getEntryName(i1).trim().toLowerCase(), searchTerm.trim().toLowerCase());
                int i2Score = StringUtils.getLevenshteinDistance(getEntryName(i2).trim().toLowerCase(), searchTerm.trim().toLowerCase());
                return Integer.compare(i1Score, i2Score);
            });
        }
        return !searchTerm.isEmpty();
    }


    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);

        Renderer2D.drawFixedString(drawContext.getMatrices(), FontRenderers.fxfontRenderer.trimToWidth(name, moduleWidth), x + 2, y + 4, -1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(FontRenderers.fxfontRenderer.trimToWidth(name, moduleWidth));
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.black.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + nameWidth + 12.4f, y + 4.5f, -1);
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
            selectedEntries.clear();
            selectedEntries.addAll(defaultSelectedEntries);
            postSettingChange();
        }

        if (hoveredOverEdit(mouseX, mouseY)) {
            HeliosClient.MC.setScreen(new ListSettingsScreen(this));
        }
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    @Override
    public List<T> get() {
        return selectedEntries;
    }

    public List<T> getSelectedEntries() {
        return selectedEntries;
    }

    @Override
    public void setValue(List<T> value) {
        this.value.clear();
        for (T entry : value) {
            if (filter.test(entry)) {
                this.value.add(entry);
            }
        }
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        if(selectedEntries.size() == value.size()){
            return "all";
        }

        for (T entry : selectedEntries) {
            objectList.add(registry.getRawId(entry));
        }
        return objectList;
    }

    @Override
    public void loadFromFile(MapReader map) {
        if(!map.has(getSaveName())) {
            selectedEntries.clear();
            selectedEntries.addAll(defaultSelectedEntries);
            return;
        }

        if(map.getString(getSaveName(),"null").equalsIgnoreCase("all")){
            selectedEntries.clear();
            selectedEntries.addAll(value);
            return;
        }

        List<Object> tomlSelectedItem = (List<Object>) map.getAs(getSaveName(),List.class);

        selectedEntries.clear();
        if (tomlSelectedItem != null) {
            for (Object objectEntryID : tomlSelectedItem) {
                int entryID = MathUtils.o2iSafe(objectEntryID);
                T retrievedEntry = registry.getEntry(entryID).get().value();
                if (retrievedEntry != null) {
                    selectedEntries.add(retrievedEntry);
                } else {
                    HeliosClient.LOGGER.error("Entry of id {} was not found in setting {}", entryID, this.name);
                }
            }
        } else {
            selectedEntries.addAll(defaultSelectedEntries);
        }

    }
}