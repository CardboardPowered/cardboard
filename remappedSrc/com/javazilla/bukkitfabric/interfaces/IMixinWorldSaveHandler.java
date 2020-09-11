package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.nbt.CompoundTag;

public interface IMixinWorldSaveHandler {

    public CompoundTag getPlayerData(String s);

}