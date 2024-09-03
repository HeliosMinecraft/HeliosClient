package dev.heliosclient.hud;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.heliosclient.FontChangeEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.system.UniqueID;
import dev.heliosclient.ui.clickgui.gui.HudBox;
import dev.heliosclient.ui.clickgui.hudeditor.HudEditorScreen;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.interfaces.ISaveAndLoad;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for creating HudElements for HUD
 *
 * <h4>Position is stored using this method:</h4><br>
 * There are `[NUMBER_OF_LINES]` (imaginary lines) on each axis. The screen is divided on each axis (x,y) in the count of lines.<br>
 * The HUD element stores on each axis to which section of the lines it lies to, <br>
 * (For example if the 3rd line in x-axis is present at 30 pixels and the second line is at 20 pixels,
 * and the hud element is present at 25 pixels then the line chosen will be 3rd line [The spacing is controlled by number of lines]).
 * <br>
 * This is stored in the posX and posY variable. Element also stores the distance to this line in pixels in the distanceX and distance Y variables.
 * These are capped so the elements can`t go off the screen. These values aren't changed at all while not dragging ensuring no shifts happening.<br>
 * This approach ensures that when resizing the elements on screen don't overlap much and don't shift around, unless they are touched in the editor.
 *
 * <h4>Warning: Makes sure the element is drawn with its top-left vertex as the element's x and y position. If this is not met they will go off-screen and hit-boxes will be broken.</h4>
 */
public class HudElement implements ISettingChange, ISaveAndLoad, Listener {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public String name;
    public String description;
    public int height = FontManager.hudFontSize;
    public int width = 10;
    public int x;
    public int y;
    public boolean dragging;
    public int posY = 0;
    public int posX = 0;
    public int distanceY = 0;
    public int distanceX = 0;
    public boolean selected = false;
    public boolean draggable = true;
    public boolean renderOutLineBox = true;
    public HudBox hudBox;
    public boolean shiftDown = false;
    public List<SettingGroup> settingGroups = new ArrayList<>();
    public SettingGroup sgUI = new SettingGroup("UI");
    public boolean isInHudEditor = false;
    public UniqueID id;
    int startX, startY;
    private static int snapSize = 120;

    //This variable is used to control the number of imaginary lines to use, to
    //anchor the hud elements during resize, as stated in the class comment.
    public static final int NUMBER_OF_LINES = 120;

    // Default settings
    // This is a lot of complete customisation.
    // Todo: Add radius setting for rounded background
    public BooleanSetting renderBg = sgUI.add(new BooleanSetting.Builder()
            .name("Render background")
            .description("Render the background for the element")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build());
    public BooleanSetting rounded = sgUI.add(new BooleanSetting.Builder()
            .name("Rounded background")
            .description("Rounds the background of the element for better visuals")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(() -> renderBg.value)
            .build());
    public BooleanSetting clientColorCycle = sgUI.add(new BooleanSetting.Builder()
            .name("Client Color Cycle")
            .description("Use the client default color cycle for the background of the element")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(() -> renderBg.value)
            .build());
    public RGBASetting backgroundColor = sgUI.add(new RGBASetting.Builder()
            .name("Background Color")
            .description("Render the background for the element")
            .value(new Color(4, 3, 3, 157))
            .defaultValue(new Color(4, 3, 3, 157))
            .shouldRender(() -> renderBg.value && !clientColorCycle.value)
            .onSettingChange(this)
            .build());
    public DoubleSetting padding = sgUI.add(new DoubleSetting.Builder()
            .name("Padding")
            .description("Amount of Padding around the borders")
            .value(0D)
            .min(0)
            .max(15)
            .defaultValue(0D)
            .shouldRender(() -> renderBg.value)
            .onSettingChange(this)
            .build());
    public BooleanSetting shadow = sgUI.add(new BooleanSetting.Builder()
            .name("Shadow")
            .description("Shadow for the background of the element")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(() -> renderBg.value)
            .build());
    public DoubleSetting shadowRadius = sgUI.add(new DoubleSetting.Builder()
            .name("Shadow Radius")
            .description("Radius of the shadow")
            .min(0)
            .max(50)
            .defaultValue(5D)
            .value(5D)
            .shouldRender(() -> shadow.value && renderBg.value)
            .onSettingChange(this)
            .build());
    public BooleanSetting syncShadowColorAsBackground = sgUI.add(new BooleanSetting.Builder()
            .name("Sync Background color to shadow")
            .description("Syncs shadow color with background color")
            .defaultValue(true)
            .onSettingChange(this)
            .shouldRender(() -> shadow.value && renderBg.value)
            .build());
    public RGBASetting shadowColor = sgUI.add(new RGBASetting.Builder()
            .name("Shadow Color")
            .description("Render the shadow for the element")
            .value(new Color(4, 3, 3, 157))
            .defaultValue(new Color(4, 3, 3, 157))
            .shouldRender(() -> shadow.value && !syncShadowColorAsBackground.value && renderBg.value)
            .onSettingChange(this)
            .build());


    public HudElement(HudElementData<?> hudElementInfo) {
        this.name = hudElementInfo.name();
        this.description = hudElementInfo.description();
        this.id = UniqueID.generate();
        hudBox = new HudBox(x, y, width, height);
        EventManager.register(this);
    }

    /**
     * Method for rendering in the editor. You probably shouldn't override this.
     *
     * @param drawContext
     * @param textRenderer
     * @param mouseX
     * @param mouseY
     */
    public void renderEditor(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        // Get right line to align to
        double posYInterval = (double) drawContext.getScaledWindowHeight() / (NUMBER_OF_LINES);
        double posXInterval = (double) drawContext.getScaledWindowWidth() / (NUMBER_OF_LINES);

        if (dragging) {
            int newX = mouseX - startX;
            int newY = mouseY - startY;

            if (this.shiftDown && drawContext != null) {
                // Calculate the size of each snap box
                int snapBoxWidth = drawContext.getScaledWindowWidth() / snapSize;
                int snapBoxHeight = drawContext.getScaledWindowHeight() / snapSize;

                // Calculate the index of the snap box that the new position would be in
                int snapBoxX = newX / snapBoxWidth;
                int snapBoxY = newY / snapBoxHeight;

                // Snap the new position to the top-left corner of the snap box
                newX = snapBoxX * snapBoxWidth;
                newY = snapBoxY * snapBoxHeight;
            }

            x = newX;
            y = newY;


            posX = (int) (x / posXInterval);
            posY = (int) (y / posYInterval);

            distanceX = (int) (x % posXInterval);
            distanceY = (int) (y % posYInterval);
        }

        // Move to right position according to pos and distance variables
        if (posX == 0) {
            x = Math.max(Math.min(distanceX, drawContext.getScaledWindowWidth() - width), 0);
        } else if (posX == NUMBER_OF_LINES) {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth() - distanceX, drawContext.getScaledWindowWidth() - width), 0);
        } else {
            x = (int) Math.max(Math.min(posXInterval * posX + distanceX, drawContext.getScaledWindowWidth() - width), 0);
        }

        if (posY == 0) {
            y = Math.max(Math.min(distanceY, drawContext.getScaledWindowHeight() - height), 0);
        } else if (posY == NUMBER_OF_LINES) {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight() - distanceY, drawContext.getScaledWindowHeight() - height), 0);
        } else {
            y = (int) Math.max(Math.min(posYInterval * posY + distanceY, drawContext.getScaledWindowHeight() - height), 0);
        }

        //Draw outline when selected
        if (this.selected && renderOutLineBox) {
            if (dragging) {
                //Draw X and Y lines when centered
                if (x + width / 2f == drawContext.getScaledWindowWidth()) {
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (float) drawContext.getScaledWindowWidth() / 2 - 1, 0, 2, drawContext.getScaledWindowHeight(), 0xFFFF0000);
                }
                if (y + height / 2f == drawContext.getScaledWindowHeight()) {
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 0, (float) drawContext.getScaledWindowHeight() / 2 - 1, drawContext.getScaledWindowWidth(), 2, 0xFF00FF00);
                }
            }
            Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), (float) (x - 1 - padding.value / 2 - 1), (float) (y - 1 - padding.value / 2 - 1), hudBox.getWidth(), hudBox.getHeight() + 2f, 0.4f, 0xFFFFFFFF);
        }
        //Renders element
        renderElement(drawContext, textRenderer);

        // Set the hudBox values after the render element in-case any change of width/height occurs
        setHudBox();
    }

    public void setHudBox() {
        hudBox.set((float) (x - 1 - (padding.value / 2f)), (float) (y - 1 - (padding.value / 2f)), (float) (width + padding.value + 4f), (float) (height + padding.value + 1));
    }


    /**
     * Rendering outside of editor.
     *
     * @param drawContext
     * @param textRenderer
     */
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        //Skip if in F3 menu or hud is hidden
        if (HeliosClient.MC.options.hudHidden || HeliosClient.MC.getDebugHud().shouldShowDebugHud()) return;

        double posYInterval = (double) drawContext.getScaledWindowHeight() / NUMBER_OF_LINES;
        double posXInterval = (double) drawContext.getScaledWindowWidth() / NUMBER_OF_LINES;

        // Move to right position according to pos and distance variables
        if (posX == 0) {
            x = Math.max(Math.min(distanceX, drawContext.getScaledWindowWidth() - width), 0);
        } else if (posX == NUMBER_OF_LINES) {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth() - distanceX, drawContext.getScaledWindowWidth() - width), 0);
        } else {
            x = (int) Math.max(Math.min(posXInterval * posX + distanceX, drawContext.getScaledWindowWidth() - width), 0);
        }

        if (posY == 0) {
            y = Math.max(Math.min(distanceY, drawContext.getScaledWindowHeight() - height), 0);
        } else if (posY == NUMBER_OF_LINES) {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight() - distanceY, drawContext.getScaledWindowHeight() - height), 0);
        } else {
            y = (int) Math.max(Math.min(posYInterval * posY + distanceY, drawContext.getScaledWindowHeight() - height), 0);
        }

        //Render element
        renderElement(drawContext, textRenderer);

        setHudBox();
    }

    /**
     * Rendering function. Put everything you want to render here. Don't forget to set height and width.
     * Defaults add background rendering which can be easy replaced by overriding without super call.
     * Call the super method after defining your width and height, so it is rendered with appropriate coordinates behind the text or item displayed
     *
     * @param drawContext
     * @param textRenderer
     */
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        setHudBox();

        if (renderBg.value) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0D, 0D, 0D);
            Color bgStart, bgEnd;
            if (clientColorCycle.value) {
                bgStart = ColorManager.INSTANCE.getPrimaryGradientStart();
                bgEnd = ColorManager.INSTANCE.getPrimaryGradientEnd();
            } else {
                bgStart = backgroundColor.getColor();
                bgEnd = bgStart;
            }
            Color blended = ColorUtils.blend(bgStart, bgEnd, 1 / 2f);
            Color finalShadowColor = syncShadowColorAsBackground.value ? blended : shadowColor.value;

            drawBackground(drawContext, bgStart, bgEnd, finalShadowColor);

            drawContext.getMatrices().pop();
        }
    }

    private void drawBackground(DrawContext drawContext, Color bgStart, Color bgEnd, Color finalShadowColor) {
        if (rounded.value) {
            if (shadow.value) {
                Renderer2D.drawRoundedGradientRectangleWithShadow(drawContext.getMatrices(), hudBox.getX(), hudBox.getY(), hudBox.getWidth() - 1.9f, hudBox.getHeight(), bgStart, bgEnd, bgEnd, bgStart, 2, (int) shadowRadius.value, finalShadowColor);
            } else {
                Renderer2D.drawRoundedGradientRectangle(drawContext.getMatrices().peek().getPositionMatrix(), bgStart, bgEnd, bgEnd, bgStart, hudBox.getX(), hudBox.getY(), hudBox.getWidth() - 1.9f, hudBox.getHeight(), 2);
            }
        } else {
            if (shadow.value) {
                Renderer2D.drawGradientWithShadow(drawContext.getMatrices(), hudBox.getX(), hudBox.getY(), hudBox.getWidth() - 1.9f, hudBox.getHeight(), (int) shadowRadius.value, bgStart.getRGB(), bgEnd.getRGB(), finalShadowColor, Renderer2D.Direction.LEFT_RIGHT);
            } else {
                Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), hudBox.getX(), hudBox.getY(), hudBox.getWidth() - 1.9f, hudBox.getHeight(), bgStart.getRGB(), bgEnd.getRGB(), Renderer2D.Direction.LEFT_RIGHT);
            }
        }
    }

    /**
     * Called on load.
     */
    public void onLoad() {
        addSettingGroup(sgUI);
    }

    @SubscribeEvent
    public void onTick(TickEvent.CLIENT e) {
        isInHudEditor = mc.currentScreen instanceof HudEditorScreen;
    }

    /**
     * Add a setting group to the setting groups list
     *
     * @param settingGroup setting group to be added
     */
    public void addSettingGroup(SettingGroup settingGroup) {
        settingGroups.add(settingGroup);
    }

    /**
     * Removes a setting group to the setting groups list
     *
     * @param settingGroup setting group to be added
     */
    public void removeSettingGroup(SettingGroup settingGroup) {
        settingGroups.remove(settingGroup);
    }

    /**
     * Mouse click event.
     *
     * @param mouseX
     * @param mouseY
     * @param button
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        //Start dragging
        if (selected && button == 0 && draggable) {
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
            dragging = true;
        }
        // Divides the screen into several "grid boxes" which the elements snap to. Higher the snapSize, more the grid boxes
        if (this.shiftDown) {
            // Calculate the size of each snap box
            int snapBoxWidth = this.width / this.snapSize;
            int snapBoxHeight = this.height / this.snapSize;

            // Calculate the index of the snap box that the mouse is currently in
            int snapBoxX = (int) (mouseX / snapBoxWidth);
            int snapBoxY = (int) (mouseY / snapBoxHeight);

            // Snap the element to the top-left corner of the snap box
            this.x = snapBoxX * snapBoxWidth;
            this.y = snapBoxY * snapBoxHeight;
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @return if mouse is hovered over this element
     */
    public boolean hovered(double mouseX, double mouseY) {
        return hudBox.contains(mouseX, mouseY);
    }

    /**
     * Mouse released event.
     *
     * @param mouseX
     * @param mouseY
     * @param button
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {
        //Stop dragging
        dragging = false;
        if (button == 0) {
            this.x = (int) mouseX;
            this.y = (int) mouseY;
        }
    }

    /* Getter and setters */
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Called when setting gets changed.
     */
    public void onSettingChange(Setting<?> setting) {

    }

    @SubscribeEvent
    public void onFontChange(FontChangeEvent event) {
        this.height = Math.round(Renderer2D.getStringHeight());
    }

    @Override
    public Object saveToFile(List<Object> objects) {
        Map<String, Object> map = new HashMap<>();

        map.put("name", name);

        objects.add(x);
        objects.add(y);
        objects.add(width);
        objects.add(height);

        map.put("dimensions", objects);

        saveSettingsToMap(map);

        return map;
    }

    public void saveSettingsToMap(Map<String, Object> MAP){
        for (SettingGroup settingGroup : settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (setting.name != null) {
                    MAP.put(setting.getSaveName(), setting.saveToFile(new ArrayList<>()));
                }
            }
        }
    }

    @Override
    public void loadFromFile(Map<String, Object> MAP) {
        //Hope it saves as double and not any other.
        List<Double> obj = (List<Double>) MAP.get("dimensions");
        this.x = MathUtils.d2iSafe(obj.get(0));
        this.y = MathUtils.d2iSafe(obj.get(1));
        this.width = MathUtils.d2iSafe(obj.get(2));
        this.height = MathUtils.d2iSafe(obj.get(3));
        this.distanceX = x;
        this.distanceY = y;
    }

    public static void setSnapSize(int snapSize){
        HudElement.snapSize = MathHelper.clamp(snapSize,1,mc.getWindow().getScaledWidth() * mc.getWindow().getScaledHeight());
    }

    public static int getSnapSize() {
        return snapSize;
    }
}
