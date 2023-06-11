package org.cardboardpowered.impl.tag;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.TagKey;
//import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.Objects;

public class EntityTagImpl
extends TagImpl<EntityType<?>, org.bukkit.entity.EntityType> {
    /*public EntityTagImpl(TagGroup<EntityType<?>> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(org.bukkit.entity.EntityType entity) {
        return this.getHandle().contains(net.minecraft.util.registry.Registry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(entity.getKey())));
    }

    @Override
    public Set<org.bukkit.entity.EntityType> getValues() {
        return Collections.unmodifiableSet(this.getHandle().values().stream().map(nms -> Registry.ENTITY_TYPE.get(CraftNamespacedKey.fromMinecraft(EntityType.getId(nms)))).collect(Collectors.toSet()));
    }*/
    
    public EntityTagImpl(Registry<EntityType<?>> registry, TagKey<EntityType<?>> tag) {
        super(registry, tag);
    }

    public boolean isTagged(org.bukkit.entity.EntityType entity) {
        return this.registry.entryOf(RegistryKey.of(RegistryKeys.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(entity.getKey()))).isIn(this.tag);
    }

    public Set<org.bukkit.entity.EntityType> getValues() {
        return this.getHandle().stream().map(nms -> (org.bukkit.entity.EntityType)org.bukkit.Registry.ENTITY_TYPE.get(CraftNamespacedKey.fromMinecraft(EntityType.getId((EntityType)nms.value())))).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}

