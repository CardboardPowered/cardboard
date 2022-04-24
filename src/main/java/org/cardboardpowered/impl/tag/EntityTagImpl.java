package org.cardboardpowered.impl.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
//import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
//import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
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
        return this.registry.entryOf(RegistryKey.of(Registry.ENTITY_TYPE_KEY, CraftNamespacedKey.toMinecraft(entity.getKey()))).isIn(this.tag);
    }

    public Set<org.bukkit.entity.EntityType> getValues() {
        return this.getHandle().stream().map(nms -> (org.bukkit.entity.EntityType)org.bukkit.Registry.ENTITY_TYPE.get(CraftNamespacedKey.fromMinecraft(EntityType.getId((EntityType)nms.value())))).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}

