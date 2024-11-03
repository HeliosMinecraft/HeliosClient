package dev.heliosclient.managers;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.addon.HeliosAddon;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.chat.ChatHighlight;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import dev.heliosclient.module.modules.chat.Spammer;
import dev.heliosclient.module.modules.combat.*;
import dev.heliosclient.module.modules.misc.*;
import dev.heliosclient.module.modules.movement.*;
import dev.heliosclient.module.modules.player.*;
import dev.heliosclient.module.modules.render.*;
import dev.heliosclient.module.modules.render.hiteffect.HitEffect;
import dev.heliosclient.module.modules.world.*;
import dev.heliosclient.module.modules.world.painter.Painter;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleManager {
    static Set<Module_> modules = new ObjectArraySet<>();
    private static final ConcurrentHashMap<Class<?>, Module_> moduleResultCache = new ConcurrentHashMap<>();

    public static void init() {
        modules.clear();
        moduleResultCache.clear();

        //Combat
        registerModules(
                new AutoTotem(),
                new AimAssist(),
                new BowSpam(),
                new Criticals(),
                new TriggerBot()
        );

        // World
        registerModules(
                new AntiBot(),
                new AntiBookBan(),
                new SpeedMine(),
                new PacketMine(),
                new PacketPlace(),
                new NoInteract(),
                new Timer(),
                new TNTIgnite(),
                new NewChunks(),
                new PaletteExploit(),
                new AutoNametag(),
                new BetterPortals(),
                new LiquidInteract(),
                new ServerScraper(),
                new Painter(),
                new SignTweaks(),
                new Collisions(),
                new AbortBreaking(),
                new AntiGhostBlocks(),
                new ChatHighlight()
        );
        // Misc
        registerModules(
                new InventoryTweaks(),
                new AutoReconnect(),
                new AutoLog(),
                new NoSwing(),
                new NoNarrator(),
                new Spammer(),
                new BrandSpoof(),
                new DiscordRPCModule(),
                new CapeModule(),
                new NotificationModule(),
                new UnfocusedCPU(),
                new ChestAura(),
                new Fucker(),
                new SilentClose(),
                new ChatTweaks(),
                // new ScriptModule(),
                new Teams()
        );
        // Movement
        registerModules(
                new AirJump(),
                new AutoJump(),
                new AutoSneak(),
                new AutoWalk(),
                new Sprint(),
                new AntiVoid(),
                new EntityControl(),
                new EntitySpeed(),
                new Fly(),
                new BoatFly(),
                new GuiMove(),
                new NoFall(),
                new NoSlow(),
                new Jesus(),
                new Phase(),
                new NoJumpDelay(),
                new NoLevitation(),
                new Scaffold(),
                new SafeWalk(),
                new Slippy(),
                new Spider(),
                new Speed(),
                new Step(),
                new Velocity(),
                new TargetStrafe(),
                new TridentTweaker(),
                new TickShift()
        );

        // Player
        registerModules(
                new AirPlace(),
                new AntiHunger(),
                new AutoClicker(),
                new AutoTool(),
                new AutoEat(),
                new AntiAFK(),
                new AutoRespawn(),
                new InventoryCleaner(),
                new NoMiningTrace(),
                new FakeLag(),
                new FastUse(),
                new ExpThrower(),
                new NoRotate(),
                new Rotation(),
                new Reach(),
                new NoBreakDelay(),
                new TpsSync(),
                new PingSpoof(),
                new XCarry()
        );

        // Render
        registerModules(
                new GUI(),
                new HUDModule(),
                new AspectRatio(),
                new TimeChanger(),
                new Fullbright(),
                new CustomFov(),
                new BlockSelection(),
                new BreakIndicator(),
                new CustomCrosshair(),
                new Freecam(),
                new FreeLook(),
                new HitEffect(),
                new Zoom(),
                new EntityOwner(),
                new Trail(),
                new NoRender(),
                new NameTags(),
                new ESP(),
                new CrystalESP(),
                new HoleESP(),
                new BlockESP(),
                new StorageESP(),
                new LightLevelESP(),
                new ItemPhysics(),
                new TntTimer(),
                new LogOutSpot(),
                new Xray(),
                new ViewModel(),
                new Test()
        );

        AddonManager.HELIOS_ADDONS.forEach(HeliosAddon::registerModules);
    }

    public static void registerModule(Module_ module) {
        if(!modules.contains(module)) {
            modules.add(module);
            module.onLoad();
            moduleResultCache.put(module.getClass(),module);
        }
    }

    public static void registerModules(Module_... modules) {
        for (Module_ module : modules) {
            registerModule(module);
        }
    }

    public static Module_ getModuleByName(String moduleName) {
        for (Module_ module : modules) {
            if (module.name.trim().equalsIgnoreCase(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static <T extends Module_> T get(Class<T> moduleClazz) {
        // Check the cache first
        Module_ cachedModule = moduleResultCache.get(moduleClazz);
        if (cachedModule != null) {
            return moduleClazz.cast(cachedModule);
        }

        // If not in cache, find the module and cache it
        for (Module_ module : modules) {
            if (moduleClazz.isInstance(module)) {
                moduleResultCache.put(moduleClazz, module);
                return moduleClazz.cast(module);
            }
        }
        return null;
    }


    public static ArrayList<Module_> getModuleByNameSearch(String moduleName, int amount) {
        ArrayList<Module_> moduleS = new ArrayList<>();

        for (Module_ module : modules) {
            if (moduleS.size() > amount) {
                break;
            }

            if (!moduleName.isEmpty() && module.name.trim().equalsIgnoreCase(moduleName.trim())) {
                moduleS.add(module);
                return moduleS;
            }
            if (!moduleName.isEmpty() && module.name.contains(moduleName)) {
                moduleS.add(module);
            }
        }

        moduleS.sort((m1, m2) -> {
            int m1Score = StringUtils.getLevenshteinDistance(m1.name.trim().toLowerCase(), moduleName.trim().toLowerCase());
            int m2Score = StringUtils.getLevenshteinDistance(m2.name.trim().toLowerCase(), moduleName.trim().toLowerCase());
            return Integer.compare(m2Score, m1Score);
        });

        return moduleS;
    }


    public static ArrayList<Module_> getModulesByCategory(Category category) {
        ArrayList<Module_> returnedModules = new ArrayList<>();
        for (Module_ module : modules) {
            if (module.category == category) {
                returnedModules.add(module);
            }
        }
        return returnedModules;
    }

    public static ArrayList<Module_> getEnabledModules() {
        ArrayList<Module_> enabledModules = new ArrayList<>();
        for (Module_ module : modules) {
            if (!module.active.value)
                continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public static Set<Module_> getModules() {
        return modules;
    }
}
