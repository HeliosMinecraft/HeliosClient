package dev.heliosclient.altmanager.accounts;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.altmanager.Account;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.util.cape.ProfileUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.IOException;
import java.util.Optional;

public class CrackedAccount extends Account {
    public CrackedAccount(String username) {
        super(username, null);
    }

    @Override
    public boolean login() {
        try {
            Session session = new Session(getUsername(), ProfileUtils.getAsUUID(getUsername()), "",
                    Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);

            ((AccessorMinecraftClient) MinecraftClient.getInstance()).setSession(session);
            return true;
        }catch (IOException io){
            HeliosClient.LOGGER.error("Unable to login via cracked account {}",getUsername());
            return false;
        }
    }

    @Override
    public String getDisplayName() {
        return getUsername();
    }
}
