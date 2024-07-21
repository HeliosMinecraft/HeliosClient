package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.world.ParticleEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.particle.ParticleTypes;

public class NoRender extends Module_ {
    SettingGroup sgWorld = new SettingGroup("World");
    SettingGroup sgHud = new SettingGroup("Hud");

    public BooleanSetting noNausea = sgHud.add(new BooleanSetting("NoNausea", "Disables nausea effect", this, false));
    public BooleanSetting noFire = sgHud.add(new BooleanSetting("NoFire", "Disables fire overlay effect", this, false));
    public BooleanSetting noLiquid = sgHud.add(new BooleanSetting("NoLiquid", "Disables liquid overlay effect", this, false));
    public BooleanSetting noPortal = sgHud.add(new BooleanSetting("NoPortal Overlay", "Disables portal overlay effect", this, false));
    public BooleanSetting noPumpkin = sgHud.add(new BooleanSetting("NoPumpkin", "Disables pumpkin overlay effect", this, false));
    public BooleanSetting noVignette = sgHud.add(new BooleanSetting("NoVignette", "Disables vignette overlay effect", this, false));
    public BooleanSetting noSpyglass = sgHud.add(new BooleanSetting("NoSpyglass", "Disables spyglass overlay effect", this, false));
    public BooleanSetting noTotemAnimation = sgHud.add(new BooleanSetting("NoTotemAnimation", "Disables totem animation", this, false));
    public BooleanSetting noBossBar = sgHud.add(new BooleanSetting("NoBossBar", "Disables bossbar rendering", this, false));
    public BooleanSetting noCrosshair = sgHud.add(new BooleanSetting("NoCrossHair", "Disables crosshair rendering", this, false));


    public BooleanSetting noExplosion = sgWorld.add(new BooleanSetting("NoExplosion", "Disables explosion rendering", this, false));
    public BooleanSetting noEnchantGlint = sgWorld.add(new BooleanSetting("NoEnchantGlint", "Disables enchant glint", this, false));
    public BooleanSetting noWeather = sgWorld.add(new BooleanSetting("NoWeather", "Disables weather", this, false));
    public BooleanSetting noBlindness = sgWorld.add(new BooleanSetting("NoBlindness", "Disables blindness effect", this, false));
    public BooleanSetting noDarkness = sgWorld.add(new BooleanSetting("NoDarkness", "Disables darkness", this, false));
    public BooleanSetting noBeaconBeam = sgWorld.add(new BooleanSetting("NoBeacon Beam", "Disables beacon beams rendering", this, false));
    public BooleanSetting noFog = sgWorld.add(new BooleanSetting("NoFog", "Disables fog rendering", this, false));
    public BooleanSetting noInvisible = sgWorld.add(new BooleanSetting("NoInvisible", "Makes invisible stuff become visible", this, false));

    public NoRender() {
        super("NoRender", "Prevents rendering overlays and other stuff", Categories.RENDER);
        addSettingGroup(sgWorld);
        addSettingGroup(sgHud);

        addQuickSettings(sgWorld.getSettings());
        addQuickSettings(sgHud.getSettings());
    }

    public static NoRender get() {
        return ModuleManager.get(NoRender.class);
    }

    @SubscribeEvent
    public void onParticle(ParticleEvent event) {
        if (event.parameters.getType() == ParticleTypes.RAIN && noWeather.value) {
            event.setCanceled(true);
        }

        if (event.parameters.getType() == ParticleTypes.EXPLOSION && noExplosion.value) {
            event.setCanceled(true);
        }
    }
}
