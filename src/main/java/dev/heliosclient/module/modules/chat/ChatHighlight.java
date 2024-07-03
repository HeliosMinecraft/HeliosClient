package dev.heliosclient.module.modules.chat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.ChatMessageEvent;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringListSetting;
import dev.heliosclient.system.Friend;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ChatHighlight extends Module_ {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    private final char sectionSymbol = '§';
    private final SettingGroup sgGeneral = new SettingGroup("General");
    public HashSet<String> highlightList = new HashSet<>();

    CycleSetting checkCase = sgGeneral.add(new CycleSetting.Builder()
            .name("Case check")
            .description("Modifies the message to the appropriate case before checking. Does not affect the displayed message, purely for checking words")
            .onSettingChange(this)
            .defaultValue(List.of(CaseCheck.values()))
            .defaultListOption(CaseCheck.UPPER_CASE)
            .build()
    );

    BooleanSetting highlightUsername = sgGeneral.add(new BooleanSetting.Builder()
            .name("Highlight Username")
            .description("Whether to highlight your username or not.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting regexSupport = sgGeneral.add(new BooleanSetting.Builder()
            .name("Support regex")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting highlightFriends = sgGeneral.add(new BooleanSetting.Builder()
            .name("Highlight Friends")
            .description("Whether to highlight friend's username or not.")
            .onSettingChange(this)
            .value(false)
            .defaultValue(true)
            .build()
    );
    CycleSetting colors = sgGeneral.add(new CycleSetting.Builder()
            .name("Color of the highlight")
            .onSettingChange(this)
            .defaultValue((List<?>) Formatting.getNames(true, true))
            .defaultListOption("red")
            .build()
    );
    StringListSetting wordsToHighlight = sgGeneral.add(new StringListSetting.Builder()
            .name("Words to highlight")
            .defaultBoxes(1)
            .defaultValue(new String[]{""})
            .characterLimit(100)
            .inputMode(InputBox.InputMode.ALL)
            .build()
    );

    public ChatHighlight() {
        super("Chat Highlighter", "Highlights specified words in chat messages. Note it will remove other styles of chat sent", Categories.WORLD);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

        //Test
        this.highlightList.add("ElBe_Gaming");
        this.highlightList.add("tanishisherewith");
    }

    public void checkForHighlights() {
        //StringList setting does not have a iSettingChange invoke, so we have to deal with it here.
        this.highlightList.clear();
        for (String word : wordsToHighlight.value) {
            if (!word.isEmpty()) {
                this.highlightList.add(word);
            }
        }
        if (highlightUsername.value) {
            this.highlightList.add(mc.getSession().getUsername());
        } else {
            this.highlightList.remove(mc.getSession().getUsername());
        }

        if (highlightFriends.value) {
            for (Friend friend : FriendManager.getFriends()) {
                this.highlightList.add(friend.playerName());
            }
        } else {
            for (Friend friend : FriendManager.getFriends()) {
                this.highlightList.remove(friend.playerName());
            }
        }
    }

    //Priority low for chat tweaks to append first.
    @SubscribeEvent
    public void onChatMessageEvent(ChatMessageEvent event) {
        event.setCanceled(true);

        checkForHighlights();

        String originalMessage = event.getMessage().getString();

        StringBuilder builder = new StringBuilder(originalMessage);


        //We will get the formatting color code for the highlighted words from the player.
        Formatting formatting = Formatting.byName(colors.getOption().toString());

        //This should not happen but just in case.
        if (formatting == null) return;

        String colorCode = String.valueOf(sectionSymbol) + formatting.getCode();

        //Flag to check if we should change the message.
        //Messages converted from Text to string usually lose their styles and formatting so it's better to avoid what we can.
        boolean messageEdited = false;

        for (String wordToHighlight : this.highlightList) {
            //Change the case of the message.
            String casedMessaged = applyCaseCheck(builder.toString());

            //Change the case of the word.
            String modifiedWord = applyCaseCheck(wordToHighlight);

            // Case-insensitivity check
            if (casedMessaged.contains(modifiedWord)) {
                //Extract the word from the cased message, using the index of the word in the cased message.
                //This allows us to get the case-sensitive word.
                //This can be optional but the word in the new message will lose its sensitivity.
                int startIndex = casedMessaged.indexOf(modifiedWord);
                int endIndex = startIndex + modifiedWord.length();

                String wordBeingHighlighted = builder.substring(startIndex, endIndex);

                //Finally add color code to the word.
                builder.replace(startIndex, endIndex, colorCode + wordBeingHighlighted + ColorUtils.reset);

                //The message has been edit, should change the text.
                messageEdited = true;
            } else if (regexSupport.value) {
                try {
                    Pattern pattern = Pattern.compile(wordToHighlight);
                    Matcher matcher = pattern.matcher(builder.toString());
                    while (matcher.find()) {
                        String find = builder.substring(matcher.start(), matcher.end());

                        builder.replace(matcher.start(), matcher.end(), colorCode + find + ColorUtils.reset);

                        //The message has been edit, should change the text.
                        messageEdited = true;
                    }
                } catch (PatternSyntaxException ignored) {
                }
            }
        }

        if (messageEdited) {
            event.setMessage(Text.of(builder.toString()));
        }
    }

    private String applyCaseCheck(String input) {
        if (checkCase.getOption() != CaseCheck.NONE) {
            return ((CaseCheck) checkCase.getOption()).getString(input);
        }
        return input;
    }


    public enum CaseCheck {
        UPPER_CASE(String::toUpperCase),
        LOWER_CASE(String::toLowerCase),
        NONE(input -> input);

        private final Function<String, String> stringFunction;

        CaseCheck(Function<String, String> stringFunction) {
            this.stringFunction = stringFunction;
        }

        public String getString(String input) {
            return stringFunction.apply(input);
        }
    }
}
