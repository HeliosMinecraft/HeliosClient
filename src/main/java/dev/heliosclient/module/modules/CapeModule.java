package dev.heliosclient.module.modules;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.buttonsetting.ButtonSetting;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.util.cape.CapeSynchronizer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class CapeModule extends Module_ {
    public SettingGroup sgCape = new SettingGroup("Cape");

    public CycleSetting capes = sgCape.add(new CycleSetting.Builder()
            .name("Cape")
            .description("Custom cape texture to use. Turn on modules to see options")
            .value(List.of(CapeManager.capes))
            .listValue(0)
            .module(this)
            .build()
    );
    public BooleanSetting customPhysics = sgCape.add(new BooleanSetting.Builder()
            .name("Better Physics")
            .description("Improved physics for capes")
            .value(true)
            .defaultValue(false)
            .module(this)
            .build()
    );
    public BooleanSetting elytra = sgCape.add(new BooleanSetting.Builder()
            .name("Elytra")
            .description("Cape Texture for elytra (bad most of times)")
            .value(true)
            .defaultValue(false)
            .module(this)
            .build()
    );
    public ButtonSetting loadCapes = sgCape.add(new ButtonSetting.Builder()
            .name("Capes")
            .build()
    );

    public CapeModule() {
        super("Custom Capes", "Use Custom Capes from `heliosclient/capes` directory", Categories.MISC);

        capes.options = List.of(CapeManager.capes);
        addSettingGroup(sgCape);
        loadCapes.addButton("Reload Capes",0,0,()->{
            CapeManager.loadCapes();
            capes.options = List.of(CapeManager.capes);
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if(CapeManager.capeIdentifiers.isEmpty())return;
        capes.options = List.of(CapeManager.capes);

        CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);

        if(mc.player != null) {
            CapeManager.setCapeAndElytra(mc.player,CapeManager.cape,CapeManager.elytraIdentifiers.get(capes.value));
            if(mc.player.getServer() != null){
                for (ServerPlayerEntity player : mc.player.getServer().getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture,CapeManager.elytraIdentifiers.get(capes.value));
                }
            }
        }

    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);
        if(CapeManager.capeIdentifiers.isEmpty())return;
        CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);

        if(mc.player != null) {
            CapeManager.setCapeAndElytra(mc.player,CapeManager.cape,CapeManager.elytraIdentifiers.get(capes.value));
            if(mc.player.getServer() != null){
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
        if(CapeManager.capeIdentifiers.isEmpty())return;
        CapeManager.cape = CapeManager.capeIdentifiers.get(capes.value);

        if(mc.player != null) {
            CapeManager.setCapeAndElytra(mc.player,CapeManager.cape,CapeManager.elytraIdentifiers.get(capes.value));
            if(mc.player.getServer() != null){
                for (ServerPlayerEntity player : mc.player.getServer().getPlayerManager().getPlayerList()) {
                    Identifier capeTexture = CapeManager.cape;
                    CapeSynchronizer.sendCapeSyncPacket(player, capeTexture, CapeManager.elytraIdentifiers.get(capes.value));
                }
            }
        }
    }
}
