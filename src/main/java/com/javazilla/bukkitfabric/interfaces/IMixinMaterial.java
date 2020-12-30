package com.javazilla.bukkitfabric.interfaces;

import org.cardboardpowered.impl.CardboardModdedMaterial;

public interface IMixinMaterial {

    boolean isModded();

    CardboardModdedMaterial getModdedData();

    void setModdedData(CardboardModdedMaterial data);

}