package com.javazilla.bukkitfabric.impl;

import org.bukkit.Registry;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;

public class AttributableImpl implements Attributable {

    private final AttributeContainer handle;

    public AttributableImpl(AttributeContainer handle) {
        this.handle = handle;
    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        EntityAttributeInstance nms = handle.getCustomInstance(toMinecraft(attribute));
        return (nms == null) ? null : new AttributeInstanceImpl(nms, attribute);
    }

    public static EntityAttribute toMinecraft(Attribute attribute) {
        return net.minecraft.util.registry.Registry.ATTRIBUTE.get(CraftNamespacedKey.toMinecraft(attribute.getKey()));
    }

    public static Attribute fromMinecraft(String nms) {
        return Registry.ATTRIBUTE.get(CraftNamespacedKey.fromString(nms));
    }

}