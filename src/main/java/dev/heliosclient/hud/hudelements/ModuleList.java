package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.heliosclient.hud.hudelements.ModuleList.ColorMode.METEOR;
import static dev.heliosclient.hud.hudelements.ModuleList.Sort.*;

/**
 * Color credits: <a href="https://github.com/MeteorDevelopment/meteor-client/">Meteor-Client</a>
 */
public class ModuleList extends HudElement implements Listener {

    private ArrayList<Module_> enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    private Color rainbow = new Color(255, 255, 255);
    private double rainbowHue1;
    private double rainbowHue2;

    public SettingGroup sgSettings = new SettingGroup("Settings");

    private final CycleSetting sort = sgSettings.add(new CycleSetting.Builder()
            .name("Sort")
            .description("Sorting method used for displaying modules")
            .value(List.of(Sort.values()))
            .onSettingChange(this)
            .defaultListOption(Biggest)
            .build()
    );
    private final CycleSetting colorMode = sgSettings.add(new CycleSetting.Builder()
            .name("Color Mode")
            .description("Mode of the color displayed")
            .value(List.of(ColorMode.values()))
            .onSettingChange(this)
            .defaultListOption(METEOR)
            .build()
    );

    private final DoubleSetting rainbowSpeed = sgSettings.add(new DoubleSetting.Builder()
            .name("Rainbow Speed")
            .description("Speed of rainbow")
            .onSettingChange(this)
            .min(0.001d)
            .max(0.2d)
            .roundingPlace(3)
            .value(0.05d)
            .shouldRender(()-> (ColorMode) colorMode.getOption() == METEOR)
            .build()
    );
    private final DoubleSetting rainbowSpread = sgSettings.add(new DoubleSetting.Builder()
            .name("Rainbow Spread")
            .description("Spread of rainbow")
            .onSettingChange(this)
            .min(0.001f)
            .max(0.05f)
            .roundingPlace(3)
            .value(0.1d)
            .shouldRender(()-> (ColorMode) colorMode.getOption() == METEOR)
            .build()
    );
    private final DoubleSetting rainbowSaturation = sgSettings.add(new DoubleSetting.Builder()
            .name("Rainbow Saturation")
            .description("Saturation of rainbow")
            .onSettingChange(this)
            .min(0.0)
            .max(1d)
            .roundingPlace(2)
            .value(1d)
            .defaultValue(1d)
            .shouldRender(()-> (ColorMode) colorMode.getOption() == METEOR)
            .build()
    );
    private final DoubleSetting rainbowBrightness = sgSettings.add(new DoubleSetting.Builder()
            .name("Rainbow Brightness")
            .description("Brightness of rainbow")
            .onSettingChange(this)
            .min(0.0)
            .max(1d)
            .roundingPlace(2)
            .value(1d)
            .defaultValue(1d)
            .shouldRender(()-> (ColorMode) colorMode.getOption() == METEOR)
            .build()
    );

    public ModuleList() {
        super(DATA);
        this.width = 50;
        EventManager.register(this);
        addSettingGroup(sgSettings);
    }

    public static HudElementData<ModuleList> DATA = new HudElementData<>("Module List", "Shows enabled modules", ModuleList::new);

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        int maxWidth = 0;

        // Calculate the maximum width of the module names for enabled modules only
        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;
            int nameWidth = Math.round(Renderer2D.getStringWidth(m.name));
            maxWidth = Math.max(maxWidth, nameWidth);
        }
        if(colorMode.getOption() == METEOR) {
            rainbowHue1 += rainbowSpeed.value * mc.getTickDelta();
            if (rainbowHue1 > 1) rainbowHue1 -= 1;
            else if (rainbowHue1 < -1) rainbowHue1 += 1;

            rainbowHue2 = rainbowHue1;
        }

        // Render each module with a different color
        this.width = maxWidth + 5;
        int yOffset = this.y; // Start rendering from this.y
        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;

            float nameWidth = Renderer2D.getStringWidth(m.name);

            // Draw a background rectangle for each module
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    x - 6 + width - nameWidth, yOffset, nameWidth + 3,
                    Math.round(Renderer2D.getStringHeight()) + 2, 0x66222222);

            // Draw a vertical separator line
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    x - 2 + width, yOffset, 2,
                    Math.round(Renderer2D.getStringHeight()) + 3, HeliosClient.uiColor);

            if(colorMode.getOption() == METEOR) {
                rainbowHue2 += rainbowSpread.value;
                rainbow = new Color(Color.HSBtoRGB((float) rainbowHue2, (float) rainbowSaturation.value, (float) rainbowBrightness.value));
            }
            // Draw the module name
            Renderer2D.drawString(drawContext.getMatrices(), m.name,
                    x - 4 + width - nameWidth, 1 + yOffset,
                    rainbow.getRGB());

            yOffset += Math.round(Renderer2D.getStringHeight()) + 2;
        }
        this.height =Math.max(yOffset - this.y + 2,40);
    }

    @SubscribeEvent
    public void update(TickEvent.CLIENT event) {
        enabledModules = ModuleManager.INSTANCE.getEnabledModules();
        enabledModules.sort((mod1, mod2) -> switch ((Sort)sort.getOption()) {
            case Alphabetical -> mod1.name.compareTo(mod2.name);
            case Biggest -> Double.compare( Renderer2D.getStringWidth(mod2.name), Renderer2D.getStringWidth(mod1.name));
            case Smallest -> Double.compare(Renderer2D.getStringWidth(mod1.name), Renderer2D.getStringWidth(mod2.name));
        });
    }

    public enum Sort {
        Alphabetical,
        Biggest,
        Smallest
    }
    public enum ColorMode {
        METEOR
    }
}
