package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.timer.TimerUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ScaffoldCount extends HudElement {
    public static HudElementData<ScaffoldCount> DATA = new HudElementData<>("ScaffoldCount", "Shows block counts in your main hand/scaffold hand", ScaffoldCount::new);
    private static ItemStack SCAFFOLD_STACK = ItemStack.EMPTY;
    private static final TimerUtils timer = new TimerUtils();
    private static boolean isVisible = false;
    private static float animationProgress = 0.0f;
    private static final float ANIMATION_DURATION = 0.5f; // Duration of the pop-in/out animation in seconds
    private static final float DISPLAY_DURATION = 2f; // Duration to display the item before popping out. Currently it is 2s
    private final SettingGroup sgSettings = new SettingGroup("Settings");
    private final DoubleSetting scale = sgSettings.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Change the scale")
            .min(0.3d)
            .max(5d)
            .value(1D)
            .defaultValue(1D)
            .onSettingChange(this)
            .roundingPlace(2)
            .build()
    );
    public ScaffoldCount() {
        super(DATA);
        addSettingGroup(sgSettings);
        setSize(40, 20);
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        if (!SCAFFOLD_STACK.isEmpty()) {
            if (!isVisible) {
                isVisible = true;
                timer.startTimer();
            }

            // Update animation progress
            if(timer.getElapsedTime() < DISPLAY_DURATION) {
                updateProgress(false);
            }

            // Render the element with animation
            if (isVisible) {
                float scale = Math.min(animationProgress, 1.0f);
                Renderer2D.scaleAndPosition(drawContext.getMatrices(), x, y, width, height, scale);
                super.renderElement(drawContext, textRenderer);
                drawContext.drawItem(SCAFFOLD_STACK, x + 2, y + 2);
                Renderer2D.drawString(drawContext.getMatrices(), String.valueOf(SCAFFOLD_STACK.getCount()), x + 20, y + 10 - Renderer2D.getStringHeight() / 2.0f, ColorManager.INSTANCE.hudColor);
                Renderer2D.stopScaling(drawContext.getMatrices());
            }

            this.width = (int) (24 + Renderer2D.getStringWidth(String.valueOf(SCAFFOLD_STACK.getCount())));
        }

        if (isVisible && timer.getElapsedTime() > DISPLAY_DURATION) {
            updateProgress(true);
            if (animationProgress <= 0.0f) {
                SCAFFOLD_STACK = ItemStack.EMPTY;
                isVisible = false;
            }
        }
    }

    public void updateProgress(boolean out) {
        if (out) {
            animationProgress -= 1.0f / (ANIMATION_DURATION * 20);
        } else {
            animationProgress += 1.0f / (ANIMATION_DURATION * 20);
        }

        animationProgress = MathHelper.clamp(animationProgress, 0, scale.getFloat());
    }

    public static void setScaffoldStack(ItemStack stack) {
        SCAFFOLD_STACK = stack;
        timer.restartTimer();
        isVisible = true;
    }
}

