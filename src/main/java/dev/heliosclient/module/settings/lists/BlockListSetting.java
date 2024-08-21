package dev.heliosclient.module.settings.lists;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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

public class BlockListSetting extends ListSetting<Block> {
    public List<Block> value;

    public BlockListSetting(String name, String description,
                            BooleanSupplier shouldRender,
                            List<Block> defaultValue,
                            List<Block> defaultSelectedBlocks,
                            Predicate<Block> filter,
                            ISettingChange iSettingChange) {
        super(name, description, shouldRender, defaultValue, defaultSelectedBlocks, filter, iSettingChange, Registries.BLOCK);
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
    public String getEntryName(Block e) {
        return e.getName().getString();
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int button, Window window) {
        // Handle mouse clicks on the filtered list
        int offsetY = 0;
        int offsetX = 0;

        for (Block block : getDisplayableEntries()) {

            if (block.getName().getString().contains("Potted")) continue;

            if (filter.test(block)) {
                if (mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16) {
                    // Handle click on the blocks
                    if (selectedEntries.contains(block)) {
                        selectedEntries.remove(block);
                    } else if (selectedEntries.size() < maxSelectable) {
                        selectedEntries.add(block);
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

        // Render the filtered list of blocks
        int offsetY = 0;
        int offsetX = 0;
        for (Block block : getDisplayableEntries()) {
            //Fuck potted plants
            if (block.getName().getString().contains("Potted")) continue;

            if (filter.test(block)) {
                if (selectedEntries.contains(block) && y + offsetY > 8) {
                    // Render a selection indicator for selected blocks
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX, y + offsetY, 16, 16, Color.GREEN.getRGB());
                }
                if (block.getName().getString().contains("Air")) {
                    FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), block.getName().getString(), x + offsetX + FontRenderers.Small_fxfontRenderer.getStringWidth(block.getName().getString()) / 2.0f, y + offsetY + 3, -1);
                }

                int anyOffset = 0;
                if (mouseX >= x + offsetX && mouseX <= x + 16 + offsetX && mouseY >= y + offsetY && mouseY <= y + offsetY + 16) {
                    anyOffset = -1;
                    drawContext.getMatrices().push();
                    drawContext.getMatrices().translate(0, 0, 160);
                    Tooltip.tooltip.changeText(block.getName().getString());
                    Tooltip.tooltip.render(drawContext, HeliosClient.MC.textRenderer, x + window.getWindowWidth() + 2, y + offsetY + 5);
                    drawContext.getMatrices().pop();
                }

                if (y + offsetY > 8) {
                    drawContext.drawItem(getItemForBlock(block).getDefaultStack(), x + offsetX, y + offsetY + anyOffset);
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

    public Item getItemForBlock(Block b) {
        if (b.equals(Blocks.LAVA)) {
            return Items.LAVA_BUCKET;
        } else if (b.equals(Blocks.WATER)) {
            return Items.WATER_BUCKET;
        } else if (b.equals(Blocks.FIRE) || b.equals(Blocks.SOUL_FIRE)) {
            return Items.FLINT_AND_STEEL;
        }

        return b.asItem();
    }

    private int renderSelected(DrawContext drawContext, int x, int y, int width) {
        if (selectedEntries.isEmpty()) {
            return 0;
        }

        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width - 30, height - 25, 3, Color.BLACK.getRGB());

        // Render the selected list of items
        int offsetY = 0;
        int offsetX = 0;
        int i = 0;
        for (Block block : selectedEntries) {
            if(i > MAX_PREVIEW_COUNT){
                //Show a text displaying the count of remaining entries
                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), "+" + (selectedEntries.size() - i), x + offsetX + 2f , y + offsetY + 4, -1);
                break;
            }

            String blockName = block.getName().getString();
            if (blockName.contains("Air")) {
                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), blockName, x + offsetX + FontRenderers.Small_fxfontRenderer.getStringWidth(blockName) / 2.0f, y + offsetY + 3, -1);
            }

            if (y + offsetY > 8) {
                drawContext.drawItem(getItemForBlock(block).getDefaultStack(), x + 2 + offsetX, y + offsetY);
            }


            if (offsetX > width - 49) {
                offsetY += 16;
                offsetX = 0;
            } else {
                offsetX += 16;
            }

            i++;
        }
        return offsetY + 16;
    }

    public static class Builder extends SettingBuilder<Builder, List<Block>, BlockListSetting> {
        List<Block> defaultSelectedBlocks = new ArrayList<>();
        private Predicate<Block> filter = blocks -> true; // default filter: always true
        private ISettingChange iSettingChange;

        public Builder() {
            super(Registries.BLOCK.stream().toList());
        }

        public Builder blocks(List<Block> defaultSelectedBlocks) {
            this.defaultSelectedBlocks = defaultSelectedBlocks;
            return this;
        }

        public Builder blocks(Block... defaultSelectedBlocks) {
            if (defaultSelectedBlocks != null)
                Collections.addAll(this.defaultSelectedBlocks, defaultSelectedBlocks);
            return this;
        }

        public Builder iSettingChange(ISettingChange change) {
            this.iSettingChange = change;
            return this;
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BlockListSetting build() {
            return new BlockListSetting(name, description, shouldRender, defaultValue, defaultSelectedBlocks, filter, iSettingChange);
        }
    }
}