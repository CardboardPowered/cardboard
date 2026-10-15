package io.papermc.paper.registry;

import io.papermc.paper.registry.PaperRegistries;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryAccessHolder;
import io.papermc.paper.registry.RegistryHolder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.entry.RegistryEntry;
import io.papermc.paper.registry.entry.RegistryEntryMeta;
import io.papermc.paper.registry.legacy.DelayedRegistry;
import io.papermc.paper.registry.legacy.DelayedRegistryEntry;
import io.papermc.paper.registry.legacy.LegacyRegistryIdentifiers;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;

import org.bukkit.Keyed;
import org.bukkit.craftbukkit.CraftRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.VisibleForTesting;

@DefaultQualifier(value=NonNull.class)
public class PaperRegistryAccess
implements RegistryAccess {
    private final Map<RegistryKey<?>, RegistryHolder<?>> registries = new ConcurrentHashMap();

    public static PaperRegistryAccess instance() {
        return (PaperRegistryAccess)RegistryAccessHolder.INSTANCE.orElseThrow(() -> new IllegalStateException("No RegistryAccess implementation found"));
    }

    @VisibleForTesting
    public Set<RegistryKey<?>> getLoadedServerBackedRegistries() {
        return this.registries.keySet().stream().filter(registryHolder -> {
            final RegistryEntry<?, ?> entry = PaperRegistries.getEntry(registryHolder);
            return entry != null && !(entry.meta() instanceof RegistryEntryMeta.ApiOnly<?,?>);
        }).collect(Collectors.toUnmodifiableSet());
    }
    
    @Deprecated(forRemoval=true)
    public <T extends Keyed> org.bukkit.Registry<T> getRegistry(final Class<T> type) {
    	final RegistryKey<T> registryKey = byType(type);
        // If our mapping from Class -> RegistryKey did not contain the passed type it was either a completely invalid type or a registry
        // that merely exists as a SimpleRegistry in the org.bukkit.Registry type. We cannot return a registry for these, return null
        // as per method contract in Bukkit#getRegistry.
        if (registryKey == null) return null;

        final RegistryEntry<?, T> entry = PaperRegistries.getEntry(registryKey);
        final RegistryHolder<T> registry = (RegistryHolder<T>) this.registries.get(registryKey);
        if (registry != null) {
            // if the registry exists, return right away. Since this is the "legacy" method, we return DelayedRegistry
            // for the non-builtin Registry instances stored as fields in Registry.
            return registry.get();
        } else if (entry instanceof DelayedRegistryEntry<?, T>) {
            // if the registry doesn't exist and the entry is marked as "delayed", we create a registry holder that is empty
            // which will later be filled with the actual registry. This is so the fields on org.bukkit.Registry can be populated with
            // registries that don't exist at the time org.bukkit.Registry is statically initialized.
            final RegistryHolder<T> delayedHolder = new RegistryHolder.Delayed<>();
            this.registries.put(registryKey, delayedHolder);
            return delayedHolder.get();
        } else {
            // if the registry doesn't exist yet or doesn't have a delayed entry, just return null
            return null;
        }
    }

    @Override
    public <T extends Keyed> org.bukkit.Registry<T> getRegistry(final RegistryKey<T> key) {
        if (PaperRegistries.getEntry(key) == null) {
            throw new NoSuchElementException(key + " is not a valid registry key");
        }
        final RegistryHolder<T> registryHolder = (RegistryHolder<T>) this.registries.get(key);
        if (registryHolder == null) {
            throw new IllegalArgumentException(key + " points to a registry that is not available yet");
        }
        // since this is the getRegistry method that uses the modern RegistryKey, we unwrap any DelayedRegistry instances
        // that might be returned here. I don't think reference equality is required when doing getRegistry(RegistryKey.WOLF_VARIANT) == Registry.WOLF_VARIANT
        return possiblyUnwrap(registryHolder.get());
    }


    private static <T extends Keyed> org.bukkit.Registry<T> possiblyUnwrap(org.bukkit.Registry<T> registry) {
    	if (registry instanceof final DelayedRegistry<T, ?> delayedRegistry) {
            return delayedRegistry.delegate();
        }
        return registry;
    }

    public <M> void registerReloadableRegistry(net.minecraft.resources.ResourceKey<? extends Registry<M>> resourceKey, Registry<M> registry) {
        this.registerRegistry(resourceKey, registry, true);
    }

    public <M> void registerRegistry(net.minecraft.resources.ResourceKey<? extends Registry<M>> resourceKey, Registry<M> registry) {
        this.registerRegistry(resourceKey, registry, false);
    }

    private <M, B extends Keyed, R extends org.bukkit.Registry<B>> void registerRegistry(net.minecraft.resources.ResourceKey<? extends Registry<M>> resourceKey, Registry<M> registry, boolean replace) {
    	@Nullable RegistryEntry<M, B> entry = PaperRegistries.getEntry(resourceKey);
        if (entry == null) {
            return;
        }
        @Nullable RegistryHolder<?> registryHolder = this.registries.get(entry.apiKey());
        if (registryHolder == null || replace) {
            this.registries.put(entry.apiKey(), entry.createRegistryHolder(registry));
        } else if (registryHolder instanceof RegistryHolder.Delayed<?, ?> && entry instanceof final DelayedRegistryEntry<M, B> delayedEntry) {
            ((RegistryHolder.Delayed<B, R>)registryHolder).loadFrom(delayedEntry, registry);
        } else {
            throw new IllegalArgumentException(String.valueOf(resourceKey) + " has already been created");
        }
    }
    
    public <M> void registerReloadableRegistry(final net.minecraft.core.Registry<M> registry) {
        this.registerRegistry(registry, true);
    }

    public <M> void registerRegistry(final net.minecraft.core.Registry<M> registry) {
        this.registerRegistry(registry, false);
    }
    
    private <M, B extends Keyed, R extends org.bukkit.Registry<B>> void registerRegistry(final net.minecraft.core.Registry<M> registry, final boolean replace) {
        final RegistryEntry<M, B> entry = PaperRegistries.getEntry(registry.key());
        if (entry == null) { // skip registries that don't have API entries
            return;
        }
        final RegistryHolder<B> registryHolder = (RegistryHolder<B>) this.registries.get(entry.apiKey());
        if (registryHolder == null || replace) {
            // if the holder doesn't exist yet, or is marked as "replaceable", put it in the map.
            this.registries.put(entry.apiKey(), entry.createRegistryHolder(registry));
        } else {
            if (registryHolder instanceof RegistryHolder.Delayed<?, ?> && entry instanceof final DelayedRegistryEntry<M, B> delayedEntry) {
                // if the registry holder is delayed, and the entry is marked as "delayed", then load the holder with the CraftRegistry instance that wraps the actual nms Registry.
                ((RegistryHolder.Delayed<B, R>) registryHolder).loadFrom(delayedEntry, registry);
            } else {
                // Cardboard - a later singleplayer world loads registries again; take the new server's.
                this.registries.put(entry.apiKey(), entry.createRegistryHolder(registry));
            }
        }
    }

    @Deprecated
    @VisibleForTesting
    public static <T extends Keyed> RegistryKey<T> byType(Class<T> type) {
        return (RegistryKey<T>) LegacyRegistryIdentifiers.CLASS_TO_KEY_MAP.get(type);
    }

	public <M> void lockReferenceHolders(net.minecraft.resources.ResourceKey<? extends Registry<M>> resourceKey) {
        RegistryEntryMeta.ServerSide serverSide;
        RegistryEntryMeta registryEntryMeta;
        RegistryEntry entry = PaperRegistries.getEntry(resourceKey);
        if (entry == null || !((registryEntryMeta = entry.meta()) instanceof RegistryEntryMeta.ServerSide) || !(serverSide = (RegistryEntryMeta.ServerSide)registryEntryMeta).registryTypeMapper().constructorUsesHolder()) {
            return;
        }
        CraftRegistry registry = (CraftRegistry)this.getRegistry(entry.apiKey());
        registry.lockReferenceHolders();
    }

}

