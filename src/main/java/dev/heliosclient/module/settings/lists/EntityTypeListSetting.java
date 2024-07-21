package dev.heliosclient.module.settings.lists;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class EntityTypeListSetting extends ListSetting<EntityType<?>> {

    public List<EntityType<?>> value;
    public List<EntityType<?>> defaultValue;

    public EntityTypeListSetting(String name,
                                 String description,
                                 BooleanSupplier shouldRender,
                                 List<EntityType<?>> defaultValue,
                                 List<EntityType<?>> defaultSelectedParticles,
                                 Predicate<EntityType<?>> filter,
                                 ISettingChange iSettingChange) {
        super(name, description, shouldRender, defaultValue, defaultSelectedParticles, filter, iSettingChange, Registries.ENTITY_TYPE);
    }

    @Override
    public String getEntryName(EntityType<?> e) {
        return e.getName().getString();
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button, Window window) {
        // Handle mouse clicks on the filtered list
        float offsetY = 0;

        for (EntityType<?> entityType : getDisplayableEntries()) {
            if (filter.test(entityType)) {
                String text = Registries.ENTITY_TYPE.getId(entityType).toString();
                if (mouseX >= x && mouseX <= x + window.getWindowWidth() - 5 && mouseY >= y + offsetY && mouseY <= y + offsetY + 10) {
                    // Handle click on the Particles
                    if (selectedEntries.contains(entityType)) {
                        selectedEntries.remove(entityType);
                    } else if (selectedEntries.size() < maxSelectable) {
                        selectedEntries.add(entityType);
                    }
                    postSettingChange();
                }
                offsetY += Renderer2D.getFxStringHeight(text) + 5;
            }
        }
    }

    @Override
    public int handleRenderingEntries(DrawContext drawContext, int x, int y, int mouseX, int mouseY, Window window) {
        this.x = x;
        this.y = y;

        // Render the filtered list of Particles
        float offsetY = 0;
        for (EntityType<?> entityType : getDisplayableEntries()) {
            if (filter.test(entityType)) {
                String text = getEntryName(entityType);
                Renderer2D.drawFixedString(drawContext.getMatrices(), text, x + 3, y + offsetY, -1);

                Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + window.getWindowWidth() - 15, y + offsetY, 10, 10, 2, 0.7f, 0xFFFFFFFF);
                Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + window.getWindowWidth() - 13.3f, y + 1.7f + offsetY, 6.3f, 6.3f, 2, selectedEntries.contains(entityType) ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222);


                offsetY += Renderer2D.getFxStringHeight(text) + 5;
            }
        }
        return Math.round(offsetY + Renderer2D.getFxStringHeight() + 6);
    }


    public static class Builder extends SettingBuilder<Builder, List<EntityType<?>>, EntityTypeListSetting> {
        List<EntityType<?>> defaultSelectedEntries = new ArrayList<>();
        private Predicate<EntityType<?>> filter = Particles -> true; // default filter: always true
        private ISettingChange iSettingChange;

        public Builder() {
            super(Registries.ENTITY_TYPE.stream().toList());
        }

        public Builder entities(List<EntityType<?>> defaultSelectedEntries) {
            this.defaultSelectedEntries = defaultSelectedEntries;
            return this;
        }

        public Builder entities(EntityType<?>... defaultSelectedEntries) {
            if (defaultSelectedEntries != null)
                Collections.addAll(this.defaultSelectedEntries, defaultSelectedEntries);
            return this;
        }

        public Builder iSettingChange(ISettingChange change) {
            this.iSettingChange = change;
            return this;
        }

        public Builder filter(Predicate<EntityType<?>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public EntityTypeListSetting build() {
            return new EntityTypeListSetting(name, description, shouldRender, defaultValue, defaultSelectedEntries, filter, iSettingChange);
        }
    }
}