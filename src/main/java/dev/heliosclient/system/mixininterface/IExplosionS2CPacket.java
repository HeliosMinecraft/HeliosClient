package dev.heliosclient.system.mixininterface;

public interface IExplosionS2CPacket {
    void helios$setVelocityX(float vX);
    void helios$setVelocityY(float vY);
    void helios$setVelocityZ(float vZ);

    double helios$getVelocityX();
    double helios$getVelocityY();
    double helios$getVelocityZ();

}
