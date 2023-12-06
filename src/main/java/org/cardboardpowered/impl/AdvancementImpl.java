package org.cardboardpowered.impl;

import java.util.Collection;
import java.util.Collections;

import net.kyori.adventure.text.Component;
import net.minecraft.advancement.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import io.papermc.paper.advancement.AdvancementDisplay;

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

    @Override
    public @NotNull @Unmodifiable Collection<org.bukkit.advancement.Advancement> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable AdvancementDisplay getDisplay() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.bukkit.advancement.@Nullable Advancement getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.bukkit.advancement.@NotNull Advancement getRoot() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public @NotNull Component displayName() {
		// TODO Auto-generated method stub
		return Component.text("Hello world");
	}

}