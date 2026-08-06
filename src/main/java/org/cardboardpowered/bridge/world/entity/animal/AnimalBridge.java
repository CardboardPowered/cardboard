package org.cardboardpowered.bridge.world.entity.animal;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Exposes the item that was used to put an {@link net.minecraft.world.entity.animal.Animal}
 * into love mode. CraftBukkit stores this on Animal as {@code breedItem}; Cardboard adds it
 * through {@code AnimalMixin} and reads it back when firing
 * {@link org.bukkit.event.entity.EntityBreedEvent}.
 */
public interface AnimalBridge {

    @Nullable ItemStack cardboard$getBreedItem();

    void cardboard$setBreedItem(@Nullable ItemStack stack);

}
