package dev.heliosclient.altmanager.accounts;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.altmanager.Account;
import dev.heliosclient.altmanager.MicrosoftAuthExtractor;
import dev.heliosclient.altmanager.XboxLiveAuthenticator;
import dev.heliosclient.mixin.AccessorMinecraftClient;
import dev.heliosclient.util.cape.ProfileUtils;
import dev.heliosclient.util.timer.TimerUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.util.Map;
import java.util.Optional;

/**
 * Untested as of September 3rd, August 2024.
 */
public class MojangAccount extends Account {
    private String email;
    TimerUtils timer = new TimerUtils();

    public MojangAccount(String username, String email, String password) {
        super(username, password);
        this.email = email;

        if(email == null || email.isEmpty())
            throw new IllegalArgumentException();

        if(password == null || password.isEmpty())
            throw new IllegalArgumentException();
    }
    public MojangAccount(String email, String password) {
        this("",email,password);
    }

    @Override
    public boolean login() {
        timer.startTimer();
        Pair<String, String> veryImportantValues = MicrosoftAuthExtractor.getsFTTagAndUrlPost();
        if(veryImportantValues == null){
            HeliosClient.LOGGER.error("sFTTag and urlPost not found. Likely user is offline");
            return false;
        }
        String accessToken = MicrosoftAuthExtractor.getAccessToken(getEmail(),getPassword(),veryImportantValues);
        try{
            if(accessToken == null){
                HeliosClient.LOGGER.error("AccessToken is null");
                return false;
            }
            Map<String, String> result = XboxLiveAuthenticator.authenticateWithXboxLive(accessToken);
            String userName = result.get("username");
            this.setUsername(userName);
            Session session =  new Session(userName,ProfileUtils.getAsUUID(userName),result.get("access_token"),Optional.empty(),Optional.empty(), Session.AccountType.MOJANG);

            ((AccessorMinecraftClient)MinecraftClient.getInstance()).setSession(session);
            HeliosClient.LOGGER.info("Time elapsed for a successful session login {}s", timer.getElapsedTime());
            timer.resetTimer();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public String getDisplayName() {
        return getUsername();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
