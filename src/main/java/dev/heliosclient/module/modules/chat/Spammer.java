package dev.heliosclient.module.modules.chat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.system.UniqueID;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.FileUtils;
import dev.heliosclient.util.InputBox;
import net.minecraft.util.math.random.Random;

import java.io.File;
import java.util.List;

public class Spammer extends Module_ {
    public File spamFile = null;
    public Random rand = Random.create();
    int timer = 0;
    String[] spamLines = null;
    int lineIndex;
    SettingGroup sgGeneral = new SettingGroup("General");
    public BooleanSetting randomLine = sgGeneral.add(new BooleanSetting.Builder()
            .name("Random")
            .description("Randomly selects a line")
            .value(true)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting antiantiSpam = sgGeneral.add(new BooleanSetting.Builder()
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
            .value(10d)
            .defaultValue(10d)
            .min(0)
            .max(256)
            .roundingPlace(0)
            .shouldRender(() -> antiantiSpam.value)
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
        timer = 0;
        setSpamLines();
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
    public void onTick(TickEvent.PLAYER event) {
        if (timer > delay.value) {
            timer = 0;

            String lineToSend = next();
            if (antiantiSpam.value) {
                lineToSend += " " + UniqueID.setLengthAndGet((int) antispam_length.value).getUniqueID();
            }
            ChatUtils.sendPlayerMessage(lineToSend);
        } else {
            timer++;
        }
    }

    public String next() {
        if (!randomLine.value) {
            lineIndex++;

            if (lineIndex > spamLines.length - 1) {
                lineIndex = 0;
            }

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

    public enum Mode {
        File,
        String
    }
}
