package dev.heliosclient.module.settings;


import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.module.settings.buttonsetting.Button;
import dev.heliosclient.module.settings.lists.ListSetting;
import dev.heliosclient.ui.clickgui.gui.tables.Table;
import dev.heliosclient.ui.clickgui.gui.tables.TableEntry;
import dev.heliosclient.ui.clickgui.settings.GradientSettingScreen;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;

public class GradientSetting extends ParentScreenSetting<GradientManager.Gradient> {
    public final Set<String> gradientList;
    protected float nameWidth = 10;
    public Table gradientTable;
    public GradientManager.Gradient value;
    public GradientManager.Gradient defaultValue;

    public GradientSetting(String name, String description, BooleanSupplier shouldRender, GradientManager.Gradient defaultValue, ISettingChange iSettingChange) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.iSettingChange = iSettingChange;
        gradientList = GradientManager.getAllGradientsNames();

        if(defaultValue == null){
            Optional<String> optional = gradientList.stream().findFirst();
            optional.ifPresent(s -> this.value = GradientManager.getGradient(s));
        }
    }
    public void createTable(double width){
        gradientTable = new Table();

        for(String gradientName: gradientList){
            gradientTable.addEntry(new GradientEntry(GradientManager.getGradient(gradientName)), width);
        }
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        //Should fix any null values
        if(value == null){
            Optional<String> optional = gradientList.stream().findFirst();
            optional.ifPresent(s -> this.value = GradientManager.getGradient(s));
        }


        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 4, -1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(name);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.BLACK.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + nameWidth + 12.4f, y + 4.5f, -1);

        //Draw preview
        Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                this.value.getStartGradient(),
                this.value.getEndGradient(),
                this.value.getEndGradient(),
                this.value.getStartGradient(),
                x + width - 50,
                y + 2,
                40,
                15,
                3
        );
    }
    public boolean isLinear2D(){
        return Objects.requireNonNull(GradientManager.getKeyForGradient(this.value)).equalsIgnoreCase("Linear2D");
    }

    public void renderAllGradients(DrawContext context, int mouseX, int mouseY){
        for (List<TableEntry> row : gradientTable.table) {
            for (TableEntry entry : row) {
                if (entry instanceof GradientEntry gE) {
                    boolean isMouseOver = ListSetting.isMouseOver(mouseX,mouseY,(float) gE.x + 3,(float) gE.y,(float) gE.width - 3, 18);

                    float y = (float)  gE.y - (isMouseOver? 1 : 0);

                    Renderer2D.drawRoundedGradientRectangle(context.getMatrices().peek().getPositionMatrix(),
                            gE.gradient.getStartGradient(),
                            gE.gradient.getEndGradient(),
                            gE.gradient.getEndGradient(),
                            gE.gradient.getStartGradient(),
                            (float) gE.x + 3,
                            y,
                            (float) gE.width - 3,
                            18,
                            3
                    );

                    if(this.value == gE.gradient){
                        Renderer2D.drawOutlineRoundedBox(context.getMatrices().peek().getPositionMatrix(),
                                (float)gE.x + 2,
                                y,
                                (float) gE.width - 2,
                                19,
                                3f,
                                1.2f,
                                Color.WHITE.getRGB()
                                );
                    }

                    String nameOfG = GradientManager.getKeyForGradient(gE.gradient);

                    if(nameOfG == null){
                        nameOfG = "Unknown";
                    }else{
                        nameOfG = nameOfG.trim();
                    }

                    Renderer2D.drawCustomString(FontRenderers.Mid_fxfontRenderer,context.getMatrices(),nameOfG,(float) (gE.x + gE.width/2.0f - Renderer2D.getCustomStringWidth(nameOfG,FontRenderers.Mid_fxfontRenderer)/2.0f + 1),y + 9 - Renderer2D.getCustomStringHeight(nameOfG,FontRenderers.Mid_fxfontRenderer)/2.0f,-1);
                }
            }
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        if(value == null){
            Optional<String> optional = gradientList.stream().findFirst();
            optional.ifPresent(s -> this.value = GradientManager.getGradient(s));
        }

        Renderer2D.drawFixedString(drawContext.getMatrices(), FontRenderers.fxfontRenderer.trimToWidth(name, moduleWidth), x + 2, y + 4, -1);

        // Draw a '🖋' button next to the text
        nameWidth = Renderer2D.getFxStringWidth(FontRenderers.fxfontRenderer.trimToWidth(name, moduleWidth));
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, Color.black.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + nameWidth + 11, y + 3, 11, 11, 0.4f, (hoveredOverEdit(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());

        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uEAF3", x + nameWidth + 12.4f, y + 4.5f, -1);

        //Draw preview
        Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                this.value.getStartGradient(),
                this.value.getEndGradient(),
                this.value.getEndGradient(),
                this.value.getStartGradient(),
                x + moduleWidth - 15,
                y + 2,
                12,
                10,
                3
        );
    }

    protected boolean hoveredOverEdit(double mouseX, double mouseY) {
        return mouseX >= x + nameWidth + 11 && mouseX <= x + nameWidth + 22 && mouseY >= y + 3 && mouseY <= y + 16;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            if(defaultValue != null) {
                this.value = defaultValue;
                postSettingChange();
            }
        }

        if (hoveredOverEdit(mouseX, mouseY)) {
            HeliosClient.MC.setScreen(new GradientSettingScreen(this));
        }
    }

    @Override
    public GradientManager.Gradient get() {
        if(value == null){
            Optional<String> optional = gradientList.stream().findFirst();
            optional.ifPresent(s -> this.value = GradientManager.getGradient(s));
        }
        return this.value;
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        return GradientManager.getKeyForGradient(this.value);
    }

    @Override
    public void loadFromFile(Map<String, Object> MAP) {
        super.loadFromFile(MAP);

        Object mapVal = MAP.get(getSaveName());

        if(mapVal instanceof String){
            this.value = GradientManager.getGradient(mapVal.toString());
        }else{
            this.value = defaultValue;
        }
    }

    public static class GradientEntry implements TableEntry{
        public double x,y,width = 50;
        public final GradientManager.Gradient gradient;

        public GradientEntry(GradientManager.Gradient gradient) {
            this.gradient = gradient;
        }

        @Override
        public double getWidth() {
            return width;
        }

        @Override
        public double getHeight() {
            return 25;
        }

        @Override
        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void setWidth(double width) {
            this.width = width;
        }
    }

    public static class Builder extends SettingBuilder<Builder, GradientManager.Gradient, GradientSetting> {
        ISettingChange ISettingChange;

        public Builder() {
            super(null);
        }

        @Override
        public Builder value(GradientManager.Gradient value) {
            return super.defaultValue(value);
        }

        public Builder defaultValue(String gradientName) {
            return super.defaultValue(GradientManager.getGradient(gradientName));
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        @Override
        public GradientSetting build() {
            return new GradientSetting(name, description,shouldRender, defaultValue, ISettingChange);
        }
    }
}
