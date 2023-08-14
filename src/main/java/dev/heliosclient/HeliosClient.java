package dev.heliosclient;

import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.CategoryPane;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.util.ColorUtils;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;

import dev.heliosclient.module.settings.Setting;

public class HeliosClient implements ModInitializer
{
	public static final HeliosClient INSTANCE = new HeliosClient();
	public static final Logger LOGGER = LoggerFactory.getLogger("Helios Client");
	public static final String clientTag = ColorUtils.red + "Helios Client";
	public static final String versionTag = ColorUtils.gray + "v0.dev";
	public static Config CONFIG = new Config();
	public static int uiColorA = 0xFF55FFFF;
	public static int uiColor = 0x55FFFF;
	public static int categoryColor = new Color(255, 110, 119, 255).getRGB();
	public static Color textColor = new Color(255, 110, 119, 255);

	//public static int categoryColor = new Color(255, 110, 119, 255).getRGB();


	@Override
	public void onInitialize() 
	{
		LOGGER.info("Helios Client loading...");
		loadConfig();
	}

	public void loadConfig()
	{
		LOGGER.info("Loading config...");
		if (CONFIG.doesConfigExist())
		{
			CONFIG.loadDefaultConfig(); 
			CONFIG.save();
		}
		CONFIG.load();
		for (Module_ m : ModuleManager.INSTANCE.modules)
		{
			for (Setting s : m.settings)
			{
				s.value = ((Map<String, Object>)((Map<String, Object>)CONFIG.config.get("modules")).get(m.name)).get(s.name);
			}
		}
	}

	public void saveConfig()
	{
		LOGGER.info("Saving config...");
		Map<String, Object> mi = new HashMap<>();
        for (Module_ m : ModuleManager.INSTANCE.modules)
		{
			Map<String, Object> mo = new HashMap<>();
			for (Setting s : m.settings)
			{
                mo.put(s.name, s.value);
			}
            mi.put(m.name, mo);
		}
        CONFIG.config.put("modules", mi);
        Map<String, Object> pi = new HashMap<>();
        for (CategoryPane c : ClickGUIScreen.INSTANCE.categoryPanes)
		{ 
            Map<String, Object> po = new HashMap<>();
			po.put("x", c.x);
            po.put("y", c.y);
			po.put("collapsed", c.collapsed);
            pi.put(c.category.name, po);
		}
        CONFIG.config.put("panes", pi);
		CONFIG.save();
	}
}
