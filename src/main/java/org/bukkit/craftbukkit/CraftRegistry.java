package org.bukkit.craftbukkit;

import java.util.Iterator;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.registry.DynamicRegistryManager;

// TODO
public class CraftRegistry<B extends Keyed, M> implements Registry<B> {

	@Override
	public Iterator<B> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @Nullable B get(@NotNull NamespacedKey arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <B extends Keyed> Registry<?> createRegistry(Class<B> bukkitClass, DynamicRegistryManager registryHolder) {
		// TODO Auto-generated method stub
		return null;
	}

    /*private static DynamicRegistryManager registry;

    public static void setMinecraftRegistry(DynamicRegistryManager registry) {
        Preconditions.checkState(CraftRegistry.registry == null, "Registry already set");
        CraftRegistry.registry = registry;
    }

    public static DynamicRegistryManager getMinecraftRegistry() {
        return registry;
    }

    public static <E> net.minecraft.util.registry.Registry<E> getMinecraftRegistry(RegistryKey<net.minecraft.util.registry.Registry<E>> key) {
        return getMinecraftRegistry().get(key);
    }

    public static <B extends Keyed> Registry<?> createRegistry(Class<B> bukkitClass, DynamicRegistryManager registryHolder) {
        if (bukkitClass == GameEvent.class) {
            return new CraftRegistry<>(registryHolder.get(RegistryKeys.GAME_EVENT), CraftGameEvent::new);
        }
        if (bukkitClass == MusicInstrument.class) {
            return new CraftRegistry<>(registryHolder.get(RegistryKeys.INSTRUMENT), CraftMusicInstrument::new);
        }
        if (bukkitClass == Structure.class) {
            return new CraftRegistry<>(registryHolder.get(RegistryKeys.STRUCTURE), CraftStructure::new);
        }
        if (bukkitClass == StructureType.class) {
            return new CraftRegistry<>(Registries.STRUCTURE_TYPE, CraftStructureType::new);
        }
        if (bukkitClass == TrimMaterial.class) {
            return new CraftRegistry<>(registryHolder.get(RegistryKeys.TRIM_MATERIAL), CraftTrimMaterial::new);
        }
        if (bukkitClass == TrimPattern.class) {
            return new CraftRegistry<>(registryHolder.get(RegistryKeys.TRIM_PATTERN), CraftTrimPattern::new);
        }

        return null;
    }

    private final Map<NamespacedKey, B> cache = new HashMap<>();
    private final net.minecraft.registry.Registry<M> minecraftRegistry;
    private final BiFunction<NamespacedKey, M, B> minecraftToBukkit;

    public CraftRegistry(net.minecraft.util.registry.Registry<M> minecraftRegistry, BiFunction<NamespacedKey, M, B> minecraftToBukkit) {
        this.minecraftRegistry = minecraftRegistry;
        this.minecraftToBukkit = minecraftToBukkit;
    }

    @Override
    public B get(NamespacedKey namespacedKey) {
        B cached = cache.get(namespacedKey);
        if (cached != null) {
            return cached;
        }

        B bukkit = createBukkit(namespacedKey, minecraftRegistry.getOrEmpty(CraftNamespacedKey.toMinecraft(namespacedKey)).orElse(null));
        if (bukkit == null) {
            return null;
        }

        cache.put(namespacedKey, bukkit);

        return bukkit;
    }

    @NotNull
    @Override
    public Stream<B> stream() {
        return minecraftRegistry.getIds().stream().map(minecraftKey -> get(CraftNamespacedKey.fromMinecraft(minecraftKey)));
    }

    @Override
    public Iterator<B> iterator() {
        return stream().iterator();
    }

    public B createBukkit(NamespacedKey namespacedKey, M minecraft) {
        if (minecraft == null) {
            return null;
        }

        return minecraftToBukkit.apply(namespacedKey, minecraft);
    }

    public Stream<B> values() {
        return minecraftRegistry.getIds().stream().map(minecraftKey -> get(CraftNamespacedKey.fromMinecraft(minecraftKey)));
    }*/
}
