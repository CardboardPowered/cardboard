package com.javazilla.bukkitfabric.impl.util;

import org.bukkit.craftbukkit.util.Waitable;

public class WaitableImpl extends Waitable<Object> {

    private Runnable runnable;
    public WaitableImpl(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    protected Object evaluate() {
        runnable.run();
        return null;
    }

}