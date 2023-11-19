package dev.heliosclient.managers;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.*;

import java.util.ArrayList;

public class ModuleManager {
    public static ModuleManager INSTANCE = new ModuleManager();
    public ArrayList<Module_> modules = new ArrayList<>();

    public ModuleManager() {
        registerModules(
                new Fly(),
                new NoFall(),
                new HUDModule(),
                new Step(),
                new Fullbright(),
                new Speed(),
                new CustomFov(),
                new Test(),
                new ChatHighlight(),
                NotificationModule.INSTANCE
        );
    }

    public void registerModule(Module_ module) {
        modules.add(module);
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
            if (!moduleName.isEmpty() && (module.name.trim().toLowerCase().startsWith(moduleName.trim().toLowerCase()) || module.description.trim().toLowerCase().contains(moduleName.trim().toLowerCase()))) {
                moduleS.add(module);
            }
        }
        moduleS.sort((m1, m2) -> {
            int m1Score = getRelevanceScore(m1.name, moduleName);
            int m2Score = getRelevanceScore(m2.name, moduleName);
            return Integer.compare(m2Score, m1Score);
        });
        return moduleS;
    }

    private int getRelevanceScore(String name, String query) {
        String lowerCaseName = name.toLowerCase();
        String lowerCaseQuery = query.toLowerCase();
        if (lowerCaseName.equals(lowerCaseQuery)) {
            return 3;
        } else if (lowerCaseName.startsWith(lowerCaseQuery)) {
            return 2;
        } else if (lowerCaseName.contains(lowerCaseQuery)) {
            return 1;
        } else {
            return 0;
        }
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
}
