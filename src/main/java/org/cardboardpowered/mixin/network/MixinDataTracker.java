package org.cardboardpowered.mixin.network;

import com.javazilla.bukkitfabric.interfaces.IMixinDataTracker;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DataTracker.class)
public abstract class MixinDataTracker implements IMixinDataTracker {

    @Shadow protected abstract <T> DataTracker.Entry<T> getEntry(TrackedData<T> trackedData);

    @Shadow private boolean dirty;

    @Override
    public <T> void markDirty(TrackedData<T> key) {
        DataTracker.Entry entry = this.getEntry(key);
        entry.setDirty(true);
        this.dirty = true;
    }
}
