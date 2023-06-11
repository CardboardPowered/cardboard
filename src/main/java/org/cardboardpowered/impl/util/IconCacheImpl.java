package org.cardboardpowered.impl.util;

import org.bukkit.util.CachedServerIcon;

public class IconCacheImpl implements CachedServerIcon {

    public byte[] value;

    public IconCacheImpl(final byte[] value) {
        this.value = value;
    }

    @Override
    public String getData() {
        // TODO Auto-generated method stub
        return null;
    }

}