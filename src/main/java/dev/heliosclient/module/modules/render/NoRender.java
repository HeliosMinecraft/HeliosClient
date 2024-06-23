package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.block.BlockBreakEvent;
import dev.heliosclient.event.events.world.ParticleEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.particle.ParticleTypes;

public class NoRender extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting noNausea = sgGeneral.add(new BooleanSetting("NoNausea", "Disables nausea effect", this, false));
    public BooleanSetting noFire = sgGeneral.add(new BooleanSetting("NoFire", "Disables fire overlay effect", this, false));
    public BooleanSetting noLiquid = sgGeneral.add(new BooleanSetting("NoLiquid", "Disables liquid overlay effect", this, false));
    public BooleanSetting noPortal = sgGeneral.add(new BooleanSetting("NoPortal Overlay", "Disables portal overlay effect", this, false));
    public BooleanSetting noPumpkin = sgGeneral.add(new BooleanSetting("NoPumpkin", "Disables pumpkin overlay effect", this, false));
    public BooleanSetting noVignette = sgGeneral.add(new BooleanSetting("NoVignette", "Disables vignette overlay effect", this, false));
    public BooleanSetting noSpyglass = sgGeneral.add(new BooleanSetting("NoSpyglass", "Disables spyglass overlay effect", this, false));
    public BooleanSetting noTotemAnimation = sgGeneral.add(new BooleanSetting("NoTotemAnimation", "Disables totem animation", this, false));
    public BooleanSetting noBossBar = sgGeneral.add(new BooleanSetting("NoBossBar", "Disables bossbar rendering", this, false));
    public BooleanSetting noCrosshair = sgGeneral.add(new BooleanSetting("NoCrossHair", "Disables crosshair rendering", this, false));
    public BooleanSetting noExplosion = sgGeneral.add(new BooleanSetting("NoExplosion", "Disables explosion rendering", this, false));
    public BooleanSetting noEnchantGlint = sgGeneral.add(new BooleanSetting("NoEnchantGlint", "Disables enchant glint", this, false));
    public BooleanSetting noWeather = sgGeneral.add(new BooleanSetting("NoWeather", "Disables weather", this, false));
    public BooleanSetting noBlindness = sgGeneral.add(new BooleanSetting("NoBlindness", "Disables blindness effect", this, false));
    public BooleanSetting noDarkness = sgGeneral.add(new BooleanSetting("NoDarkness", "Disables darkness", this, false));
    public BooleanSetting noFog = sgGeneral.add(new BooleanSetting("NoFog", "Disables fog rendering", this, false));
    public BooleanSetting noInvisible = sgGeneral.add(new BooleanSetting("NoInvisible", "Makes invisible stuff become visible", this, false));

    public NoRender() {
        super("NoRender", "Prevents rendering overlays and other stuff", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

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
