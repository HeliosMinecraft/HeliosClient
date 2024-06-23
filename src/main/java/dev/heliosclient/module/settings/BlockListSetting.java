package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.settings.lists.BlockListSettingsScreen;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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

public class BlockListSetting extends ParentScreenSetting<List<Block>> {
    private final Predicate<Block> filter;
    private final List<Block> selectedblocks;
    private final List<Block> defaultSelectedBlocks = new ArrayList<>();
    private final List<Block> searchRelevantblocks = new ArrayList<>();
    private float nameWidth = 10;
    private String searchTerm = ""; // added search term field
    public int maxSelectable = Integer.MAX_VALUE;
    public boolean showSelected = true;


    public BlockListSetting(String name, String description, BooleanSupplier shouldRender, List<Block> defaultValue,List<Block> defaultSelectedBlocks, Predicate<Block> filter) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.filter = filter;
        this.height = 24;
        this.heightCompact = 24;
        this.selectedblocks = defaultSelectedBlocks;
        this.defaultSelectedBlocks.addAll(defaultSelectedBlocks);
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

        if (showSelected) {
            int height = renderSelected(drawContext, x, y + 24, 224);
            this.height = height + 25;
        } else {
            this.height = 24;
        }
    }
    private int renderSelected(DrawContext drawContext, int x, int y, int width) {
        if(selectedblocks.isEmpty()){
            return 0;
        }

        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width - 30, height - 25, 3, Color.BLACK.getRGB());

        // Render the selected list of items
        int offsetY = 0;
        int offsetX = 0;
        for (Block block : selectedblocks) {
            if (block == Blocks.AIR) {
                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Air", x + offsetX + 4, y + offsetY + 3, -1);
            }

            if (y + offsetY > 8) {
                drawContext.drawItem(block.asItem().getDefaultStack(), x + 2 + offsetX, y + offsetY);
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
        super.mouseClicked(mouseX,mouseY,button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            selectedblocks.clear();
            selectedblocks.addAll(defaultSelectedBlocks);
        }

        if(hoveredOverEdit(mouseX,mouseY)){
            HeliosClient.MC.setScreen(new BlockListSettingsScreen(this));
        }
    }


    public void handleMouseClick(double mouseX, double mouseY, int windowWidth){
        // Handle mouse clicks on the filtered list
        int offsetY = 0;
        int offsetX = 0;

        for (Block block : checkForSearchRelevancy() ? searchRelevantblocks : value) {
            if (filter.test(block)) {
                if (mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16) {
                    // Handle click on the blocks
                    if (selectedblocks.contains(block)) {
                        selectedblocks.remove(block);
                    } else if(selectedblocks.size() < maxSelectable){
                        selectedblocks.add(block);
                    }
                }
                if(offsetX > windowWidth - 20) {
                    offsetY += 16;
                    offsetX = 0;
                }else {
                    offsetX += 16;
                }
            }
        }
    }
    public boolean checkForSearchRelevancy(){
        if (!searchTerm.isEmpty()) {
            searchRelevantblocks.clear();
            for (Block blocks : defaultValue) {
                if (filter.test(blocks)) {
                    if (blocks.getName().getString().trim().toLowerCase().contains(searchTerm.trim().toLowerCase())) {
                        searchRelevantblocks.add(blocks);
                    }
                }
            }
            searchRelevantblocks.sort((i1, i2) -> {
                int i1Score = StringUtils.getLevenshteinDistance(i1.getName().getString().trim().toLowerCase(), searchTerm.trim().toLowerCase());
                int i2Score = StringUtils.getLevenshteinDistance(i2.getName().getString().trim().toLowerCase(), searchTerm.trim().toLowerCase());
                return Integer.compare(i1Score, i2Score);
            });
        }
        return !searchTerm.isEmpty();
    }

    public int handleBlocksRendering(int x, int y, int mouseX, int mouseY,DrawContext drawContext, int windowWidth){
        this.x = x;
        this.y = y;

        // Render the filtered list of blocks
        int offsetY = 0;
        int offsetX = 0;
        for (Block block : checkForSearchRelevancy() ? searchRelevantblocks : value) {
            if (filter.test(block)) {
                if (selectedblocks.contains(block) && y + offsetY > 8) {
                    // Render a selection indicator for selected blocks
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX, y + offsetY, 16, 16, Color.GREEN.getRGB());
                }
                if(block == Blocks.AIR){
                    FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(),"Air",x + offsetX + 4,y + offsetY + 3,-1);
                }

                int anyOffset = 0;
                if(mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16){
                    anyOffset = -1;
                    drawContext.getMatrices().push();
                    drawContext.getMatrices().translate(0,0,160);
                    Tooltip.tooltip.changeText(block.getName().getString());
                    Tooltip.tooltip.render(drawContext,HeliosClient.MC.textRenderer, x + windowWidth + 2,y + offsetY + 5);
                    drawContext.getMatrices().pop();
                }

                if(y + offsetY > 8) {
                    drawContext.drawItem(block.asItem().getDefaultStack(), x + offsetX, y + offsetY + anyOffset);
                }


                if(offsetX > windowWidth - 20) {
                    offsetY += 16;
                    offsetX = 0;
                }else {
                    offsetX += 16;
                }
            }
        }
        return offsetY + 32;
    }
    @Override
    public void setValue(List<Block> value) {
        this.value.clear();
        for (Block blocks : value) {
            if (filter.test(blocks)) {
                this.value.add(blocks);
            }
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        for(Block block: selectedblocks) {
            objectList.add(Item.getRawId(block.asItem()));
        }
        return objectList;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP, toml);
        List<Long> tomlSelectedblocks = toml.getList(getSaveName());
        selectedblocks.clear();
        if (tomlSelectedblocks!= null) {
            for (Long blocksID : tomlSelectedblocks) {
                Block blocks = Block.getBlockFromItem(Item.byRawId(Math.toIntExact(blocksID)));
                if (blocks!= null) {
                    selectedblocks.add(blocks);
                }else{
                    HeliosClient.LOGGER.error("blocks of id {} was not found in setting {}",blocksID,this.name);
                }
            }
        }else{
            selectedblocks.addAll(defaultSelectedBlocks);
        }
    }

    public Predicate<Block> getFilter() {
        return filter;
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    @Override
    public List<Block> get() {
        return selectedblocks;
    }

    public List<Block> getSelectedBlocks() {
        return selectedblocks;
    }

    public static class Builder extends SettingBuilder<Builder, List<Block>, BlockListSetting> {
        private Predicate<Block> filter = blocks -> true; // default filter: always true
        List<Block> defaultSelectedBlocks = new ArrayList<>();

        public Builder() {
            super(Registries.BLOCK.stream().toList());
        }

        public Builder blocks(List<Block> defaultSelectedBlocks) {
            this.defaultSelectedBlocks = defaultSelectedBlocks;
            return this;
        }
        public Builder blocks(Block... defaultSelectedBlocks) {
            if(defaultSelectedBlocks != null)
                Collections.addAll(this.defaultSelectedBlocks, defaultSelectedBlocks);
            return this;
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BlockListSetting build() {
            return new BlockListSetting(name,description,shouldRender, defaultValue,defaultSelectedBlocks, filter);
        }
    }
}