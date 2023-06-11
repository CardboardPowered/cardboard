package org.cardboardpowered.impl;

import org.bukkit.Registry;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;

public class CardboardAttributable implements Attributable {

    private final AttributeContainer handle;

    public CardboardAttributable(AttributeContainer handle) {
        this.handle = handle;
    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        EntityAttributeInstance nms = handle.getCustomInstance(toMinecraft(attribute));
        return (nms == null) ? null : new CardboardAttributeInstance(nms, attribute);
    }

    public static EntityAttribute toMinecraft(Attribute attribute) {
        return net.minecraft.registry.Registries.ATTRIBUTE.get(CraftNamespacedKey.toMinecraft(attribute.getKey()));
    }

    public static Attribute fromMinecraft(String nms) {
        return Registry.ATTRIBUTE.get(CraftNamespacedKey.fromString(nms));
    }

    @Override
    public void registerAttribute(Attribute attribute) {
        // TODO Paper API
    }

}