package com.javazilla.bukkitfabric.interfaces;

import org.cardboardpowered.impl.CardboardModdedMaterial;

public interface IMixinMaterial {

    public boolean isModded();

    public CardboardModdedMaterial getModdedData();

    public void setModdedData(CardboardModdedMaterial data);

}