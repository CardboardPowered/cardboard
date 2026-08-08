package io.papermc.paper.registry.data;

import com.google.common.base.Preconditions;
import io.papermc.paper.registry.PaperRegistries;
import io.papermc.paper.registry.PaperRegistryBuilder;
import io.papermc.paper.registry.PaperRegistryBuilderFactory;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry.Builder;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.entry.RegistryEntryMeta;
import java.util.function.Consumer;
import org.bukkit.Art;
import org.bukkit.Keyed;
import org.bukkit.MusicInstrument;
import org.bukkit.craftbukkit.CraftRegistry;

@SuppressWarnings("BoundedWildcard")
public final class InlinedRegistryBuilderProviderImpl implements InlinedRegistryBuilderProvider {

	/*
    private static <M, A extends Keyed, B extends PaperRegistryBuilder<M, A>> A create(final RegistryKey<? extends Registry<M>> registryKey, final Consumer<PaperRegistryBuilderFactory<M, A, B>> value) {
        final RegistryEntryMeta.Buildable<M, A, B> buildableMeta = PaperRegistries.getBuildableMeta(registryKey);
        Preconditions.checkArgument(buildableMeta.registryTypeMapper().supportsDirectHolders(), "Registry type mapper must support direct holders");
        final PaperRegistryBuilderFactory<M, A, B> builderFactory = new PaperRegistryBuilderFactory<>(Conversions.global(), buildableMeta.builderFiller(), CraftRegistry.getMinecraftRegistry(buildableMeta.mcKey())::get);
        value.accept(builderFactory);
        return buildableMeta.registryTypeMapper().createBukkit(RegistryEntry.of(builderFactory.requireBuilder().build()));
    }

    // @Override
    public Art createPaintingVariant(final Consumer<RegistryBuilderFactory<Art, ? extends PaintingVariantRegistryEntry.Builder>> value) {
        return create(RegistryKeys.PAINTING_VARIANT, value::accept);
    }
    
    */
    
    
    public MusicInstrument createInstrument(Consumer<RegistryBuilderFactory<MusicInstrument, ? extends InstrumentRegistryEntry.Builder>> value) {
        return (MusicInstrument)Conversions.global().createApiInstanceFromBuilder(io.papermc.paper.registry.RegistryKey.INSTRUMENT, value);
    }

	@Override
	public io.papermc.paper.dialog.Dialog createDialog(
			Consumer<RegistryBuilderFactory<io.papermc.paper.dialog.Dialog, ? extends Builder>> value) {
		return Conversions.global().createApiInstanceFromBuilder(io.papermc.paper.registry.RegistryKey.DIALOG, value);
	}

    

    // 26.2: new on InlinedRegistryBuilderProvider
    @Override
    public org.bukkit.inventory.meta.trim.TrimPattern createTrimPattern(
            java.util.function.Consumer<io.papermc.paper.registry.RegistryBuilderFactory<
                    org.bukkit.inventory.meta.trim.TrimPattern,
                    ? extends io.papermc.paper.registry.data.TrimPatternRegistryEntry.Builder>> value) {
        return Conversions.global().createApiInstanceFromBuilder(
                io.papermc.paper.registry.RegistryKey.TRIM_PATTERN, value);
    }


    // 26.2: new on InlinedRegistryBuilderProvider
    @Override
    public org.bukkit.inventory.meta.trim.TrimMaterial createTrimMaterial(
            java.util.function.Consumer<io.papermc.paper.registry.RegistryBuilderFactory<
                    org.bukkit.inventory.meta.trim.TrimMaterial,
                    ? extends io.papermc.paper.registry.data.TrimMaterialRegistryEntry.Builder>> value) {
        return Conversions.global().createApiInstanceFromBuilder(
                io.papermc.paper.registry.RegistryKey.TRIM_MATERIAL, value);
    }

}
