package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.ClientSettingsScreen;
import dev.heliosclient.ui.clickgui.hudeditor.HudEditorScreen;
import dev.heliosclient.ui.clickgui.navbar.NavBarItem;

import java.util.ArrayList;
import java.util.Arrays;

public class NavBarManager {
    public static NavBarManager INSTANCE = new NavBarManager();
    public ArrayList<NavBarItem> navBarItems = new ArrayList<>();

    public NavBarManager() {
        registerItems(
                new NavBarItem("ClickGUI", "ClickGUI", () -> ClickGUIScreen.INSTANCE),
                new NavBarItem("Settings", "ClickGUI Setting", new ClientSettingsScreen(HeliosClient.CLICKGUI)),
                new NavBarItem("HudEditor", "HUD editor", HudEditorScreen.INSTANCE)
        );
    }

    public void registerItems(NavBarItem... items) {
        this.navBarItems.addAll(Arrays.asList(items));
    }
}
