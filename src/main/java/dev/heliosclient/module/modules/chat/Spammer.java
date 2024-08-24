package dev.heliosclient.module.modules.chat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.client.OpenScreenEvent;
import dev.heliosclient.event.events.player.DisconnectEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.system.UniqueID;
import dev.heliosclient.system.config.Config;
import dev.heliosclient.util.*;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.util.math.random.Random;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Spammer extends Module_ {
    public File spamFile = null;
    public Random rand = Random.create();
    TickTimer timer = new TickTimer();
    String[] spamLines = null;
    int lineIndex;
    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting randomLine = sgGeneral.add(new BooleanSetting.Builder()
            .name("Random")
            .description("Randomly selects a line")
            .value(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting antiantiSpam = sgGeneral.add(new BooleanSetting.Builder()
            .name("Anti - AntiSpam")
            .description("Adds random letters at the end of message of given length to bypass anti spams")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    DoubleSetting antispam_length = sgGeneral.add(new DoubleSetting.Builder()
            .name("Anti-Anti Spam length")
            .description("Length of the random letters added at the end of the message")
            .onSettingChange(this)
            .value(7d)
            .defaultValue(7d)
            .min(0)
            .max(256)
            .roundingPlace(0)
            .shouldRender(() -> antiantiSpam.value)
            .build()
    );
    BooleanSetting commands = sgGeneral.add(new BooleanSetting.Builder()
            .name("Send as command")
            .description("Sends the messages as commands instead of player messages.")
            .value(false)
            .onSettingChange(this)
            .build()
    );
    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Mode to find spam messages")
            .value(List.of(Mode.values()))
            .defaultListOption(Mode.String)
            .onSettingChange(this)
            .build()
    );
    StringListSetting spamMessages = sgGeneral.add(new StringListSetting.Builder()
            .name("Spam Messages")
            .defaultValue(new String[]{"HeliosClient on top!", "HeliosClient is based asf", "I am me!"})
            .defaultBoxes(3)
            .inputMode(InputBox.InputMode.ALL)
            .characterLimit(Integer.MAX_VALUE)
            .shouldRender(() -> mode.getOption() == Mode.String)
            .build()
    );
    ButtonSetting selectFile = sgGeneral.add(new ButtonSetting.Builder()
            .name("File selected: None")
            .defaultValue(false)
            .shouldRender(() -> mode.getOption() == Mode.File)
            .build()
    );
    DoubleSetting delay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Delay")
            .description("Delay in ticks")
            .onSettingChange(this)
            .value(10d)
            .defaultValue(10d)
            .min(0)
            .max(1200)
            .roundingPlace(0)
            .build()
    );
    BooleanSetting toggleOnDisconnect = sgGeneral.add(new BooleanSetting.Builder()
            .name("Disable on disconnect")
            .description("Toggles the module off when you disconnect")
            .value(true)
            .onSettingChange(this)
            .build()
    );

    public Spammer() {
        super("Spammer", "Spams messages in chat", Categories.MISC);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

        selectFile.addButton("Select a text file", 0, 0, () -> {
            FileUtils.openTinyFileDialog("canvas.txt", (file -> spamFile = file), false);

            if (spamFile != null) {
                selectFile.setButtonCategoryText("File Selected: " + spamFile.getName());
            }
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lineIndex = 0;
        timer.startTicking();
        setSpamLines();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        timer.resetTimer();
    }

    public void setSpamLines() {
        if (mode.getOption() == Mode.File) {
            if (spamFile == null) {
                ChatUtils.sendHeliosMsg(ColorUtils.darkRed + "Spam file has not been selected, toggling off");
                toggle();
                return;
            }

            //Read file and get the lines as an array
            spamLines = FileUtils.readLines(spamFile.getPath());
        } else {
            spamLines = spamMessages.value;
        }
    }

    @SubscribeEvent
    public void onDisconnectScreen(OpenScreenEvent event) {
        if(toggleOnDisconnect.value && (event.screen instanceof DisconnectedScreen || event.screen instanceof DisconnectedRealmsScreen)){
            toggle();
        }
    }

    @SubscribeEvent
    public void onDisconnect(DisconnectEvent event) {
        if(toggleOnDisconnect.value){
            toggle();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        timer.incrementAndEvery(delay.getInt(),() -> {
            String lineToSend = next();
            if (antiantiSpam.value) {
                lineToSend += " " + UniqueID.setLengthAndGet((int) antispam_length.value).getUniqueID();
            }
            if(commands.value){
                ChatUtils.sendChatCommand(lineToSend);
            } else {
                ChatUtils.sendPlayerMessage(lineToSend);
            }
        });
    }

    public String next() {
        if (!randomLine.value) {
            lineIndex = (lineIndex + 1) % spamLines.length;
        } else {
            lineIndex = rand.nextBetween(0, spamLines.length - 1);
        }
        return spamLines[lineIndex];
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if (setting == mode) {
            setSpamLines();
        }
    }

    @Override
    public Object saveToFile(List<Object> list) {
        Map<String,Object> map = Config.cast(super.saveToFile(list));

        map.put("File",spamFile == null ? "unknown" : spamFile.getAbsolutePath());

        return map;
    }

    @Override
    public void loadFromFile(Map<String, Object> MAP) {
        super.loadFromFile(MAP);

        String filePath = (String) MAP.get("File");
        if(filePath == null || filePath.equals("unknown")) return;
        spamFile = new File(filePath);

        if (spamFile.exists() && spamFile.isFile()) {
            selectFile.setButtonCategoryText("File Selected: " + spamFile.getName());
        }
    }

    public enum Mode {
        File,
        String
    }
}
