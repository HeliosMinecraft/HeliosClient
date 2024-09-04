package dev.heliosclient.scripting;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.managers.NotificationManager;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.SoundUtils;
import org.luaj.vm2.LuaValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class is responsible for loading Lua scripts from a specified directory.
 */
public class LuaLoader {
    File SCRIPTS_FOLDER = new File(HeliosClient.MC.runDirectory + "/heliosclient/", "scripts");

    /**
     * Loads a Lua script file into the Lua environment.
     * <p>
     * Calls the {@code onRun() } method after loading the lua file
     * </p>
     *
     * @param luaFile The Lua file to load.
     */
    public void load(LuaFile luaFile) {
        if (luaFile.isLoaded()) {
            ChatUtils.sendHeliosMsg(ColorUtils.darkRed + "Script is already loaded: " + ColorUtils.blue + luaFile.getAbsolutePath());
            return;
        }
        try {
            Reader reader = luaFile.getReader();
            LuaValue chunk = luaFile.getExecutor().load(reader);
            chunk.call();

            LuaValue onRunFunction = luaFile.getExecutor().getFunction("onRun");
            if (onRunFunction.isfunction()) {
                onRunFunction.call();
            } else {
                ChatUtils.sendHeliosMsg(ColorUtils.darkRed + "onRun() function not found for " + ColorUtils.blue + luaFile.getAbsolutePath() + ColorUtils.darkRed + " while loading");
                HeliosClient.LOGGER.error("onRun() function not found for file: {} while loading", luaFile.getName());
            }
            luaFile.isLoaded = true;
            ChatUtils.sendHeliosMsg(ColorUtils.green + "Loaded LuaFile" + ColorUtils.gray + " [" + ColorUtils.aqua + luaFile.getName() + ColorUtils.gray + "]");
            if (HeliosClient.shouldSendNotification() && ModuleManager.get(NotificationModule.class).scriptNotifications.value) {
                NotificationManager.addNotification(new InfoNotification(luaFile.getName(), "was loaded!", 1000, SoundUtils.TING_SOUNDEVENT));
            }

        } catch (FileNotFoundException e) {
            luaFile.isLoaded = false;
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the reader of a Lua file.
     * <p>
     * Calls the {@code onStop() } method before closing the lua file
     * </p>
     *
     * @param file The Lua file to close.
     */
    public void close(LuaFile file) {
        if (!file.isLoaded()) {
            ChatUtils.sendHeliosMsg(ColorUtils.darkRed + "Script not loaded: " + ColorUtils.blue + file.getAbsolutePath());
            return;
        }

        try {
            LuaValue onStopFunction = file.getExecutor().getFunction("onStop");
            if (onStopFunction.isfunction()) {
                onStopFunction.call();
            } else {
                ChatUtils.sendHeliosMsg(ColorUtils.darkRed + "onStop() function not found for " + ColorUtils.blue + file.getAbsolutePath() + ColorUtils.darkRed + " while closing");
                HeliosClient.LOGGER.error("onStop() function not found for file: {} while closing", file.getAbsolutePath());
            }

            file.getReader().close();

            //Call the Garbage collector to collect any leftover garbage by the script. Might cause lag or crash if unused properly
            System.gc();

            file.isLoaded = false;
            ChatUtils.sendHeliosMsg(ColorUtils.green + "Closed LuaFile" + ColorUtils.gray + " [" + ColorUtils.aqua + file.getScriptName() + ColorUtils.gray + "]");
            if (HeliosClient.shouldSendNotification() && ModuleManager.get(NotificationModule.class).scriptNotifications.value) {
                NotificationManager.addNotification(new InfoNotification(file.getScriptName(), "was unloaded!", 1000, SoundUtils.TING_SOUNDEVENT));
            }

            // Clear all event listeners for the Lua file
            LuaEventManager.INSTANCE.clearListeners(file);
        } catch (IOException e) {
            file.isLoaded = false;
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all Lua scripts from the scripts folder.
     *
     * @return A list of Lua files.
     */
    public List<LuaFile> getScripts() {
        ensureScriptsFolderExists();
        List<LuaFile> files = Arrays.stream(Objects.requireNonNull(SCRIPTS_FOLDER.listFiles()))
                .map(this::createLuaFile)
                .toList();

        LuaScriptManager.luaFiles.clear();
        return files;
    }

    public void toggleScript(LuaFile file) {
        if (file.isLoaded()) {
            this.close(file);
        } else {
            this.load(file);
        }
    }

    /**
     * Retrieves a single Lua script from the scripts' folder.
     *
     * @return The LuaFile
     */
    public LuaFile getScript(LuaFile file) {
        ensureScriptsFolderExists();
        LuaExecutor executor = new LuaExecutor(HeliosClient.MC, LuaEventManager.INSTANCE);

        return Arrays.stream(Objects.requireNonNull(SCRIPTS_FOLDER.listFiles()))
                .filter(f -> f.toString().equalsIgnoreCase(file.toString()))
                .findFirst()
                .map(file1 -> {
                    LuaFile luaFile = new LuaFile(file1.getPath(), executor);
                    luaFile.setBindKey(file.bindKey);
                    luaFile.isLoaded = false;
                    return luaFile;
                })
                .orElse(null);
    }

    private void ensureScriptsFolderExists() {
        if (!SCRIPTS_FOLDER.exists()) {
            SCRIPTS_FOLDER.mkdirs();
        }
    }

    private LuaFile createLuaFile(File f) {
        HeliosClient.LOGGER.info("Getting Script {}", f.getAbsolutePath());
        LuaExecutor executor = new LuaExecutor(HeliosClient.MC, LuaEventManager.INSTANCE);
        LuaFile file;
        for (int i = 0; i < LuaScriptManager.luaFiles.size(); i++) {
            file = LuaScriptManager.luaFiles.get(i);
            if (f.toString().equalsIgnoreCase(file.toString())) {
                file = new LuaFile(f.getPath(), executor);
                file.setBindKey(file.bindKey);
                return file;
            }

        }
        return new LuaFile(f.getPath(), executor);
    }

}
