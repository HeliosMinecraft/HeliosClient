package dev.heliosclient.ui.clickgui.navbar;

import dev.heliosclient.module.Module_;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.ClientSettingsScreen;

import java.util.ArrayList;
import java.util.Arrays;

public class NavBarManager {
    public static NavBarManager INSTANCE = new NavBarManager();
    public ArrayList<NavBarItem> navBarItems = new ArrayList<>();

    public NavBarManager() {
        Module_ clickGUI = new ClickGUI();
        clickGUI.onLoad();

        registerItems(
            new NavBarItem("ClickGUI", "ClickGUI", ClickGUIScreen.INSTANCE),
            new NavBarItem("Settings", "ClickGUI Setting", new ClientSettingsScreen(clickGUI))
        );
    }

    public void registerItems(NavBarItem ... items) {
        this.navBarItems.addAll(Arrays.asList(items));
    }
}
