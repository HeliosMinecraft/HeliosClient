package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.Category;
import dev.heliosclient.util.MathUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Map;

public class ClickGUIScreen extends Screen {
    public static ClickGUIScreen INSTANCE = new ClickGUIScreen();
    public ArrayList<CategoryPane> categoryPanes;

    public ClickGUIScreen() {
        super(Text.literal("ClickGUI"));
        categoryPanes = new ArrayList<CategoryPane>();
        Map<String, Object> panePos = ((Map<String, Object>) HeliosClient.CONFIG.config.get("panes"));
        for (Category category : Category.values()) {
            int xOffset = MathUtils.d2iSafe(((Map<String, Object>) panePos.get(category.name)).get("x"));
            int yOffset = MathUtils.d2iSafe(((Map<String, Object>) panePos.get(category.name)).get("y"));
            boolean collapsed = (boolean) ((Map<String, Object>) panePos.get(category.name)).get("collapsed");
            categoryPanes.add(new CategoryPane(category, xOffset, yOffset, collapsed,this));
        }
    }

    public static void onScroll(double horizontal, double vertical) {
        for (CategoryPane category : ClickGUIScreen.INSTANCE.categoryPanes) {
            category.y += vertical * 10;
            category.x += horizontal * 10;
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        for (CategoryPane category : categoryPanes) {
            category.render(drawContext, mouseX, mouseY, delta, textRenderer);
        }

        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPane category : categoryPanes) {
            category.mouseClicked((int) mouseX, (int) mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPane category : categoryPanes) {
            category.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

