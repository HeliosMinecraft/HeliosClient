package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISaveAndLoad;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class Setting<T> implements Listener, ISaveAndLoad {
    public final T defaultValue;
    public String name;
    public String description;
    public T value;
    public boolean quickSettings = false;
    public int heightCompact = 18;
    public ISettingChange iSettingChange;
    protected int height = 24;
    protected int width = 192;
    protected int widthCompact = CategoryPane.getWidth();
    protected boolean animationDone = false;
    protected int moduleWidth = CategoryPane.getWidth();
    protected int x = 0;
    protected int y = 0;
    protected BooleanSupplier shouldRender; // Default to true
    protected int hovertimer = 0;
    protected int hoverAnimationTimer = 0;
    protected boolean shouldSaveOrLoad = true;
    private float animationProgress = 0;
    private float animationSpeed = 0.13f;
    private float targetY;
    private Consumer<RenderContext> alsoRenderLogic = (renderContext) -> {
    };


    public Setting(BooleanSupplier shouldRender, T defaultValue) {
        this.shouldRender = shouldRender;
        this.defaultValue = defaultValue;
        EventManager.register(this);
    }

    /**
     * Updates the animation progress towards the target Y position.
     *
     * @param targetY The target Y position.
     */
    public void update(float targetY) {
        // If the animation is not done yet
        if (!animationDone) {
            // If this is the first update, set the initial position above the target
            if (animationProgress == 0) {
                y = (int) (targetY);
            }

            // Set the target Y position
            this.targetY = targetY;
            // Increase the animation progress by the speed of the animation
            animationProgress += animationSpeed;
            // Ensure the animation progress does not exceed 1
            animationProgress = Math.min(animationProgress, 1);

            // Calculate the eased progress using a linear in-out easing function
            float easedProgress = Easing.ease(EasingType.QUADRATIC_IN_OUT, animationProgress);
            // Update the current Y position by interpolating between the current and target Y positions
            y = Math.round(AnimationUtils.lerp(y, this.targetY, easedProgress));
            // If the animation progress has reached or exceeded 1, mark the animation as done
            if (animationProgress >= 1) {
                animationDone = true;
            }
        }
    }

    /**
     * Resets the animation progress towards the target Y position in reverse.
     *
     * @param targetY The target Y position.
     */
    public void reset(float targetY) {
        // If the animation is not done yet
        if (!animationDone) {
            // If this is the first update, set the initial position above the target
            if (animationProgress == 0) {
                y = (int) (targetY);
            }

            // Set the target Y position
            this.targetY = targetY;
            // Increase the animation progress by the speed of the animation
            animationProgress += animationSpeed;
            // Ensure the animation progress does not exceed 1
            animationProgress = Math.min(animationProgress, 1);

            // Calculate the eased progress using a linear in-out easing function
            float easedProgress = Easing.ease(EasingType.LINEAR_IN_OUT, animationProgress);
            // Update the current Y position by interpolating between the current and target Y positions in reverse
            y = Math.round(AnimationUtils.lerp(y, this.targetY, -easedProgress));
            // If the animation progress has reached or exceeded 1, mark the animation as done
            if (animationProgress >= 1) {
                animationDone = true;
            }
        }
    }


    /**
     * Renders setting in GUI.
     *
     * @param drawContext
     * @param x
     * @param y
     * @param mouseX
     * @param mouseY
     * @param textRenderer
     */
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
        if (hoveredSetting(mouseX, mouseY)) {
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 195, y + (float) this.height / 2 - 5.5f, 11, 11, Color.black.getRGB());
            Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 195, y + (float) this.height / 2 - 5.5f, 11, 11, 0.4f, (hoveredOverReset(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());


            FontRenderers.Small_iconRenderer.drawString(drawContext.getMatrices(), "\uEA1D", (x + 203.5f - FontRenderers.Small_iconRenderer.getStringWidth("\uEA1D")), (y + (float) height / 2 - 3.4f), -1);
        }
        alsoRender(new RenderContext(drawContext, x, y, mouseX, mouseY, textRenderer));
    }

    /**
     * Rendering call along with the main render method.
     * Added so that you can render other stuff along with only specific settings.
     *
     * @param renderContext {@link RenderContext} object to get all the necessary arguments.
     */
    protected void alsoRender(RenderContext renderContext) {
        alsoRenderLogic.accept(renderContext);
    }

    /**
     * Rendering call along with the main render method.
     * Added so that you can render other stuff along with the settings.
     * Handles all the scrolling and no-render automatic
     *
     * @param alsoRenderLogic Consumer object to receive the arguments and render.
     */
    public void alsoRender(Consumer<RenderContext> alsoRenderLogic) {
        this.alsoRenderLogic = alsoRenderLogic;
    }

    public boolean hoveredOverReset(double mouseX, double mouseY) {
        return mouseX >= x + 195 && mouseX <= x + 206 && mouseY >= y + (double) this.height / 2 - 5.5f && mouseY <= y + (double) this.height / 2 + 5.5f;
    }

    /**
     * Compact rendering for clickGUI.
     */
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer + 1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer - 1, 0);
        }
    }

    /**
     * If setting should render, Meant to be used for overrides for settings that are meant to be shown only if condition is met.
     *
     * @return Whether to render or not.
     */
    public boolean shouldRender() {
        return shouldRender.getAsBoolean();
    }

    /**
     * Called every time mouse gets clicked.
     *
     * @param mouseX X coordinate of mouse.
     * @param mouseY Y coordinate of mouse.
     * @param button Which button is being clicked.
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (getAnimationProgress() <= 1) {
        }
    }

    public Setting<?> setShouldSaveOrLoad(boolean shouldSaveOrLoad) {
        this.shouldSaveOrLoad = shouldSaveOrLoad;
        return this;
    }

    /**
     * Called every time mouse gets released.
     *
     * @param mouseX X coordinate of mouse.
     * @param mouseY Y coordinate of mouse,
     * @param button Which button got released.
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    /**
     * Called every time key gets pressed.
     *
     * @param keyCode   GLFW keycode of key that got pressed.
     * @param scanCode  Scan code.
     * @param modifiers Modifiers eg. Ctrl, Shift.
     */
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    /**
     * Gets called every time mouse gets dragged.
     *
     * @param mouseX
     * @param mouseY
     * @param button
     * @param deltaX
     * @param deltaY
     */
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }

    /**
     * Gets called every time key gets released.
     *
     * @param keyCode   GLFW keycode of key that got pressed.
     * @param scanCode  Scan code.
     * @param modifiers Modifiers eg. Ctrl, Shift.
     */
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
    }

    /**
     * Gets called every time char gets typed.
     *
     * @param chr       Char in question.
     * @param modifiers Modifiers eg. Ctrl, Shift.
     */
    public void charTyped(char chr, int modifiers) {
    }

    /**
     * Returns the value
     *
     * @return value selected
     */
    public T get() {
        return value;
    }

    /**
     * Boolean that tells if mouse is hovering this setting.
     *
     * @param mouseX Current mouseX.
     * @param mouseY Current mouseY.
     * @return If mouse is hovering.
     */
    protected boolean hovered(int mouseX, int mouseY) {
        //Different hit-boxes for quick and regular settings.
        if (quickSettings) {
            return mouseX > x && mouseX < x + widthCompact && mouseY > y && mouseY < y + heightCompact;
        } else {
            return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
        }
    }

    protected boolean hoveredSetting(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width + 40 && mouseY > y && mouseY < y + height;
    }

    public boolean isAnimationDone() {
        return animationDone;
    }

    public void setAnimationDone(boolean animationDone) {
        this.animationDone = animationDone;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void setAnimationProgress(float animationProgress) {
        this.animationProgress = animationProgress;
    }

    public boolean shouldSaveAndLoad() {
        return shouldSaveOrLoad;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        return objectList;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        if (!shouldSaveOrLoad) {
        }
    }

    public int getWidthCompact() {
        return widthCompact;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSaveName() {
        return this.name.replace(" ", "").replace(".", "_");
    }

    /**
     * Credits: <a href="https://github.com/MeteorDevelopment/meteor-client">Meteor Client</a>
     *
     * @param <B> Type representing the Builder Class
     * @param <V> Type representing the Data type object of the value
     * @param <S> Type representing the Class object to build
     */
    public abstract static class SettingBuilder<B, V, S> {
        protected String name = "UnDefined", description = "";
        protected V value;
        protected BooleanSupplier shouldRender = () -> true; // Default to true
        protected V defaultValue = null;
        protected boolean shouldSaveAndLoad = true;

        protected SettingBuilder(V value) {
            this.value = value;
            this.defaultValue = value;
        }

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        public B description(String description) {
            this.description = description;
            return (B) this;
        }

        public B value(V value) {
            this.value = value;
            return (B) this;
        }

        public B shouldRender(BooleanSupplier shouldRender) {
            this.shouldRender = shouldRender;
            return (B) this;
        }

        public B shouldSaveAndLoad(boolean shouldSaveAndLoad) {
            this.shouldSaveAndLoad = shouldSaveAndLoad;
            return (B) this;
        }

        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            return (B) this;
        }

        public abstract S build();
    }

    public record RenderContext(DrawContext drawContext, int x, int y, int mouseX, int mouseY,
                                TextRenderer textRenderer) {
    }
}
