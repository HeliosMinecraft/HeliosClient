package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.settings.lists.ParticleListSettingsScreen;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class ParticleListSetting extends ParentScreenSetting<List<ParticleType<?>>> {

    public List<ParticleType<?>> value;
    public List<ParticleType<?>> defaultValue;

    private final Predicate<ParticleType<?>> filter;
    private final List<ParticleType<?>> selectedParticles;
    private final List<ParticleType<?>> defaultSelectedParticles = new ArrayList<>();
    private final List<ParticleType<?>> searchRelevantParticles = new ArrayList<>();
    private float nameWidth = 10;
    private String searchTerm = ""; // added search term field
    public int maxSelectable = Integer.MAX_VALUE;

    public ParticleListSetting(String name, String description, BooleanSupplier shouldRender, List<ParticleType<?>> defaultValue, List<ParticleType<?>> defaultSelectedParticles, Predicate<ParticleType<?>> filter) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.filter = filter;
        this.height = 24;
        this.heightCompact = 24;
        this.selectedParticles = defaultSelectedParticles;
        this.defaultSelectedParticles.addAll(defaultSelectedParticles);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        Renderer2D.drawFixedString(drawContext.getMatrices(),name,x + 2, y + 4,-1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(name);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.black.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(),"\uEAF3",x + nameWidth + 12.4f, y + 4.5f,-1);
    }

    protected boolean hoveredOverEdit(double mouseX, double mouseY) {
        return mouseX >= x + nameWidth + 11 && mouseX <= x + nameWidth + 22 && mouseY >= y + 3 && mouseY <= y + 16;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX,mouseY,button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            selectedParticles.clear();
            selectedParticles.addAll(defaultSelectedParticles);
        }

        if(hoveredOverEdit(mouseX,mouseY)){
            HeliosClient.MC.setScreen(new ParticleListSettingsScreen(this));
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

    public void handleMouseClick(double mouseX, double mouseY, int windowWidth){
        // Handle mouse clicks on the filtered list
        float offsetY = 0;

        for (ParticleType<?> particleType : checkForSearchRelevancy() ? searchRelevantParticles : value) {
            if (filter.test(particleType)) {
                String text = Registries.PARTICLE_TYPE.getId(particleType).toString();
                if (mouseX >= x && mouseX <= x + windowWidth - 5 && mouseY >= y + offsetY && mouseY <= y + offsetY + 10) {
                    // Handle click on the Particles
                    if (selectedParticles.contains(particleType)) {
                        selectedParticles.remove(particleType);
                    } else if(selectedParticles.size() < maxSelectable){
                        selectedParticles.add(particleType);
                    }
                }
                offsetY += Renderer2D.getFxStringHeight(text) + 5;
            }
        }
    }
    public boolean checkForSearchRelevancy(){
        if (!searchTerm.isEmpty()) {
            searchRelevantParticles.clear();
            for (ParticleType<?> particleType : value) {
                if (filter.test(particleType)) {
                    if (Registries.PARTICLE_TYPE.getId(particleType).toString().trim().toLowerCase().contains(searchTerm.trim().toLowerCase())) {
                        searchRelevantParticles.add(particleType);
                    }
                }
            }
            searchRelevantParticles.sort((i1, i2) -> {
                int i1Score = StringUtils.getLevenshteinDistance(Registries.PARTICLE_TYPE.getId(i1).toString().trim().toLowerCase(), searchTerm.trim().toLowerCase());
                int i2Score = StringUtils.getLevenshteinDistance(Registries.PARTICLE_TYPE.getId(i2).toString().toLowerCase(), searchTerm.trim().toLowerCase());
                return Integer.compare(i1Score, i2Score);
            });
        }
        return !searchTerm.isEmpty();
    }

    public float handleParticlesRendering(int x, int y, DrawContext drawContext, int windowWidth){
        this.x = x;
        this.y = y;

        // Render the filtered list of Particles
        float offsetY = 0;
        for (ParticleType<?> particleType : checkForSearchRelevancy() ? searchRelevantParticles : value) {
            if (filter.test(particleType)) {
                String text = Registries.PARTICLE_TYPE.getId(particleType).toString();
                Renderer2D.drawFixedString(drawContext.getMatrices(),text,x + 3,y + offsetY,-1);

                Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + windowWidth - 15, y + offsetY, 10, 10, 2, 0.7f, 0xFFFFFFFF);
                Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(),  x + windowWidth - 13.3f, y + 1.7f + offsetY, 6.3f, 6.3f,2, selectedParticles.contains(particleType) ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222);


                offsetY += Renderer2D.getFxStringHeight(text) + 5;
            }
        }
        return offsetY + Renderer2D.getFxStringHeight() + 6;
    }
    @Override
    public void setValue(List<ParticleType<?>> value) {
        this.value.clear();
        for (ParticleType<?> Particles : value) {
            if (filter.test(Particles)) {
                this.value.add(Particles);
            }
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        for(ParticleType<?> particleType: selectedParticles) {
            objectList.add(Registries.PARTICLE_TYPE.getRawId(particleType));
        }
        return objectList;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP, toml);
        List<Long> tomlSelectedParticles = toml.getList(getSaveName());
        selectedParticles.clear();
        if (tomlSelectedParticles!= null) {
            for (Long particleID : tomlSelectedParticles) {
                ParticleType<?> Particles = Registries.PARTICLE_TYPE.getEntry(Math.toIntExact(particleID)).get().value();
                if (Particles!= null) {
                    selectedParticles.add(Particles);
                }else{
                    HeliosClient.LOGGER.error("Particles of id {} was not found in setting {}",particleID,this.name);
                }
            }
        }else{
            selectedParticles.addAll(defaultSelectedParticles);
        }
    }

    public Predicate<ParticleType<?>> getFilter() {
        return filter;
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    @Override
    public List<ParticleType<?>> get() {
        return selectedParticles;
    }

    public List<ParticleType<?>> getSelectedParticles() {
        return selectedParticles;
    }

    public static class Builder extends SettingBuilder<Builder, List<ParticleType<?>>, ParticleListSetting> {
        private Predicate<ParticleType<?>> filter = Particles -> true; // default filter: always true
        List<ParticleType<?>> defaultSelectedParticles = new ArrayList<>();

        public Builder() {
            super(Registries.PARTICLE_TYPE.stream().toList());
        }

        public Builder particles(List<ParticleType<?>> defaultSelectedParticles) {
            this.defaultSelectedParticles = defaultSelectedParticles;
            return this;
        }
        public Builder particles(ParticleType<?>... defaultSelectedParticles) {
            if(defaultSelectedParticles != null)
                Collections.addAll(this.defaultSelectedParticles, defaultSelectedParticles);
            return this;
        }

        public Builder filter(Predicate<ParticleType<?>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ParticleListSetting build() {
            return new ParticleListSetting(name,description,shouldRender, defaultValue,defaultSelectedParticles, filter);
        }
    }
}