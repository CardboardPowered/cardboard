package org.cardboardpowered.impl.tag;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;

public class CraftGameEventTag extends TagImpl<net.minecraft.world.event.GameEvent, GameEvent> {
	
	public CraftGameEventTag(Registry<net.minecraft.world.event.GameEvent> registry,
			TagKey<net.minecraft.world.event.GameEvent> tag) {
		super(registry, tag);
	}

	private static final Map<GameEvent, RegistryKey<net.minecraft.world.event.GameEvent>> KEY_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
	
	@Override
	public boolean isTagged(@NotNull GameEvent gameEvent) {
	    return registry.entryOf(KEY_CACHE.computeIfAbsent(gameEvent, event -> RegistryKey.of(RegistryKeys.GAME_EVENT, CraftNamespacedKey.toMinecraft(event.getKey())))).isIn(tag);
	}

	@Override
    public Set<GameEvent> getValues() {
        return getHandle().stream().map(nms -> {
        	NamespacedKey key = CraftNamespacedKey.fromMinecraft(Registries.GAME_EVENT.getId(nms.value()));
        	return GameEvent.getByKey(key);
        }).collect(Collectors.toUnmodifiableSet());
	}

}