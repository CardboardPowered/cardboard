package com.javazilla.bukkitfabric.interfaces;

import net.minecraft.entity.data.TrackedData;

public interface IMixinDataTracker {

    <T> void markDirty(TrackedData<T> key);
}
