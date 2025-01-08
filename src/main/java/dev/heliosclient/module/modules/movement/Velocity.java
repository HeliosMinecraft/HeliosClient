package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.mixin.AccessorEntityVelocityUpS2CPacket;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IExplosionS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

/**
 * Credits: <a href="https://github.com/Pan4ur/ThunderHack-Recode/blob/main/src/main/java/thunder/hack/modules/movement/Velocity.java">ThunderHack Recode</a>
 */

public class Velocity extends Module_ {

    private final SettingGroup sgGeneral = new SettingGroup("General");

    public DropDownSetting mode = sgGeneral.add(new DropDownSetting.Builder()
            .name("Mode")
            .description("Mode of way to cancel velocity")
            .onSettingChange(this)
            .value(List.of(Mode.values()))
            .defaultListOption(Mode.Cancel)
            .build()
    );
    DoubleSetting vertical = sgGeneral.add(new DoubleSetting.Builder()
            .name("Vertical")
            .range(0, 100f)
            .roundingPlace(1)
            .defaultValue(0d)
            .onSettingChange(this)
            .shouldRender(() -> mode.getOption() == Mode.Custom)
            .build()
    );
    DoubleSetting horizontal = sgGeneral.add(new DoubleSetting.Builder()
            .name("Horizontal")
            .range(0, 100f)
            .roundingPlace(1)
            .defaultValue(0d)
            .onSettingChange(this)
            .shouldRender(() -> mode.getOption() == Mode.Custom)
            .build()
    );
    public BooleanSetting explosion = sgGeneral.add(new BooleanSetting.Builder()
            .name("Modify Explosion velocity")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting pauseOnFlag = sgGeneral.add(new BooleanSetting.Builder()
            .name("PauseOnFlag")
            .description("Pauses velocity when the server flags or lags you back.")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting pauseInWater = sgGeneral.add(new BooleanSetting.Builder()
            .name("PauseOnWater")
            .description("Pauses velocity in water.")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting noPush = sgGeneral.add(new BooleanSetting.Builder()
            .name("NoPush")
            .description("Prevents you from being pushed out of blocks and away from entities")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    private int ccTickCoolDown;
    private boolean flag;

    public Velocity() {
        super("Velocity", "Cancels knockback and pushes by environment", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE event) {
        if (mc.player == null) return;

        if (ccTickCoolDown > 0) {
            ccTickCoolDown--;
            return;
        }

        if ((mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) && pauseInWater.value)
            return;

        if (event.packet instanceof EntityVelocityUpdateS2CPacket pac) {
            if (pac.getEntityId() == mc.player.getId()) {
                switch ((Mode) mode.getOption()) {
                    case Matrix -> {
                        if (!flag) {
                            event.setCanceled(true);
                            flag = true;
                        } else {
                            flag = false;
                            ((AccessorEntityVelocityUpS2CPacket) pac).setVelocityX(((int) ((double) pac.getVelocityX() * -0.1)));
                            ((AccessorEntityVelocityUpS2CPacket) pac).setVelocityZ(((int) ((double) pac.getVelocityZ() * -0.1)));
                        }
                    }
                    case Custom -> {
                        ((AccessorEntityVelocityUpS2CPacket) pac).setVelocityX((int) ((float) pac.getVelocityX() * horizontal.value / 100f));
                        ((AccessorEntityVelocityUpS2CPacket) pac).setVelocityY((int) ((float) pac.getVelocityY() * vertical.value / 100f));
                        ((AccessorEntityVelocityUpS2CPacket) pac).setVelocityZ((int) ((float) pac.getVelocityZ() * horizontal.value / 100f));
                    }
                    case Cancel -> {
                        event.setCanceled(true);
                    }
                    case Grim -> {
                        event.setCanceled(true);
                        flag = true;
                    }
                }
            }
        }

        if (event.packet instanceof ExplosionS2CPacket && this.explosion.value) {
            IExplosionS2CPacket packet = (IExplosionS2CPacket) event.packet; 
            switch ((Mode) mode.getOption()) {
                case Cancel -> {
                    packet.helios$setVelocityX(0);
                    packet.helios$setVelocityY(0);
                    packet.helios$setVelocityZ(0);
                    ccTickCoolDown = -1;
                }
                case Custom -> {
                    packet.helios$setVelocityX((float) (packet.helios$getVelocityX() * horizontal.value / 100f));
                    packet.helios$setVelocityZ((float) (packet.helios$getVelocityZ() * horizontal.value / 100f));
                    packet.helios$setVelocityY((float) (packet.helios$getVelocityY() * vertical.value / 100f));
                    ccTickCoolDown = -1;
                }
                case Grim -> {
                    packet.helios$setVelocityX(0);
                    packet.helios$setVelocityY(0);
                    packet.helios$setVelocityZ(0);
                    flag = true;
                }
            }
        }


        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            if (pauseOnFlag.value || mode.getOption() == Mode.Grim)
                ccTickCoolDown = 5;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if ((mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) && pauseInWater.value)
            return;

        switch ((Mode) mode.getOption()) {
            case Matrix -> {
                if (mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
                    double var3 = mc.player.getYaw() * 0.017453292F;
                    double var5 = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
                    mc.player.setVelocity(-Math.sin(var3) * var5, mc.player.getVelocity().y, Math.cos(var3) * var5);
                    mc.player.setSprinting(mc.player.age % 2 != 0);
                }
            }
            case Grim -> {
                if (flag) {
                    if (ccTickCoolDown <= 0) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(),mc.player.horizontalCollision));
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored(mc.player.getPos()), Direction.DOWN));
                    }
                    flag = false;
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        return mode.getOption().toString();
    }

    public enum Mode {
        Matrix, Cancel, Custom, Grim
    }
}
