package com.javazilla.bukkitfabric.impl;

import org.bukkit.util.CachedServerIcon;

public class IconCacheImpl implements CachedServerIcon {

    public final String value;

    public IconCacheImpl(final String value) {
        this.value = value;
    }

}