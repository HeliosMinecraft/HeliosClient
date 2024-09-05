package dev.heliosclient.altmanager.accounts;

import dev.heliosclient.altmanager.Account;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.util.cape.ProfileUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class SessionIDAccount extends Account {
    private String sessionID;

    public SessionIDAccount(String sessionID ) {
        super("",null);
        this.sessionID = sessionID;
    }

    @Override
    public boolean login() {
        if(this.sessionID != null && !this.sessionID.isEmpty() && this.sessionID.lastIndexOf(":") != -1){
            String uuidString = this.sessionID.substring(this.sessionID.lastIndexOf(':'));
            UUID uuid = UUID.fromString(ProfileUtils.insertHyphensToUUID(uuidString));
            String accessToken = this.sessionID.substring(this.sessionID.indexOf(":") + 1,this.sessionID.lastIndexOf(':'));

            Session session;
            try {
                session = new Session(ProfileUtils.getProfileName(uuidString), uuid,accessToken, Optional.empty(),Optional.empty(), Session.AccountType.MOJANG);
            } catch (IOException e){
                session = new Session("", uuid,accessToken, Optional.empty(),Optional.empty(), Session.AccountType.MOJANG);
                e.printStackTrace();
            }
            ((AccessorMinecraftClient) MinecraftClient.getInstance()).setSession(session);
        }

        return false;
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}
