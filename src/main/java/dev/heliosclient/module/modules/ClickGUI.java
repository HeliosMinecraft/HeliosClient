package dev.heliosclient.module.modules;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.ColorSetting;
import dev.heliosclient.util.ColorUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ClickGUI extends Module_ {
    ColorSetting CategoryColor = new ColorSetting("CategoryColor color", HeliosClient.categoryColor);
    ColorSetting TextColor = new ColorSetting("ClickGUI Text Color", HeliosClient.textColor.getRGB());
    BooleanSetting Rainbow = new BooleanSetting("Rainbow",false);
    //ColorSetting ThemeColor = new ColorSetting("ClickGUI theme color",0);



    public ClickGUI() {
        super("ClickGUI", "CLickGui related stuff.",  Category.RENDER);
        settings.add(CategoryColor);
        settings.add(TextColor);
        settings.add(Rainbow);
        active.value=true;
    }
    @Override
    public void onEnable() {
        super.onEnable();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Rainbow.value)
            {
                HeliosClient.categoryColor = ColorUtils.getRainbowColor().getRGB();
                HeliosClient.textColor = ColorUtils.getRainbowColor();
            }
            else {
                HeliosClient.categoryColor = CategoryColor.value;
                HeliosClient.textColor = ColorUtils.intToColor(TextColor.value);
            }
        });
    }
}
