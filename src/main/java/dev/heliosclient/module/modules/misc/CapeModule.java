package dev.heliosclient.module.modules.misc;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.cape.CapeSynchronizer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.List;

public class CapeModule extends Module_ {
    public SettingGroup sgCape = new SettingGroup("Cape");

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

    public CapeModule() {
        super("Capes", "Use Custom Capes from `heliosclient/capes` directory", Categories.MISC);

        capes.options = List.of(CapeManager.capes);
        addSettingGroup(sgCape);


        addQuickSetting(capes);
        addQuickSetting(customPhysics);
        addQuickSetting(elytra);


        loadCapes.addButton("Get Cape", 0, 0, () -> {
            try {
                if (!playerName.value.isEmpty() && playerName.value.length() > 3 && CapeManager.CapeType.values()[getCapeFrom.value] == CapeManager.CapeType.OPTIFINE) {
                    CapeManager.getCapes(CapeManager.CapeType.OPTIFINE, playerName.value, null);
                } else if (!UUID.value.isEmpty() && UUID.value.length() > 31) {
                    CapeManager.getCapes(CapeManager.CapeType.values()[getCapeFrom.value], null, UUID.value);
                }
                capes.options = List.of(CapeManager.capes);

                //Todo: Change to info toast
                AnimationUtils.addErrorToast("Fetched cape successfully", false, 1000);
            } catch (IOException e) {
                HeliosClient.LOGGER.error("An error has occured while fetching cape. ", e);
                AnimationUtils.addErrorToast("Failed to fetch cape. Check logs", false, 1000);
                AnimationUtils.addErrorToast("Reason for fail: " + e.getMessage(), false, 1000);

            }
        });
        loadCapes.addButton("Reload Capes", 0, 1, () -> {
            CapeManager.loadCapes();
            capes.options = List.of(CapeManager.capes);
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (CapeManager.capeIdentifiers.isEmpty()) return;
        capes.options = List.of(CapeManager.capes);

        CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);

        if (mc.player != null) {
            CapeManager.setCapeAndElytra(mc.player, CapeManager.cape, CapeManager.elytraIdentifiers.get(capes.value));
            if (mc.player.getServer() != null) {
                for (ServerPlayerEntity player : mc.player.getServer().getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture, CapeManager.elytraIdentifiers.get(capes.value));
                }
            }
        }

    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);

        if (CapeManager.capeIdentifiers.isEmpty()) return;
        CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);

        if (mc.player != null) {
            CapeManager.setCapeAndElytra(mc.player, CapeManager.cape, CapeManager.elytraIdentifiers.get(capes.value));
            if (mc.player.getServer() != null) {
                for (ServerPlayerEntity player : mc.player.getServer().getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture, CapeManager.elytraIdentifiers.get(capes.value));
                }
            }
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();

        if (CapeManager.capeIdentifiers.isEmpty()) return;
        CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);

        if (mc.player != null) {
            CapeManager.setCapeAndElytra(mc.player, CapeManager.cape, CapeManager.elytraIdentifiers.get(capes.value));
            if (mc.player.getServer() != null) {
                for (ServerPlayerEntity player : mc.player.getServer().getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture, CapeManager.elytraIdentifiers.get(capes.value));
                }
            }
        }
    }
}
