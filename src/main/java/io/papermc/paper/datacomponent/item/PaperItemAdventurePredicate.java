package io.papermc.paper.datacomponent.item;

import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.set.PaperRegistrySets;
import io.papermc.paper.util.MCUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.AdventureModePredicate;
import org.bukkit.craftbukkit.util.Handleable;
import org.cardboardpowered.Registries_Bridge;

public record PaperItemAdventurePredicate(
		AdventureModePredicate impl
) implements ItemAdventurePredicate, Handleable<AdventureModePredicate> {

    private static List<BlockPredicate> convert(final AdventureModePredicate nmsModifiers) {
        return MCUtil.transformUnmodifiable(nmsModifiers.predicates, nms -> BlockPredicate.predicate()
            .blocks(nms.blocks().map(blocks -> PaperRegistrySets.convertToApi(RegistryKey.BLOCK, blocks)).orElse(null)).build());
    }

    @Override
    public AdventureModePredicate getHandle() {
        return this.impl;
    }

    @Override
    public List<BlockPredicate> predicates() {
        return convert(this.impl);
    }

    static final class BuilderImpl implements ItemAdventurePredicate.Builder {

        private final List<net.minecraft.advancements.predicates.BlockPredicate> predicates = new ObjectArrayList<>();
        private boolean showInTooltip = true;

        @Override
        public ItemAdventurePredicate.Builder addPredicate(BlockPredicate predicate) {
            this.predicates.add(new net.minecraft.advancements.predicates.BlockPredicate(Optional.ofNullable(predicate.blocks()).map(blocks -> PaperRegistrySets.convertToNms(Registries.BLOCK, Conversions.global().lookup(), blocks)), Optional.empty(), Optional.empty(), DataComponentMatchers.ANY));
            return this;
        }

        @Override
        public io.papermc.paper.datacomponent.item.ItemAdventurePredicate.Builder addPredicates(final List<BlockPredicate> predicates) {
            for (final BlockPredicate predicate : predicates) {
                this.addPredicate(predicate);
            }
            return this;
        }

        @Override
        public ItemAdventurePredicate build() {
            return new PaperItemAdventurePredicate(new AdventureModePredicate(new ObjectArrayList<>(this.predicates)/*, this.showInTooltip*/));
        }
    }
}
