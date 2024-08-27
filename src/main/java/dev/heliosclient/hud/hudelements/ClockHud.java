package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ClockHud extends HudElement {
    public SettingGroup sgSettings = new SettingGroup("Settings");

    public static HudElementData<ClockHud> DATA = new HudElementData<>("ClockHUD","Displays in-game or real life time",ClockHud::new);

    private final BooleanSetting realLifeTime = sgSettings.add(new BooleanSetting.Builder()
            .name("RealLife time")
            .description("Shows your real life time")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    private final BooleanSetting twentyFourHourFormat = sgSettings.add(new BooleanSetting.Builder()
            .name("24 hour time format")
            .description("The real life timer shows 24 hour format (or military time format)")
            .defaultValue(false)
            .onSettingChange(this)
            .shouldRender(()->realLifeTime.value)
            .build()
    );
    private final BooleanSetting inGameTime = sgSettings.add(new BooleanSetting.Builder()
            .name("In game time")
            .description("Shows your ingame time")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );

    public ClockHud() {
        super(DATA);
        addSettingGroup(sgSettings);

        this.width = 14;
        this.height =  20;
        this.renderBg.setValue(true);
        this.rounded.setValue(true);
    }
    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        String text = "";

        if(inGameTime.value){
            text = getInGameTime();
        }else{
            text = getRealLifeTime();
        }

        this.width = (int) Renderer2D.getStringWidth(text);
        this.height = (int) Renderer2D.getStringHeight(text);

        super.renderElement(drawContext, textRenderer);
        Renderer2D.drawString(drawContext.getMatrices(), text, this.x, this.y, ColorManager.INSTANCE.hudColor);
    }

    private String getRealLifeTime() {
        LocalTime now = LocalTime.now();
        if (twentyFourHourFormat.value) {
            return now.truncatedTo(ChronoUnit.MINUTES).toString();
        } else {
            return now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
        }
    }

    private String getInGameTime() {
        if(mc.world == null){
            return "00:00";
        }

        long time = mc.world.getTimeOfDay() % 24000;
        int hours = (int) (time / 1000 + 6) % 24;
        int minutes = (int) (time % 1000 * 60 / 1000);
        return String.format("%02d:%02d", hours, minutes);
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if(inGameTime.value && realLifeTime.value){
            if(setting == realLifeTime){
                inGameTime.setValue(false);
            }else{
                realLifeTime.setValue(false);
            }
        }
    }
}
