package dev.heliosclient.addon;

import dev.heliosclient.HeliosClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<HeliosAddon> HELIOS_ADDONS = new ArrayList<>();

    public static void initializeAddons() {
        for (HeliosAddon addon : HELIOS_ADDONS) {
            addon.onInitialize();
        }
    }

    public void loadAddons() {
        HeliosClient.LOGGER.info("Loading Addons....");
        // Get the current working directory
        String currentWorkingDir = System.getProperty("user.dir");

        File addonsDir = new File(currentWorkingDir);
        if (!addonsDir.exists()) {
            HeliosClient.LOGGER.info("Current folder directory missing for some reason");
            return;
        }

        for (EntrypointContainer<HeliosAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("helios", HeliosAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            HeliosAddon addon;

            try {
                addon = entrypoint.getEntrypoint();
            } catch (Throwable e) {
                throw new RuntimeException("An error has occurred during loading addon \"%s\"".formatted(metadata.getName()), e);
            }

            addon.name = metadata.getName();

            if (metadata.getAuthors().isEmpty()) {
                throw new RuntimeException("The authors field in fabric.mod.json must contain at least one author for addon \"%s\"".formatted(addon.name));
            }

            addon.authors = new String[metadata.getAuthors().size()];

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            HELIOS_ADDONS.add(addon);
        }
    }
}
