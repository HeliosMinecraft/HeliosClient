package dev.heliosclient.util.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;

public class DisplayPreviewEntity {
    MinecraftClient mc = MinecraftClient.getInstance();

    public GameProfile profile = mc.getGameProfile();
    public SkinTextures skinTexture = DefaultSkinHelper.getSkinTextures(profile);

    public boolean slim = false;

    boolean showBody = true;
    boolean showElytra = false;

    float limbDistance = 0f;
    float lastLimbDistance = 0f;
    float limbAngle = 0f;
    float yaw = 0f;
    float prevYaw = 0f;
    float x = 0.0f;
    float prevX = 0.0f;

    public DisplayPreviewEntity(){
        mc.getSkinProvider().fetchSkinTextures(profile).thenAccept(sT -> {
            skinTexture = sT;
            slim = skinTexture.model() == SkinTextures.Model.SLIM;
        });
        updateLimbs();
        updateLimbs();
    }

    //For extra animations
    public void updateLimbs() {
        double d = this.x - this.prevX;
        double g = Math.sqrt(d * d) * 4.0f;
        if (g > 1.0f) {
            g = 1.0f;
        }
        this.lastLimbDistance = this.limbDistance;
        this.limbDistance += (float) ((g - this.limbDistance) * 0.4f);
        this.limbAngle += this.limbDistance;
    }


    public boolean isShowBody() {
        return showBody;
    }

    public void setShowBody(boolean showBody) {
        this.showBody = showBody;
    }

    public boolean isShowElytra() {
        return showElytra;
    }

    public void setShowElytra(boolean showElytra) {
        this.showElytra = showElytra;
    }

    public float getLimbDistance() {
        return limbDistance;
    }

    public void setLimbDistance(float limbDistance) {
        this.limbDistance = limbDistance;
    }

    public float getLastLimbDistance() {
        return lastLimbDistance;
    }

    public void setLastLimbDistance(float lastLimbDistance) {
        this.lastLimbDistance = lastLimbDistance;
    }

    public float getLimbAngle() {
        return limbAngle;
    }

    public void setLimbAngle(float limbAngle) {
        this.limbAngle = limbAngle;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public void setPrevYaw(float prevYaw) {
        this.prevYaw = prevYaw;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getPrevX() {
        return prevX;
    }

    public void setPrevX(float prevX) {
        this.prevX = prevX;
    }
}
