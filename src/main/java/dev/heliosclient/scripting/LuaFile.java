package dev.heliosclient.scripting;

import dev.heliosclient.event.events.input.KeyPressedEvent;

import java.io.*;

/**
 * This class represents a Lua script file.
 */
public class LuaFile extends File {
    private final LuaExecutor executor;
    public boolean isLoaded = false;
    public int bindKey = -1;
    /**
     * Managed in {@link  dev.heliosclient.managers.KeybindManager#keyPressedEvent(KeyPressedEvent)}
     */
    public boolean isListeningForBind = false;

    /**
     * Constructs a new LuaFile with the given file and executor.
     *
     * @param path The path of the file.
     * @param luaExecutor The executor to run the Lua script.
     */
    public LuaFile(String path,LuaExecutor luaExecutor) {
        super(path);
        this.executor = luaExecutor;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Retrieves a reader for the Lua script file.
     *
     * @return A reader for the Lua script file.
     * @throws FileNotFoundException If the Lua script file does not exist.
     */
    public Reader getReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(this));
    }
    /**
     * Retrieves a Buffered reader for the Lua script file.
     *
     * @return A reader for the Lua script file.
     * @throws FileNotFoundException If the Lua script file does not exist.
     */
    public BufferedReader getBufferedReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(this));
    }

    public File getFile() {
        return this;
    }

    /**
     * Retrieves the name of the Lua script.
     *
     * @return The name of the Lua script.
     */
    public String getScriptName() {
        return this.getName().replace(".lua", "");
    }

    public void setBindKey(int bindKey) {
        this.bindKey = bindKey;
    }

    public int getBind() {
        return bindKey;
    }

    public void setListening(boolean isListeningForBind) {
        this.isListeningForBind = isListeningForBind;
    }

    /**
     * Retrieves the executor of the Lua script.
     *
     * @return The executor of the Lua script.
     */
    public LuaExecutor getExecutor() {
        return executor;
    }


    /**
     * Retrieves the text inside the Lua script file.
     *
     * @return The text inside the Lua script file.
     * @throws IOException If an I/O error occurs.
     */
    public String getText() throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = getBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        }
        return text.toString();
    }
}
