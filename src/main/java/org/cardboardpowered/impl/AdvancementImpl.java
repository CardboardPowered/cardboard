package org.cardboardpowered.impl;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.advancement.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

public class AdvancementImpl implements org.bukkit.advancement.Advancement {

    private final Advancement handle;

    public AdvancementImpl(Advancement handle) {
        this.handle = handle;
    }

    public Advancement getHandle() {
        return handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(handle.getId());
    }

    @Override
    public Collection<String> getCriteria() {
        return Collections.unmodifiableCollection(handle.getCriteria().keySet());
    }

}