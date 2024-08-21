package dev.heliosclient.system;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.event.listener.Listener;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

/**
 * Credits: <a href="https://github.com/S-B99/kamiblue/blob/feature/master/src/main/java/me/zeroeightsix/kami/util/LagCompensator.java">KAMI Blue</a>
 */
public class TickRate implements Listener {
    public static TickRate INSTANCE = new TickRate();
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long lastUpdateTime = -1;
    private long gameJoinedTime;

    @SubscribeEvent
    public void onWorldTimeUpdate(PacketEvent.RECEIVE e) {
        if (e.packet instanceof WorldTimeUpdateS2CPacket) {
            long now = System.currentTimeMillis();
            float timeElapsed = (float) (now - lastUpdateTime) / 1000.0F;
            tickRates[nextIndex] = MathHelper.clamp(20.0f / timeElapsed, 0.0f, 20.0f);
            nextIndex = (nextIndex + 1) % tickRates.length;
            lastUpdateTime = now;
        }
    }

    @SubscribeEvent
    public void onJoinWorld(PlayerJoinEvent event) {
        Arrays.fill(tickRates, 0);
        nextIndex = 0;
        gameJoinedTime = lastUpdateTime = System.currentTimeMillis();
    }

    public float getTPS() {
        if (HeliosClient.MC.player == null || HeliosClient.MC.player.getWorld() == null) return 0;
        if (System.currentTimeMillis() - gameJoinedTime < 4000) return 20;

        if(tickRates.length == 0){
            return 0;
        }

        int numTicks = 0;
        float sumTickRates = 0.0f;
        for (float tickRate : tickRates) {
            if (tickRate > 0) {
                sumTickRates += tickRate;
                numTicks++;
            }
        }

        //avg
        return sumTickRates / numTicks;
    }

    public float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - gameJoinedTime < 4000) return 0;
        return (now - lastUpdateTime) / 1000f;
    }
}