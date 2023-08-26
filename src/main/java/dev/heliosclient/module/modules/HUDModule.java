package dev.heliosclient.module.modules;

import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.ColorSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.ui.HUDOverlay;
import dev.heliosclient.util.ColorUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.awt.*;

public class HUDModule extends Module_
{
    public BooleanSetting clientTag = new BooleanSetting("Client Tag", "Visibility of Client Tag.", this, true);
    public ColorSetting colorSetting = new ColorSetting("Color", "Color of HUD.", this, new Color(241, 83, 92, 255).getRGB());
    BooleanSetting Rainbow = new BooleanSetting("Rainbow", "Toggles rainbow effect for HUD.", this,false);
    public int hudColor = colorSetting.value;

    public HUDModule()
    {
        super("HUD", "The HeliosClient HUD. Toggle to update.", Category.RENDER);
        this.active.value = true;
        this.showInModulesList.value = false;
        
        settings.add(clientTag);
        settings.add(colorSetting);
        settings.add(Rainbow);
        //settings.add(color);
    }    

    @Override
    public void onEnable()
    {
        super.onEnable();
        HUDOverlay.INSTANCE.showClientTag = clientTag.value;
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (Rainbow.value)
            {
                HeliosClient.uiColorA = ColorUtils.getRainbowColor().getRGB();
                HeliosClient.uiColor = ColorUtils.getRainbowColor().getRGB();
            }
           else {
                    HeliosClient.uiColorA = colorSetting.value;
                    HeliosClient.uiColor = colorSetting.value;
            }
        });
    }

    @Override
    public void onTick(TickEvent event)
    {
        HUDOverlay.INSTANCE.showClientTag = clientTag.value;
        if (!Rainbow.value){
            HeliosClient.uiColorA = colorSetting.value;
            HeliosClient.uiColor = colorSetting.value;
        }
    }
}
