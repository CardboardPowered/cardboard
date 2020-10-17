package com.javazilla.bukkitfabric.interfaces;

public interface IMixinCreeperEntity {

    public void explodeBF();

    public int getExplosionRadiusBF();

    public void setExplosionRadiusBF(int radius);

    public void setFuseTimeBF(int ticks);

    public int getFuseTimeBF();

    public void setPowered(boolean powered);

    public boolean isPoweredBF();

}
