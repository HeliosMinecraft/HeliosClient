package dev.heliosclient.managers;

import dev.heliosclient.addon.AddonManager;
import dev.heliosclient.addon.HeliosAddon;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.chat.ChatHighlight;
import dev.heliosclient.module.modules.misc.CapeModule;
import dev.heliosclient.module.modules.misc.DiscordRPCModule;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.module.modules.misc.ScriptModule;
import dev.heliosclient.module.modules.movement.Fly;
import dev.heliosclient.module.modules.movement.Speed;
import dev.heliosclient.module.modules.movement.Step;
import dev.heliosclient.module.modules.player.NoFall;
import dev.heliosclient.module.modules.render.*;
import dev.heliosclient.util.MathUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.ArrayList;
import java.util.Set;

public class ModuleManager {
    public static ModuleManager INSTANCE = new ModuleManager();

    Set<Module_> modules = new ObjectArraySet<>();

    public ModuleManager() {
        registerModules(
                new Fly(),
                new NoFall(),
                GUI.get(),
                new HUDModule(),
                new Step(),
                new Fullbright(),
                new Speed(),
                new CustomFov(),
                new Test(),
                new ChatHighlight(),
                new DiscordRPCModule(),
                NotificationModule.get(),
                CapeModule.get(),
                ScriptModule.get()
        );

        AddonManager.HELIOS_ADDONS.forEach(HeliosAddon::registerModules);
    }

    public void registerModule(Module_ module) {
        modules.add(module);
        module.onLoad();
    }

    public void registerModules(Module_... modules) {
        for (Module_ module : modules) {
            this.modules.add(module);
            module.onLoad();
        }
    }

    public Module_ getModuleByName(String moduleName) {
        for (Module_ module : modules) {
            if ((module.name.trim().equalsIgnoreCase(moduleName))) {
                return module;
            }
        }
        return null;
    }

    public ArrayList<Module_> getModuleByNameSearch(String moduleName) {
        ArrayList<Module_> moduleS = new ArrayList<>();
        for (Module_ module : modules) {
            if (!moduleName.isEmpty() && module.name.trim().equalsIgnoreCase(moduleName.trim())) {
                moduleS.add(module);
                return moduleS;
            }
            if (!moduleName.isEmpty() && MathUtils.jaroWinklerSimilarity(module.name.trim().toLowerCase(), moduleName.trim().toLowerCase()) >= 0.66) {
                moduleS.add(module);
            }
        }
        moduleS.sort((m1, m2) -> {
            double m1Score = MathUtils.jaroWinklerSimilarity(m1.name, moduleName);
            double m2Score = MathUtils.jaroWinklerSimilarity(m2.name, moduleName);
            return Double.compare(m2Score, m1Score);
        });
        return moduleS;
    }


    public ArrayList<Module_> getModulesByCategory(Category category) {
        ArrayList<Module_> returnedModules = new ArrayList<>();
        for (Module_ module : modules) {
            if (module.category == category) {
                returnedModules.add(module);
            }
        }
        return returnedModules;
    }

    public ArrayList<Module_> getEnabledModules() {
        ArrayList<Module_> enabledModules = new ArrayList<>();
        for (Module_ module : modules) {
            if (!module.active.value)
                continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public Set<Module_> getModules() {
        return modules;
    }
}
