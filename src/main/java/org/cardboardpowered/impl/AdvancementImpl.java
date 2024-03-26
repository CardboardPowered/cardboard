package org.cardboardpowered.impl;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;

import java.util.Collection;
import java.util.Collections;

import net.kyori.adventure.text.Component;
import net.minecraft.advancement.Advancement;

import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;

public class AdvancementImpl implements org.bukkit.advancement.Advancement {

    private final AdvancementEntry handle;

    public AdvancementImpl(AdvancementEntry handle) {
        this.handle = handle;
    }

    public AdvancementEntry getHandle() {
        return handle;
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(handle.id());
    }

    @Override
    public Collection<String> getCriteria() {
        return Collections.unmodifiableCollection(handle.value().criteria().keySet());
    }

    @Override
    public @NotNull @Unmodifiable Collection<org.bukkit.advancement.Advancement> getChildren() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
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

