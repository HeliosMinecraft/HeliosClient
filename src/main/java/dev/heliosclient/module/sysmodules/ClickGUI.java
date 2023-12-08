package dev.heliosclient.module.sysmodules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.client.FontChangeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.FontUtils;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import me.x150.renderer.font.FontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.managers.FontManager.*;

/**
 * Setting for ClickGUI.
 */
public class ClickGUI extends Module_ {
    public static boolean pause = false;
    public static boolean keybinds = false;
    public static SettingGroup sgUI = new SettingGroup("UI");
    public static ClickGUI INSTANCE = new ClickGUI();
    public static CycleSetting ScrollType = sgUI.add(new CycleSetting.Builder()
            .name("Scrolling System")
            .description("Scrolling for the ClickGui")
            .onSettingChange(INSTANCE)
            .value(new ArrayList<>(List.of(ScrollTypes.values())))
            .listValue(0)
            .shouldRender(() -> true)
            .build()
    );
    public static DoubleSetting CategoryHeight = sgUI.add(new DoubleSetting.Builder()
            .name("CategoryPane Height")
            .description("CategoryPane Height for the ClickGUI")
            .onSettingChange(INSTANCE)
            .value(150.0)
            .max(1000)
            .min(25)
            .roundingPlace(0)
            .shouldRender(() -> ScrollType.value == 1)
            .build()
    );
    public static DoubleSetting ScrollSpeed = sgUI.add(new DoubleSetting.Builder()
            .name("Scroll Speed")
            .description("Change your scroll speed for the ClickGUI")
            .onSettingChange(INSTANCE)
            .value(7.0)
            .min(1)
            .max(8)
            .roundingPlace(1)
            .build()
    );
    public static CycleSetting FontRenderer = sgUI.add(new CycleSetting.Builder()
            .name("Font Renderer")
            .description("Font Rendering for the client")
            .onSettingChange(INSTANCE)
            .value(new ArrayList<>(List.of("Custom", "Vanilla")))
            .listValue(0)
            .shouldRender(() -> true)
            .build()
    );
    public static CycleSetting Font = sgUI.add(new CycleSetting.Builder()
            .name("Font")
            .description("Font for the client")
            .onSettingChange(INSTANCE)
            .value(FontManager.fontNames)
            .listValue(0)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public static DoubleSetting FontSize = sgUI.add(new DoubleSetting.Builder()
            .name("Font Size")
            .description("Change your FontSize")
            .onSettingChange(INSTANCE)
            .value(8.0)
            .min(1)
            .max(15)
            .roundingPlace(1)
            .shouldRender(() -> FontRenderer.value == 0)
            .build()
    );
    public static DoubleSetting RainbowSpeed = sgUI.add(new DoubleSetting.Builder()
            .name("Rainbow/Gradient speed")
            .description("Speed of the rainbow and gradients throughout the client")
            .onSettingChange(ClickGUI.INSTANCE)
            .value(14.0)
            .max(20)
            .min(1)
            .defaultValue(10.0)
            .roundingPlace(0)
            .build()
    );
    public SettingGroup sgTooltip = new SettingGroup("ToolTip");
    public SettingGroup sgGeneral = new SettingGroup("General");
    public CycleSetting TooltipMode = sgTooltip.add(new CycleSetting.Builder()
            .name("Tooltip mode")
            .description("Mode in what tooltips should be shown.")
            .onSettingChange(this)
            .value(new ArrayList<String>(List.of("Normal", "Fixed", "Vanilla")))
            .listValue(0)
            .shouldRender(() -> true)
            .build()
    );
    public CycleSetting TooltipPos = sgTooltip.add(new CycleSetting.Builder()
            .name("Tooltip position")
            .description("Position of fixed tooltip.")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Top-left", "Top-right", "Bottom-left", "Bottom-right", "Center")))
            .listValue(3)
            .shouldRender(() -> TooltipMode.value == 1)
            .build()
    );
    ButtonSetting loadFonts = sgGeneral.add(new ButtonSetting.Builder()
            .name("Font")
            .build()
    );
    BooleanSetting Pause = sgGeneral.add(new BooleanSetting.Builder()
            .name("Pause game")
            .description("Pause the game when Click GUI is on.")
            .onSettingChange(this)
            .value(false)
            .build()
    );
    public BooleanSetting ScreenHelp = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show keybind Help")
            .description("Show keybind Help for client screens.")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    BooleanSetting Keybinds = sgGeneral.add(new BooleanSetting.Builder()
            .name("Show Keybinds")
            .description("Show keybinds in the Module Button.")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    public SettingGroup sgRender = new SettingGroup("Render");

    public CycleSetting ColorMode = sgRender.add(new CycleSetting.Builder()
            .name("Color Mode")
            .description("Color mode for parts of the client")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Static", "Gradient")))
            .listValue(0)
            .build()
    );
    public RGBASetting staticColor = sgRender.add(new RGBASetting.Builder()
            .name("Color")
            .description("Simple single color for parts of the client")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .shouldRender(()-> ColorMode.value == 0)
            .build()
    );
    public CycleSetting GradientType = sgRender.add(new CycleSetting.Builder()
            .name("Gradient Type")
            .description("Gradient type for the gradient color mode")
            .onSettingChange(this)
            .value(new ArrayList<>(List.of("Rainbow", "DaySky", "EveningSky", "NightSky","Linear2D")))
            .listValue(0)
            .shouldRender(()-> ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2Start = sgRender.add(new RGBASetting.Builder()
            .name("Linear-Start")
            .description("Linear Color Start of Linear mode")
            .onSettingChange(this)
            .value(Color.GREEN)
            .defaultValue(Color.GREEN)
            .shouldRender(()-> GradientType.value == 4 && ColorMode.value == 1)
            .build()
    );
    public RGBASetting linear2end = sgRender.add(new RGBASetting.Builder()
            .name("Linear-End")
            .description("Linear Color End of Linear mode")
            .onSettingChange(this)
            .value(Color.YELLOW)
            .shouldRender(()-> GradientType.value == 4 && ColorMode.value == 1)
            .defaultValue(Color.YELLOW)
            .build()
    );

    public RGBASetting AccentColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Accent color")
            .description("Accent color of Click GUI.")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiSecondary))
            .build()
    );
    public RGBASetting PaneTextColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Category pane text color")
            .description("Color of pane text.")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.clickGuiPaneText))
            .defaultValue(new Color(ColorManager.INSTANCE.clickGuiPaneText))
            .build()
    );
    public RGBASetting TextColor = sgGeneral.add(new RGBASetting.Builder()
            .name("Text color")
            .description("Color of text all through out the client.")
            .onSettingChange(this)
            .value(new Color(ColorManager.INSTANCE.defaultTextColor))
            .defaultValue(new Color(ColorManager.INSTANCE.defaultTextColor))
            .build()
    );
   public ClickGUI() {
        super("ClickGUI", "ClickGui related stuff.", Categories.RENDER);

        addSettingGroup(sgUI);
        addSettingGroup(sgRender);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgTooltip);

         active.value = true;
        loadFonts.addButton("Load Fonts", 0, 0, () -> {
            FontManager.INSTANCE = new FontManager();
            Font.setOptions(FontManager.fontNames);

            System.out.println("Reloaded fonts successfully!");
        });

        EventManager.register(this);
   }
   @Override
   public void onSettingChange(Setting setting) {
       super.onSettingChange(setting);

       Tooltip.tooltip.mode = TooltipMode.value;
       Tooltip.tooltip.fixedPos = TooltipPos.value;

        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];
        pause = Pause.value;
        keybinds = Keybinds.value;

        if(setting == FontRenderer || setting == FontSize || setting == loadFonts || setting == Font){
            fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);
            FontRenderers.fontRenderer = new FontRenderer(fonts, fontSize);
            EventManager.postEvent(new FontChangeEvent(fonts));
        }

       if(setting == FontRenderer || setting == Font) {
           FontManager.INSTANCE.registerFonts();
       }
    }



    @Override
    public void onLoad() {
        Tooltip.tooltip.mode = TooltipMode.value;
        Tooltip.tooltip.fixedPos = TooltipPos.value;
        Renderer2D.renderer = Renderer2D.Renderers.values()[FontRenderer.value];

        ColorManager.INSTANCE.clickGuiSecondaryAlpha = AccentColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiSecondary = AccentColor.getColor().getRGB();
        ColorManager.INSTANCE.clickGuiSecondaryRainbow = AccentColor.isRainbow();

        ColorManager.INSTANCE.defaultTextColor = TextColor.getColor().getRGB();

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getColor().getAlpha();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.getColor().getRGB();
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = PaneTextColor.isRainbow();

        ColorManager.INSTANCE.clickGuiPaneTextAlpha = PaneTextColor.getA();
        ColorManager.INSTANCE.clickGuiPaneText = PaneTextColor.value;
        ColorManager.INSTANCE.clickGuiPaneTextRainbow = RainbowPane.value;

        pause = Pause.value;
        keybinds = Keybinds.value;

        fonts = FontUtils.rearrangeFontsArray(FontManager.Originalfonts, FontManager.Originalfonts[Font.value]);

        FontManager.INSTANCE.registerFonts();

        if(ColorMode.value == 0) {
            ColorManager.INSTANCE.primaryGradientStart = staticColor.getColor();
            ColorManager.INSTANCE.primaryGradientEnd = staticColor.getColor();
        }
        if(ColorMode.value == 1) {
            switch(GradientType.value){
                case 0->{
                    ColorManager.INSTANCE.primaryGradientStart = ColorUtils.getRainbowColor();
                    ColorManager.INSTANCE.primaryGradientEnd = ColorUtils.getRainbowColor2();
                }
                case 1, 2, 3 ->{
                    ColorManager.INSTANCE.primaryGradientStart = ColorUtils.getRainbowColor();
                    ColorManager.INSTANCE.primaryGradientEnd = ColorUtils.getRainbowColor();
                }
                case 4->{
                    ColorManager.INSTANCE.primaryGradientStart = linear2Start.getColor();
                    ColorManager.INSTANCE.primaryGradientEnd = linear2end.getColor();
                }
            }
        }
    }

    @Override
    public void toggle() {
    }
    public enum ScrollTypes {
        OLD,
        NEW
    }
}
