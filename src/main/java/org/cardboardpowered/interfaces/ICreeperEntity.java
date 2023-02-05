package org.cardboardpowered.interfaces;

public interface ICreeperEntity {

    void explodeBF();

    int getExplosionRadiusBF();

    void setExplosionRadiusBF(int radius);

    void setFuseTimeBF(int ticks);

    int getFuseTimeBF();

    void setPowered(boolean powered);

    boolean isPoweredBF();

}
