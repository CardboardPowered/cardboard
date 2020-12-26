package com.javazilla.bukkitfabric.interfaces;

public interface IMixinArmorStandEntity {

    void setHideBasePlateBF(boolean b);

    void setShowArmsBF(boolean arms);

    void setSmallBF(boolean small);

    void setMarkerBF(boolean marker);

    boolean canMoveBF();

    void setCanMoveBF(boolean b);

}