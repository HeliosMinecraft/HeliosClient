package dev.heliosclient.module.settings.lists;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class ParticleListSetting extends ListSetting<ParticleType<?>> {

    public List<ParticleType<?>> value;
    public List<ParticleType<?>> defaultValue;

    public ParticleListSetting(String name,
                               String description,
                               BooleanSupplier shouldRender,
                               List<ParticleType<?>> defaultValue,
                               List<ParticleType<?>> defaultSelectedParticles,
                               Predicate<ParticleType<?>> filter,
                               ISettingChange iSettingChange) {
        super(name, description, shouldRender, defaultValue, defaultSelectedParticles, filter, iSettingChange, Registries.PARTICLE_TYPE);
    }

    @Override
    public String getEntryName(ParticleType<?> e) {
        return Registries.PARTICLE_TYPE.getId(e).toString();
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button, Window window) {
        // Handle mouse clicks on the filtered list
        float offsetY = 0;

        for (ParticleType<?> particleType : getDisplayableEntries()) {
            if (filter.test(particleType)) {
                String text = Registries.PARTICLE_TYPE.getId(particleType).toString();
                if (mouseX >= x && mouseX <= x + window.getWindowWidth() - 5 && mouseY >= y + offsetY && mouseY <= y + offsetY + 10) {
                    // Handle click on the Particles
                    if (selectedEntries.contains(particleType)) {
                        selectedEntries.remove(particleType);
                    } else if (selectedEntries.size() < maxSelectable) {
                        selectedEntries.add(particleType);
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
        for (ParticleType<?> particleType : getDisplayableEntries()) {
            if (filter.test(particleType)) {
                String text = getEntryName(particleType);
                Renderer2D.drawFixedString(drawContext.getMatrices(), text, x + 3, y + offsetY, -1);

                Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + window.getWindowWidth() - 15, y + offsetY, 10, 10, 2, 0.7f, 0xFFFFFFFF);
                Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + window.getWindowWidth() - 13.3f, y + 1.7f + offsetY, 6.3f, 6.3f, 2, selectedEntries.contains(particleType) ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222);


                offsetY += Renderer2D.getFxStringHeight(text) + 5;
            }
        }
        return Math.round(offsetY + Renderer2D.getFxStringHeight() + 6);
    }


    public static class Builder extends Setting.SettingBuilder<Builder, List<ParticleType<?>>, ParticleListSetting> {
        List<ParticleType<?>> defaultSelectedParticles = new ArrayList<>();
        private Predicate<ParticleType<?>> filter = Particles -> true; // default filter: always true
        private ISettingChange iSettingChange;

        public Builder() {
            super(Registries.PARTICLE_TYPE.stream().toList());
        }

        public Builder particles(List<ParticleType<?>> defaultSelectedParticles) {
            this.defaultSelectedParticles = defaultSelectedParticles;
            return this;
        }

        public Builder particles(ParticleType<?>... defaultSelectedParticles) {
            if (defaultSelectedParticles != null)
                Collections.addAll(this.defaultSelectedParticles, defaultSelectedParticles);
            return this;
        }

        public Builder iSettingChange(ISettingChange change) {
            this.iSettingChange = change;
            return this;
        }

        public Builder filter(Predicate<ParticleType<?>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ParticleListSetting build() {
            return new ParticleListSetting(name, description, shouldRender, defaultValue, defaultSelectedParticles, filter, iSettingChange);
        }
    }
}