package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.ui.clickgui.settings.RGBASettingScreen;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.GlAllocationUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class RGBASetting extends ParentScreenSetting<Color> implements Listener {

    private final RGBASettingScreen screen = new RGBASettingScreen(this);
    private final int boxHeight = 70;
    private final int boxWidth = 70;
    private final int sliderWidth = 10;
    private final int offsetX = 10; // Offset from the left
    private final int offsetY = 20; // Offset from the top
    private final boolean defaultRainbow;
    public Color value;
    public InputBox hexInput;
    public boolean isPicking = false;
    private float hue, saturation, brightness, alpha;
    private int handleX, handleY, alphaHandleY, shadeHandleX, shadeHandleY;
    private boolean rainbow;
    // Add new fields to store calculated values
    private int gradientBoxX, gradientBoxY, gradientBoxWidth, gradientBoxHeight;
    private int alphaSliderX, alphaSliderY, alphaSliderWidth, alphaSliderHeight;
    private int brightnessSaturationBoxX, brightnessSaturationBoxY, brightnessSaturationBoxWidth, brightnessSaturationBoxHeight;

    public RGBASetting(String name, String description, Color defaultColor, boolean rainbow, ISettingChange ISettingChange, BooleanSupplier shouldRender) {
        super(shouldRender, defaultColor);
        this.name = name;
        this.description = description;
        this.value = defaultColor;
        this.rainbow = rainbow;
        this.defaultRainbow = rainbow;
        this.height = 24;
        this.heightCompact = 17;
        this.iSettingChange = ISettingChange;
        EventManager.register(this);

        // Calculate values once and store them
        this.gradientBoxX = x + offsetX;
        this.gradientBoxY = y + offsetY;
        this.gradientBoxWidth = boxWidth;
        this.gradientBoxHeight = boxHeight - 60;

        this.alphaSliderX = x + offsetX + boxWidth + sliderWidth;
        this.alphaSliderY = y + offsetY;
        this.alphaSliderWidth = sliderWidth;
        this.alphaSliderHeight = boxHeight;

        this.brightnessSaturationBoxX = x + offsetX + boxWidth + sliderWidth * 3;
        this.brightnessSaturationBoxY = y + offsetY;
        this.brightnessSaturationBoxWidth = boxWidth;
        this.brightnessSaturationBoxHeight = boxHeight;

        updateHandles();
        alpha = defaultColor.getAlpha() / 255f;
        alphaHandleY = Math.round((1.0f - alpha) * (float) boxHeight);

        hexInput = new InputBox(50, 11, ColorUtils.colorToHex(value), 7, InputBox.InputMode.ALL);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        this.x = x;
        this.y = y;
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 4, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 170, y + 2, 15, 15, 2, value.getRGB());

        if (rainbow) {
            value = ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), value.getAlpha());
        }
    }

    public void renderSetting(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;


        // Draw the name of setting and the value of the color we have set
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 3, y + 2, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 170, y + 2, 15, 15, 2, value.getRGB());

        int value1 = hoveredOverGradientBox(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();
        int value2 = hoveredOverAlphaSlider(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();
        int value3 = hoveredOverBrightnessSaturationBox(mouseX, mouseY) ? Color.DARK_GRAY.getRGB() : Color.BLACK.brighter().getRGB();

        if (rainbow) {
            updateHandles();
        }
        this.gradientBoxX = x + offsetX;
        this.gradientBoxY = y + offsetY;
        this.gradientBoxWidth = boxWidth;
        this.gradientBoxHeight = boxHeight - 60;

        this.alphaSliderX = x + offsetX + boxWidth + sliderWidth;
        this.alphaSliderY = y + offsetY;
        this.alphaSliderWidth = sliderWidth;
        this.alphaSliderHeight = boxHeight;

        this.brightnessSaturationBoxX = x + offsetX + boxWidth + sliderWidth * 3;
        this.brightnessSaturationBoxY = y + offsetY;
        this.brightnessSaturationBoxWidth = boxWidth;
        this.brightnessSaturationBoxHeight = boxHeight;

        drawGradientBox(drawContext, gradientBoxX, gradientBoxY, value1);
        drawAlphaSlider(drawContext, alphaSliderX, alphaSliderY, value2);
        drawBrightnessSaturationBox(drawContext, brightnessSaturationBoxX, brightnessSaturationBoxY, hue, value3);

        //Draw Rainbow button
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX - 1, y + offsetY + gradientBoxHeight + 6, Renderer2D.getFxStringWidth("Rainbow ") + 1, Renderer2D.getFxStringHeight("Rainbow ") + 1, 2, Color.DARK_GRAY.getRGB());
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Rainbow ", x + offsetX + 1, y + offsetY + gradientBoxHeight + 7, rainbow ? Color.GREEN.getRGB() : Color.RED.getRGB());

        //Draw picker button
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + offsetX + Renderer2D.getFxStringWidth("Rainbow ") + 5, y + offsetY + gradientBoxHeight + 6, FontRenderers.Mid_iconRenderer.getStringWidth("\uF1FB") + 3, FontRenderers.Mid_iconRenderer.getStringHeight("\uF17C ") + 1, 2, Color.LIGHT_GRAY.getRGB());
        FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uF1FB", x + offsetX + Renderer2D.getFxStringWidth("Rainbow ") + 6.5f, y + offsetY + gradientBoxHeight + 6.5f, isPicking ? Color.GREEN.getRGB() : Color.RED.getRGB());

        //Render the texts
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Alpha: " + value.getAlpha(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 9, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Red: " + value.getRed(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 18, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Green: " + value.getGreen(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 27, -1);
        Renderer2D.drawFixedString(drawContext.getMatrices(), "Blue: " + value.getBlue(), gradientBoxX, y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 36, -1);

        // Draw the handles
        Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), gradientBoxX + handleX, gradientBoxY + handleY, 1, -1);
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), alphaSliderX - 2, alphaSliderY + alphaHandleY, sliderWidth + 4, 3, -1);
        Renderer2D.drawFilledCircle(drawContext.getMatrices().peek().getPositionMatrix(), brightnessSaturationBoxX + shadeHandleX, brightnessSaturationBoxY + shadeHandleY, 1, -1);

        hexInput.render(drawContext, gradientBoxX, Math.round(y + offsetY + gradientBoxHeight + Renderer2D.getFxStringHeight() + 47), mouseX, mouseY, textRenderer);
        if (!hexInput.isFocused()) {
            hexInput.setText(ColorUtils.colorToHex(value));
        }
        if (isPicking && !hoveredOverPickBool(mouseX, mouseY)) {
            //This can get pretty FPS intensive because of the buffer and rendering the box.

            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0, 0, 2000);
            // Draw the cursor box
            double mouseXPick = HeliosClient.MC.mouse.getX() * HeliosClient.MC.getWindow().getScaledWidth() / (double) HeliosClient.MC.getWindow().getWidth();
            double mouseYPick = HeliosClient.MC.mouse.getY() * HeliosClient.MC.getWindow().getScaledHeight() / (double) HeliosClient.MC.getWindow().getHeight();

            Framebuffer framebuffer = HeliosClient.MC.getFramebuffer();

            //Get the position of the pixel to pick the color from
            int pickX = (int) (mouseXPick * framebuffer.textureWidth / HeliosClient.MC.getWindow().getScaledWidth());
            int pickY = (int) ((HeliosClient.MC.getWindow().getScaledHeight() - mouseYPick) * framebuffer.textureHeight / HeliosClient.MC.getWindow().getScaledHeight());

            ByteBuffer buffer = GlAllocationUtils.allocateByteBuffer(4);
            GL11.glReadPixels(pickX, pickY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            int red = buffer.get(0) & 0xFF;
            int green = buffer.get(1) & 0xFF;
            int blue = buffer.get(2) & 0xFF;


            // Render the outline box and also the color of the pixel
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (float) (mouseXPick + 10), (float) mouseYPick, 14, 14, -1);
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (float) (mouseXPick + 11), (float) (mouseYPick + 1), 12, 12, new Color(red, green, blue, (int) (alpha * 255)).getRGB());

            drawContext.getMatrices().push();
        }
    }

    public void drawGradientBox(DrawContext drawContext, int x, int y, int value) {
        // Draw the rainbow gradient box from which we pick the colors
        Renderer2D.drawRainbowGradient(drawContext.getMatrices().peek().getPositionMatrix(), x, y, boxWidth, gradientBoxHeight);

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, boxWidth + 2, gradientBoxHeight + 2, 1, 1, value);
    }


    public void drawAlphaSlider(DrawContext drawContext, int x, int y, int value3) {
        // Draw the alpha slider
        Color value1 = new Color(value.getRed(), value.getGreen(), value.getBlue(), 0);
        Color value2 = new Color(value.getRed(), value.getGreen(), value.getBlue(), 255);
        Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), x, y, sliderWidth, boxHeight, value1.getRGB(), value2.getRGB(), Renderer2D.Direction.BOTTOM_TOP);

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, sliderWidth + 2, boxHeight + 2, 1, 1, value3);

        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Alpha", x - 5, y + boxHeight + 2, -1); // Below the alpha slider
    }

    public void drawBrightnessSaturationBox(DrawContext drawContext, int x, int y, float hue, int value3) {
        // Draw the box from which we change the shade of the colors picked from gradient box
        Renderer2D.drawRoundedGradientRectangleWithShadow(drawContext.getMatrices(), x, y, boxWidth, boxHeight, Color.BLACK, Color.WHITE, Color.getHSBColor(hue, 1.0f, 1.0f), Color.BLACK, 2, 4, getColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y - 1, boxWidth + 2, boxHeight + 2, 3, 1, value3);
    }

    public void updateHandles() {
        float[] hsbvals = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        hue = hsbvals[0];
        saturation = hsbvals[1];
        brightness = hsbvals[2];
        handleX = Math.min((int) (hue * boxWidth), boxWidth);
        handleY = Math.min((int) ((1 - saturation) * gradientBoxHeight), gradientBoxHeight);
        shadeHandleX = Math.min((int) (brightness * boxWidth), boxWidth);
        shadeHandleY = Math.min((int) ((1 - saturation) * boxHeight), boxHeight);
    }


    public boolean hoveredOverPickBool(double mouseX, double mouseY) {
        return mouseX > gradientBoxX + Renderer2D.getFxStringWidth("Rainbow ") + 5 && mouseX < gradientBoxX + Renderer2D.getFxStringWidth("Rainbow ") + 5 + FontRenderers.Small_iconRenderer.getStringWidth("\uF17C ") + 3 && mouseY > y + offsetY + gradientBoxHeight + 5 && mouseY < y + offsetY + gradientBoxHeight + 6 + Renderer2D.getFxStringHeight("\uF17C ") + 2;
    }

    public boolean hoveredOverRainbowBool(double mouseX, double mouseY) {
        return mouseX > gradientBoxX - 1 && mouseX < gradientBoxX - 1 + Renderer2D.getFxStringWidth("Rainbow ") + 3 && mouseY > y + offsetY + gradientBoxHeight + 5 && mouseY < y + offsetY + gradientBoxHeight + 6 + Renderer2D.getFxStringHeight("Rainbow ") + 2;
    }

    public boolean hoveredOverGradientBox(double mouseX, double mouseY) {
        return mouseX > gradientBoxX && mouseX < gradientBoxX + gradientBoxWidth && mouseY > gradientBoxY && mouseY < gradientBoxY + gradientBoxHeight;
    }

    public boolean hoveredOverAlphaSlider(double mouseX, double mouseY) {
        return mouseX > alphaSliderX && mouseX < alphaSliderX + alphaSliderWidth && mouseY > alphaSliderY && mouseY < alphaSliderY + alphaSliderHeight;
    }

    public boolean hoveredOverBrightnessSaturationBox(double mouseX, double mouseY) {
        return mouseX > brightnessSaturationBoxX && mouseX < brightnessSaturationBoxX + brightnessSaturationBoxWidth && mouseY > brightnessSaturationBoxY && mouseY < brightnessSaturationBoxY + brightnessSaturationBoxHeight;
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name.substring(0, Math.min(12, name.length())) + "...", x + 3, y + 1, -1);
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 71, y + 1, 10, 10, 2, value.getRGB());
    }

    public void mouse(double mouseX, double mouseY, int button) {
        if (isPicking && !hoveredOverPickBool(mouseX, mouseY)) {
            double mouseXPick = HeliosClient.MC.mouse.getX() * HeliosClient.MC.getWindow().getScaledWidth() / (double) HeliosClient.MC.getWindow().getWidth();
            double mouseYPick = HeliosClient.MC.mouse.getY() * HeliosClient.MC.getWindow().getScaledHeight() / (double) HeliosClient.MC.getWindow().getHeight();

            Framebuffer framebuffer = HeliosClient.MC.getFramebuffer();
            int pickX = (int) (mouseXPick * framebuffer.textureWidth / HeliosClient.MC.getWindow().getScaledWidth());
            int pickY = (int) ((HeliosClient.MC.getWindow().getScaledHeight() - mouseYPick) * framebuffer.textureHeight / HeliosClient.MC.getWindow().getScaledHeight());

            ByteBuffer buffer = GlAllocationUtils.allocateByteBuffer(4);
            GL11.glReadPixels(pickX, pickY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            int red = buffer.get(0) & 0xFF;
            int green = buffer.get(1) & 0xFF;
            int blue = buffer.get(2) & 0xFF;

            value = new Color(red, green, blue, (int) (alpha * 255));
            isPicking = !isPicking;
            updateHandles();
            alpha = value.getAlpha() / 255f;
        }

        if (hoveredOverGradientBox(mouseX, mouseY)) {
            handleX = (int) (mouseX - x - offsetX);
            handleY = (int) (mouseY - y - offsetY);
            hue = handleX / (float) boxWidth;
            saturation = 1.0f - (float) handleY / gradientBoxHeight;
        } else if (hoveredOverBrightnessSaturationBox(mouseX, mouseY)) {
            shadeHandleX = (int) (mouseX - x - offsetX - boxWidth - sliderWidth * 3);
            shadeHandleY = (int) (mouseY - y - offsetY);
            brightness = shadeHandleX / (float) boxWidth;
            saturation = 1.0f - shadeHandleY / (float) boxHeight;
        } else if (hoveredOverRainbowBool(mouseX, mouseY)) {
            rainbow = !rainbow;
        } else if (hoveredOverPickBool(mouseX, mouseY)) {
            isPicking = !isPicking;
        }

        if (hoveredOverAlphaSlider(mouseX, mouseY)) {
            alphaHandleY = (int) (mouseY - y - offsetY);
            alpha = 1.0f - alphaHandleY / (float) boxHeight;
        }
        if (rainbow) {
            updateHandles();
        } else {
            value = Color.getHSBColor(hue, saturation, brightness);
        }
        value = new Color(value.getRed(), value.getGreen(), value.getBlue(), (int) (alpha * 255));
    }

    public void keyPress(int keyCode) {
        if ((keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) && hexInput.isFocused()) {
            if (ColorUtils.isHexColor(hexInput.getValue())) {
                try {
                    value = ColorUtils.hexToColor(hexInput.getValue());
                    updateHandles();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Something went wrong
                }
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            rainbow = defaultRainbow;
            updateHandles();
            alphaHandleY = Math.round((1.0f - alpha) * (float) boxHeight);
            alpha = value.getAlpha() / 255f;
        }
        super.mouseClicked(mouseX, mouseY, button);
        if (hovered((int) mouseX, (int) mouseY)) {
            MinecraftClient.getInstance().setScreen(screen);
        }
    }

    public Color getColor() {
        return value;
    }

    public void setColor(Color value) {
        this.value = value;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public boolean isDefaultRainbow() {
        return defaultRainbow;
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT event) {
        if (rainbow) {
            value = ColorUtils.changeAlpha(ColorUtils.getRainbowColor(), value.getAlpha());
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        objectList.add(value.getRGB());
        objectList.add(rainbow ? 1 : 0);
        return objectList;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP, toml);
        if (toml.getList(this.getSaveName()) == null) {
            value = defaultValue;
            rainbow = defaultRainbow;
            HeliosClient.LOGGER.error(this.getSaveName() + " is null, Setting loaded to default");
            return;
        }
        value = ColorUtils.intToColor(Integer.parseInt(toml.getList(this.getSaveName()).get(0).toString()));
        rainbow = Integer.parseInt(toml.getList(this.getSaveName()).get(1).toString()) == 1;
        updateHandles();
    }

    public Screen getParentScreen() {
        return parentScreen;
    }

    public void setParentScreen(Screen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public static class Builder extends SettingBuilder<Builder, Color, RGBASetting> {
        boolean rainbow = false;
        ISettingChange ISettingChange;

        public Builder() {
            super(new Color(-1));
        }

        public Builder rainbow(boolean rainbow) {
            this.rainbow = rainbow;
            return this;
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        @Override
        public RGBASetting build() {
            return new RGBASetting(name, description, value, rainbow, ISettingChange, shouldRender);
        }
    }
}