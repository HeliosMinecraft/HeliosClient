package dev.heliosclient.module.modules.misc;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.animation.AnimationUtils;

import java.util.List;

public class CapeModule extends Module_ {
    public SettingGroup sgCape = new SettingGroup("Cape Settings");

    public CycleSetting capes = sgCape.add(new CycleSetting.Builder()
            .name("Cape")
            .description("Custom cape texture to use. Turn on modules to see options")
            .value(List.of(CapeManager.capes))
            .defaultListIndex(0)
            .onSettingChange(this)
            .build()
    );
    public DropDownSetting getCapeFrom = sgCape.add(new DropDownSetting.Builder()
            .name("Get Cape from")
            .description("Gets your favourite cape from the following places. Need valid UUID or player name")
            .value(List.of(CapeManager.CapeType.values()))
            .defaultListIndex(0)
            .addOptionToolTip("None")
            .addOptionToolTip("Fetch cape from optifine api")
            .addOptionToolTip("Fetch cape from craftar, a UUID to player skin provider")
            .addOptionToolTip("Fetch custom cape from minecraftcapes.com")
            .onSettingChange(this)
            .build()
    );
    public StringSetting playerName = sgCape.add(new StringSetting.Builder()
            .name("Enter Player Name")
            .description("Name of the player to fetch the cape from")
            .value("")
            .characterLimit(16)
            .inputMode(InputBox.InputMode.DIGITS_AND_CHARACTERS_AND_UNDERSCORE)
            .shouldRender(() -> (CapeManager.CapeType.values()[getCapeFrom.value] != CapeManager.CapeType.NONE) && (CapeManager.CapeType.values()[getCapeFrom.value] == CapeManager.CapeType.OPTIFINE))
            .onSettingChange(this)
            .build()
    );
    public StringSetting UUID = sgCape.add(new StringSetting.Builder()
            .name("Enter Valid Player UUID")
            .description("UUID of the player. Use minecraftuuid.com if you dont know where to get the UUID")
            .value("")
            .characterLimit(37)
            .inputMode(InputBox.InputMode.ALL)
            .shouldRender(() -> (CapeManager.CapeType.values()[getCapeFrom.value] != CapeManager.CapeType.NONE) && (CapeManager.CapeType.values()[getCapeFrom.value] != CapeManager.CapeType.OPTIFINE))
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting customPhysics = sgCape.add(new BooleanSetting.Builder()
            .name("Better Physics")
            .description("Improved physics for capes")
            .value(true)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public BooleanSetting elytra = sgCape.add(new BooleanSetting.Builder()
            .name("Elytra")
            .description("Cape Texture for elytra (Bad most of times with improper textures)")
            .value(false)
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    public ButtonSetting loadCapes = sgCape.add(new ButtonSetting.Builder()
            .name("Capes")
            .build()
    );
    private static CapeModule INSTANCE = new CapeModule();

    protected CapeModule() {
        super("Capes", "Use Custom Capes from `heliosclient/capes` directory", Categories.MISC);

        capes.options = List.of(CapeManager.capes);
        addSettingGroup(sgCape);


        addQuickSetting(capes);
        addQuickSetting(customPhysics);
        addQuickSetting(elytra);

        playerName.setShouldSaveOrLoad(false);
        UUID.setShouldSaveOrLoad(false);

        loadCapes.addButton("Get Cape", 0, 0, () -> {
            try {
                CapeManager.CapeType capeType = CapeManager.CapeType.values()[getCapeFrom.value];
                if (shouldUsePlayerName() && capeType == CapeManager.CapeType.OPTIFINE) {
                    CapeManager.getCapes(capeType, playerName.value, null);
                } else if (shouldUseUUID()) {
                    CapeManager.getCapes(capeType, null, UUID.value);
                }
                capes.iSettingChange.onSettingChange(capes);

                if(mc.player == null) {
                    AnimationUtils.addInfoToast("Fetched cape successfully", false, 1000);
                }else{
                    ChatUtils.sendHeliosMsg("Fetched cape successfully");
                }
            } catch (Exception e) {
                HeliosClient.LOGGER.error("An error occurred while fetching cape. ", e);
                if(mc.player == null) {
                    AnimationUtils.addErrorToast("Failed to fetch cape. Check logs", false, 1000);
                    AnimationUtils.addErrorToast("Reason: " + e.getMessage().trim(), false, 1000);
                }else{
                    ChatUtils.sendHeliosMsg("Failed to fetch cape. Check logs");
                    ChatUtils.sendHeliosMsg("Reason: " + e.getMessage().trim());
                }
            }
        });


        loadCapes.addButton("Reload Capes", 0, 1, CapeManager::loadCapes);
    }

    public boolean shouldUseUUID() {
        return !UUID.value.isEmpty() && UUID.value.length() > 31;
    }

    public boolean shouldUsePlayerName() {
        return !playerName.value.isEmpty() && playerName.value.length() > 2;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        setCape();
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        setCape();
    }

    public void setCape() {
        if (CapeManager.capeIdentifiers.isEmpty()) return;

        capes.options = List.of(CapeManager.capes);

        if (capes.value < CapeManager.capeIdentifiers.size()) {
            CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
        if (capes.value < CapeManager.capeIdentifiers.size()) {
            CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);
        }
    }

    public static CapeModule get() {
        return INSTANCE;
    }
}
